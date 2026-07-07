using System.Reflection;
using System.Text;
using apiGLMS.Data;
using apiGLMS.Models;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;

namespace apiGLMS
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Enables CORS for MVC frontend communication
            // (Microsoft, 2026c)
            builder.Services.AddCors(options =>
            {
                options.AddPolicy("AllowMVC", policy =>
                {
                    policy.WithOrigins(
                        "http://localhost:5272",
                        "http://mvc-glms-service",
                        "http://mvc-glms-service:8080"
                    )
                    .AllowAnyHeader()
                    .AllowAnyMethod();
                });
            });

            // Configures EF Core with SQL Server and retry policy
            // (Microsoft, 2026w; Microsoft, 2026x)
            builder.Services.AddDbContext<ApplicationDbContext>(options =>
                options.UseSqlServer(
                    builder.Configuration.GetConnectionString("DefaultConnection"),
                    sqlOptions => sqlOptions.EnableRetryOnFailure()));

            // Configures JWT authentication for API security
            // (Microsoft, 2026an; Chapsas, 2024a)
            var jwtKey = builder.Configuration["Jwt:Key"]
                         ?? throw new InvalidOperationException("Jwt:Key is not configured.");

            builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
                .AddJwtBearer(options =>
                {
                    options.TokenValidationParameters = new TokenValidationParameters
                    {
                        ValidateIssuer = true,
                        ValidateAudience = true,
                        ValidateLifetime = true,
                        ValidateIssuerSigningKey = true,
                        ValidIssuer = builder.Configuration["Jwt:Issuer"],
                        ValidAudience = builder.Configuration["Jwt:Audience"],
                        IssuerSigningKey = new SymmetricSecurityKey(
                            Encoding.UTF8.GetBytes(jwtKey))
                    };
                });

            builder.Services.AddAuthorization();

            // Configures controllers and prevents circular reference issues
            // (Microsoft, 2026au; Code-Maze, 2023a)
            builder.Services.AddControllers()
                .AddJsonOptions(options =>
                {
                    options.JsonSerializerOptions.ReferenceHandler =
                        System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;

                    options.JsonSerializerOptions.WriteIndented = true;
                });

            builder.Services.AddEndpointsApiExplorer();

            // Swagger configuration with JWT support
            // (Microsoft, 2026aa; Code-Maze, 2023b)
            builder.Services.AddSwaggerGen(options =>
            {
                var xmlFile = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
                var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlFile);

                if (File.Exists(xmlPath))
                    options.IncludeXmlComments(xmlPath);

                options.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
                {
                    Name = "Authorization",
                    Type = Microsoft.OpenApi.Models.SecuritySchemeType.Http,
                    Scheme = "Bearer",
                    BearerFormat = "JWT",
                    In = Microsoft.OpenApi.Models.ParameterLocation.Header,
                    Description = "Paste your JWT token here."
                });

                options.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
                {
                    {
                        new Microsoft.OpenApi.Models.OpenApiSecurityScheme
                        {
                            Reference = new Microsoft.OpenApi.Models.OpenApiReference
                            {
                                Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                                Id = "Bearer"
                            }
                        },
                        Array.Empty<string>()
                    }
                });
            });

            var app = builder.Build();

            // Applies migrations and seeds database if empty
            // Uses retry logic for startup delays in container environments
            // (Microsoft, 2026aa; Stack Overflow, 2021; Microsoft, 2026bd)
            using (var scope = app.Services.CreateScope())
            {
                var db = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();

                var retries = 10;
                while (retries > 0)
                {
                    try
                    {
                        db.Database.Migrate();
                        break;
                    }
                    catch
                    {
                        retries--;
                        Thread.Sleep(3000);
                    }
                }

                // Seed Clients
                if (!db.Clients.Any())
                {
                    db.Clients.AddRange(
                        new Client { Name = "Maersk Line SA", ContactDetails = "ops@maersksa.co.za | 011 234 5678", Region = "Gauteng" },
                        new Client { Name = "DHL Freight Solutions", ContactDetails = "freight@dhlsa.co.za | 021 345 6789", Region = "Western Cape" },
                        new Client { Name = "Transnet Port Terminals", ContactDetails = "contracts@transnet.co.za | 031 456 7890", Region = "KwaZulu-Natal" },
                        new Client { Name = "Imperial Logistics", ContactDetails = "logistics@imperial.co.za | 012 567 8901", Region = "Gauteng" },
                        new Client { Name = "Bidvest Panalpina", ContactDetails = "info@bidvestpan.co.za | 011 678 9012", Region = "Gauteng" }
                    );

                    db.SaveChanges();
                }

                // Seed Contracts
                if (!db.Contracts.Any())
                {
                    var maersk = db.Clients.First(c => c.Name == "Maersk Line SA");
                    var dhl = db.Clients.First(c => c.Name == "DHL Freight Solutions");
                    var transnet = db.Clients.First(c => c.Name == "Transnet Port Terminals");
                    var imperial = db.Clients.First(c => c.Name == "Imperial Logistics");
                    var bidvest = db.Clients.First(c => c.Name == "Bidvest Panalpina");

                    db.Contracts.AddRange(
                        new Contract { ClientId = maersk.Id, Status = "Active", ServiceLevel = "Premium", StartDate = new DateTime(2025, 1, 1), EndDate = new DateTime(2025, 12, 31), SignedAgreementPath = "/uploads/dummy1.pdf" },
                        new Contract { ClientId = dhl.Id, Status = "Active", ServiceLevel = "Standard", StartDate = new DateTime(2025, 3, 1), EndDate = new DateTime(2026, 2, 28), SignedAgreementPath = "/uploads/dummy2.pdf" },
                        new Contract { ClientId = transnet.Id, Status = "Expired", ServiceLevel = "Basic", StartDate = new DateTime(2024, 1, 1), EndDate = new DateTime(2024, 12, 31), SignedAgreementPath = "/uploads/dummy3.pdf" },
                        new Contract { ClientId = imperial.Id, Status = "Active", ServiceLevel = "Premium", StartDate = new DateTime(2025, 6, 1), EndDate = new DateTime(2026, 5, 31), SignedAgreementPath = "/uploads/dummy4.pdf" },
                        new Contract { ClientId = bidvest.Id, Status = "On Hold", ServiceLevel = "Standard", StartDate = new DateTime(2025, 2, 1), EndDate = new DateTime(2025, 8, 31), SignedAgreementPath = "/uploads/dummy5.pdf" }
                    );

                    db.SaveChanges();
                }

                // Seed Service Requests
                if (!db.ServiceRequests.Any())
                {
                    var c1 = db.Contracts.First(c => c.SignedAgreementPath == "/uploads/dummy1.pdf");
                    var c2 = db.Contracts.First(c => c.SignedAgreementPath == "/uploads/dummy2.pdf");
                    var c3 = db.Contracts.First(c => c.SignedAgreementPath == "/uploads/dummy3.pdf");
                    var c4 = db.Contracts.First(c => c.SignedAgreementPath == "/uploads/dummy4.pdf");
                    var c5 = db.Contracts.First(c => c.SignedAgreementPath == "/uploads/dummy5.pdf");

                    db.ServiceRequests.AddRange(
                        new ServiceRequest { ContractId = c1.Id, Description = "Cape Town to Durban container freight coordination", CostUSD = 4500.00m, CostZAR = 85500.00m, Status = "Active" },
                        new ServiceRequest { ContractId = c1.Id, Description = "Hazardous goods compliance inspection - Pier 1", CostUSD = 1200.00m, CostZAR = 22800.00m, Status = "Active" },
                        new ServiceRequest { ContractId = c1.Id, Description = "Driver scheduling for 12-truck Johannesburg convoy", CostUSD = 3200.00m, CostZAR = 60800.00m, Status = "Draft" },
                        new ServiceRequest { ContractId = c2.Id, Description = "International air freight booking - JHB to Frankfurt", CostUSD = 8750.00m, CostZAR = 166250.00m, Status = "Active" },
                        new ServiceRequest { ContractId = c2.Id, Description = "Customs clearance and documentation - OR Tambo", CostUSD = 950.00m, CostZAR = 18050.00m, Status = "Completed" },
                        new ServiceRequest { ContractId = c3.Id, Description = "Annual port equipment maintenance scheduling", CostUSD = 6200.00m, CostZAR = 117800.00m, Status = "Completed" },
                        new ServiceRequest { ContractId = c3.Id, Description = "Final freight audit and invoice reconciliation", CostUSD = 1100.00m, CostZAR = 20900.00m, Status = "Completed" },
                        new ServiceRequest { ContractId = c4.Id, Description = "Cross-border truck fleet dispatch - Beit Bridge", CostUSD = 5400.00m, CostZAR = 102600.00m, Status = "Active" },
                        new ServiceRequest { ContractId = c4.Id, Description = "Cold chain refrigerated transport - Durban to Maputo", CostUSD = 7800.00m, CostZAR = 148200.00m, Status = "Draft" },
                        new ServiceRequest { ContractId = c5.Id, Description = "Sea freight consolidation - Cape Town to Rotterdam", CostUSD = 9200.00m, CostZAR = 174800.00m, Status = "Draft" }
                    );

                    db.SaveChanges();
                }
            }

            app.UseSwagger();
            app.UseSwaggerUI(options =>
            {
                options.SwaggerEndpoint("/swagger/v1/swagger.json", "GLMS API v1");
                options.RoutePrefix = string.Empty;
            });

            app.UseCors("AllowMVC");
            app.UseAuthentication();
            app.UseAuthorization();
            app.MapControllers();
            app.Run();
        }
    }
}

