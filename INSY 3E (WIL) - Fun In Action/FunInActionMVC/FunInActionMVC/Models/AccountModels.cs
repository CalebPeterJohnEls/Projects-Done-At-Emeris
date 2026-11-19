using System.ComponentModel.DataAnnotations;

namespace FunInAction.Mvc.Models;

public class LoginViewModel
{
    [Required(ErrorMessage = "Enter your email.")]
    [EmailAddress(ErrorMessage = "Enter a valid email.")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your password.")]
    [DataType(DataType.Password)]
    public string Password { get; set; } = string.Empty;
}

public class RegisterViewModel
{
    [Required(ErrorMessage = "Enter your first name.")]
    [Display(Name = "First name")]
    public string FirstName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your last name.")]
    [Display(Name = "Last name")]
    public string LastName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your email.")]
    [EmailAddress(ErrorMessage = "Enter a valid email.")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your phone number.")]
    [Phone(ErrorMessage = "Enter a valid phone number.")]
    public string Phone { get; set; } = string.Empty;

    // The API only accepts Client or Player; anything else becomes Client.
    [Required]
    public string Role { get; set; } = "Client";

    [Required(ErrorMessage = "Enter a password.")]
    [MinLength(8, ErrorMessage = "Use at least 8 characters.")]
    [DataType(DataType.Password)]
    public string Password { get; set; } = string.Empty;

    [Required(ErrorMessage = "Re-enter your password.")]
    [Compare(nameof(Password), ErrorMessage = "Passwords do not match.")]
    [DataType(DataType.Password)]
    [Display(Name = "Confirm password")]
    public string ConfirmPassword { get; set; } = string.Empty;
}

// Mirrors the JSON returned by /api/auth/login and /api/auth/register.
// CHANGED: added Email and ClientId — the API's AuthResponse already sends both, but nothing was
// reading them, so PortalController had no way to know which client the signed-in user actually is.
public class AuthResult
{
    public string Token { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? ClientId { get; set; }
}
