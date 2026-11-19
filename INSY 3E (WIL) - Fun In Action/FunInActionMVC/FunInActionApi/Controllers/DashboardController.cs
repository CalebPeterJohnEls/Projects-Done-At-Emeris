using System.Security.Claims;
using FunInActionApi.Data;
using FunInActionApi.Dtos;
using FunInActionApi.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FunInActionApi.Controllers;

[ApiController]
[Route("api/dashboard")]
[Authorize]
public class DashboardController : ControllerBase
{
    private readonly AppDbContext _db;
    public DashboardController(AppDbContext db) => _db = db;

    // GET /api/dashboard/coach  – counts are real; the session list is placeholder until a Sessions table exists.
    [HttpGet("coach")]
    [Authorize(Roles = "Coach")]
    public async Task<ActionResult<CoachDashboardDto>> Coach(CancellationToken ct) => new CoachDashboardDto
    {
        ActivePlayers = await _db.Clients.CountAsync(c => c.Active, ct),
        SessionsThisTerm = 4,
        PaymentsPending = await _db.Payments.CountAsync(p => p.Status == PaymentStatus.Pending, ct),
        UpcomingSessions = new()
        {
            new() { Date = "Sat, 22 Aug", Time = "09:00 - 10:00", Group = "Group A", Focus = "Attacking stances", Status = "Confirmed" },
            new() { Date = "Sat, 29 Aug", Time = "09:00 - 10:00", Group = "Group A", Focus = "Defending & receiving the ball", Status = "Confirmed" },
            new() { Date = "Sat, 5 Sep",  Time = "09:00 - 10:00", Group = "Group B", Focus = "Grip & footwork", Status = "Pending" },
            new() { Date = "Sat, 12 Sep", Time = "09:00 - 10:00", Group = "Group B", Focus = "Moving with the ball", Status = "Pending" },
        }
    };

    // GET /api/dashboard/parent – built from the signed-in client's own rows.
    [HttpGet("parent")]
    public async Task<ActionResult<ParentDashboardDto>> Parent(CancellationToken ct)
    {
        var clientId = User.FindFirstValue("clientId");
        var client = clientId is null ? null : await _db.Clients.AsNoTracking().FirstOrDefaultAsync(c => c.Id == clientId, ct);

        // Sum in memory: SQLite can't SUM decimals in SQL.
        var paidAmounts = await _db.Payments
            .Where(p => p.ClientId == clientId && p.Status == PaymentStatus.Confirmed)
            .Select(p => p.Amount).ToListAsync(ct);
        var paid = paidAmounts.Sum();

        return new ParentDashboardDto
        {
            ChildName = client?.FirstName ?? "Player",
            CurrentLevel = "Level 1",          // placeholders until sessions/feedback tables exist
            SessionsAttended = "0 / 4",
            NextSession = "To be confirmed",
            PaymentStatus = paid >= Catalog.Fee.TotalAmount ? "Paid in Full" : paid > 0 ? "Part paid" : "Unpaid",
            LatestFeedback = new()
        };
    }
}

// References
// Microsoft (n.d.) Claims-based authorization in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authorization/claims>  (Accessed: 28 September 2026).
