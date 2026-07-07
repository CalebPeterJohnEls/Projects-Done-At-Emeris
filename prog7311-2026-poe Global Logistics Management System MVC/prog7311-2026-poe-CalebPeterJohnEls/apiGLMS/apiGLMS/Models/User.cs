
using System.ComponentModel.DataAnnotations;
 
namespace apiGLMS.Models
{
    public class User
    {
        // Auto-increment int PK so AuthController can call user.Id
        [Key]
        public int Id { get; set; }
 
        [Required]
        [StringLength(50)]
        public string Username { get; set; } = string.Empty;
 
        // Required by AuthController Register and MVC Register model
        [Required]
        public string Name { get; set; } = string.Empty;
 
        [Required]
        public string Surname { get; set; } = string.Empty;
 
        [Required]
        [StringLength(100)]
        public string Email { get; set; } = string.Empty;
 
        [Required]
        public string Password { get; set; } = string.Empty;
 
        [Required]
        public string Role { get; set; } = "User";
 
        public DateTime DateCreated { get; set; } = DateTime.UtcNow;
    }
}
//@*(Troelsen, and Japikse, 2022; IIE Emeris School of Computer Science, 2025a; The IIE. 2026.)
/*References
 * IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online]
 *   Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK>
 *   [Accessed 18 March 2025].
 * The IIE. 2026. PROGRAMMING 3A [PROG7311 Module Manual].
 *   The Independent Institute of Education: Unpublished.
 * Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6. 8th ed. New York: Apress.
 * Microsoft, 2026b. RangeAttribute Class. [online]
 *   Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.componentmodel.dataannotations.rangeattribute?view=net-9.0>
 *   [Accessed 22 April 2026].
 */