using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PROG2b_POE_MVC.Models;

namespace PROG2b_POE_MVC.Controllers
{
    public class AccountController : Controller
    {
        private readonly ApplicationDbContext _context;

        public AccountController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public IActionResult Login() => View();
        [HttpPost]
        public async Task<IActionResult> Login(Login model)
        {
            if (!ModelState.IsValid)
                return View(model);
            //Alows user to log in with their password and user name
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Username == model.Username && u.Password == model.Password);
            //if information is inccorect
            if (user == null)
            {
                ModelState.AddModelError(string.Empty, "Invalid username or password.");
                return View(model);
            }

            if (user != null)
            {
                // Store role in session so the app knows if user is Admin or Customer
                HttpContext.Session.SetString("Role", user.Role);

                // Store username in session to identify logged-in user
                HttpContext.Session.SetString("Username", user.Username);

                // Redirect user to the Home page after successful login
                return RedirectToAction("Index", "Home");
            }

            return RedirectToAction("Index", "Home");
        }
        [HttpPost]
        public IActionResult Logout()
        {
            //clears session when users logs out
            HttpContext.Session.Clear();
            return RedirectToAction("Index", "Home");
        }
    }
}
//@*(Troelsen, and Japikse, 2022)
/*References
 * Israel Quiroz, 2025. How to EASILY Create a PDF in C# .NET with QuestPDF! [video online] Available at: https://www.youtube.com/watch?v=3qEPv-67iRg [Accessed 20 November 2025].
 * Nick Chapsas, 2023. The Easiest Way to Create PDFs in .NET. [video online] Available at: https://www.youtube.com/watch?v=_M0IgtGWnvE&t=151s [Accessed 20 November 2025].
 *Programming with Felipe Gavilan, 2023. Generating PDFs with C# - Very Easy (two examples). [video online] Available at: https://www.youtube.com/watch?v=bhR4Cmg16gs [Accessed 20 November 2025].
 *Microsoft, 2025. IDocument Interface (org.w3c.dom). [online] Available at: https://learn.microsoft.com/en-us/dotnet/api/org.w3c.dom.idocument?view=net-android-35.0 [Accessed 20 November 2025].
 *QuestPDF, 2025a. API Reference. [online]Available at: https://www.questpdf.com/api-reference [Accessed 20 November 2025].
 *QuestPDF, 2025b. Document Operations. [online] Available at: https://www.questpdf.com/concepts/document-operations.html [Accessed 20 November 2025].
 *QuestPDF, 2025c. Generating Output. [online] Available at: https://www.questpdf.com/concepts/generating-output.html [Accessed 20 November 2025].
 *Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.*/