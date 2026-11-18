using Microsoft.AspNetCore.Mvc;
using Smart_X_Client.Models;
using Smart_X_Client.Services;

//  MVC controller providing the standard CRUD views
// (Index, Create, Details, Edit, Delete) for sensors, plus a small JSON
// endpoint used for polling live telemetry. All actual data access goes
// through ApiService, which talks to the Smart-X API over HTTP - this
// controller never touches storage directly (The IIE, 2026; IIE Emeris School
// of Computer Science, 2025b).

namespace Smart_X_Client.Controllers
{
    public class SensorsController : Controller
    {
        private readonly ApiService _apiService;

        public SensorsController(ApiService apiService)
        {
            _apiService = apiService;
        }

        // GET /Sensors
        // Loads the sensor list and the six most recent temperature
        // readings (sorted with LINQ's OrderByDescending/Take,
        // Microsoft, 2026g). If the API cannot be reached, the page still
        // renders with empty data and a warning banner instead of crashing.
        public async Task<IActionResult> Index()
        {
            try
            {
                var sensors = await _apiService.GetSensorsAsync();
                var temperature = await _apiService.GetTemperatureAsync();

                ViewBag.ApiConnected = true;
                ViewBag.Temperature = temperature.OrderByDescending(t => t.Timestamp).Take(6).ToList();

                return View(sensors);
            }
            catch (Exception)
            {
                ViewBag.ApiConnected = false;
                ViewBag.Temperature = new List<TelemetryPacket<float>>();
                return View(new List<Sensor>());
            }
        }

        // GET /Sensors/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST /Sensors/Create
        // Accepts the form fields individually ([FromForm]) plus an
        // optional file, following the ASP.NET Core file-upload pattern
        // (Microsoft, 2026z; kudvenkat, 2024). The text fields are nullable
        // so that an empty box reaches this method as null instead of
        // failing silently, and are trimmed and checked here before the API
        // is called. New sensors are registered as active so the simulator
        // and the Command Stream pick them up. On failure, validation errors
        // are copied into ModelState so the Razor validation summary/spans
        // can display them.
        [HttpPost]
        public async Task<IActionResult> Create(
            [FromForm] string? DeviceIdentifier,
            [FromForm] string? MacAddress,
            [FromForm] string? Room,
            [FromForm] string? Zone,
            [FromForm] string? NodeId,
            [FromForm] string? Category,
            IFormFile? file)
        {
            var sensor = new Sensor
            {
                DeviceIdentifier = DeviceIdentifier?.Trim() ?? string.Empty,
                MacAddress = MacAddress?.Trim() ?? string.Empty,
                Room = Room?.Trim() ?? string.Empty,
                Zone = Zone?.Trim() ?? string.Empty,
                NodeId = NodeId?.Trim() ?? string.Empty,
                Category = string.IsNullOrWhiteSpace(Category) ? "Environmental" : Category,
                Active = true
            };

            if (string.IsNullOrWhiteSpace(sensor.DeviceIdentifier))
                ModelState.AddModelError(nameof(Sensor.DeviceIdentifier), "Device identifier is required.");

            if (string.IsNullOrWhiteSpace(sensor.MacAddress))
                ModelState.AddModelError(nameof(Sensor.MacAddress), "MAC address is required.");

            if (!ModelState.IsValid)
                return View(sensor);

            var (created, errors) = await _apiService.CreateSensorAsync(sensor);

            if (created == null)
            {
                foreach (var err in errors)
                    ModelState.AddModelError("", err);

                return View(sensor);
            }

            // Only attempt the file upload once the sensor itself has been
            // created successfully, since the upload needs its Id. A failed
            // upload no longer disappears silently: the reason is kept in
            // TempData and shown on the next page.
            if (file != null && file.Length > 0)
            {
                var (uploaded, message) = await _apiService.UploadFileAsync(created.Id, file);

                if (!uploaded)
                    TempData["UploadError"] = "Sensor " + created.DeviceIdentifier +
                        " was registered, but the file was not saved: " + message;
            }

            return RedirectToAction(nameof(Index));
        }

        // POST /Sensors/Attach/{id}
        // Attaches a file to a sensor that already exists, using the same
        // upload path as Create (Microsoft, 2026z).
        [HttpPost]
        public async Task<IActionResult> Attach(int id, IFormFile? file)
        {
            if (file == null || file.Length == 0)
            {
                TempData["UploadError"] = "Choose a file to attach.";
                return RedirectToAction(nameof(Details), new { id });
            }

            var (uploaded, message) = await _apiService.UploadFileAsync(id, file);

            if (!uploaded)
                TempData["UploadError"] = message;

            return RedirectToAction(nameof(Details), new { id });
        }

