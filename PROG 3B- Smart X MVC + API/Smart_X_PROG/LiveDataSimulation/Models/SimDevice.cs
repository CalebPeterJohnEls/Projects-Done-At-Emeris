namespace LiveDataSimulation.Models
{

    // A reading that a device stored while it was offline. When the device
    // reconnects, these are sent with their original timestamps, like a real
    // device that saves data while it has no connection (The IIE, 2026).
    public class BufferedReading
    {
        public DateTime Timestamp { get; set; }
        public double Value { get; set; }
    }


    // One simulated device (a sensor, smart plug or relay). It only holds data;
    // the logic that uses it is in the Services classes (Troelsen and
    // Japikse, 2022; Nakov and Kolev et al., 2013).
    public class SimDevice
    {
        // A property with only "get" can be set in the constructor but never
        // changed afterwards, so a device keeps the same id and kind
        // (Troelsen and Japikse, 2022).
        public string Id { get; }
        public DeviceKind Kind { get; }

        // The last value the device produced. The next value is based on it,
        // which makes the readings change gradually.
        public double Last { get; set; }
        public bool Online { get; set; } = true;

        // Flags set by the keyboard shortcuts. The next reading of the device
        // becomes a spike, a moisture drop or a heat spike, and the flag is
        // then cleared.
        public bool SpikeNext { get; set; }
        public bool DropNext { get; set; }
        public bool HeatNext { get; set; }

        // A List holds the readings collected while offline
        // (Nakov and Kolev et al., 2013).
        public List<BufferedReading> Buffer { get; } = new List<BufferedReading>();

        // The constructor sets up a new device with its id, kind and start value.
        public SimDevice(string id, DeviceKind kind, double startValue)
        {
            Id = id;
            Kind = kind;
            Last = startValue;
        }
    }
}

// References:
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.