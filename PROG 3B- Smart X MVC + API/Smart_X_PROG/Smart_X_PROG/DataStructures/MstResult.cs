namespace Smart_X_API.DataStructures
{
    // The answer from Prim's algorithm. A minimum spanning tree is the set of
    // links with the lowest total cost that still connects every node
    // (Baeldung, 2026).
    public class MstResult
    {
        // The node the tree was built from (the gateway).
        public string RootId { get; set; } = string.Empty;

        // The links in the tree, in the order they were chosen.
        public List<MeshEdge> Edges { get; } = new List<MeshEdge>();

        // The cost of all the links added together.
        public int TotalCost { get; set; }

        // For each node, the node one step closer to the gateway. Following
        // these from any node leads back to the gateway.
        public Dictionary<string, string> Parent { get; } =
            new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);

        // The cost of the link between a node and its parent.
        public Dictionary<string, int> ParentCost { get; } =
            new Dictionary<string, int>(StringComparer.OrdinalIgnoreCase);

        // The hops from a node to the gateway, or null if the node is not
        // part of the tree (offline or cut off).
        public List<string>? GetRoute(string nodeId)
        {
            if (!string.Equals(nodeId, RootId, StringComparison.OrdinalIgnoreCase) &&
                !Parent.ContainsKey(nodeId))
            {
                return null;
            }

            List<string> route = new List<string> { nodeId };
            string current = nodeId;

            // Keep stepping to the parent until the gateway is reached.
            while (!string.Equals(current, RootId, StringComparison.OrdinalIgnoreCase))
            {
                current = Parent[current];
                route.Add(current);
            }

            return route;
        }

        // The total link cost along that route.
        public int GetRouteCost(string nodeId)
        {
            int cost = 0;
            string current = nodeId;

            while (Parent.ContainsKey(current))
            {
                cost += ParentCost[current];
                current = Parent[current];
            }

            return cost;
        }
    }
}

// References:
// Baeldung, 2026. Kruskal's vs Prim's algorithm. [online] Available at: <https://www.baeldung.com/cs/kruskals-vs-prims-algorithm> [Accessed 11 November 2026].