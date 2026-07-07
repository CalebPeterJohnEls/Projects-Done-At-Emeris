using System.ComponentModel.DataAnnotations;

namespace apiGLMS.Models
{
    public class Client
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public string Name { get; set; }

        [Required]
        [Display(Name = "Contact Details")]
        public string ContactDetails { get; set; }

        [Required]
        public string Region { get; set; }

        // One client can have many contracts
        public List<Contract> Contracts { get; set; } = new List<Contract>();
    }
}