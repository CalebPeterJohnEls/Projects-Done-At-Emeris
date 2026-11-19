using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace FunInAction.Mvc.Filters;

/// <summary>
/// CHANGED: now takes zero or more allowed roles instead of a single Role property, so one attribute
/// can cover "Client OR Player" as well as the old single-role and any-login cases.
///   [RequireLogin]                    -> any signed-in user
///   [RequireLogin("Coach")]           -> Coach only (Admin always allowed too)
///   [RequireLogin("Client","Player")] -> Client or Player only (Admin always allowed too)
/// Not signed in -> redirected to /Account/Login (with returnUrl, so AccountController can send them
/// straight back). Signed in but wrong role -> redirected to their OWN home, not an error page.
/// </summary>
public class RequireLoginAttribute : Attribute, IActionFilter
{
    private readonly string[] _roles;

    public RequireLoginAttribute(params string[] roles) => _roles = roles;

    public void OnActionExecuting(ActionExecutingContext context)
    {
        var session = context.HttpContext.Session;
        var token = session.GetString("JwtToken");
        var role = session.GetString("Role");

        if (string.IsNullOrEmpty(token))
        {
            var returnUrl = context.HttpContext.Request.Path + context.HttpContext.Request.QueryString;
            context.Result = new RedirectToActionResult("Login", "Account", new { returnUrl = returnUrl.ToString() });
            return;
        }

        // Admin (nobody can self-register as this — see AuthController on the API) can go anywhere.
        var roleOk = _roles.Length == 0 || role == "Admin" || _roles.Contains(role);
        if (!roleOk)
        {
            var (controller, action) = role == "Coach" ? ("Home", "Index") : ("Portal", "Home");
            context.Result = new RedirectToActionResult(action, controller, null);
        }
    }

    public void OnActionExecuted(ActionExecutedContext context) { }
}
