using PROG2b_POE_MVC.Models;
using Microsoft.EntityFrameworkCore;
using QuestPDF.Infrastructure;

namespace PROG2b_POE_MVC
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);
            QuestPDF.Settings.License = LicenseType.Community;/*License configuration to allow to use the package(QuestPDF, 2025)*/
            builder.Services.AddDbContext<ApplicationDbContext>(options =>
            {
                options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection"));
            });
            // Add services to the container.
            builder.Services.AddControllersWithViews();
            builder.Services.AddDistributedMemoryCache();

            builder.Services.AddSession(options =>

            {

                options.IdleTimeout = TimeSpan.FromMinutes(20);

                // You can set the timeout

                options.Cookie.HttpOnly = true;

                options.Cookie.IsEssential = true;

            });
            var app = builder.Build();

            // Configure the HTTP request pipeline.
            if (!app.Environment.IsDevelopment())
            {
                app.UseExceptionHandler("/Home/Error");
                // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
                app.UseHsts();
            }

            app.UseHttpsRedirection();
            app.UseStaticFiles();

            app.UseRouting();
            app.UseSession();
            app.UseAuthorization();

            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Home}/{action=Index}/{id?}");

            app.Run();
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
Stack Overflow, 2020. Uniquely identify the extension change from Excel to Word in ASP.NET C# file. [online] Available at: https://stackoverflow.com/questions/43344606/uniquely-idenitfy-the-extension-change-from-excel-to-word-in-asp-net-c-sharp-fil [Accessed 10 October 2025].
Stack Overflow, 2020. Unit testing C# properties. [online] Available at: https://stackoverflow.com/questions/5876779/unit-testing-c-sharp-properties [Accessed 20 October 2025].
QuestPDF, 2025. License configuration. [online] Available at: https://www.questpdf.com/license/configuration.html [Accessed 20 October 2025].
Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
Tutorial Republic, 2025. How to center a column in Bootstrap. [online] Available at: https://www.tutorialrepublic.com/faq/how-to-center-a-column-in-bootstrap.php [Accessed 19 October 2025].
*/
