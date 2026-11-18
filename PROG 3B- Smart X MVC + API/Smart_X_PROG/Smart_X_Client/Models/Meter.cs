// Lightweight client-side DTO mirroring the API's Meter model,
// used only to send/receive JSON to/from the aggregate-meters endpoint.
// Operator overloading is not needed here because the arithmetic itself
// happens on the API side (see Smart_X_API Models/Meter.cs and Microsoft,
// 2026q).

namespace Smart_X_Client.Models
{
    public class Meter
    {
        public string DeviceIdentifier { get; set; } = string.Empty;

        public double Value { get; set; }

        public string Unit { get; set; } = "W";
    }
}

// References:
// Microsoft, 2026q. Operator overloading. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/operator-overloading> [Accessed 8 September 2026].
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