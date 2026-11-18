using static Smart_X_API.Models.StreamModels;

namespace Smart_X_API.Models
{
    // TopologyModels groups the small data classes sent to the Network
    // Topology page, in the same way as StreamModels (The IIE, 2026). These
    // classes only hold data. The properties are turned into JSON when they
    // are sent to the client (Troelsen and Japikse, 2022).
    public class TopologyModels
    {
        // One circle on the network drawing. The status is "Gateway",
        // "Online", "Offline" (switched off or not responding) or
        // "Unreachable" (on, but every route to the gateway is broken).
        public class TopologyNode
        {
            public string Id { get; set; } = string.Empty;
            public string MacAddress { get; set; } = string.Empty;
            public string Room { get; set; } = string.Empty;
            public string Zone { get; set; } = string.Empty;
            public string Category { get; set; } = string.Empty;
            public bool IsGateway { get; set; }
            public string Status { get; set; } = "Online";
        }

        // One line on the network drawing. InMst is true when the link is
        // part of the minimum spanning tree.
        public class TopologyEdge
        {
            public string From { get; set; } = string.Empty;
            public string To { get; set; } = string.Empty;
            public int Cost { get; set; }
            public bool InMst { get; set; }
        }

        // Everything the page needs to draw the network in one go.
        public class TopologySnapshot
        {
            public List<TopologyNode> Nodes { get; set; } = new List<TopologyNode>();
            public List<TopologyEdge> Edges { get; set; } = new List<TopologyEdge>();
            public int MstCost { get; set; }
            public int SensorCount { get; set; }
            public int ConnectedCount { get; set; }
            public List<string> Offline { get; set; } = new List<string>();
            public List<string> Unreachable { get; set; } = new List<string>();

            // The sensors grouped by how many hops they are from the gateway.
            public List<List<string>> BfsLevels { get; set; } = new List<List<string>>();

            // The spanning tree links, in the order Prim's algorithm chose them.
            public List<TopologyEdge> MstEdges { get; set; } = new List<TopologyEdge>();
            public DateTime ServerTime { get; set; }
        }

        // The answer when a user searches for one sensor. MstRoute is the
        // most power-efficient route (from the minimum spanning tree).
        // FewestHopsRoute is the shortest route in hops (from the BFS), which
        // is the route to use when a node on the usual path is down.
        public class RouteResult
        {
            public bool Found { get; set; }
            public string DeviceId { get; set; } = string.Empty;
            public string MacAddress { get; set; } = string.Empty;
            public string Status { get; set; } = string.Empty;
            public List<string> MstRoute { get; set; } = new List<string>();
            public int MstCost { get; set; }
            public List<string> FewestHopsRoute { get; set; } = new List<string>();
            public string Message { get; set; } = string.Empty;
        }

        // The answer to a history range query, with figures showing how the
        // AVL tree performed.
        public class HistoryQueryResult
        {
            public List<HistoryPoint> Points { get; set; } = new List<HistoryPoint>();
            public int NodesVisited { get; set; }
            public double ElapsedMs { get; set; }
            public int TreeCount { get; set; }
            public int TreeHeight { get; set; }
        }
    }
}

// References:
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.