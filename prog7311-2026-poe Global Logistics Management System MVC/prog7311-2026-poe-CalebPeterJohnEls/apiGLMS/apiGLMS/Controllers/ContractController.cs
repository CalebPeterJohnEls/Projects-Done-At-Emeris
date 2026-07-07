using apiGLMS.Data;
using apiGLMS.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace apiGLMS.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    public class ContractsController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public ContractsController(ApplicationDbContext context)
        {
            _context = context;
        }

        // Retrieves contracts with optional filtering using EF Core IQueryable composition
        // Supports date range and status filtering
        // (Microsoft, 2026as; Stack Overflow, 2010b)
        [HttpGet]
        public async Task<IActionResult> GetAll(
            [FromQuery] DateTime? startDate,
            [FromQuery] DateTime? endDate,
            [FromQuery] string? status)
        {
            var query = _context.Contracts
                .Include(c => c.Client)
                .AsQueryable();

            if (startDate.HasValue)
                query = query.Where(c => c.StartDate >= startDate.Value);

            if (endDate.HasValue)
                query = query.Where(c => c.EndDate <= endDate.Value);

            if (!string.IsNullOrEmpty(status))
                query = query.Where(c => c.Status.ToLower().Contains(status.ToLower()));

            return Ok(await query.ToListAsync());
        }

        // Retrieves a contract by ID including related Client and ServiceRequests
        // (Microsoft, 2026y)
        [HttpGet("{id:int}")]
        public async Task<IActionResult> GetById(int id)
        {
            var contract = await _context.Contracts
                .Include(c => c.Client)
                .Include(c => c.ServiceRequests)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (contract == null)
                return NotFound(new { message = $"Contract with id {id} was not found." });

            return Ok(contract);
        }

        // Creates a new contract record using EF Core DbSet tracking
        // (Microsoft, 2026i)
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] Contract contract)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            _context.Contracts.Add(contract);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetById), new { id = contract.Id }, contract);
        }

        // Updates a contract using EF Core entity state tracking
        // Includes concurrency handling for safe updates
        // (Microsoft, 2026z)
        [HttpPut("{id:int}")]
        public async Task<IActionResult> Update(int id, [FromBody] Contract contract)
        {
            if (id != contract.Id)
                return BadRequest(new { message = "Route id and body Id must match." });

            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            _context.Entry(contract).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.Contracts.Any(c => c.Id == id))
                    return NotFound(new { message = $"Contract with id {id} was not found." });

                throw;
            }

            return NoContent();
        }

        // Updates only contract status using PATCH semantics
        // (Microsoft, 2026m)
        [HttpPatch("{id:int}/status")]
        public async Task<IActionResult> UpdateStatus(int id, [FromBody] string status)
        {
            if (string.IsNullOrWhiteSpace(status))
                return BadRequest(new { message = "Status cannot be empty." });

            var contract = await _context.Contracts.FindAsync(id);

            if (contract == null)
                return NotFound(new { message = $"Contract with id {id} was not found." });

            contract.Status = status;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // Updates stored file path after MVC upload process
        // (kudvenkat, 2024)
        [HttpPatch("{id:int}/agreement-path")]
        public async Task<IActionResult> UpdateAgreementPath(int id, [FromBody] string path)
        {
            if (string.IsNullOrWhiteSpace(path))
                return BadRequest(new { message = "Path cannot be empty." });

            var contract = await _context.Contracts.FindAsync(id);

            if (contract == null)
                return NotFound(new { message = $"Contract with id {id} was not found." });

            contract.SignedAgreementPath = path;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // Deletes a contract permanently from database
        // (Microsoft, 2026as)
        [HttpDelete("{id:int}")]
        public async Task<IActionResult> Delete(int id)
        {
            var contract = await _context.Contracts.FindAsync(id);

            if (contract == null)
                return NotFound(new { message = $"Contract with id {id} was not found." });

            _context.Contracts.Remove(contract);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

/*
REFERENCES

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online] Available at: https://www.youtube.com/watch?v=3XoXzEPHdTA [Accessed 8 June 2026].

Code Canvas With Touseef, 2024. .NET 9 Core Web API & EF Core Code First | EF Migration. [video online] Available at: https://www.youtube.com/watch?v=m_BJYeE-j34 [Accessed 8 June 2026].

kudvenkat, 2024. File Upload in ASP.NET Core MVC. [video online] Available at: https://www.youtube.com/watch?v=aoxEJii70_I [Accessed 8 June 2026].

Microsoft, 2026as. Queryable Class. Available at: https://learn.microsoft.com/en-us/dotnet/api/system.linq.queryable [Accessed 8 June 2026].

Microsoft, 2026i. ASP.NET Core Model Validation. Available at: https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation [Accessed 8 June 2026].

Microsoft, 2026m. ASP.NET Core Web API HttpPatch. Available at: https://learn.microsoft.com/en-us/aspnet/core/web-api/ [Accessed 8 June 2026].

Microsoft, 2026y. EF Core Eager Loading. Available at: https://learn.microsoft.com/en-us/ef/core/querying/related-data/eager [Accessed 8 June 2026].

Microsoft, 2026z. EF Core Concurrency Handling. Available at: https://learn.microsoft.com/en-us/ef/core/saving/concurrency [Accessed 8 June 2026].

Stack Overflow, 2010b. Date Range Filter in ASP.NET MVC. Available at: https://stackoverflow.com/questions/46218160 [Accessed 8 June 2026].
*/