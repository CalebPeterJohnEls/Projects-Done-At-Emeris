namespace Smart_X_API.DataStructures
{
    // The mesh network as a graph. The vertices are the nodes and the edges
    // are the links (Sedgewick and Wayne, 2019a). An adjacency list (a
    // dictionary from node to its links) is used, which suits a mesh where each
    // node has only a few neighbours, because it only stores the links that
    // really exist (GeeksforGeeks, 2026; Interview Kickstart, 2026). Links work
    // both ways, so adding one adds an edge in each direction.
    public class MeshGraph
    {
        // Every node, found by its id. The comparer ignores upper and lower
        // case, so ids and MAC addresses match however they are typed.
        private readonly Dictionary<string, MeshNode> nodes =
            new Dictionary<string, MeshNode>(StringComparer.OrdinalIgnoreCase);

        // Every node's list of links.
        private readonly Dictionary<string, List<MeshEdge>> adjacency =
            new Dictionary<string, List<MeshEdge>>(StringComparer.OrdinalIgnoreCase);

        public IEnumerable<MeshNode> Nodes => nodes.Values;

        // Adds a node and gives it an empty list of links.
        public void AddNode(MeshNode node)
        {
            if (nodes.ContainsKey(node.Id))
            {
                return;
            }

            nodes[node.Id] = node;
            adjacency[node.Id] = new List<MeshEdge>();
        }

        public MeshNode? GetNode(string id)
        {
            return nodes.TryGetValue(id, out MeshNode? node) ? node : null;
        }

        // Marks a node as online or offline.
        public void SetOnline(string id, bool online)
        {
            if (nodes.TryGetValue(id, out MeshNode? node))
            {
                node.IsOnline = online;
            }
        }

        // Adds a link in both directions. A link to itself or a link that
        // already exists is ignored.
        public void AddEdge(string a, string b, int cost)
        {
            if (!nodes.ContainsKey(a) || !nodes.ContainsKey(b))
            {
                return;
            }

            string from = nodes[a].Id;
            string to = nodes[b].Id;

            if (from == to || adjacency[from].Any(e => e.To == to))
            {
                return;
            }

            adjacency[from].Add(new MeshEdge(from, to, cost));
            adjacency[to].Add(new MeshEdge(to, from, cost));
        }

        // Every link once (not once per direction), for drawing the graph.
        public List<MeshEdge> GetEdges()
        {
            List<MeshEdge> edges = new List<MeshEdge>();

            foreach (List<MeshEdge> links in adjacency.Values)
            {
                foreach (MeshEdge link in links)
                {
                    // Only keep the direction where From comes first
                    // alphabetically, so each link is counted once.
                    if (string.Compare(link.From, link.To, StringComparison.OrdinalIgnoreCase) < 0)
                    {
                        edges.Add(link);
                    }
                }
            }

            return edges;
        }

        // Breadth-first search. It visits the start node, then all its
        // neighbours, then their neighbours, using a Queue so nodes are
        // handled in the order they were found (Interview Kickstart, 2026).
        // It runs in time proportional to the nodes plus the links
        // (Baeldung, 2026a). Offline nodes are skipped, so the result is every
        // device that can still reach the start through working nodes.
        public List<string> FindConnected(string startId)
        {
            List<string> connected = new List<string>();

            if (!nodes.TryGetValue(startId, out MeshNode? start) || !start.IsOnline)
            {
                return connected;
            }

            // "visited" stops the search from going back to a node it has seen.
            HashSet<string> visited = new HashSet<string>(StringComparer.OrdinalIgnoreCase) { start.Id };
            Queue<string> queue = new Queue<string>();
            queue.Enqueue(start.Id);

            while (queue.Count > 0)
            {
                string current = queue.Dequeue();
                connected.Add(current);

                foreach (MeshEdge edge in adjacency[current])
                {
                    if (!visited.Contains(edge.To) && nodes[edge.To].IsOnline)
                    {
                        visited.Add(edge.To);
                        queue.Enqueue(edge.To);
                    }
                }
            }

            return connected;
        }

        // Breadth-first search that groups the nodes into levels. Level 0 is
        // the start node, level 1 is every node one hop away, level 2 is every
        // node two hops away, and so on. Breadth-first search finds nodes in
        // order of how many hops they are from the start (Sedgewick and Wayne,
        // 2019a). The Queue hands the nodes out in the order they were found,
        // and a Dictionary remembers how many hops each node is from the
        // start. Offline nodes are skipped.
        public List<List<string>> FindLevels(string startId)
        {
            List<List<string>> levels = new List<List<string>>();

            if (!nodes.TryGetValue(startId, out MeshNode? start) || !start.IsOnline)
            {
                return levels;
            }

            Dictionary<string, int> distance = new Dictionary<string, int>(StringComparer.OrdinalIgnoreCase);
            Queue<string> queue = new Queue<string>();

            distance[start.Id] = 0;
            levels.Add(new List<string> { start.Id });
            queue.Enqueue(start.Id);

            while (queue.Count > 0)
            {
                string current = queue.Dequeue();

                foreach (MeshEdge edge in adjacency[current])
                {
                    if (!distance.ContainsKey(edge.To) && nodes[edge.To].IsOnline)
                    {
                        int hops = distance[current] + 1;
                        distance[edge.To] = hops;

                        // Start a new level the first time this many hops is reached.
                        if (levels.Count == hops)
                        {
                            levels.Add(new List<string>());
                        }

                        levels[hops].Add(edge.To);
                        queue.Enqueue(edge.To);
                    }
                }
            }

            return levels;
        }

        // Nodes that are switched off or have stopped responding.
        public List<string> FindOffline()
        {
            return nodes.Values.Where(n => !n.IsOnline).Select(n => n.Id).ToList();
        }

        // Nodes that are still on but cannot reach the gateway, because
        // every route to it goes through an offline node.
        public List<string> FindUnreachable(string gatewayId)
        {
            HashSet<string> connected = new HashSet<string>(FindConnected(gatewayId), StringComparer.OrdinalIgnoreCase);

            return nodes.Values
                .Where(n => n.IsOnline && !connected.Contains(n.Id))
                .Select(n => n.Id)
                .ToList();
        }

        // Breadth-first search for the route with the fewest hops between
        // two nodes that avoids offline nodes. Breadth-first search finds the
        // shortest path when counting edges (Baeldung, 2026a). This is the
        // "where to hop" answer when a node goes down. It returns null if no
        // route exists.
        public List<string>? FindRoute(string fromId, string toId)
        {
            if (!nodes.TryGetValue(fromId, out MeshNode? from) || !from.IsOnline ||
                !nodes.TryGetValue(toId, out MeshNode? to) || !to.IsOnline)
            {
                return null;
            }

            // "cameFrom" remembers which node each node was reached from, so
            // the route can be rebuilt by walking backwards (Interview
            // Kickstart, 2026).
            Dictionary<string, string> cameFrom = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
            HashSet<string> visited = new HashSet<string>(StringComparer.OrdinalIgnoreCase) { from.Id };
            Queue<string> queue = new Queue<string>();
            queue.Enqueue(from.Id);

            while (queue.Count > 0)
            {
                string current = queue.Dequeue();

                // Reached the target, so walk back to the start to get the route.
                if (string.Equals(current, to.Id, StringComparison.OrdinalIgnoreCase))
                {
                    List<string> route = new List<string> { current };

                    while (cameFrom.ContainsKey(current))
                    {
                        current = cameFrom[current];
                        route.Add(current);
                    }

                    route.Reverse();
                    return route;
                }

                foreach (MeshEdge edge in adjacency[current])
                {
                    if (!visited.Contains(edge.To) && nodes[edge.To].IsOnline)
                    {
                        visited.Add(edge.To);
                        cameFrom[edge.To] = current;
                        queue.Enqueue(edge.To);
                    }
                }
            }

            return null;
        }

        // Prim's algorithm. It starts at the gateway and keeps adding the
        // cheapest link that reaches a node not yet in the tree (Educative,
        // 2026; Sedgewick and Wayne, 2019b). Prim's algorithm works like
        // Dijkstra's algorithm, but it picks the cheapest single link
        // (Baeldung, 2026b). A PriorityQueue always hands back the item with
        // the smallest priority value first (Microsoft, 2026), so the
        // cheapest waiting link comes out first. A link to a node already in
        // the tree is skipped, which is what stops loops. Offline nodes are
        // left out, so the tree is rebuilt around them.
        public MstResult CalculateMst(string rootId)
        {
            MstResult result = new MstResult();

            if (!nodes.TryGetValue(rootId, out MeshNode? root) || !root.IsOnline)
            {
                return result;
            }

            result.RootId = root.Id;

            HashSet<string> inTree = new HashSet<string>(StringComparer.OrdinalIgnoreCase) { root.Id };
            PriorityQueue<MeshEdge, int> queue = new PriorityQueue<MeshEdge, int>();

            // Start with every working link that leaves the gateway.
            foreach (MeshEdge edge in adjacency[root.Id])
            {
                if (nodes[edge.To].IsOnline)
                {
                    queue.Enqueue(edge, edge.Cost);
                }
            }

            while (queue.Count > 0)
            {
                // Take the cheapest waiting link.
                MeshEdge edge = queue.Dequeue();

                // Its far end is already in the tree, so adding it would make a loop.
                if (inTree.Contains(edge.To))
                {
                    continue;
                }

                // Add the link and remember how this node connects back.
                inTree.Add(edge.To);
                result.Edges.Add(edge);
                result.TotalCost += edge.Cost;
                result.Parent[edge.To] = edge.From;
                result.ParentCost[edge.To] = edge.Cost;

                // The new node's links are now waiting to be chosen.
                foreach (MeshEdge next in adjacency[edge.To])
                {
                    if (!inTree.Contains(next.To) && nodes[next.To].IsOnline)
                    {
                        queue.Enqueue(next, next.Cost);
                    }
                }
            }

            return result;
        }
    }
}

