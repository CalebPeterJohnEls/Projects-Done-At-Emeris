using Microsoft.Extensions.Options;
using Stripe;

namespace FunInActionApi.Services;

public class StripeOptions
{
    public string SecretKey { get; set; } = string.Empty;   // sk_test_... from user-secrets
    public string Currency { get; set; } = "zar";           // if Stripe rejects it for your account, try "usd"
}

/// <summary>
/// DUMMY payments. Two modes:
///  1. SecretKey is a Stripe TEST key (sk_test_...): creates and confirms a real PaymentIntent in Stripe's
///     test environment using Stripe's built-in test card token "pm_card_visa". It shows in your Stripe
///     test dashboard, but no real money moves and no real card number ever touches our server.
///  2. SecretKey empty: pure simulator, returns a fake success so the app works with no Stripe account.
/// A live key (sk_live_...) is refused on purpose.
/// </summary>
public class StripePaymentGateway : IPaymentGateway
{
    private readonly StripeOptions _opt;
    private readonly ILogger<StripePaymentGateway> _log;

    public StripePaymentGateway(IOptions<StripeOptions> opt, ILogger<StripePaymentGateway> log)
    {
        _opt = opt.Value;
        _log = log;
    }

    public async Task<ChargeResult> ChargeAsync(ChargeRequest req, CancellationToken ct = default)
    {
        // Mode 2: simulator
        if (string.IsNullOrWhiteSpace(_opt.SecretKey))
            return new ChargeResult(true, "Card (simulated)", $"sim_{Guid.NewGuid():N}", null);

        // Safety: this build is for test payments only.
        if (!_opt.SecretKey.StartsWith("sk_test_"))
            return new ChargeResult(false, "Card (Stripe)", null, "Only Stripe test keys (sk_test_...) are allowed right now.");

        try
        {
            var service = new PaymentIntentService(new StripeClient(_opt.SecretKey));
            var intent = await service.CreateAsync(new PaymentIntentCreateOptions
            {
                Amount = (long)Math.Round(req.Amount * 100m),   // Stripe wants the smallest unit (cents)
                Currency = _opt.Currency,
                Description = req.Description,
                PaymentMethod = "pm_card_visa",                 // Stripe's test Visa token
                Confirm = true,
                AutomaticPaymentMethods = new PaymentIntentAutomaticPaymentMethodsOptions
                {
                    Enabled = true,
                    AllowRedirects = "never"                    // no 3-D Secure redirect in this dummy flow
                }
            }, cancellationToken: ct);

            var ok = intent.Status == "succeeded";
            return new ChargeResult(ok, "Card (Stripe test)", intent.Id, ok ? null : $"Stripe status: {intent.Status}");
        }
        catch (StripeException ex)
        {
            _log.LogWarning(ex, "Stripe test charge failed.");
            return new ChargeResult(false, "Card (Stripe test)", null, ex.StripeError?.Message ?? ex.Message);
        }
    }
}

// References
// Stripe (n.d.) PaymentIntents API. Available at: https://docs.stripe.com/api/payment_intents (Accessed: 28 September 2026).
// Stripe (n.d.) Testing. Available at: https://docs.stripe.com/testing (Accessed: 28 September 2026).
