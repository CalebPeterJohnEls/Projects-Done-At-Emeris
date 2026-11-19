using Microsoft.EntityFrameworkCore;
using System.Reflection.Emit;

namespace FunInActionApi.Data;

/// <summary>The EF Core "session" with the database. One DbSet per table.</summary>
public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }
    public DbSet<ContractSignature> ContractSignatures => Set<ContractSignature>();

    public DbSet<AppUser> Users => Set<AppUser>();
    public DbSet<Client> Clients => Set<Client>();
    public DbSet<Payment> Payments => Set<Payment>();
    public DbSet<Contract> Contracts => Set<Contract>();

    protected override void OnModelCreating(ModelBuilder b)
    {
        // Two accounts can never share an email.
        b.Entity<AppUser>().HasIndex(u => u.Email).IsUnique();

        // User -> Client link. If the coach deletes a client row, the login stays but ClientId becomes null.
        b.Entity<AppUser>()
            .HasOne(u => u.Client).WithOne()
            .HasForeignKey<AppUser>(u => u.ClientId)
            .OnDelete(DeleteBehavior.SetNull);

        // Keep payment history even if the client row is removed.
        b.Entity<Payment>()
            .HasOne<Client>().WithMany()
            .HasForeignKey(p => p.ClientId)
            .OnDelete(DeleteBehavior.SetNull);

        // Matters for SQL Server / Azure SQL; SQLite ignores it.
        b.Entity<Payment>().Property(p => p.Amount).HasPrecision(18, 2);
        // One signature per user per contract.
        b.Entity<ContractSignature>()
            .HasIndex(s => new { s.ContractId, s.UserId })
            .IsUnique();

    }
}

// References
// Microsoft (n.d.) Creating and configuring a model. Available at: https://learn.microsoft.com/en-us/ef/core/modeling/ (Accessed: 28 September 2026).