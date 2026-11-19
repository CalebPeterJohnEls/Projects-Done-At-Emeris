using System.Globalization;
using System.Security.Claims;
using FunInActionApi.Data;
using FunInActionApi.Dtos;
using FunInActionApi.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FunInActionApi.Controllers;

[ApiController]
[Route("api/payments")]
[Authorize]
public class PaymentsController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly IPaymentGateway _gateway;

    public PaymentsController(AppDbContext db, IPaymentGateway gateway)
    {
        _db = db;
        _gateway = gateway;
    }

    private bool IsCoach => User.IsInRole("Coach");
    private string? MyClientId => User.FindFirstValue("clientId");

    private static PaymentDto ToDto(Payment p) => new()
    {
        Id = p.Id, ClientId = p.ClientId ?? "", Date = p.Date, Description = p.Description,
        Amount = p.Amount, Method = p.Method, Status = p.Status
    };

    // GET /api/payments?clientId=CL-0001
    // Coach: everything (optionally filtered). Client/Player: ONLY their own, whatever clientId they pass.
    [HttpGet]
    public async Task<ActionResult<List<PaymentDto>>> Get([FromQuery] string? clientId, CancellationToken ct)
    {
        var q = _db.Payments.AsNoTracking().AsQueryable();

        if (!IsCoach) q = q.Where(p => p.ClientId == MyClientId);
        else if (!string.IsNullOrWhiteSpace(clientId)) q = q.Where(p => p.ClientId == clientId);

        var rows = await q.ToListAsync(ct);
        return rows.OrderByDescending(p => p.Date).Select(ToDto).ToList();   // ordered in memory (SQLite-friendly)
    }

    // POST /api/payments/card  (dummy Stripe test payment)
    [HttpPost("card")]
    public async Task<ActionResult<PaymentDto>> PayByCard(CardPaymentRequest req, CancellationToken ct)
    {
        // NOTE: the amount comes from the browser here. Fine for a dummy flow; before real money,
        // work the amount out on the server from the programme fee instead.
        if (!TryParseRands(req.ItemAmount, out var amount) || amount <= 0 || amount > 50_000)
            return BadRequest(new { error = "Invalid payment amount." });

        // Card number / expiry / CVV in the request are ignored on purpose (see StripePaymentGateway).
        var result = await _gateway.ChargeAsync(
            new ChargeRequest(amount, string.IsNullOrWhiteSpace(req.ItemLabel) ? "Fun in Action Programme" : req.ItemLabel,
                              User.FindFirstValue("email")), ct);

        var payment = new Payment
        {
            Id = await IdGenerator.NextAsync(_db.Payments.Select(p => p.Id), "PMT", ct),
            ClientId = MyClientId,
            Date = DateTime.UtcNow,
            Description = req.ItemLabel,
            Amount = amount,
            Method = result.MethodLabel,
            Status = result.Succeeded ? PaymentStatus.Confirmed : PaymentStatus.Failed,
            ProviderRef = result.Reference
        };
        _db.Payments.Add(payment);
        await _db.SaveChangesAsync(ct);

        // 200 even when the charge failed: the Status field tells the MVC app what happened.
        return Ok(ToDto(payment));
    }

    // POST /api/payments/proof  (EFT + proof of payment, starts as Pending)
    [HttpPost("proof")]
    public async Task<ActionResult<PaymentDto>> Proof(ProofRequest req, CancellationToken ct)
    {
        var clientId = IsCoach ? req.ClientId : MyClientId;   // a client can only upload for themselves

        var payment = new Payment
        {
            Id = await IdGenerator.NextAsync(_db.Payments.Select(p => p.Id), "PMT", ct),
            ClientId = clientId,
            Date = DateTime.UtcNow,
            Description = $"Proof of payment uploaded ({req.FileName})",
            Amount = 0m,
            Method = "EFT + Proof of Payment",
            Status = PaymentStatus.Pending
        };
        _db.Payments.Add(payment);
        await _db.SaveChangesAsync(ct);
        return Ok(ToDto(payment));
    }

    // PUT /api/payments/{id}/confirm  (coach approves an EFT proof)
    [HttpPut("{id}/confirm")]
    [Authorize(Roles = "Coach")]
    public async Task<IActionResult> Confirm(string id, [FromQuery] decimal? amount, CancellationToken ct)
    {
        var p = await _db.Payments.FirstOrDefaultAsync(x => x.Id == id, ct);
        if (p is null) return NotFound();
        p.Status = PaymentStatus.Confirmed;
        if (amount is > 0) p.Amount = amount.Value;
        await _db.SaveChangesAsync(ct);
        return NoContent();
    }

    // "R1,000" or "R1 000" -> 1000. Keeps only digits and the decimal point.
    private static bool TryParseRands(string? text, out decimal value)
    {
        var cleaned = new string((text ?? "").Where(ch => char.IsDigit(ch) || ch == '.').ToArray());
        return decimal.TryParse(cleaned, NumberStyles.Number, CultureInfo.InvariantCulture, out value);
    }
}

// References
// Stripe (n.d.) Accept a payment. Available at: <https://docs.stripe.com/payments/accept-a-payment> (Accessed: 28 September 2026).
