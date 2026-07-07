using apiGLMS.Data;
using apiGLMS.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace apiGLMS.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly ApplicationDbContext _context;
        private readonly IConfiguration _config;

        public AuthController(ApplicationDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] AuthRequest request)
        {
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Username == request.Username);

            if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.Password))
                return Unauthorized(new { message = "Invalid username or password." });

            return Ok(new
            {
                token = GenerateToken(user),
                role = user.Role,
                username = user.Username
            });
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterRequest request)
        {
            var exists = await _context.Users
                .AnyAsync(u => u.Username == request.Username || u.Email == request.Email);

            if (exists)
                return Conflict(new { message = "Username or email already exists." });

            var user = new User
            {
                Username = request.Username,
                Name = request.Name,
                Surname = request.Surname,
                Email = request.Email,
                Password = BCrypt.Net.BCrypt.HashPassword(request.Password),
                Role = request.Role
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return Ok(new
            {
                token = GenerateToken(user),
                role = user.Role,
                username = user.Username
            });
        }

        private string GenerateToken(User user)
        {
            var key = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(_config["Jwt:Key"]!)
            );

            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Username),
                new Claim(ClaimTypes.Role, user.Role)
            };

            var token = new JwtSecurityToken(
                issuer: _config["Jwt:Issuer"],
                audience: _config["Jwt:Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddHours(8),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }

    public class AuthRequest
    {
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }

    public class RegisterRequest
    {
        public string Username { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Surname { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public string Role { get; set; } = "User";
    }
}

/*
REFERENCES

BCrypt.Net-Next, 2023. BCrypt.Net-Next NuGet Package. [online]
Available at: https://www.nuget.org/packages/BCrypt.Net-Next
[Accessed 22 April 2026].

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online]
Available at: https://www.youtube.com/watch?v=3XoXzEPHdTA
[Accessed 2 June 2026].

Deshwal, M., 2024. JSON Web Tokens (JWT) in .NET 9 Web API - User Registration / Login / Authentication. [video online]
Available at: https://www.youtube.com/watch?v=cR1bnbsUJGE
[Accessed 2 June 2026].

IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online]
Available at: https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK
[Accessed 18 March 2025].

The IIE, 2026. Programming 3A [PROG7311 Module Manual]. Unpublished.

Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices. 8th ed. New York: Apress.

Microsoft, 2026an. JWT Authentication in ASP.NET Core. [online]
Available at: https://learn.microsoft.com/en-us/aspnet/core/security/authentication/jwt-authn?view=aspnetcore-10.0
[Accessed 1 June 2026].
*/