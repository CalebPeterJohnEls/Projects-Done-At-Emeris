namespace FunInAction.Mvc.Models;

/// <summary>
/// Bound from the "Api" section of appsettings.json. Point BaseUrl at the real
/// Fun in Action API once it exists; until then the app talks to the in-memory
/// mock repository (see Services/MockData) whenever a call fails or
/// UseMockFallback is left on.
/// </summary>
public class ApiOptions
{
    public const string SectionName = "Api";

    public string BaseUrl { get; set; } = string.Empty;

    public int TimeoutSeconds { get; set; } = 8;

    /// <summary>
    /// When true (the default while there is no live API yet), any failed
    /// HTTP call falls back to the in-memory mock data instead of throwing,
    /// so the site stays fully usable during development.
    /// </summary>
    public bool UseMockFallback { get; set; } = true;
}
