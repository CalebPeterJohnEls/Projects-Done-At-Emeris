using Xunit;
using Moq;
using mvcGLMS.ExchangeServices;
using mvcGLMS.Controllers;
using mvcGLMS.Models;
using mvcGLMS.ObserverServices;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using System.Net;
using System.Text;

namespace TestProject
{
    // Intercepts all outbound HTTP requests and returns 200 OK with an empty JSON array.
    // This prevents controllers from hitting a real network during tests (Microsoft, 2026bc; Microsoft, 2022).
    internal class FakeHandler : HttpMessageHandler
    {
        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            // StringContent sets UTF-8 encoding and "application/json" so downstream
            // deserialisation does not throw (Microsoft, 2026ay).
            // Task.FromResult completes the Task without real I/O (Microsoft, 2026o).
            var response = new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent("[]", Encoding.UTF8, "application/json")
            };
            return Task.FromResult(response);
        }
    }

    // Same as FakeHandler but lets each test choose the status code and response body.
    // This allows tests to simulate 401, 404, 409, etc. without a live server (Microsoft, 2026bc; Microsoft, 2026ai).
    internal class FakeHandlerWithResponse : HttpMessageHandler
    {
        private readonly HttpStatusCode _statusCode;
        private readonly string _content;

        // Stores the desired status code and body for every intercepted request (Microsoft, 2026ai).
        public FakeHandlerWithResponse(HttpStatusCode statusCode, string content)
        {
            _statusCode = statusCode;
            _content = content;
        }

        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            // Returns the pre-configured response so the controller's code path under
            // test (success, not-found, conflict, etc.) is exercised (Microsoft, 2026ah).
            var response = new HttpResponseMessage(_statusCode)
            {
                Content = new StringContent(_content, Encoding.UTF8, "application/json")
            };
            return Task.FromResult(response);
        }
    }

    // Minimal in-memory ISession so AccountController tests do not throw when
    // the controller reads or writes session state. ASP.NET Core's real session
    // requires a running HTTP pipeline which is unavailable in unit tests (Microsoft, 2026j).
    internal class TestSession : ISession
    {
        // Backs all session keys and values for the lifetime of a single test (Microsoft, 2026j).
        private readonly Dictionary<string, byte[]> _store = new();

        public bool IsAvailable => true; // Always ready; no network round-trip needed.
        public string Id => "test-session"; // Fixed ID; uniqueness is irrelevant in tests.
        public IEnumerable<string> Keys => _store.Keys;

        public void Clear() => _store.Clear();
        public Task CommitAsync(CancellationToken c) => Task.CompletedTask; // No-op; nothing to persist.
        public Task LoadAsync(CancellationToken c) => Task.CompletedTask; // No-op; data is already in memory.
        public void Remove(string key) => _store.Remove(key);
        public void Set(string key, byte[] value) => _store[key] = value;
        public bool TryGetValue(string key, out byte[] value) => _store.TryGetValue(key, out value!);
    }

    // CURRENCY TESTS (Microsoft, 2026bc; Microsoft, 2026be; Refactoring Guru, 2024a)
    public class CurrencyTests
    {
        [Fact]
        public async Task ConvertUsdToZar_UsesStrategyRate()
        {
            // Arrange
            // Mock ICurrencyStrategy so no real currency API is called.
            // CurrencyContext depends on the ICurrencyStrategy abstraction (Strategy pattern),
            // so swapping in a mock is enough to control the rate (Refactoring Guru, 2024a).
            var mockStrategy = new Mock<ICurrencyStrategy>();
            mockStrategy.Setup(s => s.GetRateAsync()).ReturnsAsync(18.5m); // Stub returns 18.5 ZAR per USD (Proud, 2024).
            var context = new CurrencyContext(mockStrategy.Object); // Inject mock via constructor (The IIE, 2026).

            // Act
            var result = await context.ConvertAsync(10m); // 10 USD × 18.5 = 185 ZAR expected.

            // Assert
            Assert.Equal(185m, result); // (Microsoft, 2026be)
        }

        [Fact]
        public async Task ConvertUsdToZar_ZeroUsd_ReturnsZero()
        {
            // Arrange
            // Rate value does not matter here because 0 × any rate = 0.
            // The mock must still be configured so GetRateAsync does not return null (Proud, 2024).
            var mockStrategy = new Mock<ICurrencyStrategy>();
            mockStrategy.Setup(s => s.GetRateAsync()).ReturnsAsync(18.5m);
            var context = new CurrencyContext(mockStrategy.Object);

            // Act
            var result = await context.ConvertAsync(0m);

            // Assert
            Assert.Equal(0m, result); // Zero input must always produce zero output (Microsoft, 2026be).
        }
    }

    // OBSERVER TESTS (Refactoring Guru, 2024c; The IIE, 2026; Microsoft, 2026be)
    public class ObserverTests
    {
        [Fact]
        public void OnContractStatusChanged_DoesNotThrow_ForActiveStatus()
        {
            var service = new ContractObserverService(); // Real service; no mocking needed here.

            // Record.Exception runs the action and captures any exception it raises.
            // A null result confirms the Observer notification completed cleanly (Microsoft, 2026be).
            var exception = Record.Exception(() => service.OnContractStatusChanged("Active"));

            Assert.Null(exception); // Observer pattern must handle "Active" without faulting (Refactoring Guru, 2024c).
        }

        [Fact]
        public void OnContractStatusChanged_DoesNotThrow_ForExpiredStatus()
        {
            var service = new ContractObserverService();

            // "Expired" may trigger different observer logic (e.g. alerts), so this test
            // confirms robustness across multiple status values (Refactoring Guru, 2024c).
            var exception = Record.Exception(() => service.OnContractStatusChanged("Expired"));

            Assert.Null(exception); // (Microsoft, 2026be)
        }
    }

    // CLIENT CONTROLLER TESTS (Microsoft, 2026bc; Microsoft, 2026be)
    public class ClientControllerTests
    {
        // Builds ClientController with a stub HttpClient returning the given status and body.
        // Defaults to 200 OK / "[]" for happy-path tests (Microsoft, 2026aj; Microsoft, 2026bc).
        private ClientController BuildController(HttpStatusCode code = HttpStatusCode.OK, string content = "[]")
        {
            var mockFactory = new Mock<IHttpClientFactory>();
            var httpClient = new HttpClient(new FakeHandlerWithResponse(code, content))
            {
                BaseAddress = new Uri("http://localhost/") // Required so relative URLs resolve inside the controller (Microsoft, 2026bf).
            };
            mockFactory.Setup(f => f.CreateClient("GlmsApi")).Returns(httpClient); // (Microsoft, 2026aj)
            return new ClientController(mockFactory.Object);
        }

        [Fact]
        public async Task Index_ReturnsViewResult()
        {
            // 200 OK / "[]" simulates an empty client list; confirms the action completes
            // and returns a view (Microsoft, 2026bc).
            var result = await BuildController().Index();
            Assert.IsType<ViewResult>(result); // (Microsoft, 2026be)
        }

        [Fact]
        public async Task Details_ThrowsHttpRequestException_WhenApiReturnsNotFound()
        {
            // 404 causes EnsureSuccessStatusCode (or equivalent) inside the controller
            // to throw; the exception should propagate to the error-handling middleware (Microsoft, 2026ah).
            await Assert.ThrowsAsync<HttpRequestException>(() =>
                BuildController(HttpStatusCode.NotFound, "").Details(0));
        }

        [Fact]
        public async Task Create_Post_ReturnsView_WhenModelStateInvalid()
        {
            var controller = BuildController();

            // Manually seed a model error to simulate a failed form submission.
            // The MVC binder adds these automatically in a real request (Microsoft, 2026i; Microsoft, 2026ap).
            controller.ModelState.AddModelError("Name", "Required");

            var result = await controller.Create(new Client());

            // Invalid model state must re-display the form, not call the API (Microsoft, 2026bc).
            Assert.IsType<ViewResult>(result);
        }

        [Fact]
        public async Task Edit_Get_ThrowsHttpRequestException_WhenApiReturnsNotFound()
        {
            // 404 for an unknown ID should surface as an exception so the error page is shown (Microsoft, 2026ah).
            await Assert.ThrowsAsync<HttpRequestException>(() =>
                BuildController(HttpStatusCode.NotFound, "").Edit(0));
        }

        [Fact]
        public async Task Delete_Get_ThrowsHttpRequestException_WhenApiReturnsNotFound()
        {
            // The delete-confirmation page requires the record to exist;
            // a 404 must propagate rather than silently succeeding (Microsoft, 2026ah; Microsoft, 2026bc).
            await Assert.ThrowsAsync<HttpRequestException>(() =>
                BuildController(HttpStatusCode.NotFound, "").Delete(0));
        }
    }

    // ACCOUNT CONTROLLER TESTS (Microsoft, 2026bc; Microsoft, 2026be; Microsoft, 2026j)
    public class AccountControllerTests
    {
        // Builds AccountController with a stub HttpClient and an in-memory TestSession.
        // DefaultHttpContext + TestSession replaces the full HTTP pipeline required
        // by session-dependent actions (Microsoft, 2026j; Microsoft, 2026bc).
        private AccountController BuildController(HttpStatusCode code, string content)
        {
            var mockFactory = new Mock<IHttpClientFactory>();
            var httpClient = new HttpClient(new FakeHandlerWithResponse(code, content))
            {
                BaseAddress = new Uri("http://localhost/")
            };
            mockFactory.Setup(f => f.CreateClient("GlmsApi")).Returns(httpClient); // (Microsoft, 2026aj)

            var controller = new AccountController(mockFactory.Object);
            var httpContext = new DefaultHttpContext();
            httpContext.Session = new TestSession(); // Swap real session for in-memory stub (Microsoft, 2026j).
            controller.ControllerContext = new ControllerContext { HttpContext = httpContext };
            return controller;
        }

        [Fact]
        public async Task Login_Post_ReturnsView_WhenModelStateInvalid()
        {
            var controller = BuildController(HttpStatusCode.OK, "[]");

            // Seed a validation error to simulate a missing required field (Microsoft, 2026i; Microsoft, 2026ap).
            controller.ModelState.AddModelError("Username", "Required");

            var result = await controller.Login(new Login());

            // The form must be re-displayed so the user can correct the error (Microsoft, 2026bc).
            Assert.IsType<ViewResult>(result);
        }

        [Fact]
        public async Task Login_Post_ReturnsView_WhenApiReturnsUnauthorized()
        {
            // 401 Unauthorized means the credentials are wrong; the controller must not
            // redirect to the dashboard on a failed login (Microsoft, 2026a; Microsoft, 2026ai).
            var result = await BuildController(HttpStatusCode.Unauthorized, "{}")
                .Login(new Login { Username = "wrong", Password = "wrong" });

            Assert.IsType<ViewResult>(result); // (Microsoft, 2026bc)
        }

        [Fact]
        public async Task Register_Post_ReturnsView_WhenUsernameAlreadyExists()
        {
            // 409 Conflict signals a duplicate username; the controller must surface this
            // error to the user rather than silently failing (Microsoft, 2026ai; Microsoft, 2026bc).
            var result = await BuildController(HttpStatusCode.Conflict, "{}")
                .Register(new Register { Username = "existing", Email = "existing@test.com", Password = "password123", Name = "Test", Surname = "User" });

            Assert.IsType<ViewResult>(result); // (Microsoft, 2026be)
        }
    }

    // FILE VALIDATION TESTS (Microsoft, 2026bc; Microsoft, 2026be; Microsoft, 2026n)
    public class FileValidationTests
    {
        // Builds ContractsController with a stub HttpClient.
        // IWebHostEnvironment is null because file-save code is never reached in these
        // validation tests — the controller rejects the upload before touching the file system (Microsoft, 2026al).
        private ContractsController BuildController()
        {
            var mockFactory = new Mock<IHttpClientFactory>();
            var httpClient = new HttpClient(new FakeHandler()) { BaseAddress = new Uri("http://localhost/") };
            mockFactory.Setup(f => f.CreateClient("GlmsApi")).Returns(httpClient);
            return new ContractsController(mockFactory.Object, null!);
        }

        [Fact]
        public async Task CreateReturnsViewWhenUploadIsNull()
        {
            // Passing null simulates a form submission with no file attached.
            // The controller must guard against a null IFormFile to avoid a
            // NullReferenceException when reading ContentType or Length (Microsoft, 2026n).
            var result = await BuildController()
                .Create(new Contract { ClientId = 1, Status = "Active", ServiceLevel = "Gold" }, null);

            Assert.IsType<ViewResult>(result); // Form must be re-displayed (Microsoft, 2026bc).
        }

        [Fact]
        public async Task CreateReturnsViewWhenFileIsNotPdf()
        {
            // Mock IFormFile so ContentType and Length can be set without a real file on disk (Proud, 2024).
            var mockFile = new Mock<IFormFile>();
            mockFile.Setup(f => f.ContentType).Returns("application/exe"); // Invalid MIME type; only "application/pdf" is accepted (Microsoft, 2026n).
            mockFile.Setup(f => f.Length).Returns(1024L); // Non-zero so an "empty file" guard does not fire first.

            var result = await BuildController()
                .Create(new Contract { ClientId = 1, Status = "Active", ServiceLevel = "Gold" }, mockFile.Object);

            // A non-PDF upload must be rejected and the form re-displayed with an error (Microsoft, 2026bc).
            Assert.IsType<ViewResult>(result);
        }
    }
}
//@*(Microsoft, 2026bc; Microsoft, 2026be; Refactoring Guru, 2024a; Refactoring Guru, 2024c; The IIE, 2026)

