//  Client-side model bound to the sensor CRUD views
// (Create/Edit/Details/ Delete/Index). Category is kept as a plain string here
// (rather than the SensorCategory enum used server-side) so it binds cleanly
// from the <select> element in Create.cshtml/Edit.cshtml.

namespace Smart_X_Client.Models
{
    public class Sensor
    {
        public int Id { get; set; }

        public string MacAddress { get; set; } = string.Empty;

        public string DeviceIdentifier { get; set; } = string.Empty;

        public string Room { get; set; } = string.Empty;

        public string Zone { get; set; } = string.Empty;

        public string NodeId { get; set; } = string.Empty;

        public string Category { get; set; } = string.Empty;

        public bool Active { get; set; }

        public List<string> AttachmentPaths { get; set; } = new();
    }
}

// References:
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