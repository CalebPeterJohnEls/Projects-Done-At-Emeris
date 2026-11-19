using System.ComponentModel.DataAnnotations;

namespace FunInAction.Mvc.Models;

/// <summary>
/// The signed-in user's profile, as served by GET /api/profile.
/// </summary>
public class UserProfile
{
    public string Id { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your full name.")]
    [Display(Name = "Full name")]
    public string FullName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your email.")]
    [EmailAddress(ErrorMessage = "Enter a valid email.")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter your phone number.")]
    [Phone(ErrorMessage = "Enter a valid phone number.")]
    public string Phone { get; set; } = string.Empty;

    public string Role { get; set; } = "Coach";
}

public class ChangePasswordViewModel
{
    [Required(ErrorMessage = "Enter your current password.")]
    [DataType(DataType.Password)]
    [Display(Name = "Current password")]
    public string CurrentPassword { get; set; } = string.Empty;

    [Required(ErrorMessage = "Enter a new password.")]
    [MinLength(8, ErrorMessage = "Use at least 8 characters.")]
    [DataType(DataType.Password)]
    [Display(Name = "New password")]
    public string NewPassword { get; set; } = string.Empty;

    [Required(ErrorMessage = "Re-enter your new password.")]
    [DataType(DataType.Password)]
    [Display(Name = "Confirm new password")]
    [Compare(nameof(NewPassword), ErrorMessage = "Passwords do not match.")]
    public string ConfirmPassword { get; set; } = string.Empty;
}

/// <summary>
/// Combines the profile edit form and the change-password form on one page,
/// mirroring the accordion layout of myprofile_page.html.
/// </summary>
public class ProfilePageViewModel
{
    public UserProfile Profile { get; set; } = new();
    public ChangePasswordViewModel ChangePassword { get; set; } = new();
}
