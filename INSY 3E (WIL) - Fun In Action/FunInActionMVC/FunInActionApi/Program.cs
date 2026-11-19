using System.Text;
using FunInActionApi.Data;
using FunInActionApi.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Scalar.AspNetCore;

var builder = WebApplication.CreateBuilder(args);

// -----------------------------------------------------------------------------
// DATABASE
// -----------------------------------------------------------------------------

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("Default")));


// -----------------------------------------------------------------------------
// CONFIGURATION
// -----------------------------------------------------------------------------

builder.Services.Configure<JwtOptions>(
    builder.Configuration.GetSection("Jwt"));

builder.Services.Configure<StripeOptions>(
    builder.Configuration.GetSection("Stripe"));


// -----------------------------------------------------------------------------
// SERVICES
// -----------------------------------------------------------------------------

builder.Services.AddSingleton<
    IPasswordHasher<AppUser>,
    PasswordHasher<AppUser>>();

builder.Services.AddSingleton<TokenService>();

builder.Services.AddScoped<
    IPaymentGateway,
    StripePaymentGateway>();


// -----------------------------------------------------------------------------
// JWT AUTHENTICATION
// -----------------------------------------------------------------------------

var jwt = builder.Configuration
    .GetSection("Jwt")
    .Get<JwtOptions>() ?? new JwtOptions();

if (string.IsNullOrWhiteSpace(jwt.Key) || jwt.Key.Length < 32)
{
    throw new InvalidOperationException(
        "Jwt:Key must be at least 32 characters.");
}

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        // Keep claim names exactly as written
        // ("sub", "role", "name", etc.).
        options.MapInboundClaims = false;

        options.TokenValidationParameters =
            new TokenValidationParameters
            {
                ValidateIssuer = true,
                ValidIssuer = jwt.Issuer,

                ValidateAudience = true,
                ValidAudience = jwt.Audience,

                ValidateIssuerSigningKey = true,

                IssuerSigningKey =
                    new SymmetricSecurityKey(
                        Encoding.UTF8.GetBytes(jwt.Key)),

                ValidateLifetime = true,

                NameClaimType = "name",
                RoleClaimType = "role"
            };
    });

builder.Services.AddAuthorization();


// -----------------------------------------------------------------------------
// CONTROLLERS
// -----------------------------------------------------------------------------

builder.Services.AddControllers(options =>
{
    options.SuppressImplicitRequiredAttributeForNonNullableReferenceTypes = true;
});


// -----------------------------------------------------------------------------
// OPENAPI
// -----------------------------------------------------------------------------

builder.Services.AddOpenApi();


// -----------------------------------------------------------------------------
// BUILD APPLICATION
// -----------------------------------------------------------------------------

var app = builder.Build();


// -----------------------------------------------------------------------------
// DATABASE + SEEDING
// -----------------------------------------------------------------------------

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider
        .GetRequiredService<AppDbContext>();

    // Apply EF Core migrations.
    db.Database.Migrate();

    // Add initial development data.
    await DbSeeder.SeedAsync(
        db,
        scope.ServiceProvider
            .GetRequiredService<IPasswordHasher<AppUser>>(),
        app.Configuration);
}


// -----------------------------------------------------------------------------
// MIDDLEWARE
// -----------------------------------------------------------------------------

if (!app.Environment.IsDevelopment())
{
    app.UseHttpsRedirection();
}

app.UseAuthentication();

app.UseAuthorization();


// -----------------------------------------------------------------------------
// API CONTROLLERS
// -----------------------------------------------------------------------------

app.MapControllers();


// -----------------------------------------------------------------------------
// OPENAPI + SCALAR
// -----------------------------------------------------------------------------

// OpenAPI JSON:
// https://localhost:xxxx/openapi/v1.json
app.MapOpenApi();

// Scalar:
// https://localhost:xxxx/scalar
app.MapScalarApiReference();


// -----------------------------------------------------------------------------
// RUN
// -----------------------------------------------------------------------------

app.Run();