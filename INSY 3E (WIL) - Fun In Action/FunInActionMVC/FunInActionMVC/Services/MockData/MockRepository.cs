using FunInAction.Mvc.Models;

namespace FunInAction.Mvc.Services.MockData;

/// <summary>
/// Stands in for the real Fun in Action API. Registered as a singleton so
/// data survives between requests for the lifetime of the app (it resets
/// when the app restarts, since there is no database behind it yet).
///
/// FunInActionApiClient calls into this whenever the real HTTP endpoint
/// can't be reached (or Api:UseMockFallback is true), which is what lets
/// this whole app run today even though no API has been built yet.
/// Once a real API exists, give it these same shapes at these same routes
/// (see IFunInActionApiClient) and this class can be deleted.
/// </summary>
public class MockRepository
{
    private readonly object _lock = new();
    private readonly List<Client> _clients;
    private readonly List<PaymentRecord> _payments;
    private readonly List<Contract> _contracts;
    private UserProfile _profile;
    private string _currentPasswordForDemo = "password123";

    public MockRepository()
    {
        _clients = new List<Client>
        {
            new() { Id = "CL-0101", FirstName = "Amara", LastName = "Dlamini", Email = "amara.dlamini@mail.com", Phone = "+27 82 555 0134", Active = true,  AddedOn = new DateTime(2026, 7, 2) },
            new() { Id = "CL-0102", FirstName = "Liam",  LastName = "Okafor",  Email = "liam.okafor@mail.com",   Phone = "+27 71 442 8890", Active = true,  AddedOn = new DateTime(2026, 7, 18) },
            new() { Id = "CL-0103", FirstName = "Sipho",  LastName = "Ndlovu",  Email = "sipho.n@mail.com",       Phone = "+27 63 210 7745", Active = false, AddedOn = new DateTime(2026, 6, 11) },
        };

        _payments = new List<PaymentRecord>
        {
            new() { Id = "PMT-0001", ClientId = "CL-0102", Date = new DateTime(2026, 7, 1),  Description = "Registration Fee",   Amount = 250m, Method = "PayShap",                Status = PaymentStatus.Confirmed },
            new() { Id = "PMT-0002", ClientId = "CL-0102", Date = new DateTime(2026, 7, 25), Description = "Instalment 1 of 3",  Amount = 250m, Method = "Capitec (PayShap)",       Status = PaymentStatus.Confirmed },
            new() { Id = "PMT-0003", ClientId = "CL-0102", Date = new DateTime(2026, 8, 8),  Description = "Instalment 2 of 3",  Amount = 250m, Method = "EFT + Proof of Payment",  Status = PaymentStatus.Confirmed },
            new() { Id = "PMT-0004", ClientId = "CL-0102", Date = new DateTime(2026, 8, 22), Description = "Instalment 3 of 3",  Amount = 250m, Method = "FNB (PayShap)",           Status = PaymentStatus.Confirmed },
        };

        _contracts = new List<Contract>
        {
            new() { Id = "CT-01", Title = "Parent / Guardian Consent Form",                      Description = "Confirms a guardian has agreed for the player to take part in Fun in Action sessions.", Icon = "fa-file-signature", Status = ContractStatus.Signed },
            new() { Id = "CT-02", Title = "Liability & Injury Waiver",                            Description = "Standard waiver covering minor injury risk during modified game play.",                  Icon = "fa-shield-halved",  Status = ContractStatus.Signed },
            new() { Id = "CT-03", Title = "Photography & Media Consent",                          Description = "Permission to use session photos and camera-tracking footage for coaching feedback.",    Icon = "fa-camera",         Status = ContractStatus.Pending },
            new() { Id = "CT-04", Title = "Fun in Action Programme Terms",                        Description = "Session structure, refund policy, and coaching points system.",                          Icon = "fa-scroll",         Status = ContractStatus.Signed },
            new() { Id = "CT-05", Title = "School Facility Use Agreement — Emeris Partnership",    Description = "Covers use of school grounds for Emeris-linked IP and FP 4th year sessions.",             Icon = "fa-school",         Status = ContractStatus.Pending },
        };

        _profile = new UserProfile
        {
            Id = "USR-01",
            FullName = "Demo User",
            Email = "demo.user@funinaction.co.za",
            Phone = "+27 82 555 0148",
            Role = "Coach"
        };
    }

    public List<Client> GetClients()
    {
        lock (_lock) return _clients.OrderBy(c => c.Id).ToList();
    }

    public Client? GetClient(string id)
    {
        lock (_lock) return _clients.FirstOrDefault(c => string.Equals(c.Id, id, StringComparison.OrdinalIgnoreCase));
    }

    public Client AddClient(Client client)
    {
        lock (_lock)
        {
            client.AddedOn = DateTime.UtcNow;
            _clients.Add(client);
            return client;
        }
    }

    public bool ClientIdExists(string id)
    {
        lock (_lock) return _clients.Any(c => string.Equals(c.Id, id, StringComparison.OrdinalIgnoreCase));
    }

