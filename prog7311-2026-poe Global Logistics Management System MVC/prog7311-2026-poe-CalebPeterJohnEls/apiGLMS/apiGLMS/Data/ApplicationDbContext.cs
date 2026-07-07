using Microsoft.EntityFrameworkCore;
using apiGLMS.Models;
 
namespace apiGLMS.Data
{
    // Manages all database tables for the GLMS API
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }
 
        public DbSet<Client> Clients { get; set; }
        public DbSet<Contract> Contracts { get; set; }
        public DbSet<ServiceRequest> ServiceRequests { get; set; }
        public DbSet<User> Users { get; set; }
    }
}
 