using apiGLMS.Data;
using apiGLMS.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace apiGLMS.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ServiceRequestsController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public ServiceRequestsController(ApplicationDbContext context)
        {
            _context = context;
        }

        // Retrieves all service requests with optional filtering by status
        // Uses EF Core Include for eager loading related Contract data
        // (Microsoft, 2026y; Microsoft, 2026as)
        [HttpGet]
        public async Task<IActionResult> GetAll([FromQuery] string? status)
        {
            var query = _context.ServiceRequests
                .Include(s => s.Contract)
                .AsQueryable();

            // Applies dynamic filtering when status query parameter is provided
            // (Stack Overflow, 2010b; Microsoft, 2026as)
            if (!string.IsNullOrEmpty(status))
                query = query.Where(s => s.Status == status);

            return Ok(await query.ToListAsync());
        }

        // Retrieves a single service request by ID including its parent contract
        // (Microsoft, 2026y)
        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var request = await _context.ServiceRequests
                .Include(s => s.Contract)
                .FirstOrDefaultAsync(s => s.Id == id);

            if (request == null) return NotFound();

            return Ok(request);
        }

        // Creates a new service request while enforcing business rules on contract status
        // Prevents creation if contract is Expired or On Hold
        // (Microsoft, 2026i; Chapsas, 2024a; Code Canvas With Touseef, 2024)
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] ServiceRequest serviceRequest)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var contract = await _context.Contracts.FindAsync(serviceRequest.ContractId);

            // Business rule validation ensures data integrity and contract compliance
            // (Microsoft, 2026x; Stack Overflow, 2021)
            if (contract == null || contract.Status == "Expired" || contract.Status == "On Hold")
                return BadRequest("Cannot create Service Request: parent contract is Expired, On Hold, or not found.");

            _context.ServiceRequests.Add(serviceRequest);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetById), new { id = serviceRequest.Id }, serviceRequest);
        }

        // Updates an existing service request using EF Core state tracking
        // Includes concurrency handling for safe updates
        // (Microsoft, 2026z; Microsoft, 2026ad)
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, [FromBody] ServiceRequest serviceRequest)
        {
            if (id != serviceRequest.Id)
                return NotFound();

            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var contract = await _context.Contracts.FindAsync(serviceRequest.ContractId);

            if (contract == null || contract.Status == "Expired" || contract.Status == "On Hold")
                return BadRequest("Cannot update: parent contract is Expired or On Hold.");

            _context.Entry(serviceRequest).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.ServiceRequests.Any(s => s.Id == id))
                    return NotFound();
                else
                    throw;
            }

            return NoContent();
        }

        // Updates only the status field of a service request using HTTP PATCH semantics
        // (Microsoft, 2026m; Microsoft, 2026as)
        [HttpPatch("{id}/status")]
        public async Task<IActionResult> UpdateStatus(int id, [FromBody] string status)
        {
            var request = await _context.ServiceRequests.FindAsync(id);
            if (request == null) return NotFound();

            request.Status = status;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // Deletes a service request by ID from the database
        // (Microsoft, 2026as)
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var request = await _context.ServiceRequests.FindAsync(id);
            if (request == null) return NotFound();

            _context.ServiceRequests.Remove(request);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}

/*
REFERENCES

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online] Available at: <https://www.youtube.com/watch?v=3XoXzEPHdTA> [Accessed 2 June 2026].

Code Canvas With Touseef, 2024. .NET 9 Core Web API & EF Core Code First | EF Migration | Complete Tutorial from Scratch. [video online] Available at: <https://www.youtube.com/watch?v=m_BJYeE-j34> [Accessed 2 June 2026].

Microsoft, 2026i. ASP.NET Core Model Validation. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation?view=aspnetcore-10.0> [Accessed 8 June 2026].

Microsoft, 2026m. ASP.NET Core Web API HttpPatch. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/web-api/?view=aspnetcore-10.0#httppatch> [Accessed 8 June 2026].

Microsoft, 2026x. EF Core - Creating and Configuring a Model. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/modeling/> [Accessed 8 June 2026].

Microsoft, 2026y. EF Core - Eager Loading of Related Data. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/querying/related-data/eager> [Accessed 8 June 2026].

Microsoft, 2026z. EF Core - Handling Concurrency Conflicts. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/saving/concurrency?tabs=data-annotations> [Accessed 8 June 2026].

Microsoft, 2026ad. EntityState Enum. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.entityframeworkcore.entitystate?view=efcore-9.0> [Accessed 8 June 2026].

Microsoft, 2026as. Queryable Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.linq.queryable?view=net-10.0> [Accessed 8 June 2026].

Stack Overflow, 2010b. Date Range Search Filter Functionality in ASP.NET MVC 5. [online] Available at: <https://stackoverflow.com/questions/46218160/date-range-search-filter-functionality-in-asp-net-mvc-5> [Accessed 10 April 2026].
*/