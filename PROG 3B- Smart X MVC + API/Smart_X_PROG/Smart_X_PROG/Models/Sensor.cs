// Sensor: Plain data model representing a registered IoT sensor/device. Stored
// and managed in-memory by SensorService (The IIE, 2026).

namespace Smart_X_API.Models
{
    public class Sensor
    {
        public int Id { get; set; }
        public string DeviceIdentifier { get; set; } = string.Empty;
        public string MacAddress { get; set; } = string.Empty;
        public string Room { get; set; } = string.Empty;
        public string Zone { get; set; } = string.Empty;
        public string NodeId { get; set; } = string.Empty;
        public SensorCategory Category { get; set; }
        public bool Active { get; set; } = true;
        public DateTime RegisteredAt { get; set; } = DateTime.UtcNow;

        // Relative paths (under wwwroot/uploads) to any files uploaded for
        // this sensor via FileController/FileService (Microsoft, 2026z).
        public List<string> AttachmentPaths { get; set; } = new();
    }
}

// References:
// Microsoft, 2026z. Upload files in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
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