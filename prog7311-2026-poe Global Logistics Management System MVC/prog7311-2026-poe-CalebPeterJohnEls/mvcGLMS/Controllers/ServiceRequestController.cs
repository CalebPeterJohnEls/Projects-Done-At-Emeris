using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using mvcGLMS.ExchangeServices;
using mvcGLMS.Models;
using mvcGLMS.ObserverServices;
using System.Net.Http.Json;

namespace mvcGLMS.Controllers
{
    // MVC Controller for managing Service Requests
    // Uses the Strategy pattern for currency conversion and the Observer pattern
    // to enforce contract status rules before creating a service request (Refactoring Guru, 2024a; 2024c)
    public class ServiceRequestController : Controller
    {
        private readonly HttpClient _http;
        private readonly CurrencyContext _currencyContext;
        private readonly ContractObserverService _observerService;

        // IHttpClientFactory provides a named client; CurrencyContext and ContractObserverService
        // are injected as Scoped services registered in Program.cs (Microsoft, 2026aj; Microsoft, 2026e)
        public ServiceRequestController(
            IHttpClientFactory factory,
            CurrencyContext currencyContext,
            ContractObserverService observerService)
        {
            _http            = factory.CreateClient("GlmsApi");
            _currencyContext = currencyContext;
            _observerService = observerService;
        }

        // GET: fetches all service requests from the API and passes them to the list view
        public async Task<IActionResult> Index()
        {
            var requests = await _http.GetFromJsonAsync<List<ServiceRequest>>("servicerequests");
            return View(requests ?? new List<ServiceRequest>());
        }

        // GET: fetches a single service request by ID; returns 404 if not found
        public async Task<IActionResult> Details(int id)
        {
            var request = await _http.GetFromJsonAsync<ServiceRequest>($"servicerequests/{id}");
            if (request == null) return NotFound();
            return View(request);
        }

        // GET: renders the create form with a dropdown of available contracts (Microsoft, 2026av)
        public async Task<IActionResult> Create()
        {
            var contracts = await _http.GetFromJsonAsync<List<Contract>>("contracts");
            ViewData["ContractId"] = new SelectList(contracts ?? new List<Contract>(), "Id", "Id");
            return View();
        }

        // GET endpoint called via JavaScript fetch to convert a USD amount to ZAR
        // Strategy pattern delegates conversion logic to the active ICurrencyStrategy (Refactoring Guru, 2024a)
        [HttpGet]
        public async Task<IActionResult> GetZarAmount(decimal usd)
        {
            var zar = await _currencyContext.ConvertAsync(usd);
            return Json(new { zarAmount = zar });
        }

        // POST: validates the model, enforces contract status rules via Observer pattern,
        // converts USD to ZAR, then posts the service request to the API
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(ServiceRequest serviceRequest)
        {
            if (!ModelState.IsValid)
            {
                var contracts = await _http.GetFromJsonAsync<List<Contract>>("contracts");
                ViewData["ContractId"] = new SelectList(contracts ?? new List<Contract>(), "Id", "Id", serviceRequest.ContractId);
                return View(serviceRequest);
            }

            // Fetch the selected contract to check its status via the Observer pattern
            // CanCreateServiceRequest returns false when the contract is Expired or On Hold
            // (Refactoring Guru, 2024c)
            var contract = await _http.GetFromJsonAsync<Contract>($"contracts/{serviceRequest.ContractId}");

            if (contract == null)
            {
                ModelState.AddModelError("ContractId", "Selected contract does not exist.");
            }
            else if (!_observerService.CanCreateServiceRequest(contract.Status))
            {
                ModelState.AddModelError("ContractId",
                    "Service Request cannot be created if the contract is Expired or On Hold.");
            }

            // Auto-convert USD cost to ZAR using the Strategy pattern before saving
            // (Refactoring Guru, 2024a)
            if (serviceRequest.CostUSD > 0)
                serviceRequest.CostZAR = await _currencyContext.ConvertAsync(serviceRequest.CostUSD);

            // Re-check ModelState after the Observer and conversion steps above
            if (!ModelState.IsValid)
            {
                var contracts = await _http.GetFromJsonAsync<List<Contract>>("contracts");
                ViewData["ContractId"] = new SelectList(contracts ?? new List<Contract>(), "Id", "Id", serviceRequest.ContractId);
                return View(serviceRequest);
            }

            var response = await _http.PostAsJsonAsync("servicerequests", serviceRequest);

            if (response.IsSuccessStatusCode)
                return RedirectToAction(nameof(Index));

            ModelState.AddModelError("", "Failed to create service request.");
            var contractList = await _http.GetFromJsonAsync<List<Contract>>("contracts");
            ViewData["ContractId"] = new SelectList(contractList ?? new List<Contract>(), "Id", "Id", serviceRequest.ContractId);
            return View(serviceRequest);
        }

        // GET: loads the service request and its contract dropdown for editing
        public async Task<IActionResult> Edit(int id)
        {
            var request = await _http.GetFromJsonAsync<ServiceRequest>($"servicerequests/{id}");
            if (request == null) return NotFound();

            var contracts = await _http.GetFromJsonAsync<List<Contract>>("contracts");
            ViewData["ContractId"] = new SelectList(contracts ?? new List<Contract>(), "Id", "Id", request.ContractId);
            return View(request);
        }

        // POST: validates and sends a PUT request to update the service request via the API
        [HttpPost]
        public async Task<IActionResult> Edit(int id, ServiceRequest serviceRequest)
        {
            if (!ModelState.IsValid)
            {
                var contracts = await _http.GetFromJsonAsync<List<Contract>>("contracts");
                ViewData["ContractId"] = new SelectList(contracts ?? new List<Contract>(), "Id", "Id", serviceRequest.ContractId);
                return View(serviceRequest);
            }

            var response = await _http.PutAsJsonAsync($"servicerequests/{id}", serviceRequest);

            if (response.IsSuccessStatusCode)
                return RedirectToAction(nameof(Index));

            ModelState.AddModelError("", "Failed to update service request.");
            var contractList = await _http.GetFromJsonAsync<List<Contract>>("contracts");
            ViewData["ContractId"] = new SelectList(contractList ?? new List<Contract>(), "Id", "Id", serviceRequest.ContractId);
            return View(serviceRequest);
        }

        // GET: loads the service request for the delete confirmation view
        public async Task<IActionResult> Delete(int id)
        {
            var request = await _http.GetFromJsonAsync<ServiceRequest>($"servicerequests/{id}");
            if (request == null) return NotFound();
            return View(request);
        }

        // POST: sends a DELETE request to the API and redirects to the index on success
        [HttpPost, ActionName("Delete")]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            await _http.DeleteAsync($"servicerequests/{id}");
            return RedirectToAction(nameof(Index));
        }
    }
}

// References:
// Microsoft, 2026aj. IHttpClientFactory in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/httpclient-factory> [Accessed 1 June 2026].
// Microsoft, 2026av. SelectList Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.mvc.rendering.selectlist?view=aspnetcore-9.0> [Accessed 1 June 2026].
// Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Microsoft, 2026i. ASP.NET Core Model Validation. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024a. Strategy Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/strategy> [Accessed 15 April 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].