        // GET /Sensors/Details/{id}
        public async Task<IActionResult> Details(int id)
        {
            var sensor = await _apiService.GetSensorAsync(id);
            if (sensor == null) return NotFound();
            return View(sensor);
        }

        // GET /Sensors/Edit/{id}
        public async Task<IActionResult> Edit(int id)
        {
            var sensor = await _apiService.GetSensorAsync(id);
            if (sensor == null) return NotFound();
            return View(sensor);
        }

        // POST /Sensors/Edit/{id}
        [HttpPost]
        public async Task<IActionResult> Edit(int id, Sensor sensor)
        {
            var updated = await _apiService.UpdateSensorAsync(id, sensor);

            if (!updated)
            {
                ModelState.AddModelError("", "Failed to update sensor.");
                return View(sensor);
            }

            return RedirectToAction(nameof(Index));
        }

        // GET /Sensors/Delete/{id}
        public async Task<IActionResult> Delete(int id)
        {
            var sensor = await _apiService.GetSensorAsync(id);
            if (sensor == null) return NotFound();
            return View(sensor);
        }

        // POST /Sensors/Delete/{id}
        // ActionName("Delete") lets this method be named DeleteConfirmed in
        // C# (to avoid a signature clash with the GET action above) while
        // still being routed to as "Delete" over HTTP.
        [HttpPost, ActionName("Delete")]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            await _apiService.DeleteSensorAsync(id);
            return RedirectToAction(nameof(Index));
        }

        // GET /Sensors/TelemetrySnapshot
        // Lightweight JSON endpoint polled from Sensors/Index.cshtml via
        // JavaScript's fetch() every few seconds to animate the "live"
        // telemetry feed (Mozilla Developer Network, 2026h). It returns the
        // newest temperature reading for every registered environmental
        // sensor, plus a status: Inactive (switched off), Offline (no
        // reading in the last 15 seconds) or Live (Microsoft, 2026g).
        // GET /Sensors/TelemetrySnapshot
        // Polled by Sensors/Index.cshtml every few seconds. Status now comes from
        // the Command Stream snapshot (StreamService on the API) instead of the
        // temperature-only telemetry list, so both pages always agree on whether
        // a device is online. This also lets the moisture probe appear here,
        // because moisture readings never reach the temperature list.
        [HttpGet]
        public async Task<IActionResult> TelemetrySnapshot()
        {
            try
            {
                var sensors = await _apiService.GetSensorsAsync();

                // Same data the Command Stream dashboard uses (no filters applied).
                var snapshot = await _apiService.GetStreamSnapshotAsync(null, false, null);

                // Index the live device states by id for fast lookup.
                var devices = snapshot.Devices.ToDictionary(d => d.DeviceId);

                var latest = sensors
                    // Only environmental sensors can appear in the temperature feed.
                    .Where(s => string.Equals(
                        s.Category,
                        "Environmental",
                        StringComparison.OrdinalIgnoreCase))

                    // Exclude inactive sensors.
                    .Where(s => s.Active)

                    // Only include sensors that have a stream entry.
                    .Where(s => devices.ContainsKey(s.DeviceIdentifier))

                    .Select(s =>
                    {
                        var d = devices[s.DeviceIdentifier];

                        return new
                        {
                            s.DeviceIdentifier,
                            Value = d.Value,
                            Timestamp = d.LastSeen,
                            Unit = d.Unit,
                            Metric = d.Metric,
                            Status = d.IsOnline ? "Live" : "Offline"
                        };
                    })

                    // Only temperature readings in degrees Celsius are allowed.
                    .Where(t =>
                        string.Equals(
                            t.Metric,
                            "Temperature",
                            StringComparison.OrdinalIgnoreCase)
                        &&
                        string.Equals(
                            t.Unit,
                            "°C",
                            StringComparison.OrdinalIgnoreCase))

                    .OrderBy(t => t.DeviceIdentifier)
                    .ToList();


                return Json(latest);
            }
            catch (HttpRequestException)
            {
                // Same behaviour as CommandStreamController when the API is down.
                return StatusCode(503, new { message = "API unreachable" });
            }
        }
    }
}

// References:
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025].
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
// kudvenkat, 2024. File Upload in ASP.NET Core MVC. [video online] Available at: <https://www.youtube.com/watch?v=aoxEJii70_I> [Accessed 8 June 2026].
// Microsoft, 2026g. Enumerable Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.linq.enumerable> [Accessed 10 September 2026].
// Microsoft, 2026z. Upload files in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
// Mozilla Developer Network, 2026h. Window: fetch() method. [online] Available at: <https://developer.mozilla.org/en-US/docs/Web/API/Window/fetch> [Accessed 10 September 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.