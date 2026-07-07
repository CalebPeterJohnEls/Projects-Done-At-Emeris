using System.ComponentModel.DataAnnotations;

namespace apiGLMS.Models
{
    public class Contract
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [Display(Name = "Client Id")]
        public int ClientId { get; set; }

        [Display(Name = "Client")]
        public Client? Client { get; set; }

        [Required]
        [Display(Name = "Start Date")]
        [DataType(DataType.Date)]
        public DateTime StartDate { get; set; }

        [Required]
        [Display(Name = "End Date")]
        [DataType(DataType.Date)]
        public DateTime EndDate { get; set; }

        [Required]
        public string Status { get; set; } = "Draft";

        [Required]
        [Display(Name = "Service Level")]
        public string ServiceLevel { get; set; } = "Bronze";

        [Display(Name = "Signed Agreement")]
        public string? SignedAgreementPath { get; set; }

        // One contract can have many service requests
        public List<ServiceRequest> ServiceRequests { get; set; } = new List<ServiceRequest>();
    }
}