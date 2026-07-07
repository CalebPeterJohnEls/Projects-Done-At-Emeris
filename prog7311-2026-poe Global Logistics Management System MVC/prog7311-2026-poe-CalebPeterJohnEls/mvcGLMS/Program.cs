using Microsoft.EntityFrameworkCore;

using mvcGLMS.Data;
using mvcGLMS.ExchangeServices;
using mvcGLMS.ObserverServices;

var builder = WebApplication.CreateBuilder(args);

// Both apps point at the same GLMSDb.
// This connection string is kept so dotnet ef migrations tooling works locally.
builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddControllersWithViews();

// OpenExchangeStrategy calls an external FX API and needs its own HttpClient (Microsoft, 2026aj; OpenExchangeRates, 2024).
builder.Services.AddHttpClient<ICurrencyStrategy, OpenExchangeStrategy>();

// JwtAttachHandler reads the JWT from session and adds it to every outbound API request (Microsoft, 2026an; Chapsas, 2024a).
builder.Services.AddHttpContextAccessor();
builder.Services.AddTransient<JwtAttachHandler>(); // Transient: a new instance is created each time it is requested (Microsoft, 2026e).

// Named HttpClient used by all MVC controllers to call the GLMS Web API (Microsoft, 2026aj; Codewrinkles, 2024).
// BaseUrl is read from appsettings.json; a missing value throws at startup rather than at runtime (Microsoft, 2026d).
var apiBase = builder.Configuration["ApiSettings:BaseUrl"]
              ?? throw new InvalidOperationException(
                     "ApiSettings:BaseUrl is not configured in appsettings.json");

builder.Services.AddHttpClient("GlmsApi", client =>
{
    client.BaseAddress = new Uri(apiBase.TrimEnd('/') + "/"); // Trailing slash ensures relative paths resolve correctly (Microsoft, 2026bf).
    client.DefaultRequestHeaders.Add("Accept", "application/json");
})
.AddHttpMessageHandler<JwtAttachHandler>(); // Attaches the JWT to every request made through this client (Chapsas, 2020).

// Scoped: a new instance is created once per HTTP request for both services (Microsoft, 2026e).
builder.Services.AddScoped<ContractObserverService>(); // Notifies observers when contract state changes (Refactoring Guru, 2024c).
builder.Services.AddScoped<CurrencyContext>(); // Holds the active currency strategy for the request (Refactoring Guru, 2024a).

// Session stores the JWT and login state between requests (Microsoft, 2026j).
builder.Services.AddDistributedMemoryCache(); // Required backing store for AddSession (Microsoft, 2026v).
builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromMinutes(20); // Session expires after 20 minutes of inactivity (Microsoft, 2026j).
    options.Cookie.HttpOnly = true; // Prevents client-side JavaScript from accessing the session cookie (Microsoft, 2026b).
    options.Cookie.IsEssential = true; // Cookie is written even when the user has not consented to non-essential cookies (Microsoft, 2026j).
});

var app = builder.Build();

// Only use the custom error page and HSTS in production; in development the developer exception page is used instead (Microsoft, 2026bc).
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts(); // Tells browsers to use HTTPS for all future requests (Microsoft, 2026a).
}

app.UseHttpsRedirection(); // Redirects HTTP requests to HTTPS (Microsoft, 2026a).
app.UseRouting(); // Matches incoming requests to controller routes (Microsoft, 2026f).
app.UseSession(); // Must be called before UseAuthorization so the JWT is available when the request is authorised (Microsoft, 2026j).
app.UseAuthorization(); // Enforces [Authorize] attributes on controllers and actions (Microsoft, 2026a).
app.MapStaticAssets(); // Serves CSS, JS, and image files from wwwroot (Microsoft, 2026h).

// Default route: controller=Home, action=Index, optional id segment (Microsoft, 2026h).
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}")
    .WithStaticAssets();

app.Run();

//@*(Chapsas, 2020; Chapsas, 2024a; Codewrinkles, 2024; Microsoft, 2026a; Microsoft, 2026b; Microsoft, 2026d; Microsoft, 2026e; Microsoft, 2026f; Microsoft, 2026h; Microsoft, 2026j; Microsoft, 2026v; Microsoft, 2026aj; Microsoft, 2026an; Microsoft, 2026bf; OpenExchangeRates, 2024; Refactoring Guru, 2024a; Refactoring Guru, 2024c; The IIE, 2026)

/*References

 - Chapsas, N., 2020. Resilient HttpClient using Polly and IHttpClientFactory in .NET Core 3. [video online] Available at: <https://www.youtube.com/watch?v=9pgvX_Dk0n8> [Accessed 2 June 2026].
 - Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online] Available at: <https://www.youtube.com/watch?v=3XoXzEPHdTA> [Accessed 2 June 2026].
 - Codewrinkles, 2024. All You Need To Know About IHttpClientFactory in ASP.NET Core. [video online] Available at: <https://www.youtube.com/watch?v=xI6uMT0bg4I> [Accessed 2 June 2026].
 - Microsoft, 2026a. ASP.NET Core Authentication Overview. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authentication/?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026b. ASP.NET Core Anti-Request Forgery. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/anti-request-forgery?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026d. ASP.NET Core Configuration. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/configuration/?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026f. ASP.NET Core Fundamentals - Routing. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/routing?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026h. ASP.NET Core MVC Controller Routing. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/controllers/routing?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026j. ASP.NET Core Session and State Management. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/app-state?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026v. Distributed Caching in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/performance/caching/distributed?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026aj. IHttpClientFactory in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/httpclient-factory> [Accessed 1 June 2026].
 - Microsoft, 2026an. JWT Authentication in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authentication/jwt-authn?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026bf. Uri Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.uri?view=net-10.0> [Accessed 1 June 2026].
 - OpenExchangeRates, 2024. Open Exchange Rates API Documentation. [online] Available at: <https://docs.openexchangerates.org/reference/api-introduction> [Accessed 1 June 2026].
 - Refactoring Guru, 2024a. Strategy Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/strategy> [Accessed 15 April 2026].
 - Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].
 - The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
 - Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.

 */