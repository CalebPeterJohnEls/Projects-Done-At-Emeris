using System.ComponentModel.DataAnnotations;

namespace PROG2b_POE_MVC.Models
{
    public class User
    {

        [Key]
        [Required]
        [StringLength(50)]
        public string Username { get; set; }
        [Display(Name = "Name")]
        [Required(ErrorMessage = "Please Enter Name")]
        public string Name { get; set; }
        [Display(Name = "Surname")]
        [Required(ErrorMessage = "Please Enter Surname")]
        public string Surname { get; set; }
        [Required]
        [StringLength(100)]
        public string Email { get; set; }

        [Required]
        public string Password { get; set; }

        [Required]
        [StringLength(50)]
        public string Role { get; set; }
        [Required]
        public int HourlyRate { get; set; }
        public DateTime DateCreated { get; set; } = DateTime.UtcNow;
    }
}
//@*(Troelsen, and Japikse, 2022)
/*References
 * Israel Quiroz, 2025. How to EASILY Create a PDF in C# .NET with QuestPDF! [video online] Available at: https://www.youtube.com/watch?v=3qEPv-67iRg [Accessed 20 November 2025].
 * Nick Chapsas, 2023. The Easiest Way to Create PDFs in .NET. [video online] Available at: https://www.youtube.com/watch?v=_M0IgtGWnvE&t=151s [Accessed 20 November 2025].
 *Programming with Felipe Gavilan, 2023. Generating PDFs with C# - Very Easy (two examples). [video online] Available at: https://www.youtube.com/watch?v=bhR4Cmg16gs [Accessed 20 November 2025].
 *Microsoft, 2025. IDocument Interface (org.w3c.dom). [online] Available at: https://learn.microsoft.com/en-us/dotnet/api/org.w3c.dom.idocument?view=net-android-35.0 [Accessed 20 November 2025].
 *QuestPDF, 2025a. API Reference. [online]Available at: https://www.questpdf.com/api-reference [Accessed 20 November 2025].
 *QuestPDF, 2025b. Document Operations. [online] Available at: https://www.questpdf.com/concepts/document-operations.html [Accessed 20 November 2025].
 *QuestPDF, 2025c. Generating Output. [online] Available at: https://www.questpdf.com/concepts/generating-output.html [Accessed 20 November 2025].
 *Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.*/
