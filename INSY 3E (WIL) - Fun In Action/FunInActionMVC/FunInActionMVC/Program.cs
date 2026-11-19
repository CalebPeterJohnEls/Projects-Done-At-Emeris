using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services;
using FunInAction.Mvc.Services.MockData;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<ApiOptions>(builder.Configuration.GetSection(ApiOptions.SectionName));

builder.Services.AddControllersWithViews();

// Session holds the JWT after login; the handler copies it onto every API call.
builder.Services.AddHttpContextAccessor();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(o =>
{
    o.IdleTimeout = TimeSpan.FromMinutes(120);   // matches the JWT's ExpiryMinutes
    o.Cookie.HttpOnly = true;
    o.Cookie.IsEssential = true;
});
builder.Services.AddTransient<JwtAttachHandler>();

builder.Services.AddSingleton<MockRepository>();

builder.Services.AddHttpClient<IFunInActionApiClient, FunInActionApiClient>((sp, client) =>
{
    var apiOptions = builder.Configuration.GetSection(ApiOptions.SectionName).Get<ApiOptions>() ?? new ApiOptions();
    if (!string.IsNullOrWhiteSpace(apiOptions.BaseUrl))
    {
        client.BaseAddress = new Uri(apiOptions.BaseUrl);
    }
    client.Timeout = TimeSpan.FromSeconds(apiOptions.TimeoutSeconds > 0 ? apiOptions.TimeoutSeconds : 8);
})
.AddHttpMessageHandler<JwtAttachHandler>();

// Used by AccountController for login/register (no token exists yet, so no handler).
builder.Services.AddHttpClient("FunInActionApi", client =>
{
    var apiOptions = builder.Configuration.GetSection(ApiOptions.SectionName).Get<ApiOptions>() ?? new ApiOptions();
    client.BaseAddress = new Uri(apiOptions.BaseUrl);
    client.Timeout = TimeSpan.FromSeconds(apiOptions.TimeoutSeconds > 0 ? apiOptions.TimeoutSeconds : 8);
});

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();
app.UseSession();          // must come before UseAuthorization
app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.Run();