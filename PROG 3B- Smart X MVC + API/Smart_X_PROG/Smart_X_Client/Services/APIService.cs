using Smart_X_Client.Models;
using System.Text.Json;
using static Smart_X_Client.Models.StreamModels;
using static Smart_X_Client.Models.TopologyModels;

// Thin wrapper around HttpClient that centralises every call the
// MVC client makes to the Smart-X API. Keeping all HTTP calls in one service
// (rather than scattering HttpClient calls across controllers) follows the
// dependency-injection pattern encouraged by ASP.NET Core (Microsoft, 2026c).
// GetFromJsonAsync/PostAsJsonAsync automatically serialise and deserialise
// JSON using System.Text.Json.

namespace Smart_X_Client.Services
{
    public class ApiService
    {
        private readonly HttpClient _httpClient;

        // The named client "SmartXApi" was registered (with its base
        // address) in Program.cs via IHttpClientFactory (Microsoft, 2026c).
        public ApiService(IHttpClientFactory httpClientFactory)
        {
            _httpClient = httpClientFactory.CreateClient("SmartXApi");
        }

        public async Task<List<Sensor>> GetSensorsAsync()
        {
            var sensors = await _httpClient.GetFromJsonAsync<List<Sensor>>("api/sensors");
            return sensors ?? new List<Sensor>();
        }

        public async Task<Sensor?> GetSensorAsync(int id)
        {
            return await _httpClient.GetFromJsonAsync<Sensor>($"api/sensors/{id}");
        }

        // Posts a new sensor and, on failure, parses the API's JSON error
        // body manually with JsonDocument so both a validation-style
        // { "errors": {...} } payload and a simple { "message": "..." }
        // payload can be handled without needing a fixed error DTO.
        public async Task<(Sensor? Sensor, List<string> Errors)> CreateSensorAsync(Sensor sensor)
        {
            var response = await _httpClient.PostAsJsonAsync("api/sensors", sensor);

            if (response.IsSuccessStatusCode)
            {
                var created = await response.Content.ReadFromJsonAsync<Sensor>();
                return (created, new List<string>());
            }

            var errors = new List<string>();
            var body = await response.Content.ReadAsStringAsync();

            try
            {
                using var doc = JsonDocument.Parse(body);
                if (doc.RootElement.TryGetProperty("errors", out var errorsElement))
                {
                    foreach (var field in errorsElement.EnumerateObject())
                        foreach (var msg in field.Value.EnumerateArray())
                            errors.Add(msg.GetString() ?? "");
                }
                else if (doc.RootElement.TryGetProperty("message", out var messageElement))
                {
                    errors.Add(messageElement.GetString() ?? "Sensor could not be created.");
                }
            }
            catch
            {
                // Falls back to a generic message if the body was not the
                // JSON shape expected above (e.g. the API was unreachable).
                errors.Add("Sensor could not be created.");
            }

            errors = errors.Distinct().ToList();

            return (null, errors);
        }

        public async Task<bool> UpdateSensorAsync(int id, Sensor sensor)
        {
            var response = await _httpClient.PutAsJsonAsync($"api/sensors/{id}", sensor);
            return response.IsSuccessStatusCode;
        }

        public async Task<bool> DeleteSensorAsync(int id)
        {
            var response = await _httpClient.DeleteAsync($"api/sensors/{id}");
            return response.IsSuccessStatusCode;
        }

        // Streams the uploaded IFormFile straight through to the API as
        // multipart/form-data, avoiding loading the whole file into memory
        // first (Microsoft, 2026z; Microsoft, 2026aa). The file's own content
        // type is sent with it, and the result is returned as a success flag
        // plus a message so the controller can tell the user when an upload
        // fails instead of ignoring the response.
        public async Task<(bool Success, string Message)> UploadFileAsync(int sensorId, IFormFile file)
        {
            try
            {
                using var content = new MultipartFormDataContent();
                using var stream = file.OpenReadStream();

                var fileContent = new StreamContent(stream);
                fileContent.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(
                    string.IsNullOrWhiteSpace(file.ContentType) ? "application/octet-stream" : file.ContentType);

                content.Add(fileContent, "file", file.FileName);

                var response = await _httpClient.PostAsync($"api/file/{sensorId}", content);

                if (response.IsSuccessStatusCode)
                    return (true, "File uploaded.");

                var body = await response.Content.ReadAsStringAsync();

                try
                {
                    using var doc = JsonDocument.Parse(body);
                    if (doc.RootElement.TryGetProperty("message", out var messageElement))
                        return (false, messageElement.GetString() ?? "Upload failed.");
                }
                catch
                {
                    // The body was not JSON, so the status code is reported.
                }

                return (false, "Upload failed (" + (int)response.StatusCode + ").");
            }
            catch (HttpRequestException)
            {
                return (false, "The API could not be reached.");
            }
        }

