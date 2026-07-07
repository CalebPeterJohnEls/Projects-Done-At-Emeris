using mvcGLMS.ObserverServices;

namespace mvcGLMS.ObserverServices
{
    // Facade service that wraps the Observer pattern components for ASP.NET Core dependency injection
    // Registered as Scoped in Program.cs so each HTTP request gets its own instance
    // Injected into ContractsController and ServiceRequestController (Refactoring Guru, 2024c)
    public class ContractObserverService
    {
        // The concrete subject that holds contract status and drives notifications
        private readonly ContractStatusManager _statusManager;

        // Concrete observers exposed publicly so controllers can read their state after notification
        public readonly LogisticsManager LogisticsManager;
        public readonly BillingSystem BillingSystem;

        // Constructor wires up the full Observer pattern chain:
        // creates the subject, creates both observers, and registers them with the subject
        // ASP.NET Core will resolve this via the DI container (Microsoft, 2026e)
        public ContractObserverService()
        {
            _statusManager   = new ContractStatusManager();
            LogisticsManager = new LogisticsManager();
            BillingSystem    = new BillingSystem();

            // Register both observers with the subject so they receive all future status changes
            _statusManager.Attach(LogisticsManager);
            _statusManager.Attach(BillingSystem);
        }

        // Called after a contract status change is persisted to the database
        // Delegates to ContractStatusManager.SetStatus which triggers Notify() on all observers
        // (Refactoring Guru, 2024c)
        public void OnContractStatusChanged(string newStatus)
            => _statusManager.SetStatus(newStatus);

        // Convenience method used in ServiceRequestController.Create() to enforce the business rule
        // that service requests cannot be raised against Expired or On Hold contracts
        // Sets the status on the subject first so observers are up to date before the check is read
        // (The IIE, 2026)
        public bool CanCreateServiceRequest(string contractStatus)
        {
            _statusManager.SetStatus(contractStatus);
            return LogisticsManager.CanCreateServiceRequests;
        }
    }
}

// References:
// Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.