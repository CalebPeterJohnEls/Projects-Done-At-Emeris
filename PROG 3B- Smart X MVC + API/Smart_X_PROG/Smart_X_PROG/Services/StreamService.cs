using static Smart_X_API.Models.StreamModels;

namespace Smart_X_API.Services
{
    // StreamService is the centre of the Command Stream feature. It is
    // registered as a singleton (one shared instance), so its data survives
    // between requests, in the same way as TelemetryService in Part 1
    // (Troelsen and Japikse, 2022; The IIE, 2026).
    public class StreamService
    {
        // Many requests and the background worker use this class at the same
        // time, and these collections are not thread-safe. The lock lets only
        // one thread change the data at a time (Microsoft, 2026l; The IIE, 2026).
        private readonly object _lock = new object();
        private readonly InsightService _insights;

        private readonly TopologyService _topology;

        // A Queue is first-in, first-out: normal readings are processed in
        // the order they arrived (Nakov and Kolev et al., 2013; Microsoft, 2026h).
        private Queue<StreamReading> standardQueue = new Queue<StreamReading>();

        // A PriorityQueue always gives back the item with the lowest priority
        // number first, so critical alerts jump ahead of the standard queue
        // (Microsoft, 2026g; The IIE, 2026).
        private PriorityQueue<StreamReading, int> alertQueue = new PriorityQueue<StreamReading, int>();

        // A Dictionary finds a value by its key without looping through every
        // item, so each device is found by its id straight away
        // (Nakov and Kolev et al., 2013; Microsoft, 2026c).
        private Dictionary<string, DeviceState> devices = new Dictionary<string, DeviceState>();

        // A SortedDictionary keeps its entries in order of the key, so the
        // readings of each device stay sorted by time for the timeline graph
        // (Microsoft, 2026j; Nakov and Kolev et al., 2013).
        private Dictionary<string, SortedDictionary<DateTime, double>> history =
            new Dictionary<string, SortedDictionary<DateTime, double>>();

        // A HashSet only stores unique items, so the same alert cannot be
        // stored twice (Microsoft, 2026d; Nakov and Kolev et al., 2013).
        private HashSet<string> activeAlerts = new HashSet<string>();

        // A Stack is last-in, first-out: the newest command is on top, which
        // is exactly the one an "Undo" should reverse first
        // (Microsoft, 2026k; Nakov and Kolev et al., 2013).
        private Stack<CommandRecord> commandHistory = new Stack<CommandRecord>();

        // Commands waiting for a device to collect them. Each device has its
        // own Queue, so its commands are delivered in the order they were
        // given (Microsoft, 2026c; Microsoft, 2026h).
        private Dictionary<string, Queue<bool>> pendingCommands = new Dictionary<string, Queue<bool>>();

        private List<string> processingLog = new List<string>();
        private int processedCount = 0;
        private int duplicatesBlocked = 0;
        private int nextCommandId = 0;

        // InsightService is passed in through the constructor (dependency
        // injection), so ASP.NET Core supplies the shared instance
        // (Troelsen and Japikse, 2022).
        public StreamService(InsightService insights, TopologyService topology)
        {
            _insights = insights;
            _topology = topology;
        }

        // Decides if a reading is critical. It returns the alert name, or
        // null (no value) when the reading is normal. The "?" after string
        // allows null (Troelsen and Japikse, 2022).
        private string? GetAlertType(StreamReading r)
        {
            if (r.Metric == "Power" && r.Value > 3000)
            {
                return "PowerSpike";
            }
            if (r.Metric == "Temperature" && r.Value > 35)
            {
                return "HighTemperature";
            }

            if (r.Metric == "Moisture" && r.Value < 20)
            {
                return "LowMoisture";
            }
            return null;
        }

        // Step 1: a reading arrives and is put in the correct queue. Normal
        // readings join the back of the standard queue. Alerts go into the
        // priority queue, where a PowerSpike (priority 0) is served before
        // other alerts (priority 1) (Microsoft, 2026g; Microsoft, 2026h).
        public void AddReading(StreamReading reading)
        {
            lock (_lock)
            {
                string? alertType = GetAlertType(reading);

                if (alertType == null)
                {
                    standardQueue.Enqueue(reading);
                }
                else
                {
                    int priority = (alertType == "PowerSpike") ? 0 : 1;
                    alertQueue.Enqueue(reading, priority);
                }
            }
        }

