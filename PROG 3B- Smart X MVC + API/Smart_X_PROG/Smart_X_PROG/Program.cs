using Scalar.AspNetCore;
using Smart_X_API.Services;


// This is the composition root / entry point for the Smart-X Web API.
// It wires up the ASP.NET Core hosting pipeline: it registers services in
// the Dependency Injection (DI) container, configures middleware, and then
// starts the Kestrel web server (Microsoft, 2026c; The IIE, 2026).


namespace Smart_X_API
{
    public class Program
    {
        public static void Main(string[] args)
        {
            // WebApplication.CreateBuilder bootstraps configuration, logging
            // and the DI container in one call (Microsoft, 2026c).
            var builder = WebApplication.CreateBuilder(args);

            // Add controllers and configure JSON
            // AddJsonOptions() customises System.Text.Json so that enum
            // values (e.g. SensorCategory) are serialised as their string
            // name instead of their underlying integer, which keeps the
            // JSON payload readable for the client (Microsoft, 2026o).
            builder.Services.AddControllers()
                .AddJsonOptions(options =>
                {
                    options.JsonSerializerOptions.Converters.Add(
                        new System.Text.Json.Serialization.JsonStringEnumConverter()
                    );
                });

            // Add OpenAPI
            // Generates an OpenAPI (Swagger-style) document describing the
            // API's endpoints, later exposed through Scalar's UI
            // (Microsoft, 2026i).
            builder.Services.AddOpenApi();

            // Configure CORS so the Smart-X Client can communicate with the API
            // Cross-Origin Resource Sharing (CORS) has to be explicitly
            // enabled because the MVC client (Smart_X_Client) runs on a
            // different origin/port to this API (Microsoft, 2026b).
            builder.Services.AddCors(options =>
            {
                options.AddPolicy("AllowClient", policy =>
                {
                    policy.WithOrigins("https://localhost:7148")
                          .AllowAnyHeader()
                          .AllowAnyMethod();
                });
            });

            // Register Smart-X services
            // Each service is registered as a Singleton because they hold
            // their state (sensors/telemetry) in memory for the lifetime of
            // the application, rather than per-request, via constructor
            // injection (Microsoft, 2026c; The IIE, 2026).
            builder.Services.AddSingleton<SensorService>();
            builder.Services.AddSingleton<TelemetryService>();
            builder.Services.AddSingleton<TelemetryHistoryService>();
            builder.Services.AddSingleton<DeploymentValidationService>();
            builder.Services.AddSingleton<FileService>();

            // Part 2: InsightService (the pattern analysis) and StreamService
            // (the queues, stack, dictionaries and set) are also singletons, so
            // the live data is shared by every request and survives between
            // them (Microsoft, 2026c; The IIE, 2026).
            builder.Services.AddSingleton<InsightService>();
            builder.Services.AddSingleton<StreamService>();

            // StreamWorker is a hosted service: it starts together with the API
            // and keeps running in the background, emptying the queues twice a
            // second (Microsoft, 2026aa).
            builder.Services.AddHostedService<StreamWorker>();

            builder.Services.AddSingleton<TopologyService>();

            var app = builder.Build();

            // Redirect HTTP requests to HTTPS
            app.UseHttpsRedirection();

            // Enable static files
            // Required so that uploaded attachments under wwwroot/uploads
            // can be served back to the client (Microsoft, 2026z).
            app.UseStaticFiles();

            // Enable CORS
            // Must be applied to the policy defined above before requests
            // reach the controllers (Microsoft, 2026b).
            app.UseCors("AllowClient");

            // Enable authorization
            app.UseAuthorization();

            // OpenAPI
            app.MapOpenApi();

            // Controllers
            // Maps attribute-routed API controllers (e.g. [Route("api/[controller]")]).
            app.MapControllers();

            // Scalar API documentation
            // Exposes an interactive documentation page built from the
            // OpenAPI document above (Microsoft, 2026i).
            app.MapScalarApiReference();

            app.Run();
        }
    }
}
// References used in this file:
//   - Microsoft, 2026c. ASP.NET Core Dependency Injection.
//   - Microsoft, 2026b. ASP.NET Core CORS.
//   - Microsoft, 2026o. JsonStringEnumConverter Class.
//   - Microsoft, 2026i. Generate OpenAPI documents and use Scalar for
//   - Microsoft, 2026aa. Background tasks with hosted services in ASP.NET Core.
//     interactive API documentation.
//   - The IIE, 2026. Programming 3A [PROG7311 Module Manual].
//   - IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist.

//