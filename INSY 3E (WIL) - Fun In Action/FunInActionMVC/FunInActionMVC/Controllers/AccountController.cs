using FunInAction.Mvc.Models;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

public class AccountController : Controller
{
    private readonly HttpClient _http;

    public AccountController(IHttpClientFactory factory)
    {
        _http = factory.CreateClient("FunInActionApi");
    }

    [HttpGet]
    public IActionResult Login(string? returnUrl = null)   // CHANGED: accept returnUrl from RequireLoginAttribute
    {
        ViewData["ReturnUrl"] = returnUrl;
        return View(new LoginViewModel());
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Login(LoginViewModel model, string? returnUrl = null)   // CHANGED
    {
        ViewData["ReturnUrl"] = returnUrl;
        if (!ModelState.IsValid) return View(model);

        var response = await _http.PostAsJsonAsync("api/auth/login",
            new { email = model.Email, password = model.Password });

        if (!response.IsSuccessStatusCode)
        {
            ModelState.AddModelError(string.Empty,
                await ReadErrorAsync(response, "Invalid email or password."));
            return View(model);
        }

        var result = await response.Content.ReadFromJsonAsync<AuthResult>();
        StartSession(result!);
        return RedirectAfterLogin(result!.Role, returnUrl);   // CHANGED
    }

    [HttpGet]
    public IActionResult Register() => View(new RegisterViewModel());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Register(RegisterViewModel model)
    {
        if (!ModelState.IsValid) return View(model);

        var response = await _http.PostAsJsonAsync("api/auth/register", new
        {
            firstName = model.FirstName,
            lastName = model.LastName,
            email = model.Email,
            phone = model.Phone,
            password = model.Password,
            role = model.Role
        });

        if (!response.IsSuccessStatusCode)
        {
            ModelState.AddModelError(string.Empty,
                await ReadErrorAsync(response, "Registration failed."));
            return View(model);
        }

        var result = await response.Content.ReadFromJsonAsync<AuthResult>();
        StartSession(result!);
        return RedirectAfterLogin(result!.Role, null);   // CHANGED
    }

    [HttpPost, ValidateAntiForgeryToken]
    public IActionResult Logout()
    {
        HttpContext.Session.Clear();
        return RedirectToAction(nameof(Login));
    }

    private void StartSession(AuthResult r)
    {
        HttpContext.Session.SetString("JwtToken", r.Token);
        HttpContext.Session.SetString("Role", r.Role);
        HttpContext.Session.SetString("FullName", r.FullName);
        HttpContext.Session.SetString("Email", r.Email ?? string.Empty);          // NEW
        if (!string.IsNullOrEmpty(r.ClientId))
            HttpContext.Session.SetString("ClientId", r.ClientId);                // NEW: used by PortalController
    }

    // CHANGED: honours returnUrl (only when it's a local URL) so RequireLoginAttribute's
    // redirect-back-after-login actually works; falls back to each role's home page otherwise.
    private IActionResult RedirectAfterLogin(string role, string? returnUrl)
    {
        if (!string.IsNullOrEmpty(returnUrl) && Url.IsLocalUrl(returnUrl)) return Redirect(returnUrl);
        return role == "Coach"
            ? RedirectToAction("Index", "Home")
            : RedirectToAction("Home", "Portal");
    }

    // The API sends { "error": "..." } for 401 and 409 responses.
    private static async Task<string> ReadErrorAsync(HttpResponseMessage response, string fallback)
    {
        try
        {
            var body = await response.Content.ReadFromJsonAsync<Dictionary<string, string>>();
            return body is not null && body.TryGetValue("error", out var msg) ? msg : fallback;
        }
        catch
        {
            return fallback;
        }
    }
}
