using FunInActionApi.Data;
using FunInActionApi.Dtos;
using FunInActionApi.Services;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Mvc;

namespace FunInActionApi.Controllers;

/// <summary>POST /api/auth/register and /api/auth/login. Both return a JWT.</summary>
[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly IPasswordHasher<AppUser> _hasher;
    private readonly TokenService _tokens;

    public AuthController(AppDbContext db, IPasswordHasher<AppUser> hasher, TokenService tokens)
    {
        _db = db;
        _hasher = hasher;
        _tokens = tokens;
    }

    // Registering creates BOTH a login (AppUser) and a row in the Clients table,
    // which is exactly what the coach's Clients page lists.
    [HttpPost("register")]
    public async Task<ActionResult<AuthResponse>> Register(RegisterRequest req, CancellationToken ct)
    {
        var email = req.Email.Trim().ToLowerInvariant();

        if (await _db.Users.AnyAsync(u => u.Email == email, ct))
            return Conflict(new { error = "An account with that email already exists." });

        // Whitelist: only Client or Player. Nobody can register themselves as Coach.
        var role = req.Role.Equals("Player", StringComparison.OrdinalIgnoreCase) ? "Player" : "Client";

        var client = new Client
        {
            Id = await IdGenerator.NextAsync(_db.Clients.Select(c => c.Id), "CL", ct),
            FirstName = req.FirstName.Trim(),
            LastName = req.LastName.Trim(),
            Email = email,
            Phone = req.Phone.Trim(),
            Active = true,
            AddedOn = DateTime.UtcNow,
            Type = role
        };

        var user = new AppUser
        {
            Email = email,
            FullName = $"{client.FirstName} {client.LastName}",
            Phone = client.Phone,
            Role = role,
            ClientId = client.Id
        };
        user.PasswordHash = _hasher.HashPassword(user, req.Password);

        _db.Clients.Add(client);
        _db.Users.Add(user);
        await _db.SaveChangesAsync(ct);

        return Ok(_tokens.CreateToken(user));
    }

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponse>> Login(LoginRequest req, CancellationToken ct)
    {
        var email = req.Email.Trim().ToLowerInvariant();
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Email == email, ct);

        // Same message for "no such user" and "wrong password" so attackers can't probe which emails exist.
        if (user is null ||
            _hasher.VerifyHashedPassword(user, user.PasswordHash, req.Password) == PasswordVerificationResult.Failed)
            return Unauthorized(new { error = "Invalid email or password." });

        return Ok(_tokens.CreateToken(user));
    }
}

// References
// Microsoft (n.d.) Create web APIs with ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/web-api/> (Accessed: 28 September 2026).
