using LiveDataSimulation.Models;

namespace LiveDataSimulation.Services
{
    // DeviceRunner runs the send loop for ONE device. The program starts one
    // loop per device, so they all run side by side as separate tasks
    // (Troelsen and Japikse, 2022; The IIE, 2026).
    public class DeviceRunner
    {
        private readonly ApiClient _api;
        private readonly ValueGenerator _generator;

        // The helper classes are passed in through the constructor
        // (Troelsen and Japikse, 2022).
        public DeviceRunner(ApiClient api, ValueGenerator generator)
        {
            _api = api;
            _generator = generator;
        }

        // Repeats until the user stops the program: online devices send a
        // reading, offline devices save readings in their buffer. Each loop
        // then waits a random 0.8 to 2.5 seconds, so the devices do not all
        // send at the same moment, like real hardware (Nakov and Kolev et al.,
        // 2013; Microsoft, 2026i).
        public async Task RunDeviceAsync(SimDevice device, CancellationToken ct)
        {
            while (!ct.IsCancellationRequested)
            {
                if (device.Online)
                {
                    try
                    {
                        // If the device was offline, first send the readings it
                        // saved.
                        await ReplayBufferedAsync(device);

                        // Relays collect any manual command waiting for them and
                        // obey it by changing their state. The next reading then
                        // reports the new state back to the dashboard.
                        if (device.Kind == DeviceKind.Actuator)
                        {
                            bool? command = await _api.GetPendingCommandAsync(device.Id);
                            if (command != null)
                            {
                                device.Last = command.Value ? 1 : 0;
                                Logger.Log(device.Id.PadRight(14) + " command received: " + (command.Value ? "ON" : "OFF"));
                            }
                        }

                        double value = _generator.NextValue(device);
                        HttpResponseMessage response = await _api.PostPacketAsync(device, value, DateTime.UtcNow);

                        // The API's reply code is shown: 200 means accepted.
                        Logger.Log(device.Id.PadRight(14) + " " + _generator.Describe(device, value).PadLeft(10)
                                   + "  -> " + (int)response.StatusCode);
                    }
                    // If the API is down or slow, the error is logged and the
                    // loop carries on instead of crashing (Nakov and Kolev
                    // et al., 2013).
                    catch (Exception ex) when (ex is HttpRequestException || ex is TaskCanceledException)
                    {
                        if (!ct.IsCancellationRequested)
                            Logger.Log(device.Id.PadRight(14) + " send failed: " + ex.Message);
                    }
                }
                else
                {
                    // Offline: the device keeps measuring and stores each
                    // reading, with its time, in a list (Nakov and Kolev et
                    // al., 2013).
                    BufferedReading reading = new BufferedReading();
                    reading.Timestamp = DateTime.UtcNow;
                    reading.Value = _generator.NextValue(device);
                    device.Buffer.Add(reading);
                }

                await Task.Delay(Random.Shared.Next(800, 2500), ct);
            }
        }

        // Sends the readings saved while the device was offline. The buffer is
        // copied and cleared first, then each reading is sent with its
        // original timestamp, so the history on the dashboard shows when the
        // data was really measured (Nakov and Kolev et al., 2013).
        private async Task ReplayBufferedAsync(SimDevice device)
        {
            if (device.Buffer.Count == 0) return;

            List<BufferedReading> pending = device.Buffer.ToList();
            device.Buffer.Clear();
            Logger.Log(device.Id.PadRight(14) + " reconnected, replaying " + pending.Count + " buffered readings");

            foreach (BufferedReading reading in pending)
                await _api.PostPacketAsync(device, reading.Value, reading.Timestamp);
        }
    }
}

// References:
// Microsoft, 2026i. Random.Shared Property (System). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.random.shared> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.