using LiveDataSimulation.Models;
using System.Net.Http.Json;
using System.Text.Json;

namespace LiveDataSimulation.Services
{
    // ApiClient holds all the HTTP calls to the Smart-X API. Keeping them in
    // one class means the rest of the simulator never deals with web
    // addresses or JSON (Troelsen and Japikse, 2022; The IIE, 2026).
    public class ApiClient
    {
        private readonly HttpClient _http;
        private readonly string _baseUrl;

        // One HttpClient is created and reused for every request. Requests
        // time out after 5 seconds, so a stopped API does not freeze the
        // simulator (Troelsen and Japikse, 2022).
        public ApiClient(string baseUrl)
        {
            _baseUrl = baseUrl;
            _http = new HttpClient();
            _http.BaseAddress = new Uri(baseUrl);
            _http.Timeout = TimeSpan.FromSeconds(5);
        }

        // Gets the sensors registered in Part 1. GetFromJsonAsync converts
        // the JSON reply into a list of RegisteredSensor objects. If the reply
        // is empty, an empty list is returned instead of null
        // (Troelsen and Japikse, 2022).
        public async Task<List<RegisteredSensor>> GetRegisteredSensorsAsync()
        {
            List<RegisteredSensor>? sensors = await _http.GetFromJsonAsync<List<RegisteredSensor>>("api/sensors");
            return sensors ?? new List<RegisteredSensor>();
        }

        // A read-only property that lets other classes see the address.
        public string BaseUrl
        {
            get { return _baseUrl; }
        }

        // Tries up to 15 times, two seconds apart, until the API answers.
        // Connection errors are caught with try/catch and ignored, so the
        // loop just tries again (Nakov and Kolev et al., 2013). The "when"
        // filter only ignores a timeout if the user did not press Ctrl+C.
        public async Task<bool> WaitForApiAsync(CancellationToken ct)
        {
            for (int i = 1; i <= 15; i++)
            {
                try
                {
                    HttpResponseMessage response = await _http.GetAsync("api/telemetry/count", ct);
                    if (response.IsSuccessStatusCode)
                    {
                        Logger.Log("API reachable.");
                        return true;
                    }
                }
                catch (HttpRequestException) { }
                catch (TaskCanceledException) when (!ct.IsCancellationRequested) { }

                Logger.Log("Waiting for API (" + i + "/15)...");
                await Task.Delay(2000, ct);
            }
            return false;
        }

        // Real devices ask for their orders, so the relay checks whether a
        // manual command is waiting for it. The API answers 200 with the
        // command, or 204 when nothing is waiting. null means "no command".
        public async Task<bool?> GetPendingCommandAsync(string deviceId)
        {
            HttpResponseMessage response = await _http.GetAsync("api/stream/pending/" + deviceId);
            if (response.StatusCode != System.Net.HttpStatusCode.OK)
                return null;

            JsonElement body = await response.Content.ReadFromJsonAsync<JsonElement>();
            return body.GetProperty("turnOn").GetBoolean();
        }

        // Sends one reading to the API like a real device would. The device's
        // kind decides which endpoint is used and which data type is sent
        // (float, int or bool), matching the generic TelemetryPacket<T> used
        // in Part 1 (Troelsen and Japikse, 2022). PostAsJsonAsync converts the
        // object to JSON and posts it in one step (Microsoft, 2026e).
        public async Task<HttpResponseMessage> PostPacketAsync(SimDevice device, double value, DateTime timestamp)
        {
            switch (device.Kind)
            {
                case DeviceKind.Temperature:
                    return await _http.PostAsJsonAsync("api/telemetry/temperature", new
                    {
                        deviceIdentifier = device.Id,
                        timestamp = timestamp,
                        metricName = "Temperature",
                        value = (float)value,
                        unit = "°C"
                    });

                case DeviceKind.Moisture:
                    return await _http.PostAsJsonAsync("api/telemetry/moisture", new
                    {
                        deviceIdentifier = device.Id,
                        timestamp = timestamp,
                        metricName = "Moisture",
                        value = (float)value,
                        unit = "%"
                    });

                case DeviceKind.Power:
                    return await _http.PostAsJsonAsync("api/telemetry/power", new
                    {
                        deviceIdentifier = device.Id,
                        timestamp = timestamp,
                        metricName = "Power",
                        value = (int)value,
                        unit = "W"
                    });

                default:
                    return await _http.PostAsJsonAsync("api/telemetry/actuator", new
                    {
                        deviceIdentifier = device.Id,
                        timestamp = timestamp,
                        metricName = "ValveState",
                        value = value > 0.5,
                        unit = "bool"
                    });
            }
        }
    }
}

// References:
// Microsoft, 2026e. HttpClientJsonExtensions.PostAsJsonAsync Method (System.Net.Http.Json). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.http.json.httpclientjsonextensions.postasjsonasync> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.