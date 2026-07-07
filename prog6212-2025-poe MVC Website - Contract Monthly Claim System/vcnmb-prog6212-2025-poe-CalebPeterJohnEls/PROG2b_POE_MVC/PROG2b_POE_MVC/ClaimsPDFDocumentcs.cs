using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;
using PROG2b_POE_MVC.Models;
using DocumentFormat.OpenXml.Spreadsheet;
using Colors = QuestPDF.Helpers.Colors;

// This class generates a PDF document using QuestPDF.
// It implements the IDocument interface required by QuestPDF.
public class ClaimPDFDocument : IDocument
{
    // The list of Claims that will be printed in the PDF table.
    public List<Claims> Claims { get; set; }

    // Constructor receives a list of claim objects.
    public ClaimPDFDocument(List<Claims> claims)
    {
        Claims = claims;
    }

    // Returns default PDF metadata.
    public DocumentMetadata GetMetadata() => DocumentMetadata.Default;

    // Builds the PDF layout and content.
    public void Compose(IDocumentContainer container)
    {
        container.Page(page =>
        {
            page.Margin(2, Unit.Centimetre);// 2 cm margins
            page.PageColor(Colors.White);// Background color
            page.DefaultTextStyle(x => x.FontSize(20));// Default text size
            page.Size(PageSizes.A4);// A4 page size

            // Header
            page.Header()
                .Text("Approved Claims Invoice / Report")
                .SemiBold().FontSize(22).FontColor(Colors.Blue.Darken2);

            // Main content area
            page.Content()
                .PaddingVertical(1, Unit.Centimetre)
                .Column(x =>
                {
                    x.Spacing(20);

                    // Claims table
                    x.Item().Table(table =>
                    {
                        // Column layout
                        table.ColumnsDefinition(columns =>
                        {
                            columns.RelativeColumn(1); // ID
                            columns.RelativeColumn(2); // User
                            columns.RelativeColumn(1); // Hours
                            columns.RelativeColumn(1); // Rate
                            columns.RelativeColumn(2); // Amount
                            columns.RelativeColumn(2); // Start Date
                            columns.RelativeColumn(2); // End Date
                            columns.RelativeColumn(3); // Submitted
                            columns.RelativeColumn(2); // Status
                            columns.RelativeColumn(2); // Verified
                        });

                        // Table header
                        table.Header(header =>
                        {
                            header.Cell().Element(HeaderCell).Text("ID");
                            header.Cell().Element(HeaderCell).Text("User");
                            header.Cell().Element(HeaderCell).Text("Hours");
                            header.Cell().Element(HeaderCell).Text("Rate");
                            header.Cell().Element(HeaderCell).Text("Amount");
                            header.Cell().Element(HeaderCell).Text("Start Date");
                            header.Cell().Element(HeaderCell).Text("End Date");
                            header.Cell().Element(HeaderCell).Text("Submitted");
                            header.Cell().Element(HeaderCell).Text("Status");
                            header.Cell().Element(HeaderCell).Text("Verified");

                            // Header cell style
                            static IContainer HeaderCell(IContainer container) =>
                                container
                                    .Border(1)
                                    .Background(Colors.Blue.Darken2)
                                    .DefaultTextStyle(x => x.FontSize(9).SemiBold().FontColor(Colors.White));
                        });

                        // Table data rows
                        foreach (var c in Claims)
                        {
                            table.Cell().Element(DataCell).Text(c.Claim_ID.ToString());
                            table.Cell().Element(DataCell).Text(c.User);
                            table.Cell().Element(DataCell).Text(c.Total_Hours.ToString());
                            table.Cell().Element(DataCell).Text($"R {c.Hourly_Rate}");
                            table.Cell().Element(DataCell).Text($"R {c.Total_Amount}");
                            table.Cell().Element(DataCell).Text(c.Start_Date.ToShortDateString());
                            table.Cell().Element(DataCell).Text(c.End_Date.ToShortDateString());
                            table.Cell().Element(DataCell).Text(c.Timestamp.ToString());
                            table.Cell().Element(DataCell).Text(c.Status);
                            table.Cell().Element(DataCell).Text(c.Verified);

                            // Row cell style
                            static IContainer DataCell(IContainer container) =>
                                container
                                    .Border(1)
                                    .DefaultTextStyle(x => x.FontSize(9));
                        }
                    });
                });

            // Footer
            page.Footer()
                .AlignRight()
                .Text(text =>
                {
                    text.Span("Generated on: ").SemiBold();
                    text.Span(DateTime.Now.ToString("yyyy-MM-dd HH:mm"));
                });
        });
    }
}

/*(Chapsas, 2023; Gavilan, 2023; Microsoft, 2025; QuestPDF, 2025a; QuestPDF, 2025b; QuestPDF, 2025c; Quiroz, 2025)
 * (Troelsen, and Japikse, 2022)*@
/*References
 * Part 3
 * Israel Quiroz, 2025. How to EASILY Create a PDF in C# .NET with QuestPDF! [video online] Available at: https://www.youtube.com/watch?v=3qEPv-67iRg [Accessed 18 November 2025].
 * Nick Chapsas, 2023. The Easiest Way to Create PDFs in .NET. [video online] Available at: https://www.youtube.com/watch?v=_M0IgtGWnvE&t=151s [Accessed 18 November 2025].
 *Programming with Felipe Gavilan, 2023. Generating PDFs with C# - Very Easy (two examples). [video online] Available at: https://www.youtube.com/watch?v=bhR4Cmg16gs [Accessed 18 November 2025].
 *Microsoft, 2025. IDocument Interface (org.w3c.dom). [online] Available at: https://learn.microsoft.com/en-us/dotnet/api/org.w3c.dom.idocument?view=net-android-35.0 [Accessed 18 November 2025].
 *QuestPDF, 2025a. API Reference. [online]Available at: https://www.questpdf.com/api-reference [Accessed 18 November 2025].
 *QuestPDF, 2025b. Document Operations. [online] Available at: https://www.questpdf.com/concepts/document-operations.html [Accessed 18 November 2025].
 *QuestPDF, 2025c. Generating Output. [online] Available at: https://www.questpdf.com/concepts/generating-output.html [Accessed 18 November 2025].
 Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.*/