        public async Task<List<TelemetryPacket<float>>> GetTemperatureAsync()
        {
            var data = await _httpClient.GetFromJsonAsync<List<TelemetryPacket<float>>>("api/telemetry/temperature");
            return data ?? new List<TelemetryPacket<float>>();
        }

        // Sends two Meter values to the API, where they are combined using
        // Meter's overloaded '+' operator server-side (Microsoft, 2026q).
        public async Task<Meter?> AggregateMetersAsync(Meter first, Meter second)
        {
            var response = await _httpClient.PostAsJsonAsync(
                "api/telemetry/meters/aggregate",
                new { First = first, Second = second });

            if (!response.IsSuccessStatusCode) return null;
            return await response.Content.ReadFromJsonAsync<Meter>();
        }

        public async Task<DeploymentNode?> GetSampleDeploymentTreeAsync()
        {
            return await _httpClient.GetFromJsonAsync<DeploymentNode>("api/deployment/sample");
        }

        // Sends the tree to the API for recursive validation (Code-Maze,
        // 2026) and unpacks the result into a simple tuple for the
        // controller/view to consume.
        public async Task<(bool IsValid, string Message)> ValidateDeploymentAsync(DeploymentNode node)
        {
            var response = await _httpClient.PostAsJsonAsync("api/deployment/validate", node);
            var result = await response.Content.ReadFromJsonAsync<DeploymentValidationResult>();
            return (result?.IsValid ?? false, result?.Message ?? "No response from API.");
        }

        // Command Stream methods
        // These methods call the Stream endpoints of the API. Each one is
        // async, so the program can do other work while it waits for the
        // answer (Troelsen and Japikse, 2022; The IIE, 2026).

        // Asks the API for the dashboard snapshot. The search and filter
        // values are added to the address as a query string, and
        // Uri.EscapeDataString makes sure special characters in the text are
        // safe to send. GetFromJsonAsync turns the JSON reply into a
        // StreamSnapshot object. If the reply is empty, an empty snapshot is
        // returned instead of null (Troelsen and Japikse, 2022).
        public async Task<StreamSnapshot> GetStreamSnapshotAsync(string? category, bool alertsOnly, string? search)
        {
            string url = "api/stream/snapshot?alertsOnly=" + alertsOnly.ToString().ToLower()
                       + "&category=" + Uri.EscapeDataString(category ?? "")
                       + "&search=" + Uri.EscapeDataString(search ?? "");

            var snapshot = await _httpClient.GetFromJsonAsync<StreamSnapshot>(url);
            return snapshot ?? new StreamSnapshot();
        }

        // Gets the sorted history of one device for the timeline graph.
        public async Task<List<HistoryPoint>> GetStreamHistoryAsync(string deviceId)
        {
            var points = await _httpClient.GetFromJsonAsync<List<HistoryPoint>>(
                "api/stream/history/" + Uri.EscapeDataString(deviceId));
            return points ?? new List<HistoryPoint>();
        }

        // Sends a manual command. PostAsJsonAsync converts the request object
        // to JSON and posts it in one step (Microsoft, 2026e). It returns true
        // when the API accepted the command.
        public async Task<bool> SendCommandAsync(string deviceId, bool turnOn)
        {
            var request = new CommandRequest { DeviceId = deviceId, TurnOn = turnOn };
            var response = await _httpClient.PostAsJsonAsync("api/stream/command", request);
            return response.IsSuccessStatusCode;
        }

        // Asks the API to undo the last command. It returns false when the API
        // answers that there is nothing to undo.
        public async Task<bool> UndoCommandAsync()
        {
            var response = await _httpClient.PostAsync("api/stream/undo", null);
            return response.IsSuccessStatusCode;
        }

