using System.Text;
using FunInActionApi.Data;
using FunInActionApi.Dtos;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;

namespace FunInActionApi.Services;

/// <summary>Bound from the "Jwt" section of appsettings.json.</summary>
public class JwtOptions
{
    public string Key { get; set; } = string.Empty;
    public string Issuer { get; set; } = "FunInActionApi";
    public string Audience { get; set; } = "FunInActionMvc";
    public int ExpiryMinutes { get; set; } = 120;
}

/// <summary>Creates the signed JWT the MVC app sends back on every API call (Authorization: Bearer ...).</summary>
public class TokenService
{
    private readonly JwtOptions _opt;
    public TokenService(IOptions<JwtOptions> opt) => _opt = opt.Value;

    public AuthResponse CreateToken(AppUser user)
    {
        var expires = DateTime.UtcNow.AddMinutes(_opt.ExpiryMinutes);

        // Short claim names on purpose ("sub", "role", ...). Program.cs sets MapInboundClaims = false so they stay as-is.
        var claims = new Dictionary<string, object>
        {
            ["sub"] = user.Id.ToString(),
            ["email"] = user.Email,
            ["name"] = user.FullName,
            ["role"] = user.Role
        };
        if (user.ClientId is not null) claims["clientId"] = user.ClientId;

        var descriptor = new SecurityTokenDescriptor
        {
            Claims = claims,
            Issuer = _opt.Issuer,
            Audience = _opt.Audience,
            Expires = expires,
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_opt.Key)),
                SecurityAlgorithms.HmacSha256)
        };

        return new AuthResponse
        {
            Token = new JsonWebTokenHandler().CreateToken(descriptor),
            ExpiresOn = expires,
            Role = user.Role,
            FullName = user.FullName,
            Email = user.Email,
            ClientId = user.ClientId
        };
    }
}

// References
// Microsoft (n.d.) Configure JWT bearer authentication in ASP.NET Core. Available at: https://learn.microsoft.com/en-us/aspnet/core/security/authentication/ (Accessed: 28 September 2026).