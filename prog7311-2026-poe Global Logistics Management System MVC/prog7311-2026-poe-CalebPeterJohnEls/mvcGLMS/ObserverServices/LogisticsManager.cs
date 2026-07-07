namespace mvcGLMS.ObserverServices
{
    // Concrete Observer in the Observer pattern - receives status updates from the subject
    // and enforces the business rule that service requests can only be raised on Active contracts
    // (Refactoring Guru, 2024c)
    public class LogisticsManager : IObserver
    {
        // Tracks whether the current contract status allows new service requests to be created
        // Defaults to true; gets updated whenever the observed contract changes status
        public bool CanCreateServiceRequests { get; private set; } = true;
 
        // Called by the subject (ContractStatusManager) when contract status changes
        // Only "Active" contracts permit new service requests; Expired and On Hold do not
        // Using a simple equality check rather than an enum to stay consistent with the string-based
        // status values stored in the database (Microsoft, 2026ax)
        public void Update(string status)
        {
            CanCreateServiceRequests = status == "Active";
        }
    }
}

// References:
// Microsoft, 2026ax. StringComparison Enum. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.stringcomparison?view=net-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].