namespace Smart_X_Client.Models
{
    // These classes mirror the topology models in the API project, in the
    // same way as StreamModels. The names in the JSON from the API are
    // matched to these properties (Troelsen and Japikse, 2022; The IIE, 2026).
    public class TopologyModels
    {
        // One circle on the network drawing.
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

        // One line on the network drawing.
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
            public List<List<string>> BfsLevels { get; set; } = new List<List<string>>();
            public List<TopologyEdge> MstEdges { get; set; } = new List<TopologyEdge>();
            public DateTime ServerTime { get; set; }
        }

        // The routes found for one sensor.
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

        // The answer to a history range query. TreeCount and TreeHeight
        // describe the AVL tree that was searched.
        public class TopologyHistoryResult
        {
            public string DeviceId { get; set; } = string.Empty;
            public string Metric { get; set; } = string.Empty;
            public DateTime From { get; set; }
            public DateTime To { get; set; }
            public int Count { get; set; }
            public int TreeCount { get; set; }
            public int TreeHeight { get; set; }
            public int NodesVisited { get; set; }
            public double ElapsedMs { get; set; }
            public List<StreamModels.HistoryPoint> Readings { get; set; } = new List<StreamModels.HistoryPoint>();
        }
    }
}

// References:
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.