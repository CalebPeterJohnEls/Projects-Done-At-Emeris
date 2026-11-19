using System.ComponentModel.DataAnnotations;
using FunInActionApi.Data;

namespace FunInActionApi.Dtos;

// ---------------------------------------------------------------------------
// Request/response shapes. Property names match our MVC project's models so
// its HttpClient can deserialise them with no changes (JSON is camelCase).
// ---------------------------------------------------------------------------

// ----- Auth -----
public class RegisterRequest
{
    [Required, StringLength(60)] public string FirstName { get; set; } = string.Empty;
    [Required, StringLength(60)] public string LastName { get; set; } = string.Empty;
    [Required, EmailAddress] public string Email { get; set; } = string.Empty;
    [Required, Phone] public string Phone { get; set; } = string.Empty;
    [Required, MinLength(8, ErrorMessage = "Use at least 8 characters.")] public string Password { get; set; } = string.Empty;

    /// <summary>"Client" (parent/guardian) or "Player". Anything else is treated as Client; Coach can never self-register.</summary>
    public string Role { get; set; } = "Client";
}

public class LoginRequest
{
    [Required, EmailAddress] public string Email { get; set; } = string.Empty;
    [Required] public string Password { get; set; } = string.Empty;
}

public class AuthResponse
{
    public string Token { get; set; } = string.Empty;
    public DateTime ExpiresOn { get; set; }
    public string Role { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? ClientId { get; set; }
}

// ----- Clients -----
public class ClientDto
{
    [Required] public string Id { get; set; } = string.Empty;
    [Required] public string FirstName { get; set; } = string.Empty;
    [Required] public string LastName { get; set; } = string.Empty;
    [Required, EmailAddress] public string Email { get; set; } = string.Empty;
    [Required] public string Phone { get; set; } = string.Empty;
    public bool Active { get; set; } = true;
    public DateTime AddedOn { get; set; }
    public string? Type { get; set; }   // extra field the MVC model simply ignores
}

// ----- Profile -----
public class ProfileDto
{
    public string Id { get; set; } = string.Empty;
    [Required] public string FullName { get; set; } = string.Empty;
    [Required, EmailAddress] public string Email { get; set; } = string.Empty;
    [Required] public string Phone { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
}

public class ChangePasswordRequest
{
    [Required] public string CurrentPassword { get; set; } = string.Empty;
    [Required, MinLength(8)] public string NewPassword { get; set; } = string.Empty;
    public string ConfirmPassword { get; set; } = string.Empty;
}

// ----- Payments -----
public class PaymentDto
{
    public string Id { get; set; } = string.Empty;
    public string ClientId { get; set; } = string.Empty;
    public DateTime Date { get; set; }
    public string Description { get; set; } = string.Empty;
    public decimal Amount { get; set; }
    public string Method { get; set; } = string.Empty;
    public PaymentStatus Status { get; set; }
}

/// <summary>Same fields as the MVC CardPaymentViewModel. Card number / CVV are accepted but deliberately IGNORED.</summary>
public class CardPaymentRequest
{
    public string ItemLabel { get; set; } = string.Empty;
    public string ItemAmount { get; set; } = string.Empty;   // e.g. "R1,000"
    public string CardholderName { get; set; } = string.Empty;
    public string CardNumber { get; set; } = string.Empty;   // ignored, never stored or logged
    public string ExpiryDate { get; set; } = string.Empty;   // ignored
    public string Cvv { get; set; } = string.Empty;          // ignored
    public string ReceiptEmail { get; set; } = string.Empty;
}

public class ProofRequest
{
    public string? ClientId { get; set; }   // only used when the coach uploads on someone's behalf
    [Required] public string FileName { get; set; } = string.Empty;
}

public class PaymentMethodDto
{
    public string Name { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
}

public class ProgrammeFeeDto
{
    public string ProgrammeName { get; set; } = string.Empty;
    public decimal TotalAmount { get; set; }
    public decimal RegistrationFee { get; set; }
    public string SessionsSummary { get; set; } = string.Empty;
    public string StatusLabel { get; set; } = string.Empty;
}

// ----- Contracts -----
public class ContractDto
{
    public string Id { get; set; } = string.Empty;
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Icon { get; set; } = string.Empty;
    public ContractStatus Status { get; set; }
    public string? DocumentUrl { get; set; }
}

// ----- Dashboards -----
public class SessionInfoDto
{
    public string Date { get; set; } = string.Empty;
    public string Time { get; set; } = string.Empty;
    public string Group { get; set; } = string.Empty;
    public string Focus { get; set; } = string.Empty;
    public string Status { get; set; } = "Pending";
}

public class CoachDashboardDto
{
    public int ActivePlayers { get; set; }
    public int SessionsThisTerm { get; set; }
    public int PaymentsPending { get; set; }
    public List<SessionInfoDto> UpcomingSessions { get; set; } = new();
}

public class TrainingFeedbackDto
{
    public string Title { get; set; } = string.Empty;
    public string Date { get; set; } = string.Empty;
    public string RatingLabel { get; set; } = string.Empty;
    public string Comment { get; set; } = string.Empty;
}

public class ParentDashboardDto
{
    public string ChildName { get; set; } = string.Empty;
    public string CurrentLevel { get; set; } = string.Empty;
    public string SessionsAttended { get; set; } = string.Empty;
    public string NextSession { get; set; } = string.Empty;
    public string PaymentStatus { get; set; } = string.Empty;
    public List<TrainingFeedbackDto> LatestFeedback { get; set; } = new();
}

// References
// Microsoft (n.d.) Model validation in ASP.NET Core MVC. Available at: https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation (Accessed: 28 September 2026).