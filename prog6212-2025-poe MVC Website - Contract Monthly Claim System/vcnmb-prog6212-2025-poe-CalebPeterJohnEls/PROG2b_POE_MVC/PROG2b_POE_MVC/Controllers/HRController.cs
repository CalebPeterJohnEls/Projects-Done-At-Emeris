using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PROG2b_POE_MVC.Models;
using QuestPDF.Fluent;

namespace PROG2b_POE_MVC.Controllers
{
    public class HRController : Controller
    {

        private readonly ApplicationDbContext _context;
        private readonly IWebHostEnvironment _env;



        public HRController(ApplicationDbContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        //Displays a list of all claims and users in the system
        public async Task<IActionResult> Index()
        {

            //Calls all users and claims to view in the HR Index
            var viewModel = new HR
            {
                Claims = await _context.Claims.ToListAsync(),
                Users = await _context.Users.ToListAsync()
            };

            return View(viewModel);
        }
        // Shows the registration form
        [HttpGet]
        public IActionResult RegisterUser() => View();

        // Handles form submission
        [HttpPost]
        public async Task<IActionResult> RegisterUser(Register model)
        {
            // If frontend validation fails, reload form with errors
            if (!ModelState.IsValid)
                return View(model);

            // Check if a user already exists with the same username or email
            var existingUser = await _context.Users
                .FirstOrDefaultAsync(u => u.Username == model.Username || u.Email == model.Email);

            // If found, return error message to the form
            if (existingUser != null)
            {
                ModelState.AddModelError(string.Empty, "Username or email already exists.");
                return View(model);
            }

            // Create a new User object from the form data
            var newUser = new User
            {
                Username = model.Username,
                Name = model.Name,
                Surname = model.Surname,
                Email = model.Email,
                Password = model.Password, // In real apps, this should be hashed
                Role = model.Role,
                HourlyRate = model.HourlyRate
            };

            // Add user to the database
            _context.Users.Add(newUser);
            await _context.SaveChangesAsync();

            // Redirect to homepage or user list after successful registration
            return RedirectToAction("Index");
        }

        //edits user data
        [HttpGet]
        public async Task<IActionResult> EditUser(string username)
        {
            if (username == null)
                return NotFound();
            //aouto inputs users information
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Username == username);

            if (user == null)
                return NotFound();

            return View(user);
        }
        
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> EditUser(string username, User updatedUser)
        {
            if (username != updatedUser.Username)
                return NotFound();

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(updatedUser);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!_context.Users.Any(e => e.Username == username))
                        return NotFound();
                    else
                        throw;
                }

                return RedirectToAction(nameof(Index));
            }

            return View(updatedUser);
        }
        //deletes Claim
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteClaim(int id)
        {
            var claim = await _context.Claims.FindAsync(id);

            if (claim != null)
            {
                _context.Claims.Remove(claim);
                await _context.SaveChangesAsync();
            }

            return RedirectToAction("Index");
        }
        //Deletes User
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteUser(string username)
        {
            var user = await _context.Users.FindAsync(username);

            if (user != null)
            {
                _context.Users.Remove(user);
                await _context.SaveChangesAsync();
            }

            return RedirectToAction("Index");
        }
        //Genetares report
        public async Task<IActionResult> GenerateReport()
        {
            //uses all approved claims
            var approvedClaims = await _context.Claims
            .Where(c => c.Status == "Approved")
            .ToListAsync();
            
            //makes document a pdf
            var document = new ClaimPDFDocument(approvedClaims);
            var pdfBytes = document.GeneratePdf();

            //File path for the document to save
            var uploadsFolder = Path.Combine(_env.WebRootPath, "Reports");
            //Creates folder
            Directory.CreateDirectory(uploadsFolder);
            var filePath = Path.Combine(uploadsFolder, "ApprovedClaimsReport.pdf");

            await System.IO.File.WriteAllBytesAsync(filePath, pdfBytes);

            return File(pdfBytes, "application/pdf", "ApprovedClaimsReport.pdf");
            /*(Chapsas, 2023; Gavilan, 2023; Microsoft, 2025; QuestPDF, 2025a;
             * QuestPDF, 2025b; QuestPDF, 2025c; Quiroz, 2025)*/
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
