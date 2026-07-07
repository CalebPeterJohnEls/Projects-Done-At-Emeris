namespace mvcGLMS.ObserverServices
{
    // Concrete Observer in the Observer pattern (Gang of Four, Behavioural)
    // Reacts to contract status changes for billing purposes - enabling or halting charges
    // depending on the status received from the subject (Refactoring Guru, 2024c)
    public class BillingSystem : IObserver
    {
        // Tracks whether billing is currently active for the observed contract
        // Defaults to false until an Active status is received
        public bool IsBillingActive { get; private set; } = false;

        // Called by ContractStatusManager.Notify() whenever contract status changes
        // Updates IsBillingActive and logs a billing action message to the console
        public void Update(string status)
        {
            IsBillingActive = status == "Active";

            // Switch expression maps each known status to a billing action message
            // The discard pattern _ handles any unrecognised status values gracefully
            // (Microsoft, 2026az)
            var msg = status switch
            {
                "Active"   => "Billing ENABLED for contract.",
                "Expired"  => "Contract EXPIRED — final invoice issued, billing halted.",
                "On Hold"  => "Contract ON HOLD — billing suspended.",
                "Draft"    => "Contract in DRAFT — billing not started.",
                _          => $"Unrecognised status '{status}' — no billing action taken."
            };

            // Writes the billing outcome to the console for diagnostic visibility
            // In production this would be replaced with a proper logging service (Microsoft, 2026g)
            Console.WriteLine($"[BillingSystem] {msg}");
        }
    }
}

// References:
// Microsoft, 2026az. switch Expression. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/switch-expression> [Accessed 1 June 2026].
// Microsoft, 2026g. ASP.NET Core Logging. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/logging/?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].