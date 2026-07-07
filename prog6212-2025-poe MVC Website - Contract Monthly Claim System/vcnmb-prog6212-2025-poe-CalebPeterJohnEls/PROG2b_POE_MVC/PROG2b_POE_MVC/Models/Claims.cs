using System.ComponentModel.DataAnnotations;

namespace PROG2b_POE_MVC.Models
{/*Model for claim data*/
    public class Claims
    {
        [Key]
        public int Claim_ID { get; set; }

        [Display(Name = "User")]
        [Required(ErrorMessage = "Please Enter Name")]
        public string User { get; set; }

        public string Name { get; set; }
        [Display(Name = "Surname")]
        [Required(ErrorMessage = "Please Enter Surname")]
        public string Surname { get; set; }

        [DataType(DataType.Date)]
        [Required(ErrorMessage = "Please Enter Start date")]
        [Display(Name = "Start Date")]
        public DateTime Start_Date { get; set; }

        [DataType(DataType.Date)]
        [Required(ErrorMessage = "Please Enter End date")]
        [Display(Name = "End Date")]
        public DateTime End_Date { get; set; }

        [Required(ErrorMessage = "Please Enter Total Hours")]
        [Display(Name = "Total hours")]
        public int Total_Hours { get; set; }


        [Display(Name = "Hourly Rate")]
        [Required(ErrorMessage = "Please Enter Hourly Rate")]
        public int Hourly_Rate { get; set; }

        public int Total_Amount { get; set; }
        public string Status { get; set; } = "Pending";
        public string Verified { get; set; } = "Pending";
        public string? DocumentPath { get; set; }  // stores encrypted file name
        [Required(ErrorMessage = "Please Enter Addtional Notes")]
        public string Comment { get; set; } = "No Comment";
        [Display(Name = "Submitted Date")]
        public DateTimeOffset? Timestamp { get; set; } = DateTimeOffset.Now;


        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public IFormFile? Document { get; set; } // only for upload form
        // ITableEntity Implementation
        // public string? PartitionKey { get; set; }
        // public string? RowKey { get; set; }

        //  public ETag ETag { get; set; }

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
Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
Tutorial Republic, 2025. How to center a column in Bootstrap. [online] Available at: https://www.tutorialrepublic.com/faq/how-to-center-a-column-in-bootstrap.php [Accessed 19 October 2025].
*/

