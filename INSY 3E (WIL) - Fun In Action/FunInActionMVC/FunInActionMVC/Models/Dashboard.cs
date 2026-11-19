namespace FunInAction.Mvc.Models;

/// <summary>
/// An upcoming coaching session, shown on the coach dashboard (GET /api/sessions/upcoming).
/// </summary>
public class SessionInfo
{
    public string Date { get; set; } = string.Empty;
    public string Time { get; set; } = string.Empty;
    public string Group { get; set; } = string.Empty;
    public string Focus { get; set; } = string.Empty;
    public string Status { get; set; } = "Pending";
}

/// <summary>
/// The stat cards + upcoming sessions on the coach home page.
/// </summary>
public class CoachDashboardViewModel
{
    public int ActivePlayers { get; set; }
    public int SessionsThisTerm { get; set; }
    public int PaymentsPending { get; set; }
    public List<SessionInfo> UpcomingSessions { get; set; } = new();
}

/// <summary>
/// A single piece of training feedback for a player, shown on the parent home page.
/// </summary>
public class TrainingFeedback
{
    public string Title { get; set; } = string.Empty;
    public string Date { get; set; } = string.Empty;
    public string RatingLabel { get; set; } = string.Empty; // e.g. "Green"
    public string Comment { get; set; } = string.Empty;
}

/// <summary>
/// The stat cards + latest feedback on the parent (client) home page.
/// </summary>
public class ParentDashboardViewModel
{
    public string ChildName { get; set; } = string.Empty;
    public string CurrentLevel { get; set; } = string.Empty;
    public string SessionsAttended { get; set; } = string.Empty;
    public string NextSession { get; set; } = string.Empty;
    public string PaymentStatus { get; set; } = string.Empty;
    public List<TrainingFeedback> LatestFeedback { get; set; } = new();
}
