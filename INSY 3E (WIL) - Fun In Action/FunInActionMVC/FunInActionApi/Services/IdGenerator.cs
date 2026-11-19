using Microsoft.EntityFrameworkCore;

namespace FunInActionApi.Services;

/// <summary>Makes readable IDs like CL-0001 / PMT-0001 (max existing number + 1).
/// Fine for a student project; a busy production system would use a database sequence instead.</summary>
public static class IdGenerator
{
    public static async Task<string> NextAsync(IQueryable<string> existingIds, string prefix, CancellationToken ct)
    {
        var ids = await existingIds.ToListAsync(ct);
        var start = prefix.Length + 1;   // skip "CL-"
        var max = ids
            .Where(i => i.StartsWith(prefix + "-") && int.TryParse(i[start..], out _))
            .Select(i => int.Parse(i[start..]))
            .DefaultIfEmpty(0)
            .Max();
        return $"{prefix}-{max + 1:0000}";
    }
}

// References
// Microsoft (n.d.) Querying data in EF Core. Available at: https://learn.microsoft.com/en-us/ef/core/querying/ (Accessed: 28 September 2026).
