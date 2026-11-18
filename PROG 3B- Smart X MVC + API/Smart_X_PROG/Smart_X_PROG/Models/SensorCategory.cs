// SensorCategory: Enum describing the kind of telemetry a sensor produces.
// Serialised as a string (rather than its underlying int) in JSON responses
// because of the JsonStringEnumConverter registered in Program.cs (Microsoft,
// 2026o).

namespace Smart_X_API.Models
{
    public enum SensorCategory
    {
        Environmental,
        PowerConsumption,
        Actuator
    }
}

// References:
// Microsoft, 2026o. JsonStringEnumConverter Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.text.json.serialization.jsonstringenumconverter> [Accessed 9 September 2026].
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