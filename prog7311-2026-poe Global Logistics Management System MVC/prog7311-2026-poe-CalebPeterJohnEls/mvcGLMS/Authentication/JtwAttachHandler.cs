// Custom DelegatingHandler that intercepts outgoing HTTP requests and
// automatically attaches a JWT token before the request is sent to the API
// (Microsoft, 2022; Microsoft, 2026u; Chapsas, 2020).

public class JwtAttachHandler : DelegatingHandler
{
    private readonly IHttpContextAccessor _accessor;

    // Constructor injection is used to obtain access to the current
    // HttpContext through ASP.NET Core's dependency injection container
    // (Microsoft, 2026e; Microsoft, 2026ak; Tiwari, 2024).
    public JwtAttachHandler(IHttpContextAccessor accessor)
    {
        _accessor = accessor;
    }

    // Overrides SendAsync to inspect and modify outgoing requests before
    // forwarding them through the HttpClient pipeline
    // (Microsoft, 2022; Chapsas, 2020; Codewrinkles, 2024).
    protected override Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request,
        CancellationToken cancellationToken)
    {
        // Retrieves the JWT token stored in session after user login
        // (Microsoft, 2026j; Microsoft, 2026an; Chapsas, 2024a).
        var token = _accessor.HttpContext?.Session.GetString("JwtToken");

        // Adds the JWT as a Bearer token in the Authorization header so
        // protected API endpoints can authenticate the current user
        // (Microsoft, 2026an; Chapsas, 2024a).
        if (!string.IsNullOrEmpty(token))
        {
            request.Headers.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue(
                    "Bearer",
                    token);
        }

        // Passes the request to the next handler in the processing pipeline
        // (Microsoft, 2022; Microsoft, 2026u; Troelsen and Japikse, 2022).
        return base.SendAsync(request, cancellationToken);
    }
}
/*
References

Chapsas, N., 2020. Resilient HttpClient using Polly and IHttpClientFactory in .NET Core 3.
[video online] Available at: <https://www.youtube.com/watch?v=9pgvX_Dk0n8>
[Accessed 2 June 2026].

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core.
[video online] Available at: <https://www.youtube.com/watch?v=3XoXzEPHdTA>
[Accessed 2 June 2026].

Codewrinkles, 2024. All You Need To Know About IHttpClientFactory in ASP.NET Core.
[video online] Available at: <https://www.youtube.com/watch?v=xI6uMT0bg4I>
[Accessed 2 June 2026].

Microsoft, 2022. HTTP Message Handlers in ASP.NET Web API.
[online] Available at:
<https://learn.microsoft.com/en-us/aspnet/web-api/overview/advanced/http-message-handlers>
[Accessed 4 June 2026].

Microsoft, 2026e. ASP.NET Core Dependency Injection.
[online] Available at:
<https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection/?view=aspnetcore-10.0>
[Accessed 1 June 2026].

Microsoft, 2026j. ASP.NET Core Session and State Management.
[online] Available at:
<https://learn.microsoft.com/en-us/aspnet/core/fundamentals/app-state?view=aspnetcore-10.0>
[Accessed 1 June 2026].

Microsoft, 2026ak. IHttpContextAccessor Interface.
[online] Available at:
<https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.http.ihttpcontextaccessor?view=aspnetcore-9.0>
[Accessed 1 June 2026].

Microsoft, 2026an. JWT Authentication in ASP.NET Core.
[online] Available at:
<https://learn.microsoft.com/en-us/aspnet/core/security/authentication/jwt-authn?view=aspnetcore-10.0>
[Accessed 1 June 2026].

Microsoft, 2026u. DelegatingHandler Class.
[online] Available at:
<https://learn.microsoft.com/en-us/dotnet/api/system.net.http.delegatinghandler?view=net-10.0>
[Accessed 1 June 2026].

Tiwari, N., 2024. ASP.NET Core Dependency Injection | C# Dependency Injection Tutorial | DI in C#.
[video online] Available at:
<https://www.youtube.com/watch?v=GoDmm-4yYPg>
[Accessed 2 June 2026].

Troelsen, A. and Japikse, P., 2022.
Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming.
8th ed. New York: Apress.
*/