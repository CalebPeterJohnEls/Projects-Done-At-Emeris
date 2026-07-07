using Microsoft.AspNetCore.Mvc;
using mvcGLMS.Models;
using System.Net.Http.Json;

namespace mvcGLMS.Controllers
{
    // MVC Controller providing full CRUD operations for Clients
    // All data operations are delegated to the Web API via a named HttpClient (Microsoft, 2026aj)
    public class ClientController : Controller
    {
        private readonly HttpClient _http;

        // Named HttpClient "GlmsApi" injected via IHttpClientFactory; base address set in Program.cs
        // (Microsoft, 2026aj)
        public ClientController(IHttpClientFactory factory)
        {
            _http = factory.CreateClient("GlmsApi");
        }

        // GET: fetches all clients from the API and passes them to the list view
        public async Task<IActionResult> Index()
        {
            var clients = await _http.GetFromJsonAsync<List<Client>>("clients");
            return View(clients ?? new List<Client>());
        }

        // GET: fetches a single client by ID; returns 404 if the API returns null
        public async Task<IActionResult> Details(int id)
        {
            var client = await _http.GetFromJsonAsync<Client>($"clients/{id}");
            if (client == null) return NotFound();
            return View(client);
        }

        // GET: renders the empty create form
        public IActionResult Create() => View();

        // POST: validates the model and posts the new client to the API
        // ValidateAntiForgeryToken guards against CSRF attacks (Microsoft, 2026b)
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(Client client)
        {
            if (!ModelState.IsValid) return View(client);

            var response = await _http.PostAsJsonAsync("clients", client);

            if (response.IsSuccessStatusCode)
                return RedirectToAction(nameof(Index));

            ModelState.AddModelError("", "Failed to create client.");
            return View(client);
        }

        // GET: fetches the client for the edit form; returns 404 if not found
        public async Task<IActionResult> Edit(int id)
        {
            var client = await _http.GetFromJsonAsync<Client>($"clients/{id}");
            if (client == null) return NotFound();
            return View(client);
        }

        // POST: validates and sends a PUT request to update the client via the API
        [HttpPost]
        public async Task<IActionResult> Edit(int id, Client client)
        {
            if (!ModelState.IsValid) return View(client);

            var response = await _http.PutAsJsonAsync($"clients/{id}", client);

            if (response.IsSuccessStatusCode)
                return RedirectToAction(nameof(Index));

            ModelState.AddModelError("", "Failed to update client.");
            return View(client);
        }

        // GET: fetches the client for the delete confirmation view
        public async Task<IActionResult> Delete(int id)
        {
            var client = await _http.GetFromJsonAsync<Client>($"clients/{id}");
            if (client == null) return NotFound();
            return View(client);
        }

        // POST: sends a DELETE request to the API and redirects to the index on success
        [HttpPost, ActionName("Delete")]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            await _http.DeleteAsync($"clients/{id}");
            return RedirectToAction(nameof(Index));
        }
    }
}

// References:
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025].
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
// Microsoft, 2026aj. IHttpClientFactory in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/httpclient-factory> [Accessed 1 June 2026].
// Microsoft, 2026b. ASP.NET Core Anti-Request Forgery. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/anti-request-forgery?view=aspnetcore-10.0> [Accessed 1 June 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.