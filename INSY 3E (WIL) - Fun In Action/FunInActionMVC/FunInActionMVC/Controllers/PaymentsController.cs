using FunInAction.Mvc.Filters;
using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

[RequireLogin("Coach")]   // CHANGED: coach payments page. Clients use PortalController (/Portal/Payments).
public class PaymentsController : Controller
{
    private readonly IFunInActionApiClient _api;

    public PaymentsController(IFunInActionApiClient api)
    {
        _api = api;
    }

    private void SetChrome()
    {
        ViewData["Title"] = "Payments";
        ViewData["PageCss"] = "stylepayments.css";
        ViewData["ActiveNav"] = "Payments";
        ViewData["Portal"] = "Coach";
    }

    // GET /Payments
    public async Task<IActionResult> Index(CancellationToken ct)
    {
        SetChrome();

        ViewBag.FeeSummary = await _api.GetProgrammeFeeSummaryAsync(ct);
        ViewBag.Payments = await _api.GetPaymentHistoryAsync(null, ct);   // NEW: null = all clients' payments
        var methods = await _api.GetPaymentMethodsAsync(ct);
        return View(methods);
    }

    // NEW: POST /Payments/UpdateStatus  changes one payment's status from the list.
    // Only the id and the new status come from the form; nothing else can be altered.
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> UpdateStatus(string id, PaymentStatus status, CancellationToken ct)
    {
        if (string.IsNullOrWhiteSpace(id) || !Enum.IsDefined(typeof(PaymentStatus), status))
        {
            TempData["ToastMessage"] = "That status change wasn't valid.";
            TempData["ToastIsError"] = true;
            return RedirectToAction(nameof(Index));
        }

        var ok = await _api.UpdatePaymentStatusAsync(id, status, ct);

        TempData["ToastMessage"] = ok
            ? $"Payment marked as {status}."
            : "The payment could not be updated.";
        if (!ok) TempData["ToastIsError"] = true;

        return RedirectToAction(nameof(Index));
    }

    // REMOVED: Pay. Card payments are client-only and now live in PortalController.
    // as advised by Courteney (farren made note for group)
}