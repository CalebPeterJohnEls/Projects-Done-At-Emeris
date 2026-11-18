using LiveDataSimulation.Models;
using LiveDataSimulation.Services;

namespace LiveDataSimulation
{
    // Program is the starting point of the simulator. It creates the helper
    // classes, builds the list of simulated devices from the sensors
    // registered in the API, then runs one loop per device so each one sends
    // readings on its own, like real hardware (Troelsen and Japikse, 2022;
    // The IIE, 2026).
    internal class Program
    {
        // Main is async so it can wait for the API and the device loops
        // without freezing the program (Troelsen and Japikse, 2022).
        static async Task Main(string[] args)
        {
            // The API address comes from a command-line argument, then an
            // environment variable, then a default. "??" means "use the next
            // value if the left one is null" (Troelsen and Japikse, 2022).
            string apiBase = args.FirstOrDefault()
                             ?? Environment.GetEnvironmentVariable("API_BASE")
                             ?? "http://localhost:5231/";
            if (!apiBase.EndsWith('/')) apiBase += "/";

            // A CancellationTokenSource lets one part of the program tell all
            // the running loops to stop. Pressing Ctrl+C or Q cancels it, so
            // the simulator shuts down cleanly (Troelsen and Japikse, 2022).
            using CancellationTokenSource cts = new CancellationTokenSource();
            Console.CancelKeyPress += (sender, e) =>
            {
                e.Cancel = true;
                cts.Cancel();
            };

            // Each class has one job: ApiClient talks to the API,
            // ValueGenerator makes the readings, and DeviceRunner runs a
            // device's send loop. Splitting the work like this is called
            // separation of concerns (Troelsen and Japikse, 2022;
            // Nakov and Kolev et al., 2013).
            ApiClient api = new ApiClient(apiBase);
            ValueGenerator generator = new ValueGenerator();
            DeviceRunner runner = new DeviceRunner(api, generator);

            Console.WriteLine("Smart-X live data simulator");
            Console.WriteLine("API: " + api.BaseUrl);
            Console.WriteLine("S = power spike | M = moisture drop | T = heat spike | D = drop a device offline | R = restore devices | F = flood spike readings | Q = quit\n");

            try
            {
                // Wait for the API so the simulator can be started first.
                if (!await api.WaitForApiAsync(cts.Token))
                {
                    Logger.Log("API never became reachable. Is it running?");
                    return;
                }

                // The devices are built from the sensors registered in Part 1,
                // so the dashboard and the sensors page show the same devices.
                List<RegisteredSensor> registered = await api.GetRegisteredSensorsAsync();
                List<SimDevice> devices = BuildDevices(registered);

                if (devices.Count == 0)
                {
                    Logger.Log("No active registered sensors found. Register sensors first.");
                    return;
                }

                Logger.Log("Simulating " + devices.Count + " registered sensors.");

                ScenarioController scenarios = new ScenarioController(devices, api, cts);

                // One task per device, so all devices run at the same time.
                // The keyboard listener is only added when a real keyboard is
                // attached. Task.WhenAll waits until every task has finished
                // (Troelsen and Japikse, 2022).
                List<Task> tasks = devices.Select(d => runner.RunDeviceAsync(d, cts.Token)).ToList();
                if (!Console.IsInputRedirected)
                    tasks.Add(scenarios.RunAsync(cts.Token));

                await Task.WhenAll(tasks);
            }
            catch (OperationCanceledException) { }

            Logger.Log("Simulator stopped.");
        }

        // Turns each active registered sensor into a simulated device. The
        // sensor's category decides what kind of device it becomes. A switch
        // chooses between the options, and a foreach loop goes through every
        // sensor (Nakov and Kolev et al., 2013).
        private static List<SimDevice> BuildDevices(List<RegisteredSensor> sensors)
        {
            List<SimDevice> devices = new List<SimDevice>();
            int environmentalCount = 0;

            foreach (RegisteredSensor sensor in sensors)
            {
                // Inactive sensors do not send anything, matching their status.
                if (!sensor.Active) continue;

                switch (sensor.Category)
                {
                    // Only the first environmental sensor is a moisture
                    // probe; every other one is a temperature sensor, so the
                    // dashboard can show one gauge per temperature ESP.
                    // "==" compares the counter to 0 before "++" adds one to
                    // it (Nakov and Kolev et al., 2013). Random.Shared gives
                    // a random starting value (Microsoft, 2026i).
                    case "Environmental":
                        if (environmentalCount++ == 0)
                            devices.Add(new SimDevice(sensor.DeviceIdentifier, DeviceKind.Moisture, 55));
                        else
                            devices.Add(new SimDevice(sensor.DeviceIdentifier, DeviceKind.Temperature, 20 + Random.Shared.Next(0, 8)));
                        break;

                    case "PowerConsumption":
                        devices.Add(new SimDevice(sensor.DeviceIdentifier, DeviceKind.Power, Random.Shared.Next(200, 700)));
                        break;

                    case "Actuator":
                        devices.Add(new SimDevice(sensor.DeviceIdentifier, DeviceKind.Actuator, 0));
                        break;

                    default:
                        Logger.Log("Skipping " + sensor.DeviceIdentifier + " (category " + sensor.Category + ")");
                        break;
                }
            }
            return devices;
        }
    }
}

// References:
// Microsoft, 2026i. Random.Shared Property (System). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.random.shared> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.