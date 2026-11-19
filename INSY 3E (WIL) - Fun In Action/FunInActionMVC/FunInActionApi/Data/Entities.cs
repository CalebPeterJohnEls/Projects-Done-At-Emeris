namespace FunInActionApi.Data;

// ---------------------------------------------------------------------------
// Database tables (EF Core entities). Each class = one table.
// Enum order matches the MVC project's enums because JSON enums travel as numbers.
// ---------------------------------------------------------------------------

public enum PaymentStatus { Pending, Confirmed, Failed }
public enum ContractStatus { Pending, Signed }

/// <summary>A login account. Coach accounts are seeded; Client/Player accounts come from /api/auth/register.</summary>
public class AppUser
{
    public Guid Id { get; set; } = Guid.NewGuid();
    public string Email { get; set; } = string.Empty;          // stored lower-case, unique
    public string PasswordHash { get; set; } = string.Empty;   // never the plain password
    public string FullName { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public string Role { get; set; } = "Client";               // Coach | Admin | Player
    public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

    // Link to the row the coach sees in the Clients table (null for the coach).
    public string? ClientId { get; set; }
    public Client? Client { get; set; }
}

/// <summary>A row in the coach's Clients table. Same shape as the MVC Client model, plus Type.</summary>
public class Client
{
    public string Id { get; set; } = string.Empty;             // e.g. CL-0001
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public bool Active { get; set; } = true;
    public DateTime AddedOn { get; set; } = DateTime.UtcNow;
    public string Type { get; set; } = "Client";               // Client (parent/guardian) | Player
}

public class Payment
{
    public string Id { get; set; } = string.Empty;             // e.g. PMT-0001
    public string? ClientId { get; set; }                      // null when the coach pays
    public DateTime Date { get; set; } = DateTime.UtcNow;
    public string Description { get; set; } = string.Empty;
    public decimal Amount { get; set; }
    public string Method { get; set; } = string.Empty;
    public PaymentStatus Status { get; set; } = PaymentStatus.Pending;
    public string? ProviderRef { get; set; }                   // Stripe PaymentIntent id (pi_...) or sim_...
}

public class Contract
{
    public string Id { get; set; } = string.Empty;
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Icon { get; set; } = "fa-file-alt";
    public ContractStatus Status { get; set; } = ContractStatus.Pending;
    public string? DocumentUrl { get; set; }
}

// References
// Microsoft (n.d.) Entity Framework Core documentation. Available at: https://learn.microsoft.com/en-us/ef/core/ (Accessed: 28 September 2026).