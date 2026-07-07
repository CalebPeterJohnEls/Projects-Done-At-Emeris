using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using mvcGLMS.LogisticsComposite;
using mvcGLMS.Models;
using System.Net.Http.Json;

namespace mvcGLMS.Controllers
{
    // MVC Controller for managing Contracts including file upload and date/status filtering
    // Uses IWebHostEnvironment to resolve the wwwroot path for PDF storage (Microsoft, 2026al)
    public class ContractsController : Controller
    {
        private readonly HttpClient _http;
        private readonly IWebHostEnvironment _env;

        // Named HttpClient and IWebHostEnvironment injected via DI (Microsoft, 2026e; Microsoft, 2026al)
        public ContractsController(IHttpClientFactory factory, IWebHostEnvironment env)
        {
            _http = factory.CreateClient("GlmsApi");
            _env  = env;
        }

        // GET: fetches contracts from the API with optional date range and status filters
        // Query string parameters are appended manually before the API call (Stack Overflow, 2010b)
        public async Task<IActionResult> Index(DateTime? startDate, DateTime? endDate, string? statusFilter)
        {
            var url = "contracts?";
            if (startDate.HasValue)    url += $"startDate={startDate:yyyy-MM-dd}&";
            if (endDate.HasValue)      url += $"endDate={endDate:yyyy-MM-dd}&";
            if (!string.IsNullOrEmpty(statusFilter)) url += $"status={statusFilter}";

            var contracts = await _http.GetFromJsonAsync<List<Contract>>(url);
            ViewBag.StatusFilter = statusFilter;
            return View(contracts ?? new List<Contract>());
        }

        // GET: renders the create form with a client dropdown (Microsoft, 2026av)
        public async Task<IActionResult> Create()
        {
            var clients = await _http.GetFromJsonAsync<List<Client>>("clients");
            ViewData["ClientId"] = new SelectList(clients ?? new List<Client>(), "Id", "Name");
            return View();
        }

        // POST: validates the uploaded PDF, saves it to wwwroot/uploads, creates the contract
        // via the API, then patches the signed agreement path onto the created record
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(Contract contract, IFormFile? upload)
        {
            long fileLimit = 10 * 1024 * 1024;

            // Validate that a file was provided and that it is a PDF under 10MB (Microsoft, 2026n)
            if (upload == null)
                ModelState.AddModelError("upload", "Please upload a file.");
            else
            {
                if (!upload.ContentType.Equals("application/pdf", StringComparison.OrdinalIgnoreCase))
                    ModelState.AddModelError("upload", "File must be PDF.");
                if (upload.Length > fileLimit)
                    ModelState.AddModelError("upload", "File must be smaller than 10MB.");
            }

            if (!ModelState.IsValid)
            {
                var clients = await _http.GetFromJsonAsync<List<Client>>("clients");
                ViewData["ClientId"] = new SelectList(clients ?? new List<Client>(), "Id", "Name", contract.ClientId);
                return View(contract);
            }

            // Save the PDF to wwwroot/uploads with a GUID prefix to prevent filename collisions
            // (Microsoft, 2026af; Microsoft, 2026aq)
            if (upload != null && upload.Length > 0)
            {
                var uploadsFolder = Path.Combine(_env.WebRootPath, "uploads");
                if (!Directory.Exists(uploadsFolder))
                    Directory.CreateDirectory(uploadsFolder);

                var filename = Guid.NewGuid() + "_" + upload.FileName;
                var filePath = Path.Combine(uploadsFolder, filename);

                using (var stream = new FileStream(filePath, FileMode.Create))
                    await upload.CopyToAsync(stream);

                contract.SignedAgreementPath = "/uploads/" + filename;
            }

            // Post the contract to the API; on success patch the file path onto the new record
            var response = await _http.PostAsJsonAsync("contracts", contract);

            if (response.IsSuccessStatusCode)
            {
                var created = await response.Content.ReadFromJsonAsync<Contract>();

                // PATCH is used here to update only the agreement path without resending the full contract
                // (Microsoft, 2026m)
                if (created != null && !string.IsNullOrEmpty(contract.SignedAgreementPath))
                    await _http.PatchAsJsonAsync($"contracts/{created.Id}/agreement-path", contract.SignedAgreementPath);

                return RedirectToAction(nameof(Index));
            }

            ModelState.AddModelError("", "Failed to create contract.");
            var clientList = await _http.GetFromJsonAsync<List<Client>>("clients");
            ViewData["ClientId"] = new SelectList(clientList ?? new List<Client>(), "Id", "Name", contract.ClientId);
            return View(contract);
        }

        // GET: loads the contract and client dropdown for the edit form
        public async Task<IActionResult> Edit(int id)
        {
            var contract = await _http.GetFromJsonAsync<Contract>($"contracts/{id}");
            if (contract == null) return NotFound();

            var clients = await _http.GetFromJsonAsync<List<Client>>("clients");
            ViewData["ClientId"] = new SelectList(clients ?? new List<Client>(), "Id", "Name", contract.ClientId);
            return View(contract);
        }

