using Smart_X_API.DataStructures;
using Smart_X_API.Models;
using static Smart_X_API.Models.StreamModels;
using static Smart_X_API.Models.TopologyModels;
using System.Diagnostics;

namespace Smart_X_API.Services
{
    // TopologyService is the centre of the Network Topology feature. It is a
    // singleton, so one copy is shared and its data survives between requests
    // (Microsoft, 2026a). It does three jobs: it stores every reading in an AVL
    // tree so a time range can be queried quickly, it remembers which sensors
    // are offline, and it builds the mesh graph from the registered sensors to
    // run BFS and Prim's algorithm on (Troelsen and Japikse, 2022; The IIE,
    // 2026).
    public class TopologyService
    {
        // The id of the gateway node in the graph.
        public const string GatewayId = "Gateway";

        // Many requests and the stream worker use this class at the same
        // time, so a lock lets only one thread change the data at once
        // (Microsoft, 2026b). The code inside the lock is kept short.
        private readonly object _lock = new object();
        private readonly SensorService _sensors;

        // device id -> metric name -> AVL tree of readings keyed by time.
        // Each sensor can report several metrics, so each one gets its own
        // tree. A time-ordered sequence would make a plain tree slow, so an
        // AVL tree is used (Baeldung, 2026).
        private readonly Dictionary<string, Dictionary<string, AvlTree<DateTime, HistoryPoint>>> history =
            new Dictionary<string, Dictionary<string, AvlTree<DateTime, HistoryPoint>>>(StringComparer.OrdinalIgnoreCase);

        // The ids of the devices that have stopped sending data.
        private readonly HashSet<string> offline = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

        // The sensors that already have their sample history.
        private readonly HashSet<string> backfilled = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

        public TopologyService(SensorService sensors)
        {
            _sensors = sensors;
        }

        // Called by StreamService for every processed reading. The reading
        // is inserted into the AVL tree of its sensor and metric.
        public void RecordReading(string deviceId, string metric, DateTime timestamp, double value)
        {
            lock (_lock)
            {
                // Make the sensor's dictionary if it is the first reading.
                if (!history.TryGetValue(deviceId, out Dictionary<string, AvlTree<DateTime, HistoryPoint>>? metrics))
                {
                    metrics = new Dictionary<string, AvlTree<DateTime, HistoryPoint>>(StringComparer.OrdinalIgnoreCase);
                    history[deviceId] = metrics;
                }

                // Make the tree for this metric if it is the first one.
                if (!metrics.TryGetValue(metric, out AvlTree<DateTime, HistoryPoint>? tree))
                {
                    tree = new AvlTree<DateTime, HistoryPoint>();
                    metrics[metric] = tree;
                }

                HistoryPoint point = new HistoryPoint();
                point.Time = timestamp;
                point.Value = value;
                tree.Insert(timestamp, point);
            }
        }

        // Called by StreamService when a device stops or starts sending.
        public void SetOnline(string deviceId, bool online)
        {
            lock (_lock)
            {
                if (online)
                {
                    offline.Remove(deviceId);
                }
                else
                {
                    offline.Add(deviceId);
                }
            }
        }

        // Finds a sensor by its identifier or its MAC address.
        public Sensor? FindSensor(string term)
        {
            if (string.IsNullOrWhiteSpace(term))
            {
                return null;
            }

            string search = term.Trim();

            return _sensors.GetAll().FirstOrDefault(s =>
                string.Equals(s.DeviceIdentifier, search, StringComparison.OrdinalIgnoreCase) ||
                string.Equals(s.MacAddress, search, StringComparison.OrdinalIgnoreCase));
        }

        // The metrics a sensor has reported, for the page's drop-down list.
        public List<string> GetMetrics(string term)
        {
            string deviceId = FindSensor(term)?.DeviceIdentifier ?? term;

            lock (_lock)
            {
                if (history.TryGetValue(deviceId, out Dictionary<string, AvlTree<DateTime, HistoryPoint>>? metrics))
                {
                    return metrics.Keys.OrderBy(m => m).ToList();
                }

                return new List<string>();
            }
        }

