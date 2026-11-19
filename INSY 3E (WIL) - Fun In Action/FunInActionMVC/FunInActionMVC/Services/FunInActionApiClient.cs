using System.Net.Http.Json;
using FunInAction.Mvc.Models;
using FunInAction.Mvc.Services.MockData;
using Microsoft.Extensions.Options;

namespace FunInAction.Mvc.Services;

/// <summary>
/// Talks to the real Fun in Action API over HTTP using the typed HttpClient
/// injected by DI (see Program.cs, base address comes from Api:BaseUrl).
///
/// No API exists yet, so every method is written "API-first": it always
/// tries the real HTTP call first. If that call fails for any reason
/// (nothing listening, DNS error, timeout, non-success status) and
/// Api:UseMockFallback is true, it transparently falls back to the shared
/// in-memory MockRepository so the app keeps working. Flip UseMockFallback
/// to false (or just stand up the real endpoints) once the API is live and
/// this class needs no further changes.
/// </summary>
public class FunInActionApiClient : IFunInActionApiClient
{
    private readonly HttpClient _http;
    private readonly MockRepository _mock;
    private readonly ApiOptions _options;
    private readonly ILogger<FunInActionApiClient> _logger;

    public FunInActionApiClient(
        HttpClient http,
        MockRepository mock,
        IOptions<ApiOptions> options,
        ILogger<FunInActionApiClient> logger)
    {
        _http = http;
        _mock = mock;
        _options = options.Value;
        _logger = logger;
    }

    private async Task<T> WithFallback<T>(Func<Task<T>> call, Func<T> fallback, string what)
    {
        try
        {
            return await call();
        }
        catch (Exception ex) when (_options.UseMockFallback)
        {
            _logger.LogWarning(ex, "API call for {What} failed; using placeholder/mock data instead.", what);
            return fallback();
        }
    }

    // ---------- Clients ----------