        // POST: validates, handles optional new file upload (deleting the old file first),
        // then sends a PUT to update the contract via the API (Stack Overflow, 2016)
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, Contract contract, IFormFile? upload)
        {
            long fileLimit = 10 * 1024 * 1024;

            if (upload == null)
                ModelState.AddModelError("SignedAgreementPath", "Please upload a file.");
            else
            {
                if (!upload.ContentType.Equals("application/pdf", StringComparison.OrdinalIgnoreCase))
                    ModelState.AddModelError("SignedAgreementPath", "File must be PDF.");
                if (upload.Length > fileLimit)
                    ModelState.AddModelError("SignedAgreementPath", "File must be smaller than 10MB.");
            }

            if (!ModelState.IsValid)
            {
                var clientList = await _http.GetFromJsonAsync<List<Client>>("clients");
                ViewData["ClientId"] = new SelectList(clientList ?? new List<Client>(), "Id", "Name", contract.ClientId);
                return View(contract);
            }

            if (upload != null && upload.Length > 0)
            {
                var uploadsFolder = Path.Combine(_env.WebRootPath, "uploads");
                if (!Directory.Exists(uploadsFolder))
                    Directory.CreateDirectory(uploadsFolder);

                // Fetch the existing contract to get the old file path before overwriting it
                // Delete the old file from disk to avoid orphaned uploads (Microsoft, 2026ae)
                var existing = await _http.GetFromJsonAsync<Contract>($"contracts/{id}");
                if (existing != null && !string.IsNullOrEmpty(existing.SignedAgreementPath))
                {
                    var oldPath = Path.Combine(_env.WebRootPath, existing.SignedAgreementPath.TrimStart('/'));
                    if (System.IO.File.Exists(oldPath))
                        System.IO.File.Delete(oldPath);
                }

                // Save the new file and patch the updated path onto the contract record
                var filename = Guid.NewGuid() + "_" + upload.FileName;
                var filePath = Path.Combine(uploadsFolder, filename);
                using (var stream = new FileStream(filePath, FileMode.Create))
                    await upload.CopyToAsync(stream);

                contract.SignedAgreementPath = "/uploads/" + filename;
                await _http.PatchAsJsonAsync($"contracts/{id}/agreement-path", contract.SignedAgreementPath);
            }

            var response = await _http.PutAsJsonAsync($"contracts/{id}", contract);

            if (response.IsSuccessStatusCode)
                return RedirectToAction(nameof(Index));

            ModelState.AddModelError("", "Failed to update contract.");
            var clients = await _http.GetFromJsonAsync<List<Client>>("clients");
            ViewData["ClientId"] = new SelectList(clients ?? new List<Client>(), "Id", "Name", contract.ClientId);
            return View(contract);
        }

        // GET: loads the contract for the delete confirmation view
        public async Task<IActionResult> Delete(int id)
        {
            var contract = await _http.GetFromJsonAsync<Contract>($"contracts/{id}");
            if (contract == null) return NotFound();
            return View(contract);
        }

        // POST: sends a DELETE request to the API and redirects to the index
        [HttpPost, ActionName("Delete")]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            await _http.DeleteAsync($"contracts/{id}");
            return RedirectToAction(nameof(Index));
        }

        // GET: loads contract details and builds the Composite pattern tree of service request leaves
        // ContractComposite acts as the root node; each ServiceRequest becomes a leaf (Refactoring Guru, 2024b)
        public async Task<IActionResult> Details(int id)
        {
            var contract = await _http.GetFromJsonAsync<Contract>($"contracts/{id}");
            if (contract == null) return NotFound();

            // Build the composite structure so the tree can be traversed uniformly
            var contractComposite = new ContractComposite
            {
                Id     = contract.Id,
                Status = contract.Status
            };

            foreach (var sr in contract.ServiceRequests ?? new List<ServiceRequest>())
            {
                contractComposite.Add(new ServiceRequestLeaf
                {
                    Id          = sr.Id,
                    Description = sr.Description
                });
            }

            return View(contract);
        }
    }
}

// References:
// Microsoft, 2026ae. File.Delete Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.io.file.delete?view=net-10.0> [Accessed 1 June 2026].
// Microsoft, 2026af. Guid.NewGuid Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.guid.newguid?view=net-10.0> [Accessed 1 June 2026].
// Microsoft, 2026al. IWebHostEnvironment Interface. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.hosting.iwebhostenvironment?view=aspnetcore-9.0> [Accessed 1 June 2026].
// Microsoft, 2026aq. Path.Combine Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.io.path.combine?view=net-10.0> [Accessed 1 June 2026].
// Microsoft, 2026av. SelectList Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.mvc.rendering.selectlist?view=aspnetcore-9.0> [Accessed 1 June 2026].
// Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Microsoft, 2026m. ASP.NET Core Web API HttpPatch. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/web-api/?view=aspnetcore-10.0#httppatch> [Accessed 1 June 2026].
// Microsoft, 2026n. ASP.NET Core Upload Files. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
// Refactoring Guru, 2024b. Composite Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/composite> [Accessed 15 March 2026].
// Stack Overflow, 2010b. Date Range Search Filter Functionality in ASP.NET MVC 5. [online] Available at: <https://stackoverflow.com/questions/46218160/date-range-search-filter-functionality-in-asp-net-mvc-5> [Accessed 10 April 2026].
// Stack Overflow, 2016. How to Update Uploaded File and Delete the Old One in ASP.NET MVC. [online] Available at: <https://stackoverflow.com/questions/31335380/how-to-update-uploaded-file-and-delete-the-old-one-in-asp-net-mvc> [Accessed 15 April 2026].