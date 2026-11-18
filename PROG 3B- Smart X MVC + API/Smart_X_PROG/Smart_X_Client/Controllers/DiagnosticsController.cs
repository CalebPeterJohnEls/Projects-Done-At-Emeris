using Microsoft.AspNetCore.Mvc;
using Smart_X_Client.Models;
using Smart_X_Client.Services;

// DiagnosticsController: Renders a demonstration page that fetches a sample
// deployment tree from the API, validates it, and also demonstrates
// aggregating two Meter readings via the overloaded '+' operator (see
// Smart_X_API Models/Meter.cs and Microsoft, 2026q). Data is passed to the
// view using ViewBag (Microsoft, 2026r).

namespace Smart_X_Client.Controllers
{
    public class DiagnosticsController : Controller
    {
        private readonly ApiService _apiService;

        public DiagnosticsController(ApiService apiService)
        {
            _apiService = apiService;
        }

        // GET /Diagnostics
        // Loads the sample tree and its validation result, then passes both
        // to the view via ViewBag so the Razor page can render them
        // (Microsoft, 2026r).
        public async Task<IActionResult> Index()
        {
            var tree = await _apiService.GetSampleDeploymentTreeAsync();
            var validation = tree != null
                ? await _apiService.ValidateDeploymentAsync(tree)
                : (false, "No sample tree returned.");

            ViewBag.Tree = tree;
            ViewBag.IsValid = validation.Item1;
            ViewBag.ValidationMessage = validation.Item2;

            return View();
        }

        // POST /Diagnostics/AggregateMeters
        // Builds two Meter objects from form input and asks the API to
        // combine them (which internally uses Meter's '+' operator
        // overload, Microsoft, 2026q), then re-renders the Index view with
        // the result attached.
        [HttpPost]
        public async Task<IActionResult> AggregateMeters(
            string firstId, double firstValue,
            string secondId, double secondValue)
        {
            var first = new Meter { DeviceIdentifier = firstId, Value = firstValue };
            var second = new Meter { DeviceIdentifier = secondId, Value = secondValue };

            var result = await _apiService.AggregateMetersAsync(first, second);

            ViewBag.AggregateResult = result;

            var tree = await _apiService.GetSampleDeploymentTreeAsync();
            var validation = tree != null
                ? await _apiService.ValidateDeploymentAsync(tree)
                : (false, "No sample tree returned.");

            ViewBag.Tree = tree;
            ViewBag.IsValid = validation.Item1;
            ViewBag.ValidationMessage = validation.Item2;

            return View(nameof(Index));
        }
    }
}

// References:
// Microsoft, 2026q. Operator overloading. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/operator-overloading> [Accessed 8 September 2026].
// Microsoft, 2026r. Pass data to views in ASP.NET Core MVC. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/views/overview> [Accessed 9 September 2026].
/*IIE Emeris School of Computer Science, 2025a. CLDV6211 -
Playlist. [video online] Available at:
<https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK>
[Accessed 18 March 2025].



IIE Emeris School of Computer Science, 2025b. CLDV6212 -
Playlist. [video online] Available at:
<https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7>
[Accessed 20 September 2025].



Nakov, S. and Kolev, V. et al., 2013. Fundamentals of
Computer Programming with C#. Sofia. [ebook] Available at:
<https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf>
[Accessed 9 September 2026].



The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The
Independent Institute of Education: Unpublished.



Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6:
Foundational Principles and Practices in Programming. 8th ed. New York: Apress.*/