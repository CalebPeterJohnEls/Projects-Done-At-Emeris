namespace FunInAction.Mvc.Models;

/// <summary>
/// A single line in a client's payment history, as served by GET /api/payments?clientId=...
/// </summary>
public class PaymentRecord
{
    public string Id { get; set; } = string.Empty;
    public string ClientId { get; set; } = string.Empty;
    public DateTime Date { get; set; }
    public string Description { get; set; } = string.Empty;
    public decimal Amount { get; set; }
    public string Method { get; set; } = string.Empty;
    public PaymentStatus Status { get; set; } = PaymentStatus.Pending;
}

public enum PaymentStatus
{
    Pending,
    Confirmed,
    Failed
}

/// <summary>
/// A payment method the programme accepts (Peach Payments, PayGate, PayShap, EFT, etc.)
/// as served by GET /api/payment-methods.
/// </summary>
public class PaymentMethodInfo
{
    public string Name { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
}

/// <summary>
/// The programme fee summary shown at the top of the Payments pages.
/// </summary>
public class ProgrammeFeeSummary
{
    public string ProgrammeName { get; set; } = "Fun in Action Programme";
    public decimal TotalAmount { get; set; }
    public decimal RegistrationFee { get; set; }
    public string SessionsSummary { get; set; } = string.Empty;
    public string StatusLabel { get; set; } = string.Empty;
}

/// <summary>
/// Card details submitted from the placeholder "pay by card" overlay.
/// No real card data should ever reach a production controller un-encrypted;
/// this only exists so the form has somewhere to post to until a real
/// payment gateway (Peach Payments / PayGate) is wired up via the API.
/// </summary>
public class CardPaymentViewModel
{
    public string ItemLabel { get; set; } = string.Empty;
    public string ItemAmount { get; set; } = string.Empty;
    public string CardholderName { get; set; } = string.Empty;
    public string CardNumber { get; set; } = string.Empty;
    public string ExpiryDate { get; set; } = string.Empty;
    public string Cvv { get; set; } = string.Empty;
    public string ReceiptEmail { get; set; } = string.Empty;
}
