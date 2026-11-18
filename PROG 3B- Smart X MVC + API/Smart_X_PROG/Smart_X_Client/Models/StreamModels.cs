namespace Smart_X_Client.Models
{
    // These classes mirror the models in the API project. When the JSON
    // from the API arrives, its property names are matched to these classes,
    // so the client can work with typed objects instead of raw text. This is
    // the same idea as the TelemetryPacket model in Part 1
    // (Troelsen and Japikse, 2022; The IIE, 2026).
    public class StreamModels
    {

        // The latest known state of one device. "bool?" can be true, false
        // or null, where null means no manual command is active
        // (Troelsen and Japikse, 2022).
        public class DeviceState
        {
            public string DeviceId { get; set; } = string.Empty;
            public string Category { get; set; } = string.Empty;
            public string Metric { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public DateTime LastSeen { get; set; }
            public bool IsOnline { get; set; }
            public bool HasAlert { get; set; }
            public bool? CommandedState { get; set; }
        }

        // One manual override command, shown in the command history list.
        public class CommandRecord
        {
            public int Id { get; set; }
            public string DeviceId { get; set; } = string.Empty;
            public bool NewState { get; set; }
            public bool PreviousState { get; set; }
            public DateTime IssuedAt { get; set; }
        }

        // One recommendation for the "Suggested Actions" panel, including the
        // action its button performs.
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

        // A reading still waiting in a queue.
        public class WaitingReading
        {
            public string DeviceId { get; set; } = string.Empty;
            public double Value { get; set; }
            public string Unit { get; set; } = string.Empty;
            public string AlertType { get; set; } = string.Empty;
            public int Priority { get; set; } = -1;
            public DateTime Timestamp { get; set; }
        }

        // A reading that has been processed, with its severity and deviation.
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

        // How many duplicate alerts were blocked for one alert.
        public class BlockedAlert
        {
            public string Alert { get; set; } = string.Empty;
            public int Blocked { get; set; }
        }

        // Everything the dashboard needs, received in a single response.
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

        // The data sent to the API when the user issues a command.
        public class CommandRequest
        {
            public string DeviceId { get; set; } = string.Empty;
            public bool TurnOn { get; set; }
        }

        // The data sent to the API when the user searches.
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