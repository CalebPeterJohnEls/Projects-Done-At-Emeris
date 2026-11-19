using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;

namespace FunInActionApi.Data;

/// <summary>Runs on startup: adds the coach login and the five contracts if the tables are empty.</summary>
public static class DbSeeder
{
    public static async Task SeedAsync(AppDbContext db, IPasswordHasher<AppUser> hasher, IConfiguration config)
    {
        if (!await db.Users.AnyAsync(u => u.Role == "Coach"))
        {
            var coach = new AppUser
            {
                Email = (config["Seed:CoachEmail"] ?? "coach@funinaction.co.za").ToLowerInvariant(),
                FullName = "Head Coach",
                Phone = "+27 82 555 0148",
                Role = "Coach"
            };
            coach.PasswordHash = hasher.HashPassword(coach, config["Seed:CoachPassword"] ?? "Coach@1234");
            db.Users.Add(coach);
        }
       

        if (!await db.Contracts.AnyAsync())
        {
            db.Contracts.AddRange(
                new Contract { Id = "CT-01", Title = "Parent / Guardian Consent Form", Description = "Confirms a guardian has agreed for the player to take part in Fun in Action sessions.", Icon = "fa-file-signature", Status = ContractStatus.Signed },
                new Contract { Id = "CT-02", Title = "Liability & Injury Waiver", Description = "Standard waiver covering minor injury risk during modified game play.", Icon = "fa-shield-halved", Status = ContractStatus.Signed },
                new Contract { Id = "CT-03", Title = "Photography & Media Consent", Description = "Permission to use session photos and camera-tracking footage for coaching feedback.", Icon = "fa-camera", Status = ContractStatus.Pending },
                new Contract { Id = "CT-04", Title = "Fun in Action Programme Terms", Description = "Session structure, refund policy, and coaching points system.", Icon = "fa-scroll", Status = ContractStatus.Signed },
                new Contract { Id = "CT-05", Title = "School Facility Use Agreement — Emeris Partnership", Description = "Covers use of school grounds for Emeris-linked IP and FP 4th year sessions.", Icon = "fa-school", Status = ContractStatus.Pending });
        }

        await db.SaveChangesAsync();
    }
}

// References
// Microsoft (n.d.) PasswordHasher<TUser> class. Available at: https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.identity.passwordhasher-1 (Accessed: 28 September 2026).