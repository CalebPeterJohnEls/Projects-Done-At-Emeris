using FunInAction.Mvc.Filters;
using FunInAction.Mvc.Services;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

[RequireLogin("Client", "Player")]
public class ContractsController : Controller
{
    private readonly IFunInActionApiClient _api;

    public ContractsController(IFunInActionApiClient api)
    {
        _api = api;
    }

    private void SetPageData(string title)
    {
        ViewData["Title"] = title;
        ViewData["PageCss"] = "style.css";
        ViewData["ActiveNav"] = "Contracts";
        ViewData["Portal"] = "Parent";
    }

    // GET /Contracts
    public async Task<IActionResult> Index(CancellationToken ct)
    {
        SetPageData("Contracts");
        var contracts = await _api.GetContractsAsync(ct);
        return View(contracts);
    }

    // GET /Contracts/Details/{id}  (read the terms and sign)
    public async Task<IActionResult> Details(string id, CancellationToken ct)
    {
        var contracts = await _api.GetContractsAsync(ct);
        var contract = contracts.FirstOrDefault(c => c.Id == id);
        if (contract is null) return NotFound();

        SetPageData(contract.Title);
        return View(contract);
    }

    // POST /Contracts/Sign
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Sign(string id, bool agreed, CancellationToken ct)
    {
        if (!agreed)
        {
            TempData["ToastMessage"] = "Please tick the box to confirm you have read and agree.";
            TempData["ToastIsError"] = true;
            return RedirectToAction(nameof(Details), new { id });
        }

        try
        {
            await _api.SignContractAsync(id, ct);
        }
        catch (HttpRequestException ex)
        {
            TempData["ToastMessage"] = $"Could not save your signature (server said {(int?)ex.StatusCode}). Please try again.";
            TempData["ToastIsError"] = true;
            return RedirectToAction(nameof(Details), new { id });
        }

        TempData["ToastMessage"] = "Thank you, your agreement has been recorded.";
        return RedirectToAction(nameof(Index));
    }
}