using LiveDataSimulation.Models;

namespace LiveDataSimulation.Services
{
    // ValueGenerator creates believable readings for each type of device. A
    // switch chooses the rule that matches the device's kind
    // (Nakov and Kolev et al., 2013; The IIE, 2026).
    public class ValueGenerator
    {
        // Returns the next reading for a device. A flag set by a keyboard
        // shortcut is checked first, and when it is set the device produces
        // the special value once, then the flag is cleared.
        public double NextValue(SimDevice device)
        {
            // S key: a severe power spike (above the 3000 W alert limit).
            if (device.SpikeNext && device.Kind == DeviceKind.Power)
            {
                device.SpikeNext = false;
                device.Last = 3800;
                return device.Last;
            }

            // M key: a critical moisture drop (below the 20% alert limit).
            if (device.DropNext && device.Kind == DeviceKind.Moisture)
            {
                device.DropNext = false;
                device.Last = 12;
                return device.Last;
            }

            // T key: a heat spike (above the 35 degree alert limit).
            if (device.HeatNext && device.Kind == DeviceKind.Temperature)
            {
                device.HeatNext = false;
                device.Last = 38;
                return device.Last;
            }

            switch (device.Kind)
            {
                // Temperature drifts up or down by a small random amount.
                // Math.Clamp keeps the value inside 15 to 40 (Microsoft,
                // 2026f). After a heat spike it cools down by one degree per
                // reading, so the alert clears naturally.
                case DeviceKind.Temperature:
                    if (device.Last > 33)
                        device.Last -= 1.0;
                    else
                        device.Last = Math.Clamp(device.Last + (Random.Shared.NextDouble() - 0.5), 15, 40);
                    break;

                // Power changes by a random whole number of watts, kept
                // between 50 and 1200. Random.Shared is safe to use from many
                // device loops at once (Microsoft, 2026f; Microsoft, 2026i).
                case DeviceKind.Power:
                    device.Last = Math.Clamp(device.Last + Random.Shared.Next(-40, 41), 50, 1200);
                    break;

                // Moisture slowly dries out, and is "watered" back to 60%
                // when it gets too low (Microsoft, 2026f; Microsoft, 2026i).
                case DeviceKind.Moisture:
                    device.Last = Math.Clamp(device.Last - Random.Shared.NextDouble() * 0.6, 5, 80);
                    if (device.Last <= 8)
                        device.Last = 60;
                    break;

                // Actuators keep their value. It only changes when a command
                // arrives from the API.
                default:
                    break;
            }
            return device.Last;
        }

        // Turns a reading into text for the console, with the right unit.
        public string Describe(SimDevice device, double value)
        {
            switch (device.Kind)
            {
                case DeviceKind.Temperature: return value.ToString("F1") + " °C";
                case DeviceKind.Power: return ((int)value) + " W";
                case DeviceKind.Moisture: return value.ToString("F1") + " %";
                default: return value > 0.5 ? "ON" : "OFF";
            }
        }
    }
}

// References:
// Microsoft, 2026f. Math.Clamp Method (System). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.math.clamp> [Accessed 17 October 2026].
// Microsoft, 2026i. Random.Shared Property (System). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.random.shared> [Accessed 17 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.