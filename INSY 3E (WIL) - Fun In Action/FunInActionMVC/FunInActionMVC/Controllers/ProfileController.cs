using FunInAction.Mvc.Filters;
using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

[RequireLogin]
public class ProfileController : Controller
{
    private readonly IFunInActionApiClient _api;

    public ProfileController(IFunInActionApiClient api)
    {
        _api = api;
    }

    private void SetChrome(string portal)
    {
        ViewData["Title"] = "My Profile";
        ViewData["PageCss"] = "styleprofile.css";
        ViewData["ActiveNav"] = "Profile";
        ViewData["Portal"] = portal;
    }

    // GET /Profile?portal=Coach|Parent
    public async Task<IActionResult> Index(string portal = "Coach", CancellationToken ct = default)
    {
        SetChrome(portal);

        var profile = await _api.GetProfileAsync(ct);
        var vm = new ProfilePageViewModel { Profile = profile };
        return View(vm);
    }

    // POST /Profile/UpdateDetails
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> UpdateDetails(UserProfile profile, string portal = "Coach", CancellationToken ct = default)
    {
        SetChrome(portal);

        // Role is read-only in the view; keep whatever the API already has.
        var existing = await _api.GetProfileAsync(ct);
        profile.Role = existing.Role;
        profile.Id = existing.Id;

        if (!ModelState.IsValid)
        {
            var vm = new ProfilePageViewModel { Profile = profile };
            return View("Index", vm);
        }

        await _api.UpdateProfileAsync(profile, ct);
        TempData["ToastMessage"] = "Profile details saved.";
        return RedirectToAction(nameof(Index), new { portal });
    }

    // POST /Profile/ChangePassword
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ChangePassword(ChangePasswordViewModel changePassword, string portal = "Coach", CancellationToken ct = default)
    {
        SetChrome(portal);

        if (!ModelState.IsValid)
        {
            var profile = await _api.GetProfileAsync(ct);
            var vm = new ProfilePageViewModel { Profile = profile, ChangePassword = changePassword };
            return View("Index", vm);
        }

        var success = await _api.ChangePasswordAsync(changePassword, ct);
        if (!success)
        {
            ModelState.AddModelError("ChangePassword.CurrentPassword", "Current password is incorrect.");
            var profile = await _api.GetProfileAsync(ct);
            var vm = new ProfilePageViewModel { Profile = profile, ChangePassword = changePassword };
            return View("Index", vm);
        }

        TempData["ToastMessage"] = "Password updated.";
        return RedirectToAction(nameof(Index), new { portal });
    }
}
