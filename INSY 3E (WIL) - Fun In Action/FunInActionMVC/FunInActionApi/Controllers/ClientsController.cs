using FunInActionApi.Data;
using FunInActionApi.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FunInActionApi.Controllers;

/// <summary>The coach's Clients table. Coach-only: /api/clients, /api/clients/{id}.</summary>
[ApiController]
[Route("api/clients")]
[Authorize(Roles = "Coach")]
public class ClientsController : ControllerBase
{
    private readonly AppDbContext _db;
    public ClientsController(AppDbContext db) => _db = db;

    private static ClientDto ToDto(Client c) => new()
    {
        Id = c.Id, FirstName = c.FirstName, LastName = c.LastName, Email = c.Email,
        Phone = c.Phone, Active = c.Active, AddedOn = c.AddedOn, Type = c.Type
    };

    [HttpGet]
    public async Task<ActionResult<List<ClientDto>>> GetAll(CancellationToken ct)
    {
        var rows = await _db.Clients.AsNoTracking().OrderBy(c => c.Id).ToListAsync(ct);
        return rows.Select(ToDto).ToList();
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ClientDto>> Get(string id, CancellationToken ct)
    {
        var c = await _db.Clients.AsNoTracking().FirstOrDefaultAsync(x => x.Id == id, ct);
        return c is null ? NotFound() : ToDto(c);
    }

    // The coach can also add a client by hand (the MVC "Add client" form supplies the Id).
    [HttpPost]
    public async Task<ActionResult<ClientDto>> Create(ClientDto dto, CancellationToken ct)
    {
        if (await _db.Clients.AnyAsync(c => c.Id == dto.Id, ct))
            return Conflict(new { error = "That client ID is already in use." });

        var c = new Client
        {
            Id = dto.Id.Trim(), FirstName = dto.FirstName.Trim(), LastName = dto.LastName.Trim(),
            Email = dto.Email.Trim().ToLowerInvariant(), Phone = dto.Phone.Trim(),
            Active = dto.Active, AddedOn = DateTime.UtcNow,
            Type = string.IsNullOrWhiteSpace(dto.Type) ? "Client" : dto.Type
        };
        _db.Clients.Add(c);
        await _db.SaveChangesAsync(ct);
        return CreatedAtAction(nameof(Get), new { id = c.Id }, ToDto(c));
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(string id, ClientDto dto, CancellationToken ct)
    {
        var c = await _db.Clients.FirstOrDefaultAsync(x => x.Id == id, ct);
        if (c is null) return NotFound();

        c.FirstName = dto.FirstName.Trim();
        c.LastName = dto.LastName.Trim();
        c.Email = dto.Email.Trim().ToLowerInvariant();
        c.Phone = dto.Phone.Trim();
        c.Active = dto.Active;       // Id, AddedOn and Type are deliberately left alone
        await _db.SaveChangesAsync(ct);
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(string id, CancellationToken ct)
    {
        var c = await _db.Clients.FirstOrDefaultAsync(x => x.Id == id, ct);
        if (c is null) return NotFound();

        _db.Clients.Remove(c);
        await _db.SaveChangesAsync(ct);
        return NoContent();
    }
}

// References
// Microsoft (n.d.) Role-based authorization in ASP.NET Core. Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authorization/roles> (Accessed: 28 September 2026).
