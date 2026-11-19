using FunInAction.Mvc.Filters;
using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

[RequireLogin("Coach")]   // CHANGED: only the coach sees the Clients table
public class ClientsController : Controller
{
    private readonly IFunInActionApiClient _api;

    public ClientsController(IFunInActionApiClient api)
    {
        _api = api;
    }

    private void SetChrome(string activeNav = "Clients")
    {
        ViewData["PageCss"] = "stylesclients.css";
        ViewData["ActiveNav"] = activeNav;
        ViewData["Portal"] = "Coach";
    }

    // GET /Clients
    public async Task<IActionResult> Index(string? search, CancellationToken ct)
    {
        SetChrome();
        ViewData["Title"] = "Clients";

        var clients = await _api.GetClientsAsync(ct);

        if (!string.IsNullOrWhiteSpace(search))
        {
            var q = search.Trim();
            clients = clients.Where(c =>
                    c.Id.Contains(q, StringComparison.OrdinalIgnoreCase) ||
                    c.FirstName.Contains(q, StringComparison.OrdinalIgnoreCase) ||
                    c.LastName.Contains(q, StringComparison.OrdinalIgnoreCase) ||
                    c.Email.Contains(q, StringComparison.OrdinalIgnoreCase) ||
                    c.Phone.Contains(q, StringComparison.OrdinalIgnoreCase))
                .ToList();
        }

        var vm = new ClientsIndexViewModel { Clients = clients, SearchTerm = search };
        return View(vm);
    }

    // GET /Clients/Create
    public IActionResult Create()
    {
        SetChrome();
        ViewData["Title"] = "Add client";
        return View("Save", new ClientFormViewModel { IsEditing = false });
    }

    // POST /Clients/Create
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Create(ClientFormViewModel form, CancellationToken ct)
    {
        SetChrome();
        ViewData["Title"] = "Add client";

        if (!string.IsNullOrWhiteSpace(form.Client.Id) && await _api.GetClientAsync(form.Client.Id, ct) is not null)
        {
            ModelState.AddModelError("Client.Id", "That client ID is already in use.");
        }

        if (!ModelState.IsValid)
        {
            return View("Save", form);
        }

        await _api.CreateClientAsync(form.Client, ct);
        TempData["ToastMessage"] = $"Client {form.Client.FirstName} {form.Client.LastName} added.";
        return RedirectToAction(nameof(Index));
    }

    // GET /Clients/Edit/{id}
    public async Task<IActionResult> Edit(string id, CancellationToken ct)
    {
        SetChrome();
        var client = await _api.GetClientAsync(id, ct);
        if (client is null)
        {
            return NotFound();
        }

        ViewData["Title"] = "Edit client";
        return View("Save", new ClientFormViewModel { Client = client, IsEditing = true });
    }

    // POST /Clients/Edit/{id}
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Edit(string id, ClientFormViewModel form, CancellationToken ct)
    {
        SetChrome();
        ViewData["Title"] = "Edit client";
        form.IsEditing = true;
        form.Client.Id = id;

        if (!ModelState.IsValid)
        {
            return View("Save", form);
        }

        var updated = await _api.UpdateClientAsync(id, form.Client, ct);
        if (!updated)
        {
            return NotFound();
        }

        TempData["ToastMessage"] = "Changes saved.";
        return RedirectToAction(nameof(Index));
    }

    // POST /Clients/Delete/{id}
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Delete(string id, CancellationToken ct)
    {
        var deleted = await _api.DeleteClientAsync(id, ct);
        TempData["ToastMessage"] = deleted ? "Client deleted." : "That client could not be found.";
        TempData["ToastIsError"] = !deleted;
        return RedirectToAction(nameof(Index));
    }
}