        // Step 2: the background worker calls this every half a second. The
        // alert queue is always emptied first, then the standard queue. The
        // limit of 200 stops one pass from running for too long
        // (Nakov and Kolev et al., 2013; The IIE, 2026).
        public void ProcessQueues()
        {
            lock (_lock)
            {
                int handled = 0;
                while (handled < 200 && (alertQueue.Count > 0 || standardQueue.Count > 0))
                {
                    if (alertQueue.Count > 0)
                    {
                        ApplyReading(alertQueue.Dequeue(), "PRIORITY");
                    }

                    else
                    {
                        ApplyReading(standardQueue.Dequeue(), "STANDARD");
                    }

                    handled++;
                }

                CheckDisconnects();
            }
        }

        // Step 3: store one processed reading. This updates the device, the
        // history, the alerts and the processing log.
        // Step 3: store one processed reading. This updates the device, the
        // history, the alerts and the lists used by the pop-up windows.
        private void ApplyReading(StreamReading r, string source)
        {
            // TryGetValue looks the device up and returns it in one step. If
            // it is not found, a new DeviceState is created and added
            // (Microsoft, 2026ac).
            DeviceState? device;
            if (!devices.TryGetValue(r.DeviceId, out device))
            {
                device = new DeviceState();
                device.DeviceId = r.DeviceId;
                device.Category = r.Category;
                devices[r.DeviceId] = device;
            }

            // Work out first whether this reading is an alert, and how urgent
            // it is (0 = most urgent, -1 = not an alert).
            string? alertType = GetAlertType(r);
            int priority = -1;
            if (alertType != null)
            {
                priority = (alertType == "PowerSpike") ? 0 : 1;
            }

            device.Metric = r.Metric;
            device.Value = r.Value;
            device.Unit = r.Unit;
            device.LastSeen = DateTime.UtcNow;
            device.IsOnline = true;
            _topology.SetOnline(r.DeviceId, true);

            // The device is sending again, so its "Disconnected" alert is
            // removed from the set (Microsoft, 2026ad).
            activeAlerts.Remove(r.DeviceId + ":Disconnected");

            // Once the device reports the state that was commanded, the
            // command has taken effect, so the manual override is cleared.
            if (r.Category == "Actuator" && device.CommandedState != null && (r.Value > 0.5) == device.CommandedState)
            {
                device.CommandedState = null;
            }

            // Each device gets its own SortedDictionary the first time it
            // reports (Microsoft, 2026aj).
            if (!history.ContainsKey(r.DeviceId))
            {
                history[r.DeviceId] = new SortedDictionary<DateTime, double>();

            }

            _topology.RecordReading(r.DeviceId, r.Metric, r.Timestamp, r.Value);

            // Deviation: how far this value is from the average of the
            // device's last 10 readings, as a percentage. It is worked out
            // before the new value is added to the history. Actuators only
            // switch between 0 and 1, so they always show 0%.
            double deviation = 0;
            if (r.Category != "Actuator" && history[r.DeviceId].Count > 0)
            {
                double average = history[r.DeviceId].TakeLast(10).Average(kvp => kvp.Value);
                if (average != 0)
                {
                    deviation = Math.Abs(r.Value - average) / Math.Abs(average) * 100;
                }
            }

            // The indexer adds a new entry or replaces an existing one, so two
            // readings with the same timestamp do not cause an error.
            history[r.DeviceId][r.Timestamp] = r.Value;

            // Only the newest 200 readings are kept. The first key in a
            // SortedDictionary is the oldest one, so it is removed.
            if (history[r.DeviceId].Count > 200)
            {
                history[r.DeviceId].Remove(history[r.DeviceId].Keys.First());
            }

            // HashSet.Add returns true if the alert is new and false if it
            // is already in the set. A new alert is recorded for the pattern
            // engine; a repeat is counted per alert, so the pop-up can show
            // which alerts were repeated (Microsoft, 2026ad; Nakov and Kolev
            // et al., 2013).
            if (alertType != null)
            {
                device.HasAlert = true;
                string alertKey = r.DeviceId + ":" + alertType;

                if (activeAlerts.Add(alertKey))
                {
                    _insights.Record("alert:" + alertKey);
                }
                else
                {
                    duplicatesBlocked++;
                    if (blockedByAlert.ContainsKey(alertKey))
                    {
                        blockedByAlert[alertKey]++;
                    }
                    else
                    {
                        blockedByAlert[alertKey] = 1;
                    }
                }
            }
            else
            {
                // The reading is normal again, so this device's alerts are
                // cleared (except a disconnect alert, which has its own rule).
                device.HasAlert = false;
                activeAlerts.RemoveWhere(a => a.StartsWith(r.DeviceId + ":") && !a.EndsWith(":Disconnected"));
            }

            // Severity: an alert is Critical, a value more than 20% away from
            // the recent average is a Warning, anything else is Normal.
            string severity = "Normal";
            if (alertType != null)
            {
                severity = "Critical";
            }
            else if (deviation > 20)
            {
                severity = "Warning";
            }

            // Remember this reading for the pop-up windows. Only the newest
            // 100 are kept.
            ProcessedReading done = new ProcessedReading();
            done.DeviceId = r.DeviceId;
            done.Value = r.Value;
            done.Unit = r.Unit;
            done.Source = source;
            done.Severity = severity;
            done.Deviation = Math.Round(deviation, 1);
            done.AlertType = alertType ?? string.Empty;
            done.Priority = priority;
            done.ProcessedAt = DateTime.UtcNow;
            recentProcessed.Add(done);
            if (recentProcessed.Count > 100)
            {
                recentProcessed.RemoveAt(0);
            }

            // The processing log keeps the last 12 lines. It shows whether a
            // reading was handled as PRIORITY or STANDARD.
            processedCount++;
            processingLog.Add(DateTime.Now.ToString("HH:mm:ss.fff") + " " + source + " " + r.DeviceId + " " + r.Value);
            if (processingLog.Count > 12)
            {
                processingLog.RemoveAt(0);
            }
        }

