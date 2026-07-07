using PROG2b_POE_MVC.Models;

namespace TestProject
{
    [TestClass]
    public sealed class Test1
    {
        [TestClass]
        public sealed class TestClaimsModel
        {
            [TestMethod]
            public void Test_CreateClaim_AssignsValuesCorrectly()
            {
                // Arrange
                Claims c = new Claims();
                c.User = "Caleb";
                c.Total_Hours = 8;
                c.Hourly_Rate = 200;
                c.Start_Date = new DateTime(2025, 10, 1);
                c.End_Date = new DateTime(2025, 10, 2);

                // Assert
                Assert.AreEqual("Caleb", c.User);
                Assert.AreEqual(8, c.Total_Hours);
                Assert.AreEqual(200, c.Hourly_Rate);
                Assert.AreEqual(new DateTime(2025, 10, 1), c.Start_Date);
                Assert.AreEqual(new DateTime(2025, 10, 2), c.End_Date);
            }

            [TestMethod]
            public void Test_DefaultStatus_IsPending()
            {
               
                Claims c = new Claims();

              
                Assert.AreEqual("Pending", c.Status);
                Assert.AreEqual("Pending", c.Verified);
            }

            [TestMethod]
            public void Test_Timestamp_IsNotNull()
            {
               
                Claims c = new Claims();

               
                Assert.IsNotNull(c.Timestamp);
            }
            [TestMethod]
           
            public void StatusAndVerificationChange()
            {
                // Arrange
                var claim = new Claims { Status = "Pending", Verified = "Pending" };

                // Act
                claim.Status = "Approved";
                claim.Verified = "Approved";

                // Assert
                Assert.AreEqual("Approved", claim.Status, "Claim status did not update correctly.");
                Assert.AreEqual("Approved", claim.Verified, "Claim verification did not update correctly.");
            }
            [TestMethod]
            public void RejectionChange()
            {
                // Arrange
                var claim = new Claims { Status = "Pending", Verified = "Pending" };

                // Act
                claim.Status = "Rejected";
                claim.Verified = "Rejected";

                // Assert
                Assert.AreEqual("Rejected", claim.Status, "Claim status did not update correctly to Rejected.");
                Assert.AreEqual("Rejected", claim.Verified, "Claim verification did not update correctly to Rejected.");
            }

        }
    }
}
//(Microsoft Learn, 2025c)
//(Stack Overflow, 2020)
//@*(Troelsen, and Japikse, 2022)*@
/*References
Bootstrap, 2025a. Grid system. [online] Available at: https://getbootstrap.com/docs/5.0/layout/grid [Accessed 19 October 2025].
Bootstrap, 2025b. Flex utilities. [online] Available at: https://getbootstrap.com/docs/5.0/utilities/flex [Accessed 19 October 2025].
C# Corner, 2025. Comprehensive guide to unit testing with MSTest in C#. [online] Available at: https://www.c-sharpcorner.com/article/comprehensive-guide-to-unit-testing-with-mstest-in-c-sharp [Accessed 18 October 2025].
Microsoft Learn, 2025a. Reserved keywords (Transact-SQL). [online] Available at: https://learn.microsoft.com/en-us/sql/t-sql/language-elements/reserved-keywords-transact-sql?view=sql-server-ver17 [Accessed 20 October 2025].
Microsoft Learn, 2025b. T-SQL naming issues - SQL code analysis. [online] Available at: https://learn.microsoft.com/en-us/sql/tools/sql-database-projects/concepts/sql-code-analysis/t-sql-naming-issues?view=sql-server-ver17 [Accessed 20 October 2025].
Microsoft Learn, 2025c. Unit testing C# code with MSTest - Writing tests and assertions. [online] Available at: https://learn.microsoft.com/en-us/dotnet/core/testing/unit-testing-mstest-writing-tests-assertions [Accessed 20 October 2025].
Stack Overflow, 2021. Uniquely identify the extension change from Excel to Word in ASP.NET C# file. [online] Available at: https://stackoverflow.com/questions/43344606/uniquely-idenitfy-the-extension-change-from-excel-to-word-in-asp-net-c-sharp-fil [Accessed 10 October 2025].
Stack Overflow, 2020. Unit testing C# properties. [online] Available at: https://stackoverflow.com/questions/5876779/unit-testing-c-sharp-properties [Accessed 20 October 2025].
Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
Tutorial Republic, 2025. How to center a column in Bootstrap. [online] Available at: https://www.tutorialrepublic.com/faq/how-to-center-a-column-in-bootstrap.php [Accessed 19 October 2025].
*/
