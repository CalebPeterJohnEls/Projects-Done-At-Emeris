using static Smart_X_API.Models.StreamModels;

namespace Smart_X_API.Services
{
    // InsightService is the "Automated Action Engine". It learns from what
    // the user does (pattern analysis) and from simple rules, then suggests
    // actions the user can run with one click. It has no lock of its own,
    // because it is only called from StreamService while that class holds
    // its lock (Microsoft, 2026al; The IIE, 2026).
    public class InsightService
    {
        // A Dictionary inside a Dictionary. The outer key is an event, such
        // as "cmd:RELAY-PUMP-A:ON". The inner Dictionary counts how many
        // times each other event happened straight after it
        // (Microsoft, 2026ac; Nakov and Kolev et al., 2013).
        private Dictionary<string, Dictionary<string, int>> followUps =
            new Dictionary<string, Dictionary<string, int>>();

        // How many times each search term was used. The key is the term in
        // lower case, so "ESP32" and "esp32" count as the same search
        // (Microsoft, 2026ac).
        private Dictionary<string, int> searchCounts = new Dictionary<string, int>();

        private string? lastEvent = null;
        private DateTime lastEventTime;

        // Records an event and, if it happened within two minutes of the
        // previous one, adds one to the count for "previous event -> this
        // event". Over time the counts show which events tend to follow each
        // other (Nakov and Kolev et al., 2013; The IIE, 2026).
        public void Record(string evt)
        {
            DateTime now = DateTime.UtcNow;

            if (lastEvent != null && lastEvent != evt &&
                (now - lastEventTime) < TimeSpan.FromMinutes(2))
            {
                // Create the inner Dictionary the first time this event is seen.
                if (!followUps.ContainsKey(lastEvent))
                {
                    followUps[lastEvent] = new Dictionary<string, int>();
                }

                // Add one to the existing count, or start a new count at 1.
                Dictionary<string, int> next = followUps[lastEvent];
                if (next.ContainsKey(evt))
                {
                    next[evt]++;
                }
                else
                {
                    next[evt] = 1;
                }
            }

            lastEvent = evt;
            lastEventTime = now;
        }

        // Records a search. The term is counted for the "habit" suggestion
        // and also recorded as an event, so the engine can learn what the
        // user usually does after searching for it.
        public void RecordSearch(string term)
        {
            string cleaned = term.Trim().ToLowerInvariant();
            if (cleaned == "")
            {
                return;
            }

            if (searchCounts.ContainsKey(cleaned))
            {
                searchCounts[cleaned]++;
            }
            else
            {
                searchCounts[cleaned] = 1;
            }

            Record("search:" + cleaned);
        }