        // A device that has been silent for 10 seconds is marked as
        // disconnected. Devices that are already offline are skipped, so the
        // alert is only raised once (Microsoft, 2026d).
        private void CheckDisconnects()
        {
            foreach (DeviceState d in devices.Values)
            {
                if (!d.IsOnline)
                {
                    continue;
                }

                if (DateTime.UtcNow - d.LastSeen > TimeSpan.FromSeconds(10))
                {
                    if (activeAlerts.Add(d.DeviceId + ":Disconnected"))
                    {
                        d.IsOnline = false;
                        _topology.SetOnline(d.DeviceId, false);
                        d.HasAlert = true;
                        _insights.Record("alert:" + d.DeviceId + ":Disconnected");
                    }
                }
            }
        }

        // Handles a manual override. The previous state is saved in the
        // command, so Undo knows what to go back to. The command is pushed
        // on the Stack and also queued for the device to collect
        // (Microsoft, 2026k; Nakov and Kolev et al., 2013).
        public CommandRecord IssueCommand(string deviceId, bool turnOn)
        {
            lock (_lock)
            {
                // "??" uses the right-hand value when the left side is null:
                // the commanded state if there is one, otherwise the last
                // reported value (Troelsen and Japikse, 2022).
                bool previous = false;
                if (devices.ContainsKey(deviceId))
                {
                    DeviceState d = devices[deviceId];
                    previous = d.CommandedState ?? (d.Value > 0.5);
                    d.CommandedState = turnOn;
                }

                CommandRecord command = new CommandRecord();
                command.Id = ++nextCommandId;
                command.DeviceId = deviceId;
                command.NewState = turnOn;
                command.PreviousState = previous;
                command.IssuedAt = DateTime.UtcNow;

                EnqueuePending(deviceId, turnOn);
                commandHistory.Push(command);
                _insights.Record("cmd:" + deviceId + ":" + (turnOn ? "ON" : "OFF"));
                return command;
            }
        }