/*References

 - Microsoft, 2022. HTTP Message Handlers in ASP.NET Web API. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/web-api/overview/advanced/http-message-handlers> [Accessed 4 June 2026].
 - Microsoft, 2026a. ASP.NET Core Authentication Overview. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authentication/?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026ah. HttpResponseMessage Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.http.httpresponsemessage?view=net-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026ai. HttpStatusCode Enum. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.httpstatuscode?view=net-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026aj. IHttpClientFactory in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/extensions/httpclient-factory> [Accessed 1 June 2026].
 - Microsoft, 2026al. IWebHostEnvironment Interface. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.hosting.iwebhostenvironment?view=aspnetcore-9.0> [Accessed 1 June 2026].
 - Microsoft, 2026ap. ModelStateDictionary Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.mvc.modelbinding.modelstatedictionary?view=aspnetcore-9.0> [Accessed 1 June 2026].
 - Microsoft, 2026ay. StringContent Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.net.http.stringcontent?view=net-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026bc. Testing ASP.NET Core MVC Apps. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/controllers/testing?view=aspnetcore-10.0> [Accessed 19 April 2026].
 - Microsoft, 2026be. Unit Testing C# with xUnit. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/testing/unit-testing-csharp-with-xunit> [Accessed 22 April 2026].
 - Microsoft, 2026bf. Uri Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.uri?view=net-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026i. ASP.NET Core Model Validation. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026j. ASP.NET Core Session and State Management. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/app-state?view=aspnetcore-10.0> [Accessed 1 June 2026].
 - Microsoft, 2026n. ASP.NET Core Upload Files. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
 - Microsoft, 2026o. Asynchronous Programming with async and await. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/asynchronous-programming/> [Accessed 1 June 2026].
 - Proud, N., 2024. Make Your C# Unit Tests 10x Better With Moq. [video online] Available at: <https://www.youtube.com/watch?v=lKbW88aSwjY> [Accessed 3 June 2026].
 - Refactoring Guru, 2024a. Strategy Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/strategy> [Accessed 15 April 2026].
 - Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].
 - The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
 - Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.

 */