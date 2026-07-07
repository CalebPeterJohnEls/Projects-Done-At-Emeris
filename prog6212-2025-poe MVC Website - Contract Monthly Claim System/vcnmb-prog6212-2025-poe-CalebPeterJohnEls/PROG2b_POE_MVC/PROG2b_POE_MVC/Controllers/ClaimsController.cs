using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.StaticFiles;
using Microsoft.EntityFrameworkCore;

using PROG2b_POE_MVC.Models;
using System.Security.Cryptography;

namespace PROG2b_POE_MVC.Controllers
{
    public class ClaimsController : Controller
    {
        private readonly ApplicationDbContext _context;
        private readonly IWebHostEnvironment _env;
        // Encryption key used to secure uploaded files (Base64 encoded AES key)
        private const string Base64Key = "iKwxciCzDX6mWEll+c2Ko2xFUaT2TlwbmMfWLY+kX/4=";

        // Constructor - dependency injection for database and environment
        public ClaimsController(ApplicationDbContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        // Displays a list of all submitted claims
        public async Task<IActionResult> Index()
        {
            var loggedInUsername = HttpContext.Session.GetString("Username");
            var claims = await _context.Claims.Where(c => c.User == loggedInUsername).ToListAsync(); // Retrieve all claim records of the user logded in
            return View(claims); // Send data to the Index view
        }

        // Displays the form for creating a new claim
        [HttpGet]
        public IActionResult Create()
        {
            var claim = new Claims();

            //If a user is logged in, their username, name, surname and hourly rate will auto inputs
            var loggedInUsername = HttpContext.Session.GetString("Username");

            if (!string.IsNullOrEmpty(loggedInUsername))
            {

                var currentUser = _context.Users.FirstOrDefault(u => u.Username == loggedInUsername);

                if (currentUser != null)
                {
                    claim.User = currentUser.Username;
                    claim.Hourly_Rate = currentUser.HourlyRate;
                    claim.Name = currentUser.Name;
                    claim.Surname = currentUser.Surname;
                }
            }
            //sets defualt dates
            claim.Start_Date = DateTime.Today;
            claim.End_Date = DateTime.Today;

            return View(claim);
        }

        //  Handles the creation and encryption of uploaded claim documents
        [HttpPost]
        public async Task<IActionResult> Create(Claims claim)
        {
            //set variables
            var document = claim.Document;
            var comment = claim.Comment;

            var maxHours = 180;//set max hours


            //If no date is added
            if (claim.Start_Date == default)
            {
                ModelState.AddModelError("Start_Date", "Please choose a start date.");
            }

            if (claim.End_Date == default)
            {
                ModelState.AddModelError("End_Date", "Please choose an end date.");
            }
            
            if (claim.Start_Date != default && claim.End_Date != default)
            {
                // End date cannot be before start date
                if (claim.End_Date < claim.Start_Date)
                {
                    ModelState.AddModelError("End_Date", "End Date cannot be earlier than Start Date.");
                }

                // Start and end date must be in the same month
                if (claim.Start_Date.Month != claim.End_Date.Month || claim.Start_Date.Year != claim.End_Date.Year)
                {
                    ModelState.AddModelError("End_Date", "Start Date and End Date must be in the same month.");
                }
            }
            //(Microsoft, 2025d;Microsoft, 2025e)


            //If total hours is more than max hours
            if (claim.Total_Hours > maxHours)
            {
                ModelState.AddModelError("Total_Hours", $"Total hours cannot exceed {maxHours} hours.");
            }


            // Calculate total amount
            claim.Total_Amount = claim.Total_Hours * claim.Hourly_Rate; // (Stack Overflow, 2012)
            
            //If no comment is added
            if (comment == null)
            {
                ModelState.AddModelError("Comment", "Please give additional information.");
            }
            // Define maximum allowed file size (10 MB)
            long fileLimit = 10 * 1024 * 1024;

          
            //If no file is added
            if (document == null)
            {
                ModelState.AddModelError("Document", "Please upload a file.");
               
            }
            else
            {//only allows specific files to be uploaded
                bool validType =
                    document.ContentType.Equals("application/pdf", StringComparison.OrdinalIgnoreCase) ||
                    document.ContentType.Equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", StringComparison.OrdinalIgnoreCase) ||
                    document.ContentType.Equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", StringComparison.OrdinalIgnoreCase);

                if (!validType)
                {
                    ModelState.AddModelError("Document", "File must be PDF, Word, or Excel.");
               
                }

                if (document.Length > fileLimit)
                {
                    ModelState.AddModelError("Document", "File must be smaller than 10MB.");
                
                }
            }

            // If date validation fails, return the same view so errors show
            if (!ModelState.IsValid)
                return View(claim);

            var uploadsFolder = Path.Combine(_env.WebRootPath, "claimDocs");
            Directory.CreateDirectory(uploadsFolder);

            // Prepare file paths for encryption
            var originalName = Path.GetFileName(document.FileName); // Keep the original filename
            var encryptedPath = Path.Combine(uploadsFolder, originalName + ".enc"); // Encrypted file path

            // Read the file content into a byte array
            byte[] plainBytes;
            using (var ms = new MemoryStream())
            {
                await document.CopyToAsync(ms);
                plainBytes = ms.ToArray();
            }

            // Convert encryption key from Base64 to byte array
            var key = Convert.FromBase64String(Base64Key);
            var iv = RandomNumberGenerator.GetBytes(16); // Generate a random IV (Initialization Vector)

            // Encrypt the uploaded document using AES (CBC mode, PKCS7 padding)
            using (var aes = Aes.Create())
            {
                aes.Key = key;
                aes.IV = iv;
                aes.Mode = CipherMode.CBC;
                aes.Padding = PaddingMode.PKCS7;

                using var encryptor = aes.CreateEncryptor();
                var cipherBytes = encryptor.TransformFinalBlock(plainBytes, 0, plainBytes.Length);

                // Combine IV + encrypted data into one file and save it
                await System.IO.File.WriteAllBytesAsync(encryptedPath, iv.Concat(cipherBytes).ToArray());
            }

            // Store original file name reference in the database
            claim.DocumentPath = originalName;


            // Add timestamp and save claim to the database
            claim.Timestamp = DateTimeOffset.Now;
            _context.Claims.Add(claim);
            await _context.SaveChangesAsync();

            // Redirect back to the claims list
            return RedirectToAction(nameof(Index));
        }


        // Decrypts and opens an encrypted file
        public async Task<IActionResult> OpenFile(string fileName)
        {
            if (string.IsNullOrWhiteSpace(fileName))
                return NotFound();

            var uploadsFolder = Path.Combine(_env.WebRootPath, "claimDocs");
            var encryptedPath = Path.Combine(uploadsFolder, fileName + ".enc");

            // Ensure file exists before attempting to decrypt
            if (!System.IO.File.Exists(encryptedPath))
                return NotFound();

            // Read encrypted bytes
            var data = await System.IO.File.ReadAllBytesAsync(encryptedPath);
            if (data.Length < 16)
                return BadRequest("File is corrupted.");

            // Extract IV (first 16 bytes) and ciphertext
            var iv = new byte[16];
            Buffer.BlockCopy(data, 0, iv, 0, 16);
            var cipher = new byte[data.Length - 16];
            Buffer.BlockCopy(data, 16, cipher, 0, cipher.Length);

            // Decrypt the file using AES
            var key = Convert.FromBase64String(Base64Key);
            byte[] plain;
            using (var aes = Aes.Create())
            {
                aes.Key = key;
                aes.IV = iv;
                aes.Mode = CipherMode.CBC;
                aes.Padding = PaddingMode.PKCS7;

                using var decryptor = aes.CreateDecryptor();
                plain = decryptor.TransformFinalBlock(cipher, 0, cipher.Length);
            }

            // Detect type based on file extension (e.g., pdf, docx, xlsx)
            var provider = new FileExtensionContentTypeProvider();
            if (!provider.TryGetContentType(fileName, out var contentType))
                contentType = "application/octet-stream"; // Default fallback

            // Return the decrypted file to the browser for download or viewing
            return File(plain, contentType, fileName);
        }

        // Displays the claim’s current status (Approved, Pending, Rejected, etc.)
        public IActionResult ClaimStatus(int id)
        {
            // Retrieve the claim by ID from the database
            var claim = _context.Claims.FirstOrDefault(c => c.Claim_ID == id);
            if (claim == null)
            {
                return NotFound();
            }

            // Pass claim to the view
            return View(claim);
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
Microsoft, 2025d. DateTime.Compare(DateTime, DateTime) Method. [online] Available at: https://learn.microsoft.com/en-us/dotnet/api/system.datetime.compare?view=net-10.0 [Accessed 19 November 2025].
Microsoft, 2025e. DateTime.AddDays(Double) Method. [online] Available at: https://learn.microsoft.com/en-us/dotnet/api/system.datetime.adddays?view=net-10.0 [Accessed 19 November 2025].
Stack Overflow, 2012. How to make simple calculations using Model.Items and an input from a form in ASP.NET MVC. [online] Available at: https://stackoverflow.com/questions/8923352/how-to-make-simple-calculations-using-model-items-and-an-input-from-a-form-in-as [Accessed 18 November 2025].
Stack Overflow, 2021. Uniquely identify the extension change from Excel to Word in ASP.NET C# file. [online] Available at: https://stackoverflow.com/questions/43344606/uniquely-idenitfy-the-extension-change-from-excel-to-word-in-asp-net-c-sharp-fil [Accessed 10 October 2025].
Stack Overflow, 2020. Unit testing C# properties. [online] Available at: https://stackoverflow.com/questions/5876779/unit-testing-c-sharp-properties [Accessed 20 October 2025].
Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
Tutorial Republic, 2025. How to center a column in Bootstrap. [online] Available at: https://www.tutorialrepublic.com/faq/how-to-center-a-column-in-bootstrap.php [Accessed 19 October 2025].
*/
