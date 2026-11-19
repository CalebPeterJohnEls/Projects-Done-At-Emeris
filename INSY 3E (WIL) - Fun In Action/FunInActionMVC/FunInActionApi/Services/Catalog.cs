using FunInActionApi.Dtos;

namespace FunInActionApi.Services;

/// <summary>Static programme info (fee + payment methods). Move to database tables when the coach needs to edit them.</summary>
public static class Catalog
{
    public static ProgrammeFeeDto Fee => new()
    {
        ProgrammeName = "Fun in Action Programme",
        TotalAmount = 1000m,
        RegistrationFee = 250m,
        SessionsSummary = "4 Saturday sessions · 09:00 – 10:00",
        StatusLabel = "Pay in full or in instalments"
    };

    public static List<PaymentMethodDto> Methods => new()
    {
        new() { Name = "Card (Stripe test mode)", Description = "Dummy card payment through Stripe's test environment. No real money moves." },
        new() { Name = "PayShap", Description = "Real-time bank transfer using just a cellphone number. Capitec and FNB are the preferred banks." },
        new() { Name = "Direct EFT + Proof of Payment", Description = "Pay directly into the bank account and upload proof of payment, which reflects on this table." },
    };
}

// References
// Microsoft (n.d.) Static classes and static class members. Available at: https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/classes-and-structs/static-classes-and-static-class-members (Accessed: 28 September 2026).
