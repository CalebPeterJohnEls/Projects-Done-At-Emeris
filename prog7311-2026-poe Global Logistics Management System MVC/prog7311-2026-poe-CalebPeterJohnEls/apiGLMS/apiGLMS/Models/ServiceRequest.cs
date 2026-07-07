using System.ComponentModel.DataAnnotations;

namespace apiGLMS.Models
{
    public class ServiceRequest
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public int ContractId { get; set; }

        public Contract? Contract { get; set; }

        [Required]
        public string Description { get; set; }

        [Required]
        [Range(0.01, 100000)]
        [Display(Name = "Cost (USD)")]
        public decimal CostUSD { get; set; }

        [Display(Name = "Cost (ZAR)")]
        public decimal CostZAR { get; set; }

        [Required]
        public string Status { get; set; } = "Draft";
    }
}