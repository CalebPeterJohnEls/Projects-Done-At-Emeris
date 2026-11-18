// TelemetryPacket<T>: A generic model used for every telemetry reading in the
// system. Instead of writing three near-identical classes (TemperaturePacket,
// PowerPacket, ActuatorPacket), a single generic type parameter T lets the
// same class hold a float, an int or a bool value while keeping full
// compile-time type safety (Microsoft, 2026j; Troelsen and Japikse, 2022). See
// TelemetryController and TelemetryService for how T is substituted with a
// concrete type at each call site.

namespace Smart_X_API.Models
{
    public class TelemetryPacket<T>
    {
        public string DeviceIdentifier { get; set; } = string.Empty;

        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        public string MetricName { get; set; } = string.Empty;

        // The generic payload: float for temperature, int for power,
        // bool for actuator state (Microsoft, 2026j).
        public T Value { get; set; } = default!;

        public string Unit { get; set; } = string.Empty;

        public TelemetryPacket()
        {
        }

        public TelemetryPacket(
            string deviceIdentifier,
            string metricName,
            T value,
            string unit)
        {
            DeviceIdentifier = deviceIdentifier;
            MetricName = metricName;
            Value = value;
            Unit = unit;
            Timestamp = DateTime.UtcNow;
        }
    }
}

// References:
// Microsoft, 2026j. Generic types and methods. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/fundamentals/types/generics> [Accessed 9 September 2026].
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.
/*IIE Emeris School of Computer Science, 2025a. CLDV6211 -
Playlist. [video online] Available at:
<https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK>
[Accessed 18 March 2025].



IIE Emeris School of Computer Science, 2025b. CLDV6212 -
Playlist. [video online] Available at:
<https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7>
[Accessed 20 September 2025].



Nakov, S. and Kolev, V. et al., 2013. Fundamentals of
Computer Programming with C#. Sofia. [ebook] Available at:
<https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf>
[Accessed 9 September 2026].



The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The
Independent Institute of Education: Unpublished.



Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6:
Foundational Principles and Practices in Programming. 8th ed. New York: Apress.*/