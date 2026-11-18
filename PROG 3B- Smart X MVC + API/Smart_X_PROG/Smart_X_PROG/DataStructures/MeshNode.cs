namespace Smart_X_API.DataStructures
{
    // One vertex of the mesh: an ESP32 sensor or the central gateway. In a
    // graph, a vertex is a point and the lines between points are edges
    // (Sedgewick and Wayne, 2019).
    public class MeshNode
    {
        public string Id { get; set; }
        public string MacAddress { get; set; }

        // True for the central gateway, false for a sensor.
        public bool IsGateway { get; set; }

        // False when the device has stopped sending data.
        public bool IsOnline { get; set; } = true;

        public MeshNode(string id, string macAddress, bool isGateway = false)
        {
            Id = id;
            MacAddress = macAddress;
            IsGateway = isGateway;
        }
    }
}

// References:
// Sedgewick, R. and Wayne, K., 2019. 4.1 Undirected Graphs. [lecture slides online] Available at: <https://www.cs.princeton.edu/courses/archive/fall19/cos226/lectures/41UndirectedGraphs.pdf> [Accessed 11 November 2026].