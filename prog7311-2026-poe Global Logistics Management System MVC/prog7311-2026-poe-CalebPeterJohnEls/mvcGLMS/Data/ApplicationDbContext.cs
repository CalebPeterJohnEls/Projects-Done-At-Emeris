using Microsoft.EntityFrameworkCore;
using mvcGLMS.Models;

namespace mvcGLMS.Data
{
    // Represents the database and manages all tables
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        // Represents the Clients table in the database
        public DbSet<Client> Clients { get; set; }

        // Represents the Contracts table
        public DbSet<Contract> Contracts { get; set; }

        // Represents the ServiceRequests table
        public DbSet<ServiceRequest> ServiceRequests { get; set; }

        // Represents the Users table
        public DbSet<User> Users { get; set; }
    }
}
//@*(Troelsen, and Japikse, 2022; IIE Emeris School of Computer Science, 2025a; IIE Emeris School of Computer Science, 2025b; The IIE. 2026.)
/*References

 •	IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025].
•	IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
•	IIE, 2025. Information System 2C [INSY7213 Module Manual]. The Independent Institute of Education: Unpublished.
•	The IIE. 2026. PROGRAMMING 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
•	Microsoft, 2025. IDocument Interface (org.w3c.dom). [online] Available at: < https://learn.microsoft.com/en-us/dotnet/api/org.w3c.dom.idocument?view=net-android-35.0 > [Accessed 22 April 2026].
•	Microsoft, 2026a. CultureInfo Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.globalization.cultureinfo?view=net-10.0> [Accessed 20 April 2026].
•	Microsoft, 2026b. RangeAttribute Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.componentmodel.dataannotations.rangeattribute?view=net-9.0> [Accessed 22 April 2026].
•	Microsoft, 2026c. Testing ASP.NET Core MVC Apps. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/controllers/testing?view=aspnetcore-10.0> [Accessed 19 April 2026].
•	Microsoft, 2026d. Unit Testing C# with xUnit. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/core/testing/unit-testing-csharp-with-xunit> [Accessed 22 April 2026].
•	Microsoft, 2026e. Upload files in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads >[Accessed 10 April 2026].
•	Mozilla Developer Network, 2026. Response.json(). [online] Available at: <https://developer.mozilla.org/en-US/docs/Web/API/Response/json> [Accessed 10 April 2026].
•	O’Reilly Media, 2004. Overriding Currency Formatting. Chapter 14, Section 4. Available at: <https://www.oreilly.com/library/view/asp-net-cookbook/0596003781/ch14s04.html> [Accessed 22 April 2026].
•	Refactoring Guru, 2024a. Strategy Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/strategy> [Accessed 15 April 2026].
•	Refactoring Guru, 2024b. Composite Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/composite> [Accessed 15 March 2026].
•	Stack Overflow, 2010a. Format string by CultureInfo. [online] Available at: <https://stackoverflow.com/questions/1266093/format-string-by-cultureinfo> [Accessed 10 April 2026].
•	Stack Overflow, 2010b. Date range search filter functionality in ASP.NET MVC 5. [online] Available at: <https://stackoverflow.com/questions/46218160/date-range-search-filter-functionality-in-asp-net-mvc-5> [Accessed 10 April 2026].
•	Stack Overflow, 2016. How to update uploaded file and delete the old one in ASP.NET MVC. [online] Available at: <https://stackoverflow.com/questions/31335380/how-to-update-uploaded-file-and-delete-the-old-one-in-asp-net-mvc> [Accessed15 April 2026].
•	The Open Group, 2025. Razor C# Logic. [online] Available at: <https://www.w3schools.com/asp/razor_cs_logic.asp> [Accessed 20 April 2026].
•	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
•	W3Schools, 2025a. CSS Tutorial. [online] Available at: <https://www.w3schools.com/css/> [Accessed 20 April 2026].
•	W3Schools, 2025b. HTML Tutorial. [online] Available at: <https://www.w3schools.com/Html/> [Accessed 20 April 2026].
•	W3Schools, 2025c. JavaScript Fetch API. [online] Available at: <https://www.w3schools.com/js/js_api_fetch.asp> [Accessed 20 April 2026].

 
 */