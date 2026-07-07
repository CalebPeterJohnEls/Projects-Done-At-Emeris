namespace mvcGLMS.ObserverServices
{
    // Observer interface for the Observer pattern (Gang of Four, Behavioural)
    // Any component that needs to react to contract status changes implements this
    // Concrete implementations include LogisticsManager and BillingSystem (Refactoring Guru, 2024c)
    public interface IObserver
    {
        // Called by the subject whenever contract status changes
        // The string status parameter keeps the interface loosely coupled from any specific enum or model
        // (Microsoft, 2026ax)
        void Update(string status);
    }
}

// References:
// Microsoft, 2026ax. StringComparison Enum. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.stringcomparison?view=net-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].