    public Task<List<Client>> GetClientsAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<List<Client>>("api/clients", ct) ?? new(),
            () => _mock.GetClients(),
            nameof(GetClientsAsync));

    public Task<Client?> GetClientAsync(string id, CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<Client?>($"api/clients/{id}", ct),
            () => _mock.GetClient(id),
            nameof(GetClientAsync));

    public Task<Client> CreateClientAsync(Client client, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PostAsJsonAsync("api/clients", client, ct);
                response.EnsureSuccessStatusCode();
                return await response.Content.ReadFromJsonAsync<Client>(cancellationToken: ct) ?? client;
            },
            () => _mock.AddClient(client),
            nameof(CreateClientAsync));

    public Task<bool> UpdateClientAsync(string id, Client client, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PutAsJsonAsync($"api/clients/{id}", client, ct);
                return response.IsSuccessStatusCode;
            },
            () => _mock.UpdateClient(id, client),
            nameof(UpdateClientAsync));

    public Task<bool> DeleteClientAsync(string id, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.DeleteAsync($"api/clients/{id}", ct);
                return response.IsSuccessStatusCode;
            },
            () => _mock.DeleteClient(id),
            nameof(DeleteClientAsync));

    // ---------- Payments ----------

    public Task<List<PaymentRecord>> GetPaymentHistoryAsync(string? clientId = null, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var query = string.IsNullOrWhiteSpace(clientId) ? "api/payments" : $"api/payments?clientId={clientId}";
                return await _http.GetFromJsonAsync<List<PaymentRecord>>(query, ct) ?? new();
            },
            () => _mock.GetPayments(clientId),
            nameof(GetPaymentHistoryAsync));

    public Task<List<PaymentMethodInfo>> GetPaymentMethodsAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<List<PaymentMethodInfo>>("api/payment-methods", ct) ?? new(),
            MockRepository.GetPaymentMethods,
            nameof(GetPaymentMethodsAsync));

    public Task<ProgrammeFeeSummary> GetProgrammeFeeSummaryAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<ProgrammeFeeSummary>("api/programme-fee", ct) ?? MockRepository.GetProgrammeFeeSummary(),
            MockRepository.GetProgrammeFeeSummary,
            nameof(GetProgrammeFeeSummaryAsync));

    // Coach changes a payment's status (Pending / Confirmed / Failed) from the payments list.
    public Task<bool> UpdatePaymentStatusAsync(string id, PaymentStatus status, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PutAsJsonAsync($"api/payments/{id}/status", new { status }, ct);
                return response.IsSuccessStatusCode;
            },
            () => _mock.UpdatePaymentStatus(id, status),
            nameof(UpdatePaymentStatusAsync));

    // CHANGED: takes the paying client's id so the record shows in their history and in the coach's Client column.
    public Task<PaymentRecord> SubmitCardPaymentAsync(string clientId, CardPaymentViewModel payment, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PostAsJsonAsync("api/payments/card", new { clientId, payment }, ct);
                response.EnsureSuccessStatusCode();
                return await response.Content.ReadFromJsonAsync<PaymentRecord>(cancellationToken: ct)
                       ?? throw new InvalidOperationException("Empty response from payments/card.");
            },
            () => _mock.AddPayment(new PaymentRecord
            {
                ClientId = clientId,   // NEW
                Date = DateTime.UtcNow,
                Description = payment.ItemLabel,
                // strips "R", commas and spaces (South African formatting can use a space as the thousands separator)
                Amount = decimal.TryParse(
                    payment.ItemAmount.Replace("R", "").Replace(",", "").Replace(" ", "").Replace("\u00A0", ""),
                    out var amt) ? amt : 0m,
                Method = "Card (placeholder gateway)",
                Status = PaymentStatus.Pending
            }),
            nameof(SubmitCardPaymentAsync));

    public Task<PaymentRecord> SubmitProofOfPaymentAsync(string clientId, string fileName, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PostAsJsonAsync("api/payments/proof", new { clientId, fileName }, ct);
                response.EnsureSuccessStatusCode();
                return await response.Content.ReadFromJsonAsync<PaymentRecord>(cancellationToken: ct)
                       ?? throw new InvalidOperationException("Empty response from payments/proof.");
            },
            () => _mock.AddPayment(new PaymentRecord
            {
                ClientId = clientId,
                Date = DateTime.UtcNow,
                Description = $"Proof of payment uploaded ({fileName})",
                Amount = 0m,
                Method = "EFT + Proof of Payment",
                Status = PaymentStatus.Pending
            }),
            nameof(SubmitProofOfPaymentAsync));

    // ---------- Contracts ----------

    public Task<List<Contract>> GetContractsAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<List<Contract>>("api/contracts", ct) ?? new(),
            () => _mock.GetContracts(),
            nameof(GetContractsAsync));
    public async Task SignContractAsync(string contractId, CancellationToken ct = default)
    {
        var response = await _http.PostAsync($"api/contracts/{Uri.EscapeDataString(contractId)}/sign", null, ct);
        response.EnsureSuccessStatusCode();
    }

    // ---------- Dashboards ----------

    public Task<CoachDashboardViewModel> GetCoachDashboardAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<CoachDashboardViewModel>("api/dashboard/coach", ct) ?? MockRepository.GetCoachDashboard(),
            MockRepository.GetCoachDashboard,
            nameof(GetCoachDashboardAsync));

    public Task<ParentDashboardViewModel> GetParentDashboardAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<ParentDashboardViewModel>("api/dashboard/parent", ct) ?? MockRepository.GetParentDashboard(),
            MockRepository.GetParentDashboard,
            nameof(GetParentDashboardAsync));

    // ---------- Profile ----------

    public Task<UserProfile> GetProfileAsync(CancellationToken ct = default) =>
        WithFallback(
            async () => await _http.GetFromJsonAsync<UserProfile>("api/profile", ct) ?? _mock.GetProfile(),
            () => _mock.GetProfile(),
            nameof(GetProfileAsync));

    public Task<bool> UpdateProfileAsync(UserProfile profile, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PutAsJsonAsync("api/profile", profile, ct);
                return response.IsSuccessStatusCode;
            },
            () => { _mock.UpdateProfile(profile); return true; },
            nameof(UpdateProfileAsync));

    public Task<bool> ChangePasswordAsync(ChangePasswordViewModel model, CancellationToken ct = default) =>
        WithFallback(
            async () =>
            {
                var response = await _http.PostAsJsonAsync("api/profile/change-password", model, ct);
                return response.IsSuccessStatusCode;
            },
            () => _mock.ChangePassword(model.CurrentPassword, model.NewPassword),
            nameof(ChangePasswordAsync));
}