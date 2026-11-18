using Smart_X_API.Models;

// TelemetryHistoryService: Converts raw batch telemetry readings into
// TelemetryPacket<T> objects. ProcessHistory<T> is itself a generic method
// (Microsoft, 2026j) so the same implementation is reused for temperature
// (float), power (int) and actuator (bool) history, driven by a jagged array
// T[][] where each row represents one device's readings (Microsoft, 2026n).
// ProcessFacilityGrid instead works over a true two-dimensional (rectangular)
// array, float[,], showing the difference between jagged and multidimensional
// arrays in C# (Microsoft, 2026p).

namespace Smart_X_API.Services
{
    public class TelemetryHistoryService
    {

        // Generic method: T is decided by the caller (float, int or bool).
        // A jagged array (T[][]) is an "array of arrays" where each row can
        // have a different length, unlike a rectangular array
        // (Microsoft, 2026n; Microsoft, 2026j).
        public List<TelemetryPacket<T>> ProcessHistory<T>(
            T[][] rawTelemetry,
            string deviceIdPrefix,
            string metricName,
            string unit)
        {
            var processed = new List<TelemetryPacket<T>>();

            for (int row = 0; row < rawTelemetry.Length; row++)
            {
                for (int column = 0; column < rawTelemetry[row].Length; column++)
                {
                    processed.Add(new TelemetryPacket<T>(
                        $"{deviceIdPrefix}-{row + 1}",
                        metricName,
                        rawTelemetry[row][column],
                        unit
                    ));
                }
            }

            return processed;
        }

        // Each of these simply calls the shared generic method above with a
        // different type argument and metric metadata.
        public List<TelemetryPacket<float>> ProcessTemperatureHistory(float[][] rawTelemetry) =>
            ProcessHistory(rawTelemetry, "TEMP", "Temperature", "°C");

        public List<TelemetryPacket<int>> ProcessPowerHistory(int[][] rawTelemetry) =>
            ProcessHistory(rawTelemetry, "PWR", "Power", "W");

        public List<TelemetryPacket<bool>> ProcessActuatorHistory(bool[][] rawTelemetry) =>
            ProcessHistory(rawTelemetry, "ACT", "ValveState", "bool");

        // Processes a rectangular (multidimensional) grid, float[,], where
        // every row has the same fixed number of columns. GetLength(0)/(1)
        // return the row/column counts respectively (Microsoft, 2026p).
        public List<TelemetryPacket<float>> ProcessFacilityGrid(float[,] grid, string metricName, string unit)
        {
            var processed = new List<TelemetryPacket<float>>();
            int rows = grid.GetLength(0);
            int cols = grid.GetLength(1);

            for (int r = 0; r < rows; r++)
            {
                for (int c = 0; c < cols; c++)
                {
                    processed.Add(new TelemetryPacket<float>(
                        $"GRID-R{r + 1}C{c + 1}",
                        metricName,
                        grid[r, c],
                        unit
                    ));
                }
            }

            return processed;
        }
    }
}

// References:
// Microsoft, 2026j. Generic types and methods. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/fundamentals/types/generics> [Accessed 9 September 2026].
// Microsoft, 2026n. Jagged Arrays (C# Programming Guide). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/arrays/jagged-arrays> [Accessed 8 September 2026].
// Microsoft, 2026p. Multidimensional Arrays (C# Programming Guide). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/arrays/multidimensional-arrays> [Accessed 10 September 2026].
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