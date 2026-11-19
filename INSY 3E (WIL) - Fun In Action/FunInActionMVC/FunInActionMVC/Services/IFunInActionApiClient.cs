using FunInAction.Mvc.Models;

namespace FunInAction.Mvc.Services;

/// <summary>
/// Everything the MVC controllers need from the Fun in Action API.
/// Controllers only ever talk to this interface, never to HttpClient directly,
/// so the day a real API is deployed the only thing that changes is
/// FunInActionApiClient's implementation (or just appsettings' Api:BaseUrl).
/// </summary>
public interface IFunInActionApiClient
{
    // Clients
    Task<List<Client>> GetClientsAsync(CancellationToken ct = default);
    Task<Client?> GetClientAsync(string id, CancellationToken ct = default);
    Task<Client> CreateClientAsync(Client client, CancellationToken ct = default);
    Task<bool> UpdateClientAsync(string id, Client client, CancellationToken ct = default);
    Task<bool> DeleteClientAsync(string id, CancellationToken ct = default);

    // Payments
    Task<List<PaymentRecord>> GetPaymentHistoryAsync(string? clientId = null, CancellationToken ct = default);
    Task<List<PaymentMethodInfo>> GetPaymentMethodsAsync(CancellationToken ct = default);
    Task<ProgrammeFeeSummary> GetProgrammeFeeSummaryAsync(CancellationToken ct = default);
    Task<bool> UpdatePaymentStatusAsync(string id, PaymentStatus status, CancellationToken ct = default);
    Task<PaymentRecord> SubmitCardPaymentAsync(string clientId, CardPaymentViewModel payment, CancellationToken ct = default);   // CHANGED: now takes the paying client's id
    Task<PaymentRecord> SubmitProofOfPaymentAsync(string clientId, string fileName, CancellationToken ct = default);

    // Contracts
    Task<List<Contract>> GetContractsAsync(CancellationToken ct = default);
    Task SignContractAsync(string contractId, CancellationToken ct = default);

    // Dashboards
    Task<CoachDashboardViewModel> GetCoachDashboardAsync(CancellationToken ct = default);
    Task<ParentDashboardViewModel> GetParentDashboardAsync(CancellationToken ct = default);

    // Profile
    Task<UserProfile> GetProfileAsync(CancellationToken ct = default);
    Task<bool> UpdateProfileAsync(UserProfile profile, CancellationToken ct = default);
    Task<bool> ChangePasswordAsync(ChangePasswordViewModel model, CancellationToken ct = default);
}