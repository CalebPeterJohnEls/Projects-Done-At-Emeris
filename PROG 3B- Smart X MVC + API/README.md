# Smart-X Application

# Part 1

## Task 1 – Evidence

### YouTube Video Demonstration

**Figure 1: Task 1 YouTube Video Demonstration**

[[VIDEO PRESENTATION](https://youtu.be/xgU4IZZousg)]

*The video demonstrates the completed functionality implemented for Task 1.*

### PDF Evidence

**Figure 2: Task 1 PDF Evidence**

[View Task 1 PDF](https://github.com/EMECPE/prog7312-2026-poe-calebpeterjohnels/blob/main/ST10306162_PROG7312_POE%20Part1%20Task%201%20and%202.pdf)


*The PDF contains the supporting evidence for Task 1, including relevant screenshots and/or documentation of the implemented functionality.*

---

## Running the Application

### 1. Restore Dependencies

Open a terminal in the project directory and run:

```bash
dotnet restore
```

This restores all NuGet packages required by the Smart-X API and client application.

### 2. Compile the Application

Build the solution using:

```bash
dotnet build
```

To clean the previous build before compiling again:

```bash
dotnet clean
dotnet build
```

### 3. Start the Backend API

Open a terminal and run:

```bash
dotnet run --project Smart_X_PROG
```

The backend API will be available at:

```text
https://localhost:7282
```

The API endpoints include:

```text
/api/sensors
/api/telemetry
/api/deployment
/api/file
```

### 4. Run the Client UI

Open a **second terminal** and run:

```bash
dotnet run --project Smart_X_Client
```

The client application will be available at:

```text
https://localhost:7148
```

Open the following address in a web browser:

```text
https://localhost:7148
```

### 5. Client-to-API Communication

The client communicates with the Smart-X backend API using `HttpClient`, which provides a way for .NET applications to send HTTP requests and receive responses from web services (Microsoft, 2026ad; Microsoft, 2026d).

The client is configured to use:

```text
https://localhost:7282/
```

The backend API must be running before using client features that communicate with the API. This separates the browser-facing MVC client from the API that manages sensor data and application operations.

---

# Part 2

## Part 2 – Real-Time Processing and Device Management

### YouTube Video Demonstration

**Figure 3: Part 2 YouTube Video Demonstration**

[[VIDEO PRESENTATION](https://youtu.be/ZUxzRXvd3k0)]

*The video demonstrates the Command Stream & History page, the live data simulation and the file upload improvements.*



### What Was Added

- **Command Stream & History page:** a real-time dashboard that shows incoming device readings, active alerts, command history, undo, search and filters, and a timeline graph for each device. Pages load without a full refresh. The browser retrieves updated information through JavaScript HTTP requests rather than reloading the whole page (Mozilla Developer Network, 2026h; W3Schools, 2025c).
- **Data structures:** a Queue and PriorityQueue process incoming readings, a Stack supports undo, Dictionaries and a SortedDictionary hold device state and sorted history, and a HashSet tracks active alerts. These collections are selected for their different access and ordering behaviours: queues process items in arrival or priority order, stacks use last-in, first-out order, and sets prevent duplicate entries (Nakov and Kolev et al., 2013; Troelsen and Japikse, 2022; Microsoft, 2026f).
- **Suggested Actions:** an engine that learns from repeated alert patterns and from temperature and moisture trends, and suggests actions to the user.
- **Live data simulation:** a separate console project (`LiveDataSimulation`) that behaves like real devices. It sends temperature, moisture and power telemetry to the API, responds to commands sent from the dashboard, and can trigger scenarios such as power spikes, heat and disconnects from a console menu. The temperature gauges on the Sensors page now move with these live readings instead of showing dummy data. The client periodically requests the latest telemetry and updates the displayed values using browser-side JavaScript (Mozilla Developer Network, 2026h; Mozilla Developer Network, 2026i).
- **File upload fixes (Part 1 resubmission):** sensors now save as active, upload errors are shown to the user instead of being ignored, more file types are accepted (.gif, .webp, .cfg, .conf, .ini, .yaml, .yml), and attachments can be opened from the browser. ASP.NET Core supports receiving uploaded files through form-bound file data, which the application validates and stores (Microsoft, 2026z; kudvenkat, 2024).
- **More uploads from Details:** the Sensor Details page now has an **Attach file** form, so users can add more files to a sensor after it has been registered. Image attachments are shown on the page. The uploaded content is sent to the server as form data and handled by the API (Microsoft, 2026z; Microsoft, 2026ad).
- **Saved data:** registered sensors are stored in a JSON file and uploaded files are kept on disk, so neither is lost when the API restarts or the containers are rebuilt. The application serializes its sensor data as JSON and writes the resulting data to storage (Microsoft, 2026ac; Microsoft, 2026aa).

### Additional API Endpoint

```text
/api/stream
```

### Running the Live Data Simulation

Start the API and the client as described in Part 1, then open a **third terminal** and run:

```bash
dotnet run --project LiveDataSimulation
```

The simulator reads the registered sensors from the API and sends live readings for them. The Command Stream & History page updates as the readings arrive. Restart the simulator after registering new sensors so that it picks them up.

### Simulator Controls

Press a key in the simulator's console window to trigger a scenario:

| Key | Scenario |
|-----|----------|
| S | Power spike on a device (power above 3000) |
| M | Moisture drops on a device |
| T | Temperature rises on a device (above 35) |
| D | Drops a device offline (it stops sending readings) |
| R | Restores all dropped devices |
| F | Floods the API with spike readings |
| Q | Quits the simulator |

---

## Testing the Queue, PriorityQueue, Dictionary, SortedDictionary, HashSet and Stack

### Before You Start

1. Start the API, the client and the simulator (see above, or use Docker below).
2. Register at least three sensors from the Sensors page: one **Environmental**, one **Power Consumption** and one **Actuator**.
3. Restart the simulator so that it picks up the new sensors.
4. Open the **Command Stream & History** page in the browser and keep it visible while pressing keys in the simulator console. If you are using Docker, attach to the simulator first with `docker attach smart-x-simulator` in a terminal (see the Docker section).

### How Readings Are Processed

- Every incoming reading goes into one of two queues. A normal reading goes into a standard **Queue** and is processed first-in, first-out. A queue follows first-in, first-out processing, meaning the earliest item added is the next item removed (Nakov and Kolev et al., 2013; Troelsen and Japikse, 2022).
- A reading that crosses an alert limit goes into a **PriorityQueue**. A power spike (power above 3000) has priority 0 and any other alert, such as temperature above 35, has priority 1. Lower numbers are processed first. A priority queue removes elements according to their assigned priority rather than only their insertion order (Microsoft, 2026f).
- A background worker processes the queues every 500 milliseconds, handling alerts before normal readings. Background services allow long-running work to execute within the lifetime of an ASP.NET Core hosted application (Microsoft, 2026a).

### Test 1: Queue (normal readings)

1. Leave the simulator running without pressing any key.
2. Watch the readings appear on the dashboard.

**Expected result:** Readings from each device appear in the order they were sent. The alert queue count stays at 0 because no reading crosses a limit.

### Test 2: PriorityQueue (simulate a high-priority alert)

1. Press **S** in the simulator console to cause a power spike on a device.
2. Watch the dashboard.

**Expected result:**
- A power alert for that device appears in the active alerts list.
- The alert queue count rises briefly and then returns to 0 as the worker processes it.
- The alert appears straight away, even though normal readings from other devices are still arriving.

To prove that priority 0 is processed before priority 1:

1. Press **T** to start a temperature alert (priority 1).
2. Immediately press **S** to cause a power spike (priority 0).
3. Press **F** to flood the API with spike readings.

**Expected result:** The power spike alerts are handled before the temperature alert, even though the temperature alert was queued first. The alert queue count rises while the flood is processed and drains back to 0 afterwards.

### Test 3: Dictionary (latest state per device)

1. Look at the device list on the dashboard.
2. Wait for several readings from the same device.

**Expected result:** Each device appears **once** and its row updates in place with the newest value. It is not duplicated for each reading, because device state is stored in a Dictionary keyed by device ID. A dictionary stores values against unique keys, allowing the application to retrieve or update a device by its identifier (Troelsen and Japikse, 2022).

Next, use the search box and the category and alerts-only filters.

**Expected result:** The list narrows to the matching devices straight away, because a device is found by its key.

### Test 4: SortedDictionary (history in time order)

1. Click a device to open its history timeline graph.
2. Wait a few seconds and watch the graph.

**Expected result:** The points are always in time order from oldest to newest. Each device keeps at most its latest 200 readings, so very old points drop off the graph over time. A sorted dictionary maintains its key-value pairs in key order, which supports displaying readings chronologically (Troelsen and Japikse, 2022).

### Test 5: HashSet (no duplicate alerts)

1. Press **S** several times in a row for the same device.
2. Look at the active alerts list.

**Expected result:** The device shows **one** power alert, not one for every reading, because an alert is added to the HashSet only if it is not already there. A HashSet stores unique elements, so attempting to add an element that is already present does not create a duplicate (Troelsen and Japikse, 2022).

Next, press **D** to drop a device offline.

**Expected result:** After a short wait a single "Disconnected" alert appears for that device. Press **R** to restore it. The device comes back online and its alert is removed.

### Test 6: Stack (undo commands)

1. Send two commands to an actuator from the dashboard, for example turn it on and then turn it off.
2. Check the command history.
3. Click **Undo** once.

**Expected result:** The most recent command (turn off) is undone first. Click **Undo** again to undo the earlier command. This is last-in, first-out: the most recently added item is the first one removed from a stack (Nakov and Kolev et al., 2013; Troelsen and Japikse, 2022). When there is nothing left to undo, the Undo action is rejected and nothing changes.

### Test 7: Suggested Actions

1. Press **S** twice or more on the same device, waiting for the alert to clear between presses.
2. Check the Suggested Actions panel.

**Expected result:** A suggestion appears once the same alert has happened at least twice.

To test the trend suggestion, press **T** (temperature) or **M** (moisture) and let the values keep moving towards their limit.

**Expected result:** A trend suggestion appears before the limit is reached, for example when the temperature is predicted to cross 35 within five minutes.

### Testing Without the Interface

The API can be queried directly to see the same data structures at work.

```bash
curl http://localhost:8082/api/stream/snapshot
```

The response shows the device list, the active alerts, the number of alerts waiting in the priority queue and the suggestions. Use `https://localhost:7282` instead of `http://localhost:8082` when running without Docker. The API documentation page is available at `/scalar/v1` on the same address.

---

# Part 3

## Part 3 – Network Topology and Mesh Routing

### YouTube Video Demonstration

**Figure 4: Part 3 YouTube Video Demonstration**

[[VIDEO PRESENTATION]([ADD-YOUR-PART-3-LINK-HERE](https://youtu.be/xFT_GPoWVXo))]

*The video demonstrates the Network Topology page, the spanning tree, route tracking and the history query.*


**Figure 5: PDF with Project Completion Report and Technical Recommendations**

[View Task 1 PDF]([[https://github.com/EMECPE/prog7312-2026-poe-calebpeterjohnels/blob/main/ST10306162_PROG7312_POE%20Part1%20Task%201%20and%202.pdf](https://github.com/EMECPE/prog7312-2026-poe-calebpeterjohnels/blob/main/ST10306162_PROG7312_POE%20Part3.pdf](https://github.com/EMECPE/prog7312-2026-poe-calebpeterjohnels/blob/main/ST10306162_PROG7312_POE%20Part3.pdf))

### 1. What This Part Adds

Parts 1 and 2 let you register ESP32 sensors and watch their telemetry arrive in real time. Part 3 answers a different question: how are those sensors actually connected, and what happens when one of them dies?

The new **Network Topology** page draws the sensor mesh live. It shows the gateway in the middle, the sensors around it, the link costs between them, and the cheapest set of links that keeps everything connected. You can search for any sensor to see its route back to the gateway, and you can query a sensor's history between two points in time.

Three data structures and two algorithms make that work:

| Feature | What powers it | Where it lives |
|---|---|---|
| Time-indexed telemetry history | AVL tree | `DataStructures/AvlTree.cs` |
| The mesh network model | Graph (adjacency list) | `DataStructures/MeshGraph.cs` |
| Connectivity, hop levels, fallback route | Breadth-first search (BFS) | `MeshGraph.FindConnected`, `FindLevels`, `FindRoute` |
| Cheapest backbone and power-efficient routes | Minimum spanning tree (Prim's) | `MeshGraph.CalculateMst`, `MstResult` |
| Live drawing and queries | Razor view, JavaScript, SVG | `Smart_X_Client/Views/Topology` |

### 2. What You Need

You need the .NET 10 SDK and a modern browser. Docker Desktop is optional and only needed for the container route.

### 3. Restore, Build and Run

Restore and build from the `Smart_X_PROG` folder:

```bash
cd Smart_X_PROG
dotnet restore
dotnet build
```

Then open three terminals in the same folder and start things in this order, because the client needs the API to be up first:

```bash
dotnet run --project Smart_X_PROG --launch-profile https      # API     https://localhost:7282
dotnet run --project Smart_X_Client --launch-profile https    # Client  https://localhost:7148
dotnet run --project LiveDataSimulation                       # Simulator (after registering sensors)
```

If the browser complains about the certificate, run `dotnet dev-certs https --trust` once. To run everything in Docker instead, see the Docker section below.

Inside Compose, each service registers its name with an internal DNS server, so the client can reach the API using the service name instead of an IP address (Docker, 2026). That is why the client's `ApiBaseUrl` is `http://smart_x_api:8080/` in Docker. Service-to-service traffic uses the container port, while the host port is only for reaching a service from outside the network (Docker, 2026). The browser uses the published `localhost:8082` address, which is why there is a separate `ApiPublicUrl` setting. Docker Compose provides service-name-based discovery between containers, while published ports expose selected services to the host machine (Docker, 2026).

### 4. Using the Network Topology Page

1. Open **Sensors** and register at least six sensors across the three types (Environmental, Power Consumption, Actuator). Make sure they are set to **Active**.
2. Start the simulator, then open **Network Topology**. The page refreshes itself every three seconds using periodic browser-side requests (Mozilla Developer Network, 2026i).
3. Read the map. The gateway is the centre node, sensors sit on a ring around it, and grey lines are mesh links labelled with their power cost. The highlighted lines are the minimum spanning tree.
4. Type a device ID or MAC address into the route box and press **Track**. You get two answers: the lowest-power route along the spanning tree, and the fewest-hops route found by BFS.
5. To see failure handling, press **D** in the simulator window (run directly, or attached with `docker attach smart-x-simulator` in Docker) to drop a device. After about ten seconds it moves to the *Down nodes* list and the spanning tree rebuilds around it. Any sensor left with no path to the gateway is flagged **Unreachable**. Press **R** to bring the device back.
6. For history, pick a sensor, a metric and a time range (or one of the quick buttons) and press **Query**. The result shows how many readings the tree holds, how tall the tree is, how many nodes the query touched and how long it took.

The history and the mesh links are generated sample data. The history is backfilled for the last 24 hours when the page first asks for it, and the links are derived by rule because sensors have no real link information.

### 5. API Endpoints

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/topology` | Nodes, links, spanning-tree edges, BFS levels, offline and unreachable lists |
| GET | `/api/topology/route/{term}` | Both routes for a sensor (ID or MAC) |
| GET | `/api/topology/metrics/{term}` | Metrics a sensor has reported |
| GET | `/api/topology/history/{term}?metric=&from=&to=` | Range query on the AVL tree (ISO 8601 times) |

The browser never calls the API directly. It calls `/Topology/Snapshot`, `/Topology/Route`, `/Topology/Metrics` and `/Topology/History` on the client, and the client's `ApiService` forwards each request using a named `HttpClient` created by `IHttpClientFactory`. The factory centralises the creation and configuration of `HttpClient` instances for outgoing requests (Microsoft, 2026d).

### 6. How the Pieces Fit Together

```
Browser (SVG + JavaScript, polls /Topology/Snapshot every 3 s)
  -> Smart_X_Client: TopologyController -> ApiService (HttpClient)
     -> Smart_X_API: TopologyController -> TopologyService (singleton, guarded by a lock)
          |- AvlTree<DateTime, HistoryPoint>   one per (device, metric)
          |- MeshGraph (adjacency list)
          |     |- BFS      connectivity, levels, fewest-hops route
          |     |- Prim's   minimum spanning tree
          |- HashSet<string> of offline devices
StreamWorker (BackgroundService) -> StreamService -> TopologyService.RecordReading / SetOnline
```

`StreamWorker` is a hosted service, registered with `AddHostedService`, so it starts with the API and runs for its whole lifetime (Microsoft, 2026a). It feeds the topology service with every reading and every online or offline change.

### 6.1 Why a Graph and a Minimum Spanning Tree

Smart-X models a building full of ESP32 sensors, and ESP32 boards can talk to each other in a mesh. In a mesh, a node does not have to connect straight to the gateway. It can pass its readings through neighbouring nodes. ESP-MESH, for example, lets nodes scan for, choose, connect to and reconnect to other nodes and routers on their own, which is called self-organised networking (Espressif Systems, 2026). In ESP-MESH, a root node connects the mesh to the router, which is the role the gateway plays in Smart-X (Espressif Systems, 2026).

That design creates three questions that a simple list of sensors cannot answer:

| Question | Why a list is not enough | What Smart-X uses |
|---|---|---|
| Which devices can still reach the gateway? | Connectivity depends on the links between devices, not on each device alone | A **graph** (sensors as vertices, wireless links as edges) searched with **BFS** |
| Which route takes the fewest hops? | A route is a chain of links, so it has to be found by walking the graph | **BFS**, which finds the fewest hops when every link counts the same (Baeldung, 2026b) |
| Which links give the cheapest way to keep everything connected? | Every link has a power cost, and the cheapest set of links is not obvious from the devices | **Prim's minimum spanning tree**, which grows a tree from the gateway by repeatedly adding the cheapest link to a device not yet in the tree (Educative, 2026) |

When a device goes offline, the graph and the tree are rebuilt without it. Any device that is still on but can no longer reach the gateway is shown as **Unreachable**. This mirrors the self-healing idea in a mesh, where the network finds new paths when a node fails (Espressif Systems, 2026).

In Smart-X the sensors do not report real link information, so `BuildGraph` creates the links and costs by rule (see section 4). The graph, BFS and Prim's code run exactly as they would on real link data.

### 7. The Data Structures, and Why Each One Is Here

#### 7.1 AVL tree: the telemetry history

**The job.** Every reading is stored under its timestamp. `TopologyService` keeps one tree for each device and metric, so a query for "temperature on sensor X between 10:00 and 11:00" only searches that one tree.

**Why a plain search tree is not enough.** Telemetry arrives in strictly increasing time order, and that is the worst possible input for an ordinary binary search tree. Each new reading goes to the far right, the tree turns into a long chain, and its operations slow from O(log n) to O(n) (Baeldung, 2026c; GeeksforGeeks, 2026a).

An AVL tree prevents this. It was the first dynamically balanced search tree to be proposed, and its rule is simple: the two subtrees of every node may differ in height by at most one (Morris, 1998; Rajinikanth, 2026). Each node stores a balance factor, the height of its left subtree minus the height of its right, and a healthy node has a factor of -1, 0 or +1 (GeeksforGeeks, 2026a; Rajinikanth, 2026). A new key is inserted as a leaf exactly as in an ordinary search tree. The balance factors are then checked, and if the rule is broken a rotation puts things right (Morris, 1998; Rajinikanth, 2026). There are four rotations: a single left, a single right, and the two double ones (left-right and right-left) (Rajinikanth, 2026). Each rotation takes constant time (GeeksforGeeks, 2026a). Search, insert and delete all stay at O(log n) (Baeldung, 2026c; Morris, 1998).

This is the rebalancing step from `AvlTree.cs`. After an insert it recalculates the balance factor, and the four branches correspond to the four rotations (Rajinikanth, 2026):

```csharp
private AvlNode<TKey, TValue> Balance(AvlNode<TKey, TValue> node)
{
    UpdateHeight(node);
    int balance = GetHeight(node.Left) - GetHeight(node.Right);

    if (balance > 1)
    {
        if (GetHeight(node.Left!.Left) < GetHeight(node.Left.Right))
            node.Left = RotateLeft(node.Left);      // left-right case: two rotations
        return RotateRight(node);                   // left-left case: one rotation
    }
    if (balance < -1)
    {
        if (GetHeight(node.Right!.Right) < GetHeight(node.Right.Left))
            node.Right = RotateRight(node.Right);   // right-left case
        return RotateLeft(node);                    // right-right case
    }
    return node;
}
```

The class is generic with `where TKey : IComparable<TKey>`, which tells the compiler that keys can be compared and lets the same tree work for timestamps or any other ordered type (Microsoft, 2026b).

**The range query.** The history page asks for every reading between a `from` and a `to` time. The search only walks into a branch if it could contain matches, so whole subtrees outside the window are skipped:

```csharp
if (from.CompareTo(node.Key) < 0)  QueryRange(node.Left,  from, to, results, ref visited);   // matches may be on the left
if (from.CompareTo(node.Key) <= 0 && to.CompareTo(node.Key) >= 0) results.Add(node.Value);  // this node is in range
if (to.CompareTo(node.Key)   > 0)  QueryRange(node.Right, from, to, results, ref visited);   // matches may be on the right
```

That gives a cost of O(log n + k), where k is the number of readings returned. The logarithmic search component comes from the balanced height of the AVL tree, while the additional k component accounts for the matching values that must be collected (Baeldung, 2026c; Morris, 1998).

**How it scales.** The worst-case height of an AVL tree comes from a recurrence. If N(h) is the smallest number of nodes an AVL tree of height h can have, then N(0) = 1, N(1) = 2 and N(h) = 1 + N(h-1) + N(h-2) (Cornell University, 2021; GeeksforGeeks, 2026b). The values go 1, 2, 4, 7, 12, 20 and keep growing fast (Cornell University, 2021), which is why the height stays small. The tallest AVL tree that holds n nodes has the largest h where N(h) is still at most n.

I turned that into numbers, counting height in levels (the number of nodes on the longest path from the root down). A plain tree fed sorted data has n levels, and a full scan touches all n readings. These figures are calculated from the recurrence, not measured.

| Readings per sensor (n) | History at 30 s intervals | Best possible levels | AVL worst-case levels | Plain BST on sorted data | Full scan |
|---|---|---|---|---|---|
| 288 | 24 h at 5 min (actuators) | 9 | 11 | 288 | 288 |
| 2,880 | 24 h | 12 | 16 | 2,880 | 2,880 |
| 28,800 | 10 days | 15 | 21 | 28,800 | 28,800 |
| 288,000 | 100 days | 19 | 25 | 288,000 | 288,000 |
| 2,880,000 | about 1,000 days | 22 | 30 | 2,880,000 | 2,880,000 |

Here is a concrete case. A 30-minute window on the 2,880-reading tree returns 60 readings. The search follows the two boundary paths down the tree (at most 16 levels each) and collects the matches, so it visits roughly 60 + 2 x 16 = 92 nodes at most. A scan would check all 2,880. If the history grows a thousand times larger, from 2,880 readings to 2.88 million, the worst-case depth only goes from 16 levels to 30.

If your `TreeHeight` counts a single node as 0 rather than 1, subtract one from the AVL column.

**You can see this on the page.** `TopologyService.QueryHistory` wraps the query in a `Stopwatch` and returns `treeCount`, `treeHeight`, `nodesVisited` and `elapsedMs`, so the efficiency is measurable on screen and not just claimed.

#### 7.2 Graph: the mesh network

**The job.** The graph is the model of the physical network. Each vertex (`MeshNode`) is a sensor or the gateway, and each edge (`MeshEdge`) is a wireless link with a cost that stands for the power needed to send a packet. A graph represents relationships using vertices and edges, making it suitable for modelling connected devices and communication links (GeeksforGeeks, 2026c; Interview Kickstart, 2026).

```csharp
private readonly Dictionary<string, MeshNode> nodes = new(StringComparer.OrdinalIgnoreCase);
private readonly Dictionary<string, List<MeshEdge>> adjacency = new(StringComparer.OrdinalIgnoreCase);

public void AddEdge(string a, string b, int cost)
{
    adjacency[a].Add(new MeshEdge(a, b, cost));
    adjacency[b].Add(new MeshEdge(b, a, cost));     // links work in both directions
}
```

**Why an adjacency list.** Each sensor only links to three or four neighbours, so the graph is sparse. An adjacency list stores just the neighbours a node really has and uses O(V + E) space, where an adjacency matrix would need O(V squared) (GeeksforGeeks, 2026c; Interview Kickstart, 2026). The traversal algorithms also run faster on it, with BFS taking O(V + E) on a list instead of O(V squared) on a matrix (Interview Kickstart, 2026). The dictionary keyed by device ID gives average constant-time lookup, and the case-insensitive comparer means IDs and MAC addresses match whatever case the user types (Troelsen and Japikse, 2022).

**Where the links come from.** Registered sensors have a zone, a room and a MAC but no link data, so `BuildGraph` works the mesh out. It sorts sensors by zone, connects each one to the next three, and links every fifth sensor to the gateway. The link costs come from a `Random` seeded from the two device IDs, so the same pair of sensors always gets the same cost. Without that seeding the picture and the spanning tree would change on every three-second refresh.

#### 7.3 BFS: connectivity, levels and fallback routes

BFS works through a first-in-first-out queue, finds shortest paths in unweighted graphs (meaning the fewest edges), and runs in O(V + E) because each vertex and edge is considered a bounded number of times (Baeldung, 2026b; Interview Kickstart, 2026). Keeping a parent for each node lets you rebuild a path by walking back from the target, which is how the route is recovered (Interview Kickstart, 2026).

| Method | What it does |
|---|---|
| `FindConnected` | Finds which online nodes can still reach the gateway |
| `FindLevels` | Groups nodes by hop distance from the gateway |
| `FindRoute` | Returns the fewest-hops route that avoids offline nodes |
| `FindUnreachable` | Finds online nodes that are cut off because every path crosses a dead node |

Handling a failed node takes one condition in the neighbour loop. A node that is offline is simply never entered, so every route flows around it:

```csharp
foreach (MeshEdge edge in adjacency[current])
{
    if (!visited.Contains(edge.To) && nodes[edge.To].IsOnline)
    {
        visited.Add(edge.To);
        cameFrom[edge.To] = current;
        queue.Enqueue(edge.To);
    }
}
```

#### 7.4 Minimum spanning tree: Prim's algorithm

**The job.** A minimum spanning tree is the spanning tree with the lowest total edge weight (Baeldung, 2026a). In our mesh that means the cheapest set of links that still connects every online sensor to the gateway with no loops. It is the energy-saving backbone the page highlights.

Prim's algorithm is a modified version of Dijkstra's. It starts from one node and uses a priority queue to keep picking the cheapest edge that reaches a new node (Baeldung, 2026a; Educative, 2026). Our version (`MeshGraph.CalculateMst`) keeps all edges next to the tree in the queue, removes the cheapest, checks whether both ends are already in the tree, and only if not adds the edge and queues the new neighbouring edges (Educative, 2026). The queue itself is the .NET `PriorityQueue<TElement,TPriority>` (Microsoft, 2026f).

```csharp
PriorityQueue<MeshEdge, int> queue = new();
foreach (MeshEdge e in adjacency[root.Id])
    if (nodes[e.To].IsOnline) queue.Enqueue(e, e.Cost);

while (queue.Count > 0)
{
    MeshEdge edge = queue.Dequeue();             // cheapest waiting link
    if (inTree.Contains(edge.To)) continue;      // would create a loop, skip it

    inTree.Add(edge.To);
    result.Edges.Add(edge);
    result.TotalCost += edge.Cost;
    result.Parent[edge.To] = edge.From;          // parent links let us rebuild any route later
    result.ParentCost[edge.To] = edge.Cost;
    // push the new node's edges to nodes not yet in the tree
}
```

**How it scales.** With a binary heap this runs in O(E log E), which equals O(E log V) (Educative, 2026). Our mesh is sparse, so E is a small multiple of V and the cost grows almost in line with the number of sensors. Offline nodes are skipped while the tree is built, and the tree is rebuilt on every poll, so the backbone heals itself when a device drops. `MstResult.GetRoute()` walks the stored `Parent` links to return the route to the gateway, which costs only as much as the depth of the tree.

**Two routes, two goals.** The route panel shows the spanning-tree route (lowest power) next to the BFS route (fewest hops, so lowest latency). One thing to be clear about is that the route along the tree is the best path inside the tree, and it is not always the cheapest path in the whole graph. Dijkstra's algorithm finds that one, since it handles weighted graphs with a priority queue and runs in O(V + E log V) (Baeldung, 2026b).

#### 7.5 Thread safety

`TopologyService` is a singleton, so one instance is shared across the whole app. The `StreamWorker` writes readings while API requests read them at the same moment, and singleton services must be thread safe, so every access goes through one `lock`, which allows only one thread to run the protected code at a time (Microsoft, 2026c; Microsoft, 2026e). Microsoft's advice is to hold a lock for as short a time as possible, so only the tree and graph operations sit inside it (Microsoft, 2026e):

```csharp
// simplified from TopologyService
public void RecordReading(string deviceId, string metric, DateTime timestamp, double value)
{
    lock (_lock)
    {
        GetOrCreateTree(deviceId, metric).Insert(timestamp, new HistoryPoint(timestamp, value));
    }
}
```

### 8. Complexity at a Glance

| Operation | Cost |
|---|---|
| AVL insert | O(log n) |
| AVL range query | O(log n + k) |
| Graph neighbour lookup | O(degree) |
| BFS | O(V + E) |
| Prim's with a binary heap | O(E log V) |
| Route lookup on the spanning tree | O(depth) |

### 9. Troubleshooting

- **503 or "API unreachable".** Start the API first, or check `ApiBaseUrl` in `appsettings.json`. In Docker it should be `http://smart_x_api:8080/`.
- **History is empty.** Register the sensor and make sure it is Active. History is generated the first time the page asks for it.
- **A port is already in use.** Change `applicationUrl` in `Properties/launchSettings.json`.
- **Sensors or uploads disappear after `docker compose down`.** Do not use `-v`, which deletes the saved-data volumes.
- **The simulator keys do nothing in Docker.** Docker Desktop's Logs and Exec tabs cannot send keys. Use `docker attach smart-x-simulator` from a terminal (see the Docker section). The simulator also exits at start-up if there are no active sensors, so register at least one first.

---

# Lecturer Feedback Updates

The feedback responses for Parts 1 and 2 are documented in [`UPDATES.md`](UPDATES.md). The ledger records each identified lecturer comment, the change made, the affected source files/classes, and the steps to verify the implementation.

---

## Docker

The application, including the live data simulation, can also be started using Docker Compose.

Run:

```bash
docker compose up --build -d
```

The client will be available at:

```text
http://localhost:8083
```

The API will be available at:

```text
http://localhost:8082
```

Registered sensors and uploaded files are kept in Docker volumes, so they remain after the containers are stopped or rebuilt.

To use the simulator's interactive console menu, attach to its container from a terminal such as PowerShell or Windows Terminal. `docker attach` connects your terminal's input, output and error streams to the running container (Docker, Inc., 2026b). This works because the simulator service has `stdin_open` and `tty` set in `docker-compose.yml`, which is the same as running the container with `-i` and `-t` (Docker, Inc., 2026b):

```bash
docker attach smart-x-simulator
```

Press the scenario keys from the Simulator Controls table in that window. The Logs and Exec tabs in Docker Desktop cannot send keys to the simulator, so use a terminal. To leave without stopping the simulator, press Ctrl+P and then Ctrl+Q, which only works because the container was started with `-i` and `-t` (Docker, Inc., 2026b). Do not press Ctrl+C while attached, because it can stop the container.

To restart the simulator after registering new sensors:

```bash
docker compose restart smart_x_simulator
```

To stop the Docker containers (your data is kept):

```bash
docker compose down
```

Do not use `docker compose down -v` unless you want to delete the saved sensors and uploaded files.

## References

### Parts 1 and 2

Bootstrap, 2026a. Badges. [online] Available at:
https://getbootstrap.com/docs/5.3/components/badge/ [Accessed 9 September 2026].

Bootstrap, 2026b. Bootstrap Components. [online] Available at:
https://getbootstrap.com/docs/5.3/components/ [Accessed 8 September 2026].

Bootstrap, 2026c. Buttons. [online] Available at:
https://getbootstrap.com/docs/5.3/components/buttons/ [Accessed 9 September 2026].

Bootstrap, 2026d. Cards. [online] Available at:
https://getbootstrap.com/docs/5.3/components/card/ [Accessed 10 September 2026].

Bootstrap, 2026e. Grid system. [online] Available at:
https://getbootstrap.com/docs/5.3/layout/grid/ [Accessed 8 September 2026].

Code-Maze, 2026. C# Basics: Recursion. [online] Available at:
https://code-maze.com/csharp-basics-recursion/ [Accessed 9 September 2026].

Designer Who Codes, 2025. How to use Font Awesome with HTML / CSS (2025 Edition). [video online] Available at:
https://www.youtube.com/watch?v=mtA__aN2UJA [Accessed 20 April 2026].

dev.to (paulallies), 2025. Creating a Responsive Side Navigation Bar with HTML, CSS and JavaScript. [online] Available at:
https://dev.to/paulallies/creating-a-responsive-side-navigation-bar-with-html-css-and-javascript-3538 [Accessed 20 April 2026].

Docker, Inc., 2026a. Docker Compose overview. [online] Available at:
https://docs.docker.com/compose/ [Accessed 10 September 2026].

Docker, Inc., 2026b. docker container attach. [online] Available at:
https://docs.docker.com/reference/cli/docker/container/attach/ [Accessed 13 November 2026].

Espressif Systems, 2026. ESP-MESH programming guide. [online] Available at: <https://docs.espressif.com/projects/esp-idf/en/v4.3/api-reference/network/esp_mesh.html> [Accessed 13 November 2026].

Font Awesome, 2026. Icon search. [online] Available at:
https://fontawesome.com/search [Accessed 20 April 2026].

GreatStack, 2022. How To Make Side Navigation Menu Using HTML And CSS | Sidebar Menu Design. [video online] Available at:
https://www.youtube.com/watch?v=Fx_NpAPM-iU [Accessed 20 April 2026].

Hamdan, S., Ayyash, M. and Almajali, S., 2020. Edge-Computing Architectures for Internet of Things Applications: A Survey. Sensors, 20(22), p.6441. Available at:
https://doi.org/10.3390/s20226441 [Accessed 8 September 2026].

IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at:
https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK [Accessed 18 March 2025].

IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at:
https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7 [Accessed 20 September 2025].

kudvenkat, 2024. File Upload in ASP.NET Core MVC. [video online] Available at:
https://www.youtube.com/watch?v=aoxEJii70_I [Accessed 8 June 2026].

Microsoft, 2026a. Anchor Tag Helper in ASP.NET Core. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/mvc/views/tag-helpers/built-in/anchor-tag-helper [Accessed 9 September 2026].

Microsoft, 2026b. ASP.NET Core CORS. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/security/cors [Accessed 1 June 2026].

Microsoft, 2026c. ASP.NET Core Dependency Injection. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection [Accessed 1 June 2026].

Microsoft, 2026d. ConcurrentDictionary<TKey,TValue> Class. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.collections.concurrent.concurrentdictionary-2 [Accessed 10 September 2026].

Microsoft, 2026e. Containerize a .NET app. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/core/docker/build-container [Accessed 8 September 2026].

Microsoft, 2026f. Dependency injection into views in ASP.NET Core. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/mvc/views/dependency-injection [Accessed 9 September 2026].

Microsoft, 2026g. Enumerable Class. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.linq.enumerable [Accessed 10 September 2026].

Microsoft, 2026h. File.Delete Method. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.io.file.delete [Accessed 1 June 2026].

Microsoft, 2026i. Generate OpenAPI documents and use Scalar for interactive API documentation. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/fundamentals/openapi/using-openapi-documents [Accessed 8 September 2026].

Microsoft, 2026j. Generic types and methods. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/csharp/fundamentals/types/generics [Accessed 9 September 2026].

Microsoft, 2026k. Guid.NewGuid Method. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.guid.newguid [Accessed 1 June 2026].

Microsoft, 2026l. Interlocked.Increment Method. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.threading.interlocked.increment [Accessed 10 September 2026].

Microsoft, 2026m. IWebHostEnvironment Interface. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.hosting.iwebhostenvironment [Accessed 1 June 2026].

Microsoft, 2026n. Jagged Arrays (C# Programming Guide). [online] Available at:
https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/arrays/jagged-arrays [Accessed 8 September 2026].

Microsoft, 2026o. JsonStringEnumConverter Class. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.text.json.serialization.jsonstringenumconverter [Accessed 9 September 2026].

Microsoft, 2026p. Multidimensional Arrays (C# Programming Guide). [online] Available at:
https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/arrays/multidimensional-arrays [Accessed 10 September 2026].

Microsoft, 2026q. Operator overloading. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/operator-overloading [Accessed 8 September 2026].

Microsoft, 2026r. Pass data to views in ASP.NET Core. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/mvc/views/overview [Accessed 9 September 2026].

Microsoft, 2026s. Path.Combine Method. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.io.path.combine [Accessed 1 June 2026].

Microsoft, 2026t. Path.GetFileName Method (System.IO). [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.io.path.getfilename [Accessed 10 September 2026].

Microsoft, 2026u. Random Class. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.random [Accessed 8 September 2026].

Microsoft, 2026v. Razor syntax reference for ASP.NET Core. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/mvc/views/razor [Accessed 9 September 2026].

Microsoft, 2026w. SelectList Class. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.mvc.rendering.selectlist [Accessed 1 June 2026].

Microsoft, 2026x. Standard numeric format strings. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/standard/base-types/standard-numeric-format-strings [Accessed 10 September 2026].

Microsoft, 2026y. Tag Helpers in ASP.NET Core. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/mvc/views/tag-helpers/intro [Accessed 8 September 2026].

Microsoft, 2026z. Upload files in ASP.NET Core. [online] Available at:
https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads [Accessed 10 April 2026].

Microsoft, 2026aa. File.WriteAllText Method (System.IO). [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.io.file.writealltext [Accessed 19 October 2026].

Microsoft, 2026ab. FileStream Class (System.IO). [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.io.filestream [Accessed 19 October 2026].

Microsoft, 2026ac. How to serialize JSON in C# - .NET. [online] Available at:
https://learn.microsoft.com/en-us/dotnet/standard/serialization/system-text-json/how-to [Accessed 19 October 2026].

Microsoft, 2026ad. MultipartFormDataContent Class (System.Net.Http). [online] Available at:
https://learn.microsoft.com/en-us/dotnet/api/system.net.http.multipartformdatacontent [Accessed 19 October 2026].

Mozilla Developer Network, 2026a. Conditional (ternary) operator. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Conditional_operator [Accessed 9 September 2026].

Mozilla Developer Network, 2026b. Document: createElement() method. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/API/Document/createElement [Accessed 10 September 2026].

Mozilla Developer Network, 2026c. Math.min(). [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/min [Accessed 8 September 2026].

Mozilla Developer Network, 2026d. position CSS property. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/CSS/position [Accessed 9 September 2026].

Mozilla Developer Network, 2026e. Response.json(). [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/API/Response/json [Accessed 10 September 2026].

Mozilla Developer Network, 2026f. try...catch. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/try...catch [Accessed 8 September 2026].

Mozilla Developer Network, 2026g. Basic concepts of flexbox. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_flexible_box_layout/Basic_concepts_of_flexbox [Accessed 9 September 2026].

Mozilla Developer Network, 2026h. Window: fetch() method. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/API/Window/fetch [Accessed 10 September 2026].

Mozilla Developer Network, 2026i. Window: setInterval() method. [online] Available at:
https://developer.mozilla.org/en-US/docs/Web/API/Window/setInterval [Accessed 8 September 2026].

Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at:
https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf [Accessed 9 September 2026].

Refactoring Guru, 2024. Composite Design Pattern. [online] Available at:
https://refactoring.guru/design-patterns/composite [Accessed 15 March 2026].

The IIE, 2025. Information System 2C [INSY7213 Module Manual]. The Independent Institute of Education: Unpublished.

The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.

Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.

W3Schools, 2025a. CSS Tutorial. [online] Available at:
https://www.w3schools.com/css/ [Accessed 20 April 2026].

W3Schools, 2025b. HTML Tutorial. [online] Available at:
https://www.w3schools.com/Html/ [Accessed 20 April 2026].

W3Schools, 2025c. JavaScript Fetch API. [online] Available at:
https://www.w3schools.com/js/js_api_fetch.asp [Accessed 20 April 2026].

W3Schools, 2025d. Razor C# Logic. [online] Available at:
https://www.w3schools.com/asp/razor_cs_logic.asp [Accessed 20 April 2026].

### Part 3

Baeldung, 2026a. Kruskal's vs Prim's algorithm. [online] Available at: <https://www.baeldung.com/cs/kruskals-vs-prims-algorithm> [Accessed 11 November 2026].

Baeldung, 2026b. BFS vs Dijkstra's algorithm (graph algorithms). [online] Available at: <https://www.baeldung.com/cs/graph-algorithms-bfs-dijkstra> [Accessed 11 November 2026].

Baeldung, 2026c. Red-black tree vs AVL tree. [online] Available at: <https://www.baeldung.com/cs/red-black-tree-vs-avl-tree> [Accessed 11 November 2026].

Cornell University, 2021. Balanced binary trees. CS 2112 lecture notes. [online] Available at: <https://www.cs.cornell.edu/courses/cs2112/2021fa/lectures/avl> [Accessed 11 November 2026].

Docker, 2026. Networking in Compose. [online] Available at: <https://docs.docker.com/compose/how-tos/networking/> [Accessed 11 November 2026].

Educative, 2026. Jarník's ("Prim's") algorithm. [online] Available at: <https://www.educative.io/courses/mastering-algorithms-for-problem-solving-in-python/jarniks-prims-algorithm> [Accessed 11 November 2026].

GeeksforGeeks, 2026a. AVL tree data structure. [online] Available at: <https://www.geeksforgeeks.org/dsa/introduction-to-avl-tree/> [Accessed 11 November 2026].

GeeksforGeeks, 2026b. Minimum number of nodes in an AVL tree with given height. [online] Available at: <https://www.geeksforgeeks.org/minimum-number-of-nodes-in-an-avl-tree-with-given-height/> [Accessed 11 November 2026].

GeeksforGeeks, 2026c. Comparison between adjacency list and adjacency matrix representation of graph. [online] Available at: <https://www.geeksforgeeks.org/?p=430188> [Accessed 11 November 2026].

Interview Kickstart, 2026. Breadth-first search (BFS) algorithm. [online] Available at: <https://www.interviewkickstart.com/blog/learn/breadth-first-search-algorithm> [Accessed 11 November 2026].

Microsoft, 2026a. Background tasks with hosted services in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/aspnet/core/fundamentals/host/hosted-services?view=aspnetcore-10.0> [Accessed 11 November 2026].

Microsoft, 2026b. Constraints on type parameters (C# programming guide). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/generics/constraints-on-type-parameters> [Accessed 11 November 2026].

Microsoft, 2026c. Dependency injection: service lifetimes. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/dependency-injection/service-lifetimes> [Accessed 11 November 2026].

Microsoft, 2026d. IHttpClientFactory with .NET. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/httpclient-factory> [Accessed 11 November 2026].

Microsoft, 2026e. The lock statement: ensure exclusive access to a shared resource. [online] Available at: <https://learn.microsoft.com/dotnet/csharp/language-reference/statements/lock> [Accessed 11 November 2026].

Microsoft, 2026f. PriorityQueue<TElement,TPriority> class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.priorityqueue-2?view=net-10.0> [Accessed 11 November 2026].

Morris, J., 1998. Data structures and algorithms: AVL trees. [online] Available at: <https://www.cs.auckland.ac.nz/software/AlgAnim/AVL.html> [Accessed 11 November 2026].

Rajinikanth, B., 2026. AVL tree datastructure. [online] Available at: <http://btechsmartclass.com/data_structures/avl-trees.html> [Accessed 11 November 2026].