        // Builds the list shown in "Suggested Actions / Automated Insights".
        // It combines four sources: learned patterns, search habits, simple
        // rules and trend predictions. Every suggestion carries the action its
        // button performs, then the six most confident ones are kept.
        public List<Suggestion> GetSuggestions(List<string> alerts, List<DeviceState> devices,
            Dictionary<string, SortedDictionary<DateTime, double>> history)
        {
            List<Suggestion> list = new List<Suggestion>();

            // A HashSet stores each item only once, so the same suggestion is
            // never added twice (Microsoft, 2026ad).
            HashSet<string> added = new HashSet<string>();

            // 1. Learned patterns. A pattern can start from two kinds of
            // trigger: every alert that is active right now, and the last
            // thing the user did (if it was less than two minutes ago). For
            // each trigger, look up what has usually happened next. Confidence
            // is the count of that follow-up divided by the total follow-ups,
            // for example 3 out of 4 = 0.75 (Nakov and Kolev et al., 2013;
            // The IIE, 2026).
            HashSet<string> triggers = new HashSet<string>();
            foreach (string alert in alerts)
            {
                triggers.Add("alert:" + alert);
            }
            if (lastEvent != null && (DateTime.UtcNow - lastEventTime) < TimeSpan.FromMinutes(2))
            {
                triggers.Add(lastEvent);
            }

            foreach (string trigger in triggers)
            {
                if (!followUps.ContainsKey(trigger))
                {
                    continue;
                }

                Dictionary<string, int> next = followUps[trigger];
                int total = 0;
                foreach (int count in next.Values)
                {
                    total += count;
                }

                foreach (KeyValuePair<string, int> kvp in next)
                {
                    // A pattern must have happened at least twice, so a
                    // one-off action does not become a suggestion.
                    if (kvp.Value < 2)
                    {
                        continue;
                    }

                    // Add returns false if this action was already suggested.
                    if (!added.Add(kvp.Key))
                    {
                        continue;
                    }

                    double confidence = (double)kvp.Value / total;
                    string reason = "After " + DescribeEvent(trigger) + ", you did this "
                                    + kvp.Value + " out of " + total + " times.";
                    list.Add(BuildPatternSuggestion(kvp.Key, reason, confidence));
                }
            }

            // 2. Search habits. A term searched three or more times becomes a
            // suggestion that lists the latest readings of the matching
            // devices. Only the two most searched terms are used, so the
            // panel does not fill up. They are kept in their own list and
            // added back after the top-six cut below. A habit's confidence
            // (0.75 to 0.89) is lower than an alert or an offline device
            // (0.9 and 0.95), so with several of those active the habit
            // would otherwise be cut and never shown.
            List<Suggestion> habits = new List<Suggestion>();
            foreach (KeyValuePair<string, int> search in searchCounts
                         .Where(kvp => kvp.Value >= 3)
                         .OrderByDescending(kvp => kvp.Value)
                         .Take(2))
            {
                habits.Add(BuildSearchSuggestion(search.Key, search.Value, devices));
            }

            // 3. Simple rules for each device: offline, out of range, or (if
            // everything is fine) check whether the readings are heading
            // towards a limit.
            foreach (DeviceState d in devices)
            {
                if (!d.IsOnline)
                {
                    list.Add(MakeInsight("Check " + d.DeviceId + " connection", "No data received recently", d.DeviceId, 0.95));
                }
                else if (d.HasAlert)
                {
                    list.Add(MakeInsight("Inspect " + d.DeviceId, d.Metric + " is " + d.Value.ToString("F1") + " " + d.Unit + " (out of range)", d.DeviceId, 0.9));
                }
                else
                {
                    Suggestion? trend = BuildTrendInsight(d, history);
                    if (trend != null)
                    {
                        list.Add(trend);
                    }
                }
            }

            // Sort with the highest confidence first and keep the top six
            // overall. The habit suggestions are guaranteed a place, so the
            // other suggestions only fill the slots that are left. The
            // combined list is then sorted again so the order still follows
            // confidence.
            list.Sort((a, b) => b.Confidence.CompareTo(a.Confidence));
            int room = Math.Max(0, 6 - habits.Count);
            if (list.Count > room)
            {
                list = list.GetRange(0, room);
            }
            list.AddRange(habits);
            list.Sort((a, b) => b.Confidence.CompareTo(a.Confidence));
            return list;
        }

        // Predicts a problem before it happens. It looks at the last 8
        // readings, works out how fast the value is changing per second, and
        // estimates how many seconds are left until the limit is reached
        // (Nakov and Kolev et al., 2013; The IIE, 2026).
        private Suggestion? BuildTrendInsight(DeviceState d, Dictionary<string, SortedDictionary<DateTime, double>> history)
        {
            // At least 8 readings are needed for a trustworthy trend.
            if (!history.ContainsKey(d.DeviceId) || history[d.DeviceId].Count < 8)
            {
                return null;
            }

            // Only metrics that have a limit can be predicted.
            double limit;
            if (d.Metric == "Temperature")
            {
                limit = 35;
            }
            else if (d.Metric == "Moisture")
            {
                limit = 20;
            }
            else
            {
                return null;
            }

            // The SortedDictionary is already in time order, so the first and
            // last of the recent readings give the change over time
            // (Microsoft, 2026aj).
            List<KeyValuePair<DateTime, double>> recent = history[d.DeviceId].TakeLast(8).ToList();
            double seconds = (recent[recent.Count - 1].Key - recent[0].Key).TotalSeconds;
            if (seconds <= 0)
            {
                return null;
            }

            // slope = change in value per second.
            double slope = (recent[recent.Count - 1].Value - recent[0].Value) / seconds;
            if (slope == 0)
            {
                return null;
            }

            // eta = seconds until the limit at this speed. A negative value
            // means the value is moving away from the limit, and more than
            // 300 seconds is too far away to be useful.
            double eta = (limit - d.Value) / slope;
            if (eta <= 0 || eta > 300)
            {
                return null;
            }

            return MakeInsight("Watch " + d.DeviceId, d.Metric + " heading for its limit in about " + Math.Round(eta) + "s", d.DeviceId, 0.85);
        }