// References:
// Baeldung, 2026a. BFS vs Dijkstra's algorithm (graph algorithms). [online] Available at: <https://www.baeldung.com/cs/graph-algorithms-bfs-dijkstra> [Accessed 11 November 2026].
// Baeldung, 2026b. Kruskal's vs Prim's algorithm. [online] Available at: <https://www.baeldung.com/cs/kruskals-vs-prims-algorithm> [Accessed 11 November 2026].
// Educative, 2026. Jarník's ("Prim's") algorithm. [online] Available at: <https://www.educative.io/courses/mastering-algorithms-for-problem-solving-in-python/jarniks-prims-algorithm> [Accessed 11 November 2026].
// GeeksforGeeks, 2026. Comparison between adjacency list and adjacency matrix representation of graph. [online] Available at: <https://www.geeksforgeeks.org/?p=430188> [Accessed 11 November 2026].
// Interview Kickstart, 2026. Breadth-first search (BFS) algorithm. [online] Available at: <https://www.interviewkickstart.com/blog/learn/breadth-first-search-algorithm> [Accessed 11 November 2026].
// Microsoft, 2026. PriorityQueue<TElement,TPriority> class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.priorityqueue-2?view=net-10.0> [Accessed 11 November 2026].
// Sedgewick, R. and Wayne, K., 2019a. 4.1 Undirected Graphs. [lecture slides online] Available at: <https://www.cs.princeton.edu/courses/archive/fall19/cos226/lectures/41UndirectedGraphs.pdf> [Accessed 11 November 2026].
// Sedgewick, R. and Wayne, K., 2019b. 4.3 Minimum Spanning Trees. [lecture slides online] Available at: <https://www.cs.princeton.edu/courses/archive/fall19/cos226/lectures/43MinimumSpanningTrees.pdf> [Accessed 11 November 2026].