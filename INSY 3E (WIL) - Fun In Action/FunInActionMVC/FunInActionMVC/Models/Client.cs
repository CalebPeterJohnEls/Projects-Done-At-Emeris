using System.ComponentModel.DataAnnotations;

namespace FunInAction.Mvc.Models;

/// <summary>
/// Represents a client (parent/guardian) record as returned by the Fun in Action API.
/// This is the shape the API is expected to serve at GET/POST /api/clients.
/// </summary>
public class Client
{
    [Required(ErrorMessage = "Enter a client ID.")]
    [Display(Name = "Client ID")]
    public string Id { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter a first name.")]
    [Display(Name = "First name")]
    public string FirstName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter a surname.")]
    [Display(Name = "Surname")]
    public string LastName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter a valid email.")]
    [EmailAddress(ErrorMessage = "Enter a valid email.")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter a phone number.")]
    [Phone(ErrorMessage = "Enter a valid phone number.")]
    public string Phone { get; set; } = string.Empty;

    public bool Active { get; set; } = true;

    [Display(Name = "Added on")]
    public DateTime AddedOn { get; set; } = DateTime.UtcNow;
}
