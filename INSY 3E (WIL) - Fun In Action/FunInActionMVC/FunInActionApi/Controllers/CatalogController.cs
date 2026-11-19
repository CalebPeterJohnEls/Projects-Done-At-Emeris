using FunInActionApi.Data;
using FunInActionApi.Dtos;
using FunInActionApi.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace FunInActionApi.Controllers;

/// <summary>Read-only lookups: /api/payment-methods, /api/programme-fee, /api/contracts.</summary>
[ApiController]
[Route("api")]
[Authorize]
public class CatalogController : ControllerBase
{
    private readonly AppDbContext _db;
    public CatalogController(AppDbContext db) => _db = db;

    [HttpGet("payment-methods")]
    public ActionResult<List<PaymentMethodDto>> Methods() => Catalog.Methods;

    [HttpPut("payments/{id}/status")]
    public async Task<IActionResult> UpdatePaymentStatus(string id, UpdatePaymentStatusDto dto, CancellationToken ct)
    {
        // only the coach can change a payment's status
        if (User.FindFirstValue("role") != "Coach")
            return Forbid();

        var payment = await _db.Payments.FirstOrDefaultAsync(p => p.Id == id, ct);
        if (payment is null) return NotFound();

        payment.Status = dto.Status;
        await _db.SaveChangesAsync(ct);
        return NoContent();
    }

    [HttpGet("programme-fee")]
    public ActionResult<ProgrammeFeeDto> Fee() => Catalog.Fee;

    [HttpGet("contracts")]
    public async Task<ActionResult<List<ContractDto>>> Contracts(CancellationToken ct)
    {
        var userId = CurrentUserId();

        var signedIds = await _db.ContractSignatures.AsNoTracking()
            .Where(s => s.UserId == userId)
            .Select(s => s.ContractId)
            .ToListAsync(ct);

        var rows = await _db.Contracts.AsNoTracking().OrderBy(c => c.Id).ToListAsync(ct);

        return rows.Select(c => new ContractDto
        {
            Id = c.Id,
            Title = c.Title,
            Description = c.Description,
            Icon = c.Icon,
            DocumentUrl = c.DocumentUrl,
            Status = signedIds.Contains(c.Id) ? ContractStatus.Signed : ContractStatus.Pending
        }).ToList();
    }

    [HttpPost("contracts/{id}/sign")]
    public async Task<IActionResult> SignContract(string id, CancellationToken ct)
    {
        var userId = CurrentUserId();

        if (!await _db.Contracts.AnyAsync(c => c.Id == id, ct))
            return NotFound();

        var already = await _db.ContractSignatures
            .AnyAsync(s => s.ContractId == id && s.UserId == userId, ct);

        if (!already)
        {
            _db.ContractSignatures.Add(new ContractSignature
            {
                ContractId = id,
                UserId = userId,
                SignedAt = DateTime.UtcNow
            });
            await _db.SaveChangesAsync(ct);
        }

        return NoContent();
    }

    private string CurrentUserId() =>
    User.FindFirstValue("sub")
    ?? User.FindFirstValue(ClaimTypes.NameIdentifier)
    ?? throw new InvalidOperationException("No user id in token.");
}

// References
// Microsoft (n.d.) Controller action return types in ASP.NET Core web API. Available at: <https://learn.microsoft.com/en-us/aspnet/core/web-api/action-return-types> (Accessed: 28 September 2026).
