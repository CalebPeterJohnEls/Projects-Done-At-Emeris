using FunInAction.Mvc.Filters;
using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

/// <summary>
/// The Parent Portal — the client-facing counterpart to the Coach Portal's
/// Home/Payments controllers. Kept as its own controller (rather than
/// branching every coach action) because a parent's home page and payments
/// page show different data shapes to a different audience.
/// </summary>
[RequireLogin("Client", "Player")]   // CHANGED
public class PortalController : Controller
{
    private readonly IFunInActionApiClient _api;

    public PortalController(IFunInActionApiClient api)
    {
        _api = api;
    }

    // NEW: the signed-in person's own client-table row id, set at login (see AccountController).
    private string? MyClientId => HttpContext.Session.GetString("ClientId");

    // GET /Portal  and  /Portal/Home
    [HttpGet("Portal")]
    [HttpGet("Portal/Home")]
    public async Task<IActionResult> Home(CancellationToken ct)
    {
        ViewData["Title"] = "My Home";
        ViewData["PageCss"] = "style.css";
        ViewData["ActiveNav"] = "Home";
        ViewData["Portal"] = "Parent";

        var dashboard = await _api.GetParentDashboardAsync(ct);
        return View(dashboard);
    }

    // NEW: shared by Payments and the invalid-model path of Pay.
    private async Task<List<PaymentRecord>> LoadPaymentsPageAsync(CancellationToken ct)
    {
        ViewData["Title"] = "My Payments";
        ViewData["PageCss"] = "stylepayments.css";
        ViewData["ActiveNav"] = "Payments";
        ViewData["Portal"] = "Parent";

        ViewBag.FeeSummary = await _api.GetProgrammeFeeSummaryAsync(ct);
        return await _api.GetPaymentHistoryAsync(clientId: MyClientId, ct: ct);   // CHANGED: was hard-coded to "CL-0102"
    }

    // GET /Portal/Payments
    public async Task<IActionResult> Payments(CancellationToken ct)
    {
        var history = await LoadPaymentsPageAsync(ct);
        var summary = (ProgrammeFeeSummary)ViewBag.FeeSummary;

        // NEW: feeds the bank card + flip overlay
        ViewBag.CardPayment = new CardPaymentViewModel
        {
            ItemLabel = summary.ProgrammeName,
            ItemAmount = "R" + summary.TotalAmount.ToString("N0")
        };
        return View(history);
    }

    // NEW: POST /Portal/Pay — placeholder card-payment submission.
    // Card gateways (Peach Payments / PayGate) are still being costed, so this
    // just records a pending payment via the API/mock.
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Pay(CardPaymentViewModel payment, CancellationToken ct)
    {
        if (!ModelState.IsValid)
        {
            var history = await LoadPaymentsPageAsync(ct);
            ViewBag.CardPayment = payment;
            ViewBag.ReopenOverlay = true;
            return View("Payments", history);
        }

        await _api.SubmitCardPaymentAsync(MyClientId ?? "", payment, ct);   // CHANGED: now records who paid
        TempData["ToastMessage"] = "Payment processing isn't connected yet — this was recorded as a placeholder.";
        return RedirectToAction(nameof(Payments));
    }

    // POST /Portal/UploadProof
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> UploadProof(IFormFile? proofFile, CancellationToken ct)
    {
        if (proofFile is null || proofFile.Length == 0)
        {
            TempData["ToastMessage"] = "Choose a file before uploading.";
            TempData["ToastIsError"] = true;
            return RedirectToAction(nameof(Payments));
        }

        await _api.SubmitProofOfPaymentAsync(MyClientId ?? "", proofFile.FileName, ct);   // CHANGED: was hard-coded to "CL-0102"

        TempData["ToastMessage"] = "Proof of payment uploaded — it will reflect here once confirmed.";
        return RedirectToAction(nameof(Payments));
    }
}