        // Returns the readings of one sensor and metric between two times,
        // using the AVL tree's range query instead of looping over
        // everything. It also measures how long the query took and how many
        // tree nodes it visited, so the page can show the result.
        public HistoryQueryResult QueryHistory(string term, string metric, DateTime from, DateTime to)
        {
            // Make sure every sensor has its sample history first.
            BackfillMissing();

            string deviceId = FindSensor(term)?.DeviceIdentifier ?? term;
            HistoryQueryResult result = new HistoryQueryResult();

            lock (_lock)
            {
                if (history.TryGetValue(deviceId, out Dictionary<string, AvlTree<DateTime, HistoryPoint>>? metrics) &&
                    metrics.TryGetValue(metric, out AvlTree<DateTime, HistoryPoint>? tree))
                {
                    // Stopwatch measures how long the range query takes.
                    long started = Stopwatch.GetTimestamp();
                    result.Points = tree.RangeQuery(from, to, out int visited);
                    result.ElapsedMs = Stopwatch.GetElapsedTime(started).TotalMilliseconds;
                    result.NodesVisited = visited;
                    result.TreeCount = tree.Count;
                    result.TreeHeight = tree.Height;
                }
            }

            return result;
        }

        // Builds the whole picture the page draws: every node with its
        // status, every link, and which links are in the minimum spanning
        // tree.
        public TopologySnapshot GetTopology()
        {
            List<Sensor> sensors = _sensors.GetAll();

            lock (_lock)
            {
                // Rebuild the graph and run the algorithms on it every time,
                // so the picture always matches the current online/offline state.
                MeshGraph graph = BuildGraph(sensors);
                MstResult mst = graph.CalculateMst(GatewayId);
                HashSet<string> connected = new HashSet<string>(graph.FindConnected(GatewayId), StringComparer.OrdinalIgnoreCase);

                // Remember which links are in the spanning tree.
                HashSet<string> mstLinks = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
                foreach (MeshEdge edge in mst.Edges)
                {
                    mstLinks.Add(EdgeKey(edge.From, edge.To));
                }

                // Look up a sensor by its id, to fill in room, zone and category.
                Dictionary<string, Sensor> byId = new Dictionary<string, Sensor>(StringComparer.OrdinalIgnoreCase);
                foreach (Sensor sensor in sensors)
                {
                    byId[sensor.DeviceIdentifier] = sensor;
                }

                TopologySnapshot snapshot = new TopologySnapshot();

                // One TopologyNode for every circle on the drawing.
                foreach (MeshNode node in graph.Nodes)
                {
                    TopologyNode dto = new TopologyNode();
                    dto.Id = node.Id;
                    dto.MacAddress = node.MacAddress;
                    dto.IsGateway = node.IsGateway;
                    dto.Status = GetStatus(node, connected);

                    if (byId.TryGetValue(node.Id, out Sensor? sensor))
                    {
                        dto.Room = sensor.Room;
                        dto.Zone = sensor.Zone;
                        dto.Category = sensor.Category.ToString();
                    }

                    snapshot.Nodes.Add(dto);
                }

                // One TopologyEdge for every line on the drawing.
                foreach (MeshEdge edge in graph.GetEdges())
                {
                    TopologyEdge dto = new TopologyEdge();
                    dto.From = edge.From;
                    dto.To = edge.To;
                    dto.Cost = edge.Cost;
                    dto.InMst = mstLinks.Contains(EdgeKey(edge.From, edge.To));
                    snapshot.Edges.Add(dto);
                }

                snapshot.MstCost = mst.TotalCost;
                snapshot.SensorCount = sensors.Count;

                // Take away the gateway itself from the connected count.
                snapshot.ConnectedCount = connected.Count - 1;
                snapshot.Offline = graph.FindOffline();
                snapshot.Unreachable = graph.FindUnreachable(GatewayId);
                snapshot.BfsLevels = graph.FindLevels(GatewayId);

                // The spanning tree links in the order Prim's algorithm chose them.
                foreach (MeshEdge edge in mst.Edges)
                {
                    TopologyEdge dto = new TopologyEdge();
                    dto.From = edge.From;
                    dto.To = edge.To;
                    dto.Cost = edge.Cost;
                    dto.InMst = true;
                    snapshot.MstEdges.Add(dto);
                }

                snapshot.ServerTime = DateTime.UtcNow;
                return snapshot;
            }
        }

