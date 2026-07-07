using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PROG2b_POE_MVC.Models;

namespace PROG2b_POE_MVC.Controllers
{
    public class CoordinatorController : Controller
    {
        private readonly ApplicationDbContext _context;
        private readonly IWebHostEnvironment _env;

        //private const string Base64Key = "iKwxciCzDX6mWEll+c2Ko2xFUaT2TlwbmMfWLY+kX/4=";

        public CoordinatorController(ApplicationDbContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        // GET: Displays a list of all claims in the system
        public async Task<IActionResult> Index()
        {
            // Retrieve all claim records from the database asynchronously
            var claims = await _context.Claims.ToListAsync();

            // Pass the claims list to the view for display
            return View(claims);
        }

        // Displays the verification page for a specific claim based on ID
        public async Task<IActionResult> Verification(int? id)
        {
            // If no ID was provided in the URL, return a Not Found response
            if (id == null)
            {
                return NotFound();
            }

            // Retrieve the claim with the provided ID from the database
            var claim = await _context.Claims.FindAsync(id);

            // If the claim does not exist, return 404 Not Found
            if (claim == null)
            {
                return NotFound();
            }

            // Pass the claim object to the Verification view for display and editing
            return View(claim);
        }

        // Handles the submission of the verification form by the coordinator
        [HttpPost]
        [ValidateAntiForgeryToken] // Protects against Cross-Site Request Forgery attacks
        public async Task<IActionResult> Verification(int id, [Bind("Claim_ID,Verified,Comment")] Claims updatedClaim)
        {
            // Ensure the ID in the URL matches the claim ID in the form submission
            if (id != updatedClaim.Claim_ID)
            {
                return NotFound();
            }

            // Retrieve the existing claim record from the database
            var existingClaim = await _context.Claims.FindAsync(id);
            if (existingClaim == null)
            {
                return NotFound();
            }

            // Update the claim’s verification status and coordinator comment
            existingClaim.Verified = updatedClaim.Verified;
            existingClaim.Comment = updatedClaim.Comment;

            try
            {
                // Save the updated claim back to the database
                _context.Update(existingClaim);
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                // Handle concurrency exceptions if another process modified the claim simultaneously
                if (!_context.Claims.Any(e => e.Claim_ID == id))
                {
                    return NotFound(); // Claim was deleted before update
                }
                else
                {
                    throw; // Re-throw the exception for other unhandled issues
                }
            }

            // After successful update, redirect back to the list of claims
            return RedirectToAction(nameof(Index));
        }
    }
}
//@*(Troelsen, and Japikse, 2022)*@
/*References
Bootstrap, 2025a. Grid system. [online] Available at: https://getbootstrap.com/docs/5.0/layout/grid [Accessed 19 October 2025].
Bootstrap, 2025b. Flex utilities. [online] Available at: https://getbootstrap.com/docs/5.0/utilities/flex [Accessed 19 October 2025].
C# Corner, 2025. Comprehensive guide to unit testing with MSTest in C#. [online] Available at: https://www.c-sharpcorner.com/article/comprehensive-guide-to-unit-testing-with-mstest-in-c-sharp [Accessed 18 October 2025].
Microsoft Learn, 2025a. Reserved keywords (Transact-SQL). [online] Available at: https://learn.microsoft.com/en-us/sql/t-sql/language-elements/reserved-keywords-transact-sql?view=sql-server-ver17 [Accessed 20 October 2025].
Microsoft Learn, 2025b. T-SQL naming issues - SQL code analysis. [online] Available at: https://learn.microsoft.com/en-us/sql/tools/sql-database-projects/concepts/sql-code-analysis/t-sql-naming-issues?view=sql-server-ver17 [Accessed 20 October 2025].
Microsoft Learn, 2025c. Unit testing C# code with MSTest - Writing tests and assertions. [online] Available at: https://learn.microsoft.com/en-us/dotnet/core/testing/unit-testing-mstest-writing-tests-assertions [Accessed 20 October 2025].
Stack Overflow, 2021. Uniquely identify the extension change from Excel to Word in ASP.NET C# file. [online] Available at: https://stackoverflow.com/questions/43344606/uniquely-idenitfy-the-extension-change-from-excel-to-word-in-asp-net-c-sharp-fil [Accessed 10 October 2025].
Stack Overflow, 2020. Unit testing C# properties. [online] Available at: https://stackoverflow.com/questions/5876779/unit-testing-c-sharp-properties [Accessed 20 October 2025].
Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
Tutorial Republic, 2025. How to center a column in Bootstrap. [online] Available at: https://www.tutorialrepublic.com/faq/how-to-center-a-column-in-bootstrap.php [Accessed 19 October 2025].
*/


