using Smart_X_API.Models;

// TelemetryService: Stores incoming telemetry readings in-memory, separated
// into three lists keyed by metric type. Uses the same generic
// TelemetryPacket<T> model as the rest of the system (Microsoft, 2026j).

namespace Smart_X_API.Services
{
    public class TelemetryService
    {
        private readonly List<TelemetryPacket<float>> _temperaturePackets = new();

        private readonly List<TelemetryPacket<int>> _powerPackets = new();

        private readonly List<TelemetryPacket<bool>> _actuatorPackets = new();

        public TelemetryService()
        {
            SeedData();
        }

        public void AddTemperature(TelemetryPacket<float> packet)
        {
            _temperaturePackets.Add(packet);
        }

        public void AddPower(TelemetryPacket<int> packet)
        {
            _powerPackets.Add(packet);
        }

        public void AddActuator(TelemetryPacket<bool> packet)
        {
            _actuatorPackets.Add(packet);
        }

        public List<TelemetryPacket<float>> GetTemperature()
        {
            return _temperaturePackets;
        }

        public List<TelemetryPacket<int>> GetPower()
        {
            return _powerPackets;
        }

        public List<TelemetryPacket<bool>> GetActuator()
        {
            return _actuatorPackets;
        }

        public int GetTotalTelemetryCount()
        {
            return _temperaturePackets.Count
                 + _powerPackets.Count
                 + _actuatorPackets.Count;
        }

        // Seeds 12 rounds of sample readings per metric on startup, using a
        // fixed seed (7) so the demo data is the same on every run
        // (Microsoft, 2026u).
        private void SeedData()
        {
            var random = new Random(7);

            for (int i = 1; i <= 12; i++)
            {
                AddTemperature(new TelemetryPacket<float>(
                    $"ESP32-{i:0000}",
                    "Temperature",
                    (float)(20 + random.NextDouble() * 70),
                    "°C"));

                AddPower(new TelemetryPacket<int>(
                    $"ESP32-{i:0000}",
                    "Power",
                    random.Next(50, 500),
                    "W"));

                AddActuator(new TelemetryPacket<bool>(
                    $"ESP32-{i:0000}",
                    "ValveState",
                    random.NextDouble() > 0.5,
                    "bool"));
            }
        }
    }
}

// References:
// Microsoft, 2026j. Generic types and methods. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/fundamentals/types/generics> [Accessed 9 September 2026].
// Microsoft, 2026u. Random Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.random> [Accessed 8 September 2026].
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