        // Finds the route of one sensor to the gateway, looked up by its
        // identifier or MAC address.
        public RouteResult GetRoute(string term)
        {
            RouteResult result = new RouteResult();
            Sensor? sensor = FindSensor(term);

            if (sensor == null)
            {
                result.Message = "No sensor matches '" + term + "'.";
                return result;
            }

            lock (_lock)
            {
                MeshGraph graph = BuildGraph(_sensors.GetAll());
                MstResult mst = graph.CalculateMst(GatewayId);
                HashSet<string> connected = new HashSet<string>(graph.FindConnected(GatewayId), StringComparer.OrdinalIgnoreCase);
                MeshNode node = graph.GetNode(sensor.DeviceIdentifier)!;

                result.Found = true;
                result.DeviceId = node.Id;
                result.MacAddress = node.MacAddress;
                result.Status = GetStatus(node, connected);

                // An offline sensor has no route.
                if (!node.IsOnline)
                {
                    result.Message = node.Id + " is offline, so it has no route.";
                    return result;
                }

                // One route from the spanning tree, one from the BFS.
                List<string>? mstRoute = mst.GetRoute(node.Id);
                List<string>? hopRoute = graph.FindRoute(node.Id, GatewayId);

                // Still on, but every path to the gateway crosses an offline node.
                if (mstRoute == null || hopRoute == null)
                {
                    result.Message = node.Id + " is on, but every route to the gateway goes through an offline node.";
                    return result;
                }

                result.MstRoute = mstRoute;
                result.MstCost = mst.GetRouteCost(node.Id);
                result.FewestHopsRoute = hopRoute;
                result.Message = "Most power-efficient route: " + string.Join(" > ", mstRoute) +
                                 " (cost " + result.MstCost + ").";
                return result;
            }
        }

        // Gives every active sensor a day of sample history the first time it
        // is seen, so the history log has a large amount of data to query.
        // This is generated sample data, not real device history. The
        // simulator turns the first active Environmental sensor into the
        // moisture probe and every other one into a temperature sensor, so
        // the same rule is used here to give each sensor the same metric.
        private void BackfillMissing()
        {
            List<Sensor> sensors = _sensors.GetAll();

            lock (_lock)
            {
                // The one Environmental sensor that reports moisture.
                string? moistureId = sensors
                    .Where(s => s.Active && s.Category == SensorCategory.Environmental)
                    .OrderBy(s => s.Id)
                    .Select(s => s.DeviceIdentifier)
                    .FirstOrDefault();

                foreach (Sensor sensor in sensors)
                {
                    // Skip inactive sensors and ones that already have history.
                    if (!sensor.Active || backfilled.Contains(sensor.DeviceIdentifier))
                    {
                        continue;
                    }

                    backfilled.Add(sensor.DeviceIdentifier);
                    GenerateHistory(sensor, string.Equals(sensor.DeviceIdentifier, moistureId, StringComparison.OrdinalIgnoreCase));
                }
            }
        }

        // Creates 24 hours of readings, oldest first. Readings arrive in time
        // order, which is the worst case for a plain binary search tree (it
        // would become a long chain), but an AVL tree stays balanced (Baeldung,
        // 2026). The Random is seeded with the sensor id, so a sensor always
        // gets the same pattern (Microsoft, 2026c).
        private void GenerateHistory(Sensor sensor, bool isMoisture)
        {
            string metric;
            int stepSeconds;

            // The sensor type decides the metric and how often it reports.
            switch (sensor.Category)
            {
                case SensorCategory.Environmental:
                    metric = isMoisture ? "Moisture" : "Temperature";
                    stepSeconds = 30;
                    break;
                case SensorCategory.PowerConsumption:
                    metric = "Power";
                    stepSeconds = 30;
                    break;
                default:
                    metric = "ValveState";
                    stepSeconds = 300;
                    break;
            }

            Random random = new Random(sensor.Id * 7919);
            DateTime now = DateTime.UtcNow;

            // Round "now" down to a whole second.
            DateTime end = new DateTime(now.Ticks - now.Ticks % TimeSpan.TicksPerSecond, DateTimeKind.Utc);
            int steps = 24 * 3600 / stepSeconds;
            DateTime start = end.AddSeconds(-steps * stepSeconds);

            double power = 200 + random.Next(0, 500);
            bool valveOpen = false;

            for (int i = 0; i < steps; i++)
            {
                DateTime time = start.AddSeconds(i * stepSeconds);
                double value;

                if (metric == "Temperature")
                {
                    // A gentle daily wave around 22 degrees, plus a little noise.
                    double dayFraction = time.TimeOfDay.TotalHours / 24;
                    value = 22 + 3 * Math.Sin(2 * Math.PI * (dayFraction - 0.25)) + (random.NextDouble() - 0.5);
                }
                else if (metric == "Moisture")
                {
                    // Moisture slowly drops over six hours, then resets.
                    double cycle = (time - start).TotalMinutes % 360 / 360;
                    value = 70 - 45 * cycle + (random.NextDouble() - 0.5) * 2;
                }
                else if (metric == "Power")
                {
                    // Power wanders up and down but stays between 100 and 900.
                    power = Math.Clamp(power + random.Next(-40, 41), 100, 900);
                    value = power;
                }
                else
                {
                    // The valve flips open or closed now and then.
                    if (random.NextDouble() < 0.02)
                    {
                        valveOpen = !valveOpen;
                    }
                    value = valveOpen ? 1 : 0;
                }

                RecordReading(sensor.DeviceIdentifier, metric, time, Math.Round(value, 2));
            }
        }

