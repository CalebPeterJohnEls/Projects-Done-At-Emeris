using LiveDataSimulation.Models;

namespace LiveDataSimulation.Services
{
    // ScenarioController listens for key presses and triggers test scenarios
    // during a demo, such as a power spike or a device going offline. It
    // changes the devices' flags, and the device loops react to them
    // (Troelsen and Japikse, 2022; The IIE, 2026).
    public class ScenarioController
    {
        private readonly List<SimDevice> _devices;
        private readonly ApiClient _api;
        private readonly CancellationTokenSource _cts;

        public ScenarioController(List<SimDevice> devices, ApiClient api, CancellationTokenSource cts)
        {
            _devices = devices;
            _api = api;
            _cts = cts;
        }

        // Checks for a key press ten times a second. Console.KeyAvailable is
        // true when a key is waiting, so the program never stops to wait for
        // the keyboard (Microsoft, 2026b). ReadKey(true) reads the key
        // without showing it on screen, and ToUpperInvariant makes the
        // shortcuts work in lower case too. A switch runs the matching
        // scenario (Nakov and Kolev et al., 2013).
        public async Task RunAsync(CancellationToken ct)
        {
            while (!ct.IsCancellationRequested)
            {
                if (Console.KeyAvailable)
                {
                    char key = char.ToUpperInvariant(Console.ReadKey(true).KeyChar);

                    switch (key)
                    {
                        case 'S':
                            QueueSpike();
                            break;
                        case 'M':
                            QueueMoistureDrop();
                            break;
                        case 'T':
                            QueueHeat();
                            break;
                        case 'D':
                            DropDevice();
                            break;
                        case 'R':
                            RestoreDevices();
                            break;
                        case 'F':
                            await FloodSpikeAsync();
                            break;
                        case 'Q':
                            _cts.Cancel();
                            return;
                    }
                }
                await Task.Delay(100, ct);
            }
        }

        // S: makes the next power reading a spike. FirstOrDefault finds the
        // first online smart plug, or null if there is none (Troelsen and
        // Japikse, 2022). The spike goes into the priority queue in the API.
        private void QueueSpike()
        {
            SimDevice? plug = _devices.FirstOrDefault(x => x.Kind == DeviceKind.Power && x.Online);
            if (plug == null)
            {
                return;
            }

            plug.SpikeNext = true;
            Logger.Log("*** spike queued on " + plug.Id);
        }

        // M: makes the next moisture reading drop below the critical 20%.
        private void QueueMoistureDrop()
        {
            SimDevice? probe = _devices.FirstOrDefault(x => x.Kind == DeviceKind.Moisture && x.Online);
            if (probe == null)
            {
                return;
            }

            probe.DropNext = true;
            Logger.Log("*** moisture drop queued on " + probe.Id);
        }

        // T: makes the next temperature reading a heat spike.
        private void QueueHeat()
        {
            SimDevice? sensor = _devices.FirstOrDefault(x => x.Kind == DeviceKind.Temperature && x.Online);
            if (sensor == null)
            {
                return;
            }

            sensor.HeatNext = true;
            Logger.Log("*** heat spike queued on " + sensor.Id);
        }

        // D: takes one device offline. After 10 seconds without data, the API
        // raises a "Disconnected" alert once (one entry in its HashSet).
        private void DropDevice()
        {
            SimDevice? victim = _devices.FirstOrDefault(x => x.Online && x.Kind != DeviceKind.Actuator);
            if (victim == null)
            {
                return;
            }

            victim.Online = false;
            Logger.Log("*** " + victim.Id + " went OFFLINE");
        }

        // R: brings every offline device back. They then send the readings
        // they saved while they were offline.
        private void RestoreDevices()
        {
            foreach (SimDevice device in _devices.Where(x => !x.Online))
            {
                device.Online = true;
            }
            Logger.Log("*** offline devices restored");
        }

        // F: sends ten identical spike readings in a row to test that the
        // API only raises one alert and counts the rest as blocked duplicates.
        private async Task FloodSpikeAsync()
        {
            SimDevice? target = _devices.FirstOrDefault(x => x.Kind == DeviceKind.Power);
            if (target == null)
            {
                return;
            }

            for (int i = 0; i < 10; i++)
            {
                await _api.PostPacketAsync(target, 3900, DateTime.UtcNow);
            }

            Logger.Log("*** flooded " + target.Id + " with 10 identical spike readings");
        }
    }
}

// References:
// Microsoft, 2026b. Console.KeyAvailable Property (System). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.console.keyavailable> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.