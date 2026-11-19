using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace FunInActionApi.Migrations
{
    /// <inheritdoc />
    public partial class AddContractSignatures : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "ContractSignatures",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    ContractId = table.Column<int>(type: "int", nullable: false),
                    UserId = table.Column<int>(type: "int", nullable: false),
                    SignedAt = table.Column<DateTime>(type: "datetime2", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ContractSignatures", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "IX_ContractSignatures_ContractId_UserId",
                table: "ContractSignatures",
                columns: new[] { "ContractId", "UserId" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ContractSignatures");
        }
    }
}
