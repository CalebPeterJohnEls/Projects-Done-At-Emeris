namespace Smart_X_API.Services
{
    // StreamWorker runs in the background for as long as the API is running.
    // It inherits from BackgroundService, which is the standard way to run a
    // repeating task in ASP.NET Core (Microsoft, 2026a; Troelsen and
    // Japikse, 2022).
    public class StreamWorker : BackgroundService
    {
        private readonly StreamService _stream;

        // The shared StreamService is supplied through the constructor
        // (Troelsen and Japikse, 2022).
        public StreamWorker(StreamService stream)
        {
            _stream = stream;
        }

        // ExecuteAsync runs when the API starts. Every half a second it empties
        // the queues (alerts first). The stopping token tells the loop when
        // the API is shutting down (Microsoft, 2026a).
        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                _stream.ProcessQueues();

                // Waiting for 500 ms lets readings collect in the queues
                // between passes. If the API stops during the wait, the
                // cancellation is caught and the loop ends cleanly
                // (Microsoft, 2026a).
                try
                {
                    await Task.Delay(500, stoppingToken);
                }
                catch (OperationCanceledException)
                {
                    break;
                }
            }
        }
    }
}

// References:
// Microsoft, 2026a. Background tasks with hosted services in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/host/hosted-services> [Accessed 17 October 2026].
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.