        // Undo removes the newest command from the top of the Stack and sends
        // the device back to its previous state. It returns null when there
        // is nothing to undo (Microsoft, 2026k).
        public CommandRecord? UndoCommand()
        {
            lock (_lock)
            {
                if (commandHistory.Count == 0)
                {
                    return null;
                }

                CommandRecord last = commandHistory.Pop();
                if (devices.ContainsKey(last.DeviceId))
                {
                    devices[last.DeviceId].CommandedState = last.PreviousState;
                }
                EnqueuePending(last.DeviceId, last.PreviousState);
                return last;
            }
        }

        // Adds a command to the device's own queue, creating the queue the
        // first time the device is used (Microsoft, 2026c; Microsoft, 2026h).
        private void EnqueuePending(string deviceId, bool state)
        {
            if (!pendingCommands.ContainsKey(deviceId))
            {
                pendingCommands[deviceId] = new Queue<bool>();
            }
            pendingCommands[deviceId].Enqueue(state);
        }

        // A device calls this to collect its next waiting command. It returns
        // null when there is none. Dequeue gives the oldest command first.
        public bool? TakePendingCommand(string deviceId)
        {
            lock (_lock)
            {
                if (pendingCommands.TryGetValue(deviceId, out Queue<bool>? queue) && queue.Count > 0)
                {
                    return queue.Dequeue();
                }
                return null;
            }
        }

        // Every search is recorded as an event, so the pattern engine can
        // learn from what the user looks for. RecordSearch also counts how
        // many times each term was searched, for the "habit" suggestion.
        public void LogSearch(string term)
        {
            lock (_lock)
            {
                _insights.RecordSearch(term);
            }
        }

        // Builds everything the dashboard needs. Each device is copied, so
        // the page never sees data that is still being changed by the worker.
        // The category, alert and search filters decide which devices are
        // returned (Nakov and Kolev et al., 2013; The IIE, 2026).
        public StreamSnapshot GetSnapshot(string? category, bool alertsOnly, string? search)
        {
            lock (_lock)
            {
                StreamSnapshot snapshot = new StreamSnapshot();
                List<DeviceState> allDevices = new List<DeviceState>();

                foreach (DeviceState d in devices.Values.OrderBy(x => x.DeviceId))
                {
                    DeviceState copy = CopyDevice(d);
                    allDevices.Add(copy);

                    // "continue" skips devices that do not match the filters.
                    if (!string.IsNullOrWhiteSpace(category) && d.Category != category)
                    {
                        continue;
                    }
                    if (alertsOnly && !d.HasAlert)
                    {
                        continue;
                    }
                    if (!string.IsNullOrWhiteSpace(search) &&
                        !d.DeviceId.Contains(search, StringComparison.OrdinalIgnoreCase))
                    {
                        continue;
                    }

                    snapshot.Devices.Add(copy);
                }

                // Suggestions use all devices, not only the filtered ones, so
                // a filter never hides an important warning.
                snapshot.ActiveAlerts = activeAlerts.OrderBy(a => a).ToList();
                snapshot.Suggestions = _insights.GetSuggestions(activeAlerts.ToList(), allDevices, history);
                snapshot.CommandHistory = commandHistory.ToList();
                snapshot.ProcessingLog = processingLog.ToList();
                // The readings still waiting in each queue (first 25). A Queue
                // lists its items oldest first. A PriorityQueue has no order
                // when listed, so UnorderedItems is sorted by priority
                // (Microsoft, 2026ah; Microsoft, 2026ag).
                snapshot.StandardWaiting = standardQueue.Take(25).Select(r => ToWaiting(r, -1)).ToList();
                snapshot.AlertWaiting = alertQueue.UnorderedItems
                    .OrderBy(x => x.Priority)
                    .Take(25)
                    .Select(x => ToWaiting(x.Element, x.Priority))
                    .ToList();

                // The newest 25 processed readings of each kind, newest first.
                snapshot.RecentAll = recentProcessed.AsEnumerable().Reverse().Take(25).ToList();
                snapshot.RecentStandard = recentProcessed.Where(p => p.Source == "STANDARD").Reverse().Take(25).ToList();
                snapshot.RecentPriority = recentProcessed.Where(p => p.Source == "PRIORITY").Reverse().Take(25).ToList();

                // Blocked duplicates per alert, the most repeated first.
                snapshot.BlockedAlerts = blockedByAlert
                    .OrderByDescending(kvp => kvp.Value)
                    .Select(kvp => new BlockedAlert { Alert = kvp.Key, Blocked = kvp.Value })
                    .ToList();
                snapshot.StandardQueued = standardQueue.Count;
                snapshot.AlertQueued = alertQueue.Count;
                snapshot.ProcessedCount = processedCount;
                snapshot.DuplicatesBlocked = duplicatesBlocked;
                snapshot.ServerTime = DateTime.UtcNow;
                return snapshot;
            }
        }