        // Sends the search text to the API for the pattern engine.
        public async Task LogSearchAsync(string term)
        {
            await _httpClient.PostAsJsonAsync("api/stream/search", new SearchRequest { Term = term });
        }


        // Network Topology methods
        // These methods call the Topology endpoints of the API (Troelsen and
        // Japikse, 2022; The IIE, 2026).

        // Gets the whole network: nodes, links and the minimum spanning tree.
        // Network Topology methods
        // These methods call the Topology endpoints of the API (Troelsen and
        // Japikse, 2022; The IIE, 2026).

        // Gets the whole network: nodes, links and the minimum spanning tree.
        public async Task<TopologySnapshot> GetTopologyAsync()
        {
            var snapshot = await _httpClient.GetFromJsonAsync<TopologySnapshot>("api/topology");
            return snapshot ?? new TopologySnapshot();
        }

        // Gets the route of one sensor, found by its identifier or MAC
        // address. Uri.EscapeDataString makes the text safe for the address,
        // for example the colons in a MAC address.
        public async Task<RouteResult> GetRouteAsync(string term)
        {
            var result = await _httpClient.GetFromJsonAsync<RouteResult>(
                "api/topology/route/" + Uri.EscapeDataString(term));
            return result ?? new RouteResult();
        }

        // Gets the metrics a sensor has reported. An unknown sensor gives an
        // empty list.
        public async Task<List<string>> GetTopologyMetricsAsync(string term)
        {
            var response = await _httpClient.GetAsync("api/topology/metrics/" + Uri.EscapeDataString(term));
            if (!response.IsSuccessStatusCode) return new List<string>();

            var metrics = await response.Content.ReadFromJsonAsync<List<string>>();
            return metrics ?? new List<string>();
        }

        // Gets the readings of one sensor and metric between two times. The
        // times are passed on as the UTC text sent by the browser, so no
        // time zone conversion happens on the way. If the API refuses the
        // request, its message is returned so the page can show it.
        public async Task<(TopologyHistoryResult? Result, string Message)> GetTopologyHistoryAsync(
            string term, string metric, string fromUtc, string toUtc)
        {
            // Build the address with the search values added to the end.
            string url = "api/topology/history/" + Uri.EscapeDataString(term)
                       + "?metric=" + Uri.EscapeDataString(metric)
                       + "&from=" + Uri.EscapeDataString(fromUtc)
                       + "&to=" + Uri.EscapeDataString(toUtc);

            var response = await _httpClient.GetAsync(url);

            if (response.IsSuccessStatusCode)
            {
                var result = await response.Content.ReadFromJsonAsync<TopologyHistoryResult>();
                return (result, string.Empty);
            }

            // The API refused the request, so read its message from the body.
            var body = await response.Content.ReadAsStringAsync();

            try
            {
                using var doc = JsonDocument.Parse(body);
                if (doc.RootElement.TryGetProperty("message", out var messageElement))
                    return (null, messageElement.GetString() ?? "The request failed.");
            }
            catch
            {
                // The body was not JSON, so a general message is used.
            }

            return (null, "The request failed (" + (int)response.StatusCode + ").");
        }
    }

    // Small DTO matching the shape of the JSON returned by
    // api/deployment/validate.
    public class DeploymentValidationResult
    {
        public string NodeName { get; set; } = string.Empty;
        public bool IsValid { get; set; }
        public string Message { get; set; } = string.Empty;
    }
}

// References:
// Code-Maze, 2026. C# Basics: Recursion. [online] Available at: <https://code-maze.com/csharp-basics-recursion/> [Accessed 9 September 2026].
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025].
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
// Microsoft, 2026c. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection> [Accessed 1 June 2026].
// Microsoft, 2026e. HttpClientJsonExtensions.PostAsJsonAsync Method (System.Net.Http.Json). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.http.json.httpclientjsonextensions.postasjsonasync> [Accessed 17 October 2026].
// Microsoft, 2026q. Operator overloading. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/operator-overloading> [Accessed 8 September 2026].
// Microsoft, 2026z. Upload files in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
// Microsoft, 2026aa. MultipartFormDataContent Class (System.Net.Http). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.http.multipartformdatacontent> [Accessed 19 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.