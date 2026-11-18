using Smart_X_Client.Services;


// Entry point for the Smart-X MVC client application. Registers MVC
// (controllers + Razor views), a named HttpClient used to call the
// Smart-X API, and configures the standard ASP.NET Core middleware
// pipeline (Microsoft, 2026c; Microsoft, 2026v; The IIE, 2026).
//
// The full Harvard reference list for this file is included at the
// bottom of this source code.


namespace Smart_X_Client
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Registers controllers together with Razor view support
            // (as opposed to AddControllers(), which is API-only).
            builder.Services.AddControllersWithViews();

            // Registers a named HttpClient ("SmartXApi") via
            // IHttpClientFactory, configured with the API's base address
            // from configuration (Microsoft, 2026c).
            builder.Services.AddHttpClient("SmartXApi", client =>
            {
                client.BaseAddress = new Uri(
                    builder.Configuration["ApiBaseUrl"]!);
            });

            // ApiService wraps the named HttpClient above and is injected
            // into controllers as a Scoped dependency (one instance per
            // request) (Microsoft, 2026c).
            builder.Services.AddScoped<ApiService>();

            var app = builder.Build();

            // Uses the exception handler and HSTS outside development.
            if (!app.Environment.IsDevelopment())
            {
                app.UseExceptionHandler("/Home/Error");
                app.UseHsts();
            }

            // Redirects HTTP requests to HTTPS.
            app.UseHttpsRedirection();

            // Enables endpoint routing.
            app.UseRouting();

            // Enables authorization middleware.
            app.UseAuthorization();

            // Maps static assets used by the MVC application.
            app.MapStaticAssets();

            // Conventional MVC routing: {controller}/{action}/{id?}, e.g.
            // /Sensors/Edit/3 maps to SensorsController.Edit(3)
            // (Microsoft, 2026v).
            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Home}/{action=Index}/{id?}")
                .WithStaticAssets();

            // Starts the web application.
            app.Run();
        }
    }
}


/*

    REFERENCES


    Microsoft, 2026c. ASP.NET Core Dependency Injection. [online]
    Available at:
    <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection>
    [Accessed 1 June 2026].

    Microsoft, 2026v. Razor syntax reference for ASP.NET Core.
    [online] Available at:
    <https://learn.microsoft.com/en-us/aspnet/core/mvc/views/razor>
    [Accessed 9 September 2026].

    The IIE, 2026. Programming 3A [PROG7311 Module Manual].
    Unpublished.
*/
