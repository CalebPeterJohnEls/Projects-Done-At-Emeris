using apiGLMS.Data;
using apiGLMS.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace apiGLMS.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ClientsController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public ClientsController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            return Ok(await _context.Clients.ToListAsync());
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var client = await _context.Clients
                .Include(c => c.Contracts)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (client == null) return NotFound();

            return Ok(client);
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] Client client)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            _context.Clients.Add(client);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetById), new { id = client.Id }, client);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, [FromBody] Client client)
        {
            if (id != client.Id) return NotFound();
            if (!ModelState.IsValid) return BadRequest(ModelState);

            _context.Entry(client).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.Clients.Any(c => c.Id == id)) return NotFound();
                else throw;
            }

            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var client = await _context.Clients.FindAsync(id);
            if (client == null) return NotFound();

            _context.Clients.Remove(client);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

/*
REFERENCES

BCrypt.Net-Next, 2023. BCrypt.Net-Next NuGet Package. [online] Available at: <https://www.nuget.org/packages/BCrypt.Net-Next> [Accessed 22 April 2026].

Stack Overflow, 2021. How to Seed Data in .NET Core 6 with Entity Framework. [online] Available at: <https://stackoverflow.com/questions/70581816/how-to-seed-data-in-net-core-6-with-entity-framework> [Accessed 1 June 2026].

Microsoft, 2026x. EF Core - Creating and Configuring a Model. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/modeling/> [Accessed 1 June 2026].

Microsoft, 2026aa. EF Core - Migrations. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/managing-schemas/migrations/?tabs=dotnet-core-cli> [Accessed 1 June 2026].

Microsoft, 2026k. ASP.NET Core Web API Action Return Types. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/web-api/action-return-types?view=aspnetcore-10.0> [Accessed 1 June 2026].

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online] Available at: <https://www.youtube.com/watch?v=3XoXzEPHdTA> [Accessed 2 June 2026].

Code Canvas With Touseef, 2024. .NET 9 Core Web API & EF Core Code First | EF Migration | Complete Tutorial from Scratch. [video online] Available at: <https://www.youtube.com/watch?v=m_BJYeE-j34> [Accessed 2 June 2026].

Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].

Code-Maze, 2023b. Swagger UI in ASP.NET Core Web API. [online] Available at: <https://code-maze.com/swagger-ui-asp-net-core-web-api/> [Accessed 1 June 2026].
*/