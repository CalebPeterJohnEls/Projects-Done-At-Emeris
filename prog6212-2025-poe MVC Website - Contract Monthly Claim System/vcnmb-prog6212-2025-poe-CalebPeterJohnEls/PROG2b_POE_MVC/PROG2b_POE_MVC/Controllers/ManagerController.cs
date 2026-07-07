using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PROG2b_POE_MVC.Models;

namespace PROG2b_POE_MVC.Controllers
{
    public class ManagerController : Controller
    {
        private readonly ApplicationDbContext _context; // Used for database operations
        private readonly IWebHostEnvironment _env; // Provides access to web hosting environment (e.g., file paths)

        // Constructor initializes the controller with database context and environment
        public ManagerController(ApplicationDbContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        // Displays a list of claims that have already been verified and approved by the coordinator
        public async Task<IActionResult> Index()
        {
            // Fetch all claims from the database where the "Verified" field equals "Approved"
            var approvedClaims = await _context.Claims
                .Where(c => c.Verified == "Approved")
                .ToListAsync();

            // Pass the approved claims list to the Index view for display
            return View(approvedClaims);
        }

        // GET: Displays the approval page for a specific claim
        public async Task<IActionResult> Approval(int? id)
        {
            // If no claim ID is provided, return a 404 Not Found result
            if (id == null)
            {
                return NotFound();
            }

            // Look for the claim with the given ID in the database
            var claim = await _context.Claims.FindAsync(id);

            // If the claim cannot be found, return 404 Not Found
            if (claim == null)
            {
                return NotFound();
            }

            // Send the claim object to the Approval view for display and editing
            return View(claim);
        }

        // Handles the submission of the approval form
        [HttpPost]
        [ValidateAntiForgeryToken] // Helps prevent cross-site request forgery (CSRF) attacks
        public async Task<IActionResult> Approval(int id, [Bind("Claim_ID,Status")] Claims updatedClaim)
        {
            // Ensure the route ID matches the claim ID from the form to prevent tampering
            if (id != updatedClaim.Claim_ID)
            {
                return NotFound();
            }

            // Retrieve the existing claim from the database
            var existingClaim = await _context.Claims.FindAsync(id);
            if (existingClaim == null)
            {
                return NotFound();
            }

            // Update the claim’s status and manager comment with new values from the form
            existingClaim.Status = updatedClaim.Status;
            

            try
            {
                // Mark the claim as modified and save changes to the database
                _context.Update(existingClaim);
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                // Handle concurrency issues (if another user modified the claim at the same time)
                if (!_context.Claims.Any(e => e.Claim_ID == id))
                {
                    return NotFound(); // Claim was deleted during update
                }
                else
                {
                    throw; // Re-throw exception if it’s a different issue
                }
            }

            // After saving, redirect the manager back to the Index page (list of approved claims)
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

