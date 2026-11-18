using Microsoft.AspNetCore.Mvc;
using Smart_X_Client.Services;

namespace Smart_X_Client.Controllers
{
    // CommandStreamController sits between the browser and the API. The page's
    // JavaScript calls these actions, and each action asks the API for the
    // data through ApiService, then sends the result back as JSON. This keeps
    // the API address out of the browser and follows the same pattern as the
    // Sensors page in Part 1 (Troelsen and Japikse, 2022; The IIE, 2026).
    public class CommandStreamController : Controller
    {
        private readonly ApiService _apiService;

        // ApiService is supplied through the constructor (dependency
        // injection), so the controller does not create it itself
        // (Troelsen and Japikse, 2022).
        public CommandStreamController(ApiService apiService)
        {
            _apiService = apiService;
        }

        // GET /CommandStream
        // Shows the dashboard page. The live data is loaded afterwards by
        // JavaScript, so this action only returns the view.
        public IActionResult Index()
        {
            return View();
        }

        // GET /CommandStream/Snapshot
        // Returns all the dashboard data as JSON. The method is async so the
        // server is not blocked while it waits for the API. If the API cannot
        // be reached, the exception is caught and HTTP 503 (service
        // unavailable) is returned instead of crashing the page
        // (Troelsen and Japikse, 2022; Nakov and Kolev et al., 2013).
        [HttpGet]
        public async Task<IActionResult> Snapshot(string? category, bool alertsOnly, string? search)
        {
            try
            {
                return Json(await _apiService.GetStreamSnapshotAsync(category, alertsOnly, search));
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // GET /CommandStream/History
        // Returns the sorted readings for one device, used to draw the
        // timeline graph.
        [HttpGet]
        public async Task<IActionResult> History(string deviceId)
        {
            try
            {
                return Json(await _apiService.GetStreamHistoryAsync(deviceId));
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // POST /CommandStream/Command
        // Sends a manual override (ON or OFF) for one device to the API.
        // POST is used because the request changes data on the server
        // (Troelsen and Japikse, 2022).
        [HttpPost]
        public async Task<IActionResult> Command(string deviceId, bool turnOn)
        {
            try
            {
                bool ok = await _apiService.SendCommandAsync(deviceId, turnOn);
                return Json(new { success = ok });
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // POST /CommandStream/Undo
        // Asks the API to reverse the most recent command. "success" is
        // false when there was nothing to undo.
        [HttpPost]
        public async Task<IActionResult> Undo()
        {
            try
            {
                bool ok = await _apiService.UndoCommandAsync();
                return Json(new { success = ok });
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }

        // POST /CommandStream/LogSearch
        // Tells the API what the user searched for, so the pattern engine can
        // learn from it.
        [HttpPost]
        public async Task<IActionResult> LogSearch(string term)
        {
            try
            {
                await _apiService.LogSearchAsync(term);
                return Ok();
            }
            catch (HttpRequestException)
            {
                return StatusCode(503, new { message = "API unreachable" });
            }
        }
    }
}

// References:
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.