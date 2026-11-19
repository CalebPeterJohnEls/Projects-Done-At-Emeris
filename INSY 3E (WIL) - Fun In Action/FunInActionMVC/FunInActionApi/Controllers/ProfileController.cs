using System.Security.Claims;
using FunInActionApi.Data;
using FunInActionApi.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FunInActionApi.Controllers;

/// <summary>The signed-in user's own profile: /api/profile and /api/profile/change-password.</summary>
[ApiController]
[Route("api/profile")]
[Authorize]
public class ProfileController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly IPasswordHasher<AppUser> _hasher;

    public ProfileController(AppDbContext db, IPasswordHasher<AppUser> hasher)
    {
        _db = db;
        _hasher = hasher;
    }

    private async Task<AppUser?> CurrentUserAsync(CancellationToken ct)
    {
        if (!Guid.TryParse(User.FindFirstValue("sub"), out var id)) return null;
        return await _db.Users.Include(u => u.Client).FirstOrDefaultAsync(u => u.Id == id, ct);
    }

    [HttpGet]
    public async Task<ActionResult<ProfileDto>> Get(CancellationToken ct)
    {
        var u = await CurrentUserAsync(ct);
        if (u is null) return Unauthorized();
        return new ProfileDto { Id = u.Id.ToString(), FullName = u.FullName, Email = u.Email, Phone = u.Phone, Role = u.Role };
    }

    [HttpPut]
    public async Task<IActionResult> Update(ProfileDto dto, CancellationToken ct)
    {
        var u = await CurrentUserAsync(ct);
        if (u is null) return Unauthorized();

        var email = dto.Email.Trim().ToLowerInvariant();
        if (await _db.Users.AnyAsync(x => x.Email == email && x.Id != u.Id, ct))
            return Conflict(new { error = "That email is already in use." });

        u.FullName = dto.FullName.Trim();
        u.Email = email;
        u.Phone = dto.Phone.Trim();

        // Keep the coach-side Clients row in step with the profile.
        if (u.Client is not null)
        {
            var parts = u.FullName.Split(' ', 2, StringSplitOptions.RemoveEmptyEntries);
            u.Client.FirstName = parts[0];
            u.Client.LastName = parts.Length > 1 ? parts[1] : "";
            u.Client.Email = email;
            u.Client.Phone = u.Phone;
        }

        await _db.SaveChangesAsync(ct);
        return NoContent();
    }

    [HttpPost("change-password")]
    public async Task<IActionResult> ChangePassword(ChangePasswordRequest req, CancellationToken ct)
    {
        var u = await CurrentUserAsync(ct);
        if (u is null) return Unauthorized();

        if (_hasher.VerifyHashedPassword(u, u.PasswordHash, req.CurrentPassword) == PasswordVerificationResult.Failed)
            return BadRequest(new { error = "Current password is incorrect." });

        u.PasswordHash = _hasher.HashPassword(u, req.NewPassword);
        await _db.SaveChangesAsync(ct);
        return NoContent();
    }
}

// References
// Microsoft (n.d.) Password hashing in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/data-protection/consumer-apis/password-hashing> (Accessed: 28 September 2026).
