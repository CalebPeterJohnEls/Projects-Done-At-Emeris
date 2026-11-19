namespace FunInActionApi.Data;

public class ContractSignature
{
    public int Id { get; set; }
    public string ContractId { get; set; }
    public string UserId { get; set; }
    public DateTime SignedAt { get; set; }
}