        // Returns the last 60 readings of one device, already in time order
        // because they come from the SortedDictionary (Microsoft, 2026j).
        public List<HistoryPoint> GetHistory(string deviceId)
        {
            lock (_lock)
            {
                List<HistoryPoint> points = new List<HistoryPoint>();
                if (!history.ContainsKey(deviceId))
                {
                    return points;
                }

                foreach (KeyValuePair<DateTime, double> kvp in history[deviceId].TakeLast(60))
                {
                    HistoryPoint point = new HistoryPoint();
                    point.Time = kvp.Key;
                    point.Value = kvp.Value;
                    points.Add(point);
                }
                return points;
            }
        }

        // Turns a queued reading into the small object sent to the page.
        private WaitingReading ToWaiting(StreamReading r, int priority)
        {
            WaitingReading waiting = new WaitingReading();
            waiting.DeviceId = r.DeviceId;
            waiting.Value = r.Value;
            waiting.Unit = r.Unit;
            waiting.AlertType = GetAlertType(r) ?? string.Empty;
            waiting.Priority = priority;
            waiting.Timestamp = r.Timestamp;
            return waiting;
        }

        // Makes a separate copy of a device. Classes are reference types, so
        // without a copy the page would receive the original object, which
        // the worker may still be changing (Troelsen and Japikse, 2022).
        private DeviceState CopyDevice(DeviceState d)
        {
            DeviceState copy = new DeviceState();
            copy.DeviceId = d.DeviceId;
            copy.Category = d.Category;
            copy.Metric = d.Metric;
            copy.Value = d.Value;
            copy.Unit = d.Unit;
            copy.LastSeen = d.LastSeen;
            copy.IsOnline = d.IsOnline;
            copy.HasAlert = d.HasAlert;
            copy.CommandedState = d.CommandedState;
            return copy;
        }
        // The last 100 processed readings, kept for the pop-up windows. New
        // items are added at the end and the oldest is removed from the front
        // (Nakov and Kolev et al., 2013).
        private List<ProcessedReading> recentProcessed = new List<ProcessedReading>();

        // How many duplicate alerts the HashSet has blocked for each alert,
        // stored in a Dictionary (Microsoft, 2026ac; Microsoft, 2026ad).
        private Dictionary<string, int> blockedByAlert = new Dictionary<string, int>();
    }
}

// References:
// Microsoft, 2026c. Dictionary<TKey,TValue> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.dictionary-2> [Accessed 17 October 2026].
// Microsoft, 2026d. HashSet<T> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.hashset-1> [Accessed 17 October 2026].
// Microsoft, 2026g. PriorityQueue<TElement,TPriority> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.priorityqueue-2> [Accessed 17 October 2026].
// Microsoft, 2026h. Queue<T> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.queue-1> [Accessed 17 October 2026].
// Microsoft, 2026j. SortedDictionary<TKey,TValue> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.sorteddictionary-2> [Accessed 17 October 2026].
// Microsoft, 2026k. Stack<T> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.stack-1> [Accessed 17 October 2026].
// Microsoft, 2026l. The lock statement - synchronize access to shared resources - C# reference. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/statements/lock> [Accessed 17 October 2026].
// Microsoft, 2026ag. PriorityQueue<TElement,TPriority> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.priorityqueue-2> [Accessed 17 October 2026].
// Microsoft, 2026ah. Queue<T> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.queue-1> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.