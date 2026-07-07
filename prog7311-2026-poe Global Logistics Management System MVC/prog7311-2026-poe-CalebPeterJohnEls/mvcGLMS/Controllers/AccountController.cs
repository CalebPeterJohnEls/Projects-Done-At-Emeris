using Microsoft.AspNetCore.Mvc;
using mvcGLMS.Models;
using System.Net.Http.Json;

namespace mvcGLMS.Controllers
{
    // MVC Controller handling user authentication: login, logout, and registration
    // Communicates with the Web API auth endpoints via IHttpClientFactory (Microsoft, 2026aj)
    public class AccountController : Controller
    {
        private readonly HttpClient _http;

        // Named HttpClient "GlmsApi" is configured in Program.cs with the API base address
        // IHttpClientFactory manages the client lifetime to avoid socket exhaustion (Microsoft, 2026aj)
        public AccountController(IHttpClientFactory factory)
        {
            _http = factory.CreateClient("GlmsApi");
        }

        // GET: renders the login form
        [HttpGet]
        public IActionResult Login() => View();

        // POST: submits login credentials to the API and stores the returned JWT in session
        [HttpPost]
        public async Task<IActionResult> Login(Login model)
        {
            // Return the form with validation errors if model binding fails (Microsoft, 2026i)
            if (!ModelState.IsValid) return View(model);

            // Posts an anonymous object matching the API's expected login DTO (Microsoft, 2026ag)
            var response = await _http.PostAsJsonAsync("auth/login", new
            {
                username = model.Username,
                password = model.Password
            });

            if (!response.IsSuccessStatusCode)
            {
                ModelState.AddModelError(string.Empty, "Invalid username or password.");
                return View(model);
            }

            var result = await response.Content.ReadFromJsonAsync<AuthResponse>();

            // Persist JWT and user info in server-side session for use across subsequent requests
            // (Microsoft, 2026j)
            HttpContext.Session.SetString("JwtToken", result!.Token);
            HttpContext.Session.SetString("Role",     result.Role);
            HttpContext.Session.SetString("Username", result.Username);

            return RedirectToAction("Index", "Home");
        }

        // POST: clears the session and redirects to the home page (Microsoft, 2026j)
        [HttpPost]
        public IActionResult Logout()
        {
            HttpContext.Session.Clear();
            return RedirectToAction("Index", "Home");
        }

        // GET: renders the registration form
        [HttpGet]
        public IActionResult Register() => View();

        // POST: submits registration details to the API; on success stores JWT and redirects
        [HttpPost]
        public async Task<IActionResult> Register(Register model)
        {
            if (!ModelState.IsValid) return View(model);

            // Posts all required registration fields as an anonymous DTO to the API (Microsoft, 2026ag)
            var response = await _http.PostAsJsonAsync("auth/register", new
            {
                username = model.Username,
                name     = model.Name,
                surname  = model.Surname,
                email    = model.Email,
                password = model.Password,
                role     = model.Role
            });

            if (response.IsSuccessStatusCode)
            {
                var result = await response.Content.ReadFromJsonAsync<AuthResponse>();

                // Store session state immediately after registration so the user is logged in
                // (Microsoft, 2026j)
                HttpContext.Session.SetString("JwtToken", result!.Token);
                HttpContext.Session.SetString("Role",     result.Role);
                HttpContext.Session.SetString("Username", result.Username);
                return RedirectToAction("Index", "Home");
            }

            // 409 Conflict means the username or email is already taken (Microsoft, 2026ai)
            var error = response.StatusCode == System.Net.HttpStatusCode.Conflict
                ? "Username or email already exists."
                : "Registration failed.";

            ModelState.AddModelError(string.Empty, error);
            return View(model);
        }
    }

    // DTO that maps the JSON body returned by api/auth/login and api/auth/register
    // Keeps the controller decoupled from the raw HTTP response structure (Troelsen and Japikse, 2022)
    public class AuthResponse
    {
        public string Token    { get; set; } = string.Empty;
        public string Role     { get; set; } = string.Empty;
        public string Username { get; set; } = string.Empty;
    }
}

// References:
// Microsoft, 2026ag. HttpClientJsonExtensions Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.http.json.httpclientjsonextensions?view=net-10.0> [Accessed 1 June 2026].
// Microsoft, 2026ai. HttpStatusCode Enum. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.httpstatuscode?view=net-10.0> [Accessed 1 June 2026].
// Microsoft, 2026aj. IHttpClientFactory in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/httpclient-factory> [Accessed 1 June 2026].
// Microsoft, 2026i. ASP.NET Core Model Validation. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Microsoft, 2026j. ASP.NET Core Session and State Management. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/app-state?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.