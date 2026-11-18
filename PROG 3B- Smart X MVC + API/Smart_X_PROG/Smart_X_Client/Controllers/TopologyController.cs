using Microsoft.AspNetCore.Mvc;
using Smart_X_Client.Services;

namespace Smart_X_Client.Controllers
{
    // TopologyController sits between the browser and the API, in the same
    // way as CommandStreamController. The page's JavaScript calls these
    // actions, and each one asks the API for data through ApiService and
    // returns it as JSON (Troelsen and Japikse, 2022; The IIE, 2026).
    public class TopologyController : Controller
    {
        private readonly ApiService _apiService;

        // ApiService is supplied through the constructor.
        public TopologyController(ApiService apiService)
        {
            _apiService = apiService;
        }

        // GET /Topology
        // Shows the page. The data is loaded afterwards by JavaScript.
        public IActionResult Index()
        {
            return View();
        }

        // GET /Topology/Snapshot
        // Returns the whole network as JSON. If the API cannot be reached,
        // HTTP 503 is returned instead of crashing the page.
        [HttpGet]
        public async Task<IActionResult> Snapshot()
        {
            try
            {
                return Json(await _apiService.GetTopologyAsync());
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // GET /Topology/Route?term=ESP32-0001
        // Returns the route of one sensor, found by identifier or MAC address.
        [HttpGet]
        public async Task<IActionResult> Route(string term)
        {
            try
            {
                return Json(await _apiService.GetRouteAsync(term));
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // GET /Topology/Metrics?term=ESP32-0001
        // Returns the metrics a sensor has reported.
        [HttpGet]
        public async Task<IActionResult> Metrics(string term)
        {
            try
            {
                return Json(await _apiService.GetTopologyMetricsAsync(term));
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // GET /Topology/History?term=...&metric=...&from=...&to=...
        // Returns the readings in the chosen time range. A refused request
        // is returned as HTTP 400 with the API's message.
        [HttpGet]
        public async Task<IActionResult> History(string term, string metric, string from, string to)
        {
            try
            {
                var (result, message) = await _apiService.GetTopologyHistoryAsync(term, metric, from, to);

                if (result == null)
                {
                    return BadRequest(new { message });
                }

                return Json(result);
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }
    }
}

// References:
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.