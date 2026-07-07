namespace mvcGLMS.ObserverServices
{
    // Concrete Subject in the Observer pattern (Gang of Four, Behavioural)
    // Holds the current contract status and notifies all registered observers whenever it changes
    // Replaces the inline status check that previously lived directly inside ServiceRequestController
    // (Refactoring Guru, 2024c)
    public class ContractStatusManager : ISubject
    {
        // Internal list of registered observers; using List<T> for straightforward iteration
        // (Microsoft, 2026ao)
        private readonly List<IObserver> _observers = new();

        // Backing field for the current contract status; initialised to empty string
        private string _status = string.Empty;

        // Read-only property exposing the current status to external callers without allowing
        // direct mutation - status must go through SetStatus to trigger notifications
        public string Status => _status;

        // Adds an observer to the notification list only if it is not already registered
        // Prevents duplicate notifications being sent to the same observer (Refactoring Guru, 2024c)
        public void Attach(IObserver observer)
        {
            if (!_observers.Contains(observer))
                _observers.Add(observer);
        }

        // Removes an observer so it no longer receives status change notifications
        public void Detach(IObserver observer)
        {
            _observers.Remove(observer);
        }

        // Iterates over all registered observers and pushes the current status to each one
        // (Refactoring Guru, 2024c)
        public void Notify()
        {
            foreach (var observer in _observers)
                observer.Update(_status);
        }

        // Called whenever a contract status is persisted to the database
        // Only triggers a notification if the status has actually changed, avoiding redundant updates
        // (Microsoft, 2026ax)
        public void SetStatus(string status)
        {
            if (_status != status)
            {
                _status = status;
                Notify();
            }
        }
    }
}

// References:
// Microsoft, 2026ao. List<T> Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.list-1?view=net-10.0> [Accessed 1 June 2026].
// Microsoft, 2026ax. StringComparison Enum. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.stringcomparison?view=net-10.0> [Accessed 1 June 2026].
// Refactoring Guru, 2024c. Observer Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/observer> [Accessed 15 April 2026].