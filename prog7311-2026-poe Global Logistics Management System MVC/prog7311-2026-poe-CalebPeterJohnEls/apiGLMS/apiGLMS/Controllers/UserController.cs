using apiGLMS.Data;
using apiGLMS.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace apiGLMS.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public UsersController(ApplicationDbContext context)
        {
            _context = context;
        }

        // Retrieves all users from the database using EF Core asynchronous LINQ queries
        // (Microsoft, 2026as; Tiwari, 2024)
        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _context.Users.ToListAsync());
        }

        // Retrieves a single user by ID using primary key lookup
        // Returns 404 if the user does not exist
        // (Microsoft, 2026as)
        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound();
            return Ok(user);
        }

        // Retrieves a user by email address for authentication or validation logic
        // Commonly used in login/register flows in MVC systems
        // (Deshwal, 2024; Microsoft, 2026an)
        [HttpGet("byemail/{email}")]
        public async Task<IActionResult> GetByEmail(string email)
        {
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Email == email);

            if (user == null) return NotFound();
            return Ok(user);
        }

        // Creates a new user record in the database
        // Includes validation and duplicate email prevention logic
        // (Microsoft, 2026i; Chapsas, 2024a; Stack Overflow, 2021)
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] User user)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Prevent duplicate email registration using EF Core AnyAsync
            // (Microsoft, 2026ab)
            var exists = await _context.Users.AnyAsync(u => u.Email == user.Email);
            if (exists)
                return Conflict("Email already exists.");

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetById), new { id = user.Id }, user);
        }

        // Deletes a user from the database by ID
        // Returns NotFound if user does not exist
        // (Microsoft, 2026as)
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound();

            _context.Users.Remove(user);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

/*
REFERENCES

BCrypt.Net-Next, 2023. BCrypt.Net-Next NuGet Package. [online] Available at: <https://www.nuget.org/packages/BCrypt.Net-Next> [Accessed 22 April 2026].

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online] Available at: <https://www.youtube.com/watch?v=3XoXzEPHdTA> [Accessed 2 June 2026].

Deshwal, M., 2024. JSON Web Tokens (JWT) in .NET 9 Web API - User Registration / Login / Authentication (Part 2). [video online] Available at: <https://www.youtube.com/watch?v=cR1bnbsUJGE> [Accessed 2 June 2026].

Microsoft, 2026an. ASP.NET Core JWT Authentication. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authentication/jwt-authn?view=aspnetcore-10.0> [Accessed 1 June 2026].

Microsoft, 2026as. Queryable Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.linq.queryable?view=net-10.0> [Accessed 8 June 2026].

Microsoft, 2026i. ASP.NET Core Model Validation. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation?view=aspnetcore-10.0> [Accessed 8 June 2026].

Microsoft, 2026ab. EF Core Tracking vs No-Tracking Queries. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/querying/tracking> [Accessed 8 June 2026].

Stack Overflow, 2021. How to Seed Data in .NET Core 6 with Entity Framework. [online] Available at: <https://stackoverflow.com/questions/70581816/how-to-seed-data-in-net-core-6-with-entity-framework> [Accessed 1 June 2026].

Tiwari, N., 2024. ASP.NET Core Dependency Injection | C# Dependency Injection Tutorial. [video online] Available at: <https://www.youtube.com/watch?v=GoDmm-4yYPg> [Accessed 2 June 2026].
*/