namespace Smart_X_API.Models
{
    // StreamModels groups the small data classes (models) used by the Command
    // Stream feature. A class with properties holds related data together in
    // one object (Troelsen and Japikse, 2022; Nakov and Kolev et al., 2013).
    // They are nested inside one class so other files can import them all
    // with "using static".
    public class StreamModels
    {
        // One reading arriving from a device, before it has been processed.
        public class StreamReading
        {
            public string DeviceId { get; set; } = string.Empty;
            public string Category { get; set; } = string.Empty;
            public string Metric { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public DateTime Timestamp { get; set; }
        }

        // The latest known state of one device. These objects are stored in
        // a Dictionary, so each device can be found quickly by its id
        // (Nakov and Kolev et al., 2013; The IIE, 2026).
        public class DeviceState
        {
            public string DeviceId { get; set; } = string.Empty;
            public string Category { get; set; } = string.Empty;
            public string Metric { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public DateTime LastSeen { get; set; }
            public bool IsOnline { get; set; } = true;
            public bool HasAlert { get; set; }

            // bool? is a nullable bool: it can be true, false or null. Null
            // means no manual command is active (Troelsen and Japikse, 2022).
            public bool? CommandedState { get; set; }
        }

        // One manual override command. These are pushed onto a Stack so the
        // most recent one can be undone first (Nakov and Kolev et al., 2013;
        // The IIE, 2026).
        public class CommandRecord
        {
            public int Id { get; set; }
            public string DeviceId { get; set; } = string.Empty;
            public bool NewState { get; set; }
            public bool PreviousState { get; set; }
            public DateTime IssuedAt { get; set; }
        }

        // One recommendation shown in the "Suggested Actions" panel.
        // Confidence is a number between 0 and 1.
        public class Suggestion
        {
            public string Kind { get; set; } = string.Empty;
            public string Title { get; set; } = string.Empty;
            public string Reason { get; set; } = string.Empty;
            public string? DeviceId { get; set; }
            public bool? Command { get; set; }
            public double Confidence { get; set; }
            public string ActionType { get; set; } = "none";
            public string ActionLabel { get; set; } = string.Empty;
            public string Term { get; set; } = string.Empty;
            public List<SuggestionReading> Readings { get; set; } = new List<SuggestionReading>();
        }

        // The latest reading of one device, listed inside a suggestion.
        public class SuggestionReading
        {
            public string DeviceId { get; set; } = string.Empty;
            public string Metric { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public bool IsOnline { get; set; }
            public DateTime LastSeen { get; set; }
        }

        // One point on the timeline graph.
        public class HistoryPoint
        {
            public DateTime Time { get; set; }
            public double Value { get; set; }
        }
        // A reading that is still waiting in a queue (shown in the pop-ups).
        // Priority is -1 for the standard queue, which has no priority.
        public class WaitingReading
        {
            public string DeviceId { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public string AlertType { get; set; } = string.Empty;
            public int Priority { get; set; } = -1;
            public DateTime Timestamp { get; set; }
        }

        // A reading that has been processed. Severity is Normal, Warning or
        // Critical, and Deviation is how far the value was from the device's
        // recent average, in percent.
        public class ProcessedReading
        {
            public string DeviceId { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public string Source { get; set; } = string.Empty;
            public string Severity { get; set; } = "Normal";
            public double Deviation { get; set; }
            public string AlertType { get; set; } = string.Empty;
            public int Priority { get; set; } = -1;
            public DateTime ProcessedAt { get; set; }
        }

        // How many duplicate alerts the HashSet has blocked for one alert.
        public class BlockedAlert
        {
            public string Alert { get; set; } = string.Empty;
            public int Blocked { get; set; }
        }

        // Everything the dashboard needs, sent in one response so the page
        // only has to make a single request each time it refreshes.
        public class StreamSnapshot
        {
            public List<DeviceState> Devices { get; set; } = new List<DeviceState>();
            public List<string> ActiveAlerts { get; set; } = new List<string>();
            public List<Suggestion> Suggestions { get; set; } = new List<Suggestion>();
            public List<CommandRecord> CommandHistory { get; set; } = new List<CommandRecord>();
            public List<string> ProcessingLog { get; set; } = new List<string>();
            public List<WaitingReading> StandardWaiting { get; set; } = new List<WaitingReading>();
            public List<WaitingReading> AlertWaiting { get; set; } = new List<WaitingReading>();
            public List<ProcessedReading> RecentStandard { get; set; } = new List<ProcessedReading>();
            public List<ProcessedReading> RecentPriority { get; set; } = new List<ProcessedReading>();
            public List<ProcessedReading> RecentAll { get; set; } = new List<ProcessedReading>();
            public List<BlockedAlert> BlockedAlerts { get; set; } = new List<BlockedAlert>();
            public int StandardQueued { get; set; }
            public int AlertQueued { get; set; }
            public int ProcessedCount { get; set; }
            public int DuplicatesBlocked { get; set; }
            public DateTime ServerTime { get; set; }
        }

        // The data the page sends when the user issues a manual command.
        public class CommandRequest
        {
            public string DeviceId { get; set; } = string.Empty;
            public bool TurnOn { get; set; }
        }

        // The data the page sends when the user searches for a device.
        public class SearchRequest
        {
            public string Term { get; set; } = string.Empty;
        }
    }
}

// References:
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.