    public bool UpdateClient(string id, Client updated)
    {
        lock (_lock)
        {
            var existing = _clients.FirstOrDefault(c => string.Equals(c.Id, id, StringComparison.OrdinalIgnoreCase));
            if (existing is null) return false;

            existing.FirstName = updated.FirstName;
            existing.LastName = updated.LastName;
            existing.Email = updated.Email;
            existing.Phone = updated.Phone;
            existing.Active = updated.Active;
            return true;
        }
    }

    public bool DeleteClient(string id)
    {
        lock (_lock) return _clients.RemoveAll(c => string.Equals(c.Id, id, StringComparison.OrdinalIgnoreCase)) > 0;
    }

    public List<PaymentRecord> GetPayments(string? clientId)
    {
        lock (_lock)
        {
            var query = _payments.AsEnumerable();
            if (!string.IsNullOrWhiteSpace(clientId))
            {
                query = query.Where(p => string.Equals(p.ClientId, clientId, StringComparison.OrdinalIgnoreCase));
            }
            return query.OrderByDescending(p => p.Date).ToList();
        }
    }

    public PaymentRecord AddPayment(PaymentRecord record)
    {
        lock (_lock)
        {
            record.Id = $"PMT-{_payments.Count + 1:0000}";
            _payments.Add(record);
            return record;
        }
    }

    // Coach changes a payment's status (Pending / Confirmed / Failed).
    public bool UpdatePaymentStatus(string id, PaymentStatus status)
    {
        lock (_lock)
        {
            var record = _payments.FirstOrDefault(p => string.Equals(p.Id, id, StringComparison.OrdinalIgnoreCase));
            if (record is null) return false;

            record.Status = status;
            return true;
        }
    }

    public List<Contract> GetContracts()
    {
        lock (_lock) return _contracts.ToList();
    }

    public UserProfile GetProfile()
    {
        lock (_lock) return _profile;
    }

    public void UpdateProfile(UserProfile profile)
    {
        lock (_lock)
        {
            _profile.FullName = profile.FullName;
            _profile.Email = profile.Email;
            _profile.Phone = profile.Phone;
        }
    }

    public bool ChangePassword(string currentPassword, string newPassword)
    {
        lock (_lock)
        {
            if (currentPassword != _currentPasswordForDemo) return false;
            _currentPasswordForDemo = newPassword;
            return true;
        }
    }

    public static List<PaymentMethodInfo> GetPaymentMethods() => new()
    {
        new() { Name = "Peach Payment", Description = "Card gateway option being costed alongside PayGate for online registration payments." },
        new() { Name = "PayGate",       Description = "Alternative card gateway, compared with Peach Payment on setup and transaction cost." },
        new() { Name = "PayShap",       Description = "Real-time bank transfer using just a cellphone number. Capitec and FNB are the preferred banks." },
        new() { Name = "Direct EFT + Proof of Payment", Description = "Pay directly into the bank account and upload proof of payment, which reflects on this table." },
    };

    public static ProgrammeFeeSummary GetProgrammeFeeSummary() => new()
    {
        ProgrammeName = "Fun in Action Programme",
        TotalAmount = 1000m,
        RegistrationFee = 250m,
        SessionsSummary = "4 Saturday sessions · 09:00 – 10:00",
        StatusLabel = "Pay in full or in instalments"
    };

    public static CoachDashboardViewModel GetCoachDashboard() => new()
    {
        ActivePlayers = 42,
        SessionsThisTerm = 4,
        PaymentsPending = 6,
        UpcomingSessions = new List<SessionInfo>
        {
            new() { Date = "Sat, 22 Aug", Time = "09:00 - 10:00", Group = "Group A", Focus = "Attacking stances",                  Status = "Confirmed" },
            new() { Date = "Sat, 29 Aug", Time = "09:00 - 10:00", Group = "Group A", Focus = "Defending & receiving the ball",     Status = "Confirmed" },
            new() { Date = "Sat, 5 Sep",  Time = "09:00 - 10:00", Group = "Group B", Focus = "Grip & footwork",                   Status = "Pending" },
            new() { Date = "Sat, 12 Sep", Time = "09:00 - 10:00", Group = "Group B", Focus = "Moving with the ball",              Status = "Pending" },
        }
    };

    public static ParentDashboardViewModel GetParentDashboard() => new()
    {
        ChildName = "Liam",
        CurrentLevel = "Level 3",
        SessionsAttended = "3 / 4",
        NextSession = "Sat, 29 Aug",
        PaymentStatus = "Paid in Full",
        LatestFeedback = new List<TrainingFeedback>
        {
            new()
            {
                Title = "Attacking Stance",
                Date = "Sat, 22 Aug",
                RatingLabel = "Green",
                Comment = "Great form overall — knee bend and back straightness were both spot on. Keep working on foot width for an even more stable base."
            }
        }
    };
}
