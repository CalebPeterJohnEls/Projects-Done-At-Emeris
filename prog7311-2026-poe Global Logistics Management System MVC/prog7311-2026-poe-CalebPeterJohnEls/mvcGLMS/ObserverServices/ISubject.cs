namespace mvcGLMS.ObserverServices
{
    // Subject interface for the Observer pattern (Gang of Four, Behavioural)
    // Any class that needs to broadcast state changes to registered observers implements this
    // (Refactoring Guru, 2024c)
    public interface ISubject
    {
        // Registers an observer to receive notifications when state changes
        // Attach and Detach allow runtime management of the observer list (Refactoring Guru, 2024c)
        void Attach(IObserver observer);

        // Removes a previously registered observer so it no longer receives notifications
        void Detach(IObserver observer);

        // Loops through all registered observers and calls their Update method
        // Dependency injection keeps observers loosely coupled from the subject (Microsoft, 2026e)
        void Notify();
    }
}

// References:
// Microsoft, 2026e. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection?view=aspnetcore-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].