        // Builds the "habit" suggestion for a term the user keeps searching.
        // It lists the latest readings of up to six matching devices, so the
        // user sees the answer without searching again. The confidence grows
        // with every search but stays below 0.9, so a real alert always ranks
        // higher.
        private Suggestion BuildSearchSuggestion(string term, int count, List<DeviceState> devices)
        {
            Suggestion s = new Suggestion();
            s.Kind = "Habit";
            s.Title = "You keep searching \"" + term + "\"";
            s.Confidence = Math.Min(0.89, 0.6 + 0.05 * count);
            s.ActionType = "filter";
            s.ActionLabel = "Show these devices";
            s.Term = term;

            List<DeviceState> matches = devices
                .Where(d => d.DeviceId.Contains(term, StringComparison.OrdinalIgnoreCase))
                .Take(6)
                .ToList();

            foreach (DeviceState d in matches)
            {
                SuggestionReading reading = new SuggestionReading();
                reading.DeviceId = d.DeviceId;
                reading.Metric = d.Metric;
                reading.Value = d.Value;
                reading.Unit = d.Unit;
                reading.IsOnline = d.IsOnline;
                reading.LastSeen = d.LastSeen;
                s.Readings.Add(reading);
            }

            if (matches.Count == 0)
            {
                s.Reason = "I see you have been searching \"" + term + "\" (" + count
                           + " times), but no device matches it right now.";
            }
            else
            {
                s.Reason = "I see you have been searching \"" + term + "\" (" + count
                           + " times). Here are the latest readings below:";
            }
            return s;
        }

        // Helper that builds an "Insight" suggestion (a rule-based warning).
        // Its button opens the device's timeline.
        private Suggestion MakeInsight(string title, string reason, string deviceId, double confidence)
        {
            Suggestion s = new Suggestion();
            s.Kind = "Insight";
            s.Title = title;
            s.Reason = reason;
            s.DeviceId = deviceId;
            s.Confidence = confidence;
            s.ActionType = "show";
            s.ActionLabel = "Show timeline";
            return s;
        }

        // Helper that turns a stored event name back into a suggestion with a
        // button. Events are stored as text like "cmd:RELAY-PUMP-A:ON", so
        // Split(':') cuts the text into its parts (Nakov and Kolev et al.,
        // 2013). A command suggestion sends the command, a search suggestion
        // runs the search, and an alert suggestion opens the timeline.
        private Suggestion BuildPatternSuggestion(string evt, string reason, double confidence)
        {
            string[] parts = evt.Split(':');

            Suggestion s = new Suggestion();
            s.Kind = "Pattern";
            s.Reason = reason;
            s.Confidence = confidence;

            if (parts[0] == "cmd")
            {
                s.Title = "Turn " + parts[1] + " " + parts[2];
                s.DeviceId = parts[1];
                s.Command = (parts[2] == "ON");
                s.ActionType = "command";
                s.ActionLabel = "Turn " + parts[2];
            }
            else if (parts[0] == "search")
            {
                s.Title = "Look at \"" + evt.Substring(7) + "\"";
                s.ActionType = "filter";
                s.ActionLabel = "Search it";
                s.Term = evt.Substring(7);
            }
            else
            {
                s.Title = "Review " + parts[1];
                s.DeviceId = parts[1];
                s.ActionType = "show";
                s.ActionLabel = "Show timeline";
            }
            return s;
        }

        // Turns a stored event name into a readable phrase for the reason
        // text, for example "you turned RELAY-PUMP-A ON".
        private string DescribeEvent(string evt)
        {
            string[] parts = evt.Split(':');

            if (parts[0] == "cmd" && parts.Length >= 3)
            {
                return "you turned " + parts[1] + " " + parts[2];
            }
            if (parts[0] == "search")
            {
                return "you searched \"" + evt.Substring(7) + "\"";
            }
            if (parts[0] == "alert" && parts.Length >= 3)
            {
                return parts[1] + " raised " + parts[2];
            }
            return evt;
        }
    }
}

// References:
// Microsoft, 2026ac. Dictionary<TKey,TValue> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.dictionary-2> [Accessed 17 October 2026].
// Microsoft, 2026ad. HashSet<T> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.hashset-1> [Accessed 17 October 2026].
// Microsoft, 2026aj. SortedDictionary<TKey,TValue> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.sorteddictionary-2> [Accessed 17 October 2026].
// Microsoft, 2026al. The lock statement - synchronize access to shared resources - C# reference. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/statements/lock> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.