namespace LiveDataSimulation.Models
{
    // The few details the simulator needs about a sensor registered in Part 1.
    // The JSON from the API is converted into objects of this class, and
    // properties not listed here are ignored (Troelsen and Japikse, 2022).
    public class RegisteredSensor
    {
        public string DeviceIdentifier { get; set; } = string.Empty;
        public string Category { get; set; } = string.Empty;
        public bool Active { get; set; }
    }
}

// References:
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.