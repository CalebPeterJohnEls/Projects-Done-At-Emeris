using Microsoft.AspNetCore.Mvc;
using Smart_X_API.Models;
using Smart_X_API.Services;
using static Smart_X_API.Models.StreamModels;
using static Smart_X_API.Models.TopologyModels;

namespace Smart_X_API.Controllers
{
    // TopologyController is the entry point for the Network Topology page.
    // Like StreamController, it does no processing itself: each action passes
    // the request to TopologyService and returns the result (Troelsen and
    // Japikse, 2022; The IIE, 2026).
    [ApiController]
    [Route("api/[controller]")]
    public class TopologyController : ControllerBase
    {
        private readonly TopologyService _topology;

        // The shared TopologyService is supplied through the constructor
        // (Troelsen and Japikse, 2022).
        public TopologyController(TopologyService topology)
        {
            _topology = topology;
        }

        // GET api/topology
        // Returns the whole network: every node with its status, every link,
        // and which links are in the minimum spanning tree.
        [HttpGet]
        public IActionResult Get()
        {
            return Ok(_topology.GetTopology());
        }

        // GET api/topology/route/{term}
        // Returns the route of one sensor to the gateway. The term can be
        // the sensor's identifier or its MAC address.
        [HttpGet("route/{term}")]
        public IActionResult Route(string term)
        {
            return Ok(_topology.GetRoute(term));
        }

        // GET api/topology/metrics/{term}
        // Returns the metrics a sensor has reported, so the page can fill
        // its drop-down list. An unknown sensor is rejected with HTTP 404.
        [HttpGet("metrics/{term}")]
        public IActionResult Metrics(string term)
        {
            if (_topology.FindSensor(term) == null)
            {
                return NotFound(new { message = "No sensor matches '" + term + "'." });
            }

            return Ok(_topology.GetMetrics(term));
        }

        // GET api/topology/history/{term}?metric=Moisture&from=...&to=...
        // Returns the readings of one sensor and metric between two times,
        // found with the AVL tree's range query. DateTimeOffset is used for
        // from and to, so a time sent with a "Z" or an offset is converted
        // to UTC correctly, whatever time zone the server is in. The
        // response also reports the size and height of the tree.
        [HttpGet("history/{term}")]
        public IActionResult History(string term, string metric, DateTimeOffset from, DateTimeOffset to)
        {
            // Reject bad requests before doing any work.
            if (_topology.FindSensor(term) == null)
            {
                return NotFound(new { message = "No sensor matches '" + term + "'." });
            }

            if (string.IsNullOrWhiteSpace(metric))
            {
                return BadRequest(new { message = "A metric is required." });
            }

            if (from > to)
            {
                return BadRequest(new { message = "The start time must be before the end time." });
            }

            HistoryQueryResult result = _topology.QueryHistory(term, metric, from.UtcDateTime, to.UtcDateTime);

            // An anonymous object is sent back as JSON for the page to show.
            return Ok(new
            {
                deviceId = _topology.FindSensor(term)!.DeviceIdentifier,
                metric,
                from = from.UtcDateTime,
                to = to.UtcDateTime,
                count = result.Points.Count,
                treeCount = result.TreeCount,
                treeHeight = result.TreeHeight,
                nodesVisited = result.NodesVisited,
                elapsedMs = result.ElapsedMs,
                readings = result.Points
            });
        }
    }
}

// References:
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.