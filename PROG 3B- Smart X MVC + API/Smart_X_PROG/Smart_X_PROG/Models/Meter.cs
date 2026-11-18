// Meter: Represents a single power/energy meter reading. This class
// demonstrates C# operator overloading: '+', '-', '>', '<', '>=' and '<=' are
// all redefined so that two Meter objects can be compared or combined using
// normal arithmetic/comparison syntax instead of named methods like Add() or
// IsGreaterThan() (Microsoft, 2026q; Nakov and Kolev, 2013).

namespace Smart_X_API.Models
{
    public class Meter
    {
        public string DeviceIdentifier { get; set; } = string.Empty;

        public double Value { get; set; }

        public string Unit { get; set; } = "W";

        public Meter()
        {
        }

        public Meter(string deviceIdentifier, double value)
        {
            DeviceIdentifier = deviceIdentifier;
            Value = value;
        }

        // Overloading '+' lets two meters be combined directly, e.g.
        // "var total = meterA + meterB;" (Microsoft, 2026q).
        public static Meter operator +(Meter first, Meter second)
        {
            return new Meter(
                $"{first.DeviceIdentifier}+{second.DeviceIdentifier}",
                first.Value + second.Value
            );
        }

        // Overloading '-' mirrors '+' for subtracting one reading from
        // another (Microsoft, 2026q).
        public static Meter operator -(Meter first, Meter second)
        {
            return new Meter(
                $"{first.DeviceIdentifier}-{second.DeviceIdentifier}",
                first.Value - second.Value
            );
        }

        // Comparison operators are overloaded in pairs (> / <, >= / <=) so
        // that meters can be ordered purely by their Value
        // (Microsoft, 2026q).
        public static bool operator >(Meter first, Meter second)
        {
            return first.Value > second.Value;
        }

        public static bool operator <(Meter first, Meter second)
        {
            return first.Value < second.Value;
        }

        public static bool operator >=(Meter first, Meter second)
        {
            return first.Value >= second.Value;
        }

        public static bool operator <=(Meter first, Meter second)
        {
            return first.Value <= second.Value;
        }

        // Overriding ToString() gives a readable "value unit" string
        // (e.g. "42.5 W") wherever a Meter is written out as text.
        public override string ToString()
        {
            return $"{Value} {Unit}";
        }
    }
}

// References:
// Microsoft, 2026q. Operator overloading. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/operator-overloading> [Accessed 8 September 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
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