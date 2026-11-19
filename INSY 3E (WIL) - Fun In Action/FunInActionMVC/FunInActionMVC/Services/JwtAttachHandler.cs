using System.Net.Http.Headers;

namespace FunInAction.Mvc.Services;

// Attaches the JWT stored in session to every outgoing API request.
public class JwtAttachHandler : DelegatingHandler
{
    private readonly IHttpContextAccessor _accessor;

    public JwtAttachHandler(IHttpContextAccessor accessor) => _accessor = accessor;

    protected override Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request, CancellationToken cancellationToken)
    {
        var token = _accessor.HttpContext?.Session.GetString("JwtToken");
        if (!string.IsNullOrEmpty(token))
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

        return base.SendAsync(request, cancellationToken);
    }
}