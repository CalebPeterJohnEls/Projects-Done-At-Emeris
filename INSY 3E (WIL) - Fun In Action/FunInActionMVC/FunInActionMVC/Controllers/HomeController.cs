using FunInAction.Mvc.Filters;
using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services;
using Microsoft.AspNetCore.Mvc;

namespace FunInAction.Mvc.Controllers;

public class HomeController : Controller
{
    private readonly IFunInActionApiClient _api;

    public HomeController(IFunInActionApiClient api)
    {
        _api = api;
    }

    // GET /  and  GET /Home
    [RequireLogin("Coach")]   // CHANGED
    public async Task<IActionResult> Index(CancellationToken ct)
    {
        ViewData["Title"] = "Home";
        ViewData["PageCss"] = "style.css";
        ViewData["ActiveNav"] = "Home";
        ViewData["Portal"] = "Coach";

        var dashboard = await _api.GetCoachDashboardAsync(ct);
        return View(dashboard);
    }

    // GET /Home/About — CHANGED: client/player only now, no more spoofable ?portal= query param
    [RequireLogin("Client", "Player")]
    public IActionResult About()
    {
        ViewData["Title"] = "About";
        ViewData["PageCss"] = "style.css";
        ViewData["ActiveNav"] = "About";
        ViewData["Portal"] = "Parent";
        return View();
    }

    [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
    public IActionResult Error()
    {
        return View(new ErrorViewModel { RequestId = HttpContext.TraceIdentifier });
    }
}