/*
REFERENCES

Chapsas, N., 2024a. Implementing JWT Authentication in ASP.NET Core. [video online] Available at: <https://www.youtube.com/watch?v=3XoXzEPHdTA> [Accessed 2 June 2026].

Code-Maze, 2023a. Handling Circular References When Working with JSON in ASP.NET Core. [online] Available at: <https://code-maze.com/aspnetcore-handling-circular-references-when-working-with-json/> [Accessed 1 June 2026].

Code-Maze, 2023b. Swagger UI in ASP.NET Core Web API. [online] Available at: <https://code-maze.com/swagger-ui-asp-net-core-web-api/> [Accessed 1 June 2026].

Microsoft, 2026aa. ASP.NET Core Web API Help Pages using Swagger. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/tutorials/web-api-help-pages-using-swagger?view=aspnetcore-8.0> [Accessed 1 June 2026].

Microsoft, 2026an. JWT Authentication in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/authentication/jwt-authn?view=aspnetcore-10.0> [Accessed 1 June 2026].

Microsoft, 2026au. ReferenceHandler.IgnoreCycles Property. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.text.json.serialization.referencehandler.ignorecycles?view=net-9.0> [Accessed 1 June 2026].

Microsoft, 2026c. ASP.NET Core CORS. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/security/cors?view=aspnetcore-10.0> [Accessed 1 June 2026].

Microsoft, 2026w. EF Core - Connection Resiliency. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/miscellaneous/connection-resiliency> [Accessed 1 June 2026].

Microsoft, 2026x. EF Core - Creating and Configuring a Model. [online] Available at: <https://learn.microsoft.com/en-us/ef/core/modeling/> [Accessed 1 June 2026].

Microsoft, 2026bd. Thread.Sleep Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.threading.thread.sleep?view=net-10.0> [Accessed 1 June 2026].

Stack Overflow, 2021. How to Seed Data in .NET Core 6 with Entity Framework. [online] Available at: <https://stackoverflow.com/questions/70581816/how-to-seed-data-in-net-core-6-with-entity-framework> [Accessed 8 June 2026].
*/