        // Builds the mesh graph from the registered sensors. The sensors are
        // sorted by zone, so sensors in the same zone become neighbours. Each
        // sensor links to the next three sensors, which gives every node
        // several other ways round if a neighbour fails. Every fifth sensor
        // also links straight to the gateway. The link costs come from a
        // Random seeded with the two sensor ids, so the same two sensors
        // always get the same cost and the picture stays stable (Microsoft,
        // 2026c). The graph is rebuilt on each call, so newly registered
        // sensors appear automatically.
        private MeshGraph BuildGraph(List<Sensor> sensors)
        {
            MeshGraph graph = new MeshGraph();
            graph.AddNode(new MeshNode(GatewayId, "00:00:00:00:00:00", true));

            List<Sensor> ordered = sensors.OrderBy(s => s.Zone).ThenBy(s => s.Id).ToList();

            // Add a node for every sensor. A sensor is online if it is Active
            // and has not been marked offline by the stream.
            foreach (Sensor sensor in ordered)
            {
                MeshNode node = new MeshNode(sensor.DeviceIdentifier, sensor.MacAddress);
                node.IsOnline = sensor.Active && !offline.Contains(sensor.DeviceIdentifier);
                graph.AddNode(node);
            }

            for (int i = 0; i < ordered.Count; i++)
            {
                // Link to the next three sensors in the list.
                for (int step = 1; step <= 3; step++)
                {
                    if (i + step < ordered.Count)
                    {
                        graph.AddEdge(ordered[i].DeviceIdentifier, ordered[i + step].DeviceIdentifier,
                            LinkCost(ordered[i].Id, ordered[i + step].Id, 1, 9));
                    }
                }

                // Every fifth sensor also links to the gateway.
                if (i % 5 == 0)
                {
                    graph.AddEdge(GatewayId, ordered[i].DeviceIdentifier,
                        LinkCost(0, ordered[i].Id, 5, 12));
                }
            }

            return graph;
        }

        // The same two ids always give the same cost between min and max.
        private static int LinkCost(int idA, int idB, int min, int max)
        {
            return new Random(idA * 1000 + idB).Next(min, max + 1);
        }

        // The status shown on the drawing for a node.
        private static string GetStatus(MeshNode node, HashSet<string> connected)
        {
            if (node.IsGateway)
            {
                return "Gateway";
            }

            if (!node.IsOnline)
            {
                return "Offline";
            }

            return connected.Contains(node.Id) ? "Online" : "Unreachable";
        }

        // The same key for a link whichever way round its ends are given.
        private static string EdgeKey(string a, string b)
        {
            return string.Compare(a, b, StringComparison.OrdinalIgnoreCase) < 0 ? a + "|" + b : b + "|" + a;
        }
    }
}

// References:
// Baeldung, 2026. Red-black tree vs AVL tree. [online] Available at: <https://www.baeldung.com/cs/red-black-tree-vs-avl-tree> [Accessed 11 November 2026].
// Microsoft, 2026a. Dependency injection: service lifetimes. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/dependency-injection/service-lifetimes> [Accessed 11 November 2026].
// Microsoft, 2026b. The lock statement: ensure exclusive access to a shared resource. [online] Available at: <https://learn.microsoft.com/dotnet/csharp/language-reference/statements/lock> [Accessed 11 November 2026].
// Microsoft, 2026c. Random Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.random> [Accessed 8 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.