namespace FunInAction.Mvc.Models;

/// <summary>
/// A contract / consent form / waiver, as served by GET /api/contracts.
/// </summary>
public class Contract
{
    public string Id { get; set; } = string.Empty;
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Icon { get; set; } = "fa-file-alt";
    public ContractStatus Status { get; set; } = ContractStatus.Pending;
    public string? DocumentUrl { get; set; }
}

public enum ContractStatus
{
    Pending,
    Signed
}
