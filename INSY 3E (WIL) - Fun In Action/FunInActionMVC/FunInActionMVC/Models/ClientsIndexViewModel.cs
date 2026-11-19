namespace FunInAction.Mvc.Models;

/// <summary>
/// View model for the Clients dashboard (list + summary cards).
/// </summary>
public class ClientsIndexViewModel
{
    public List<Client> Clients { get; set; } = new();

    public string? SearchTerm { get; set; }

    public int TotalClients => Clients.Count;

    public int ActiveClients => Clients.Count(c => c.Active);

    public int AddedThisMonth
    {
        get
        {
            var now = DateTime.UtcNow;
            return Clients.Count(c => c.AddedOn.Year == now.Year && c.AddedOn.Month == now.Month);
        }
    }
}
