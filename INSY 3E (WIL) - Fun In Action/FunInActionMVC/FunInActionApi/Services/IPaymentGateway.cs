namespace FunInActionApi.Services;

public record ChargeRequest(decimal Amount, string Description, string? ReceiptEmail);
public record ChargeResult(bool Succeeded, string MethodLabel, string? Reference, string? Error);

/// <summary>
/// The one seam between our app and a payment provider. Controllers only know this interface,
/// so swapping Stripe test mode for Peach Payments / PayGate later means writing one new class.
/// </summary>
public interface IPaymentGateway
{
    Task<ChargeResult> ChargeAsync(ChargeRequest request, CancellationToken ct = default);
}

// References
// Microsoft (n.d.) Dependency injection in ASP.NET Core. Available at: https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection (Accessed: 28 September 2026).
