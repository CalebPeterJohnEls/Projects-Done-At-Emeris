namespace Smart_X_API.DataStructures
{
    // One wireless link between two nodes. The cost stands for the power
    // needed to send a packet over the link (a lower cost is cheaper). A graph
    // with a number on each edge is called a weighted graph (Sedgewick and
    // Wayne, 2019).
    public class MeshEdge
    {
        public string From { get; set; }
        public string To { get; set; }
        public int Cost { get; set; }

        public MeshEdge(string from, string to, int cost)
        {
            From = from;
            To = to;
            Cost = cost;
        }
    }
}

// References:
// Sedgewick, R. and Wayne, K., 2019. 4.1 Undirected Graphs. [lecture slides online] Available at: <https://www.cs.princeton.edu/courses/archive/fall19/cos226/lectures/41UndirectedGraphs.pdf> [Accessed 11 November 2026].