using Smart_X_API.Models;
using System.Collections.Concurrent;
using System.Text.Json;
using System.Text.Json.Serialization;

// SensorService: "Repository" for Sensor objects, held in memory and saved
// to a JSON file so registered sensors survive an API restart. Because this
// service is registered as a Singleton (see Program.cs) it can be called
// concurrently by multiple requests, so it uses a ConcurrentDictionary rather
// than a plain Dictionary/List to stay thread-safe (Microsoft, 2026d), and
// Interlocked.Increment to hand out Ids without race conditions (Microsoft,
// 2026l). The file is written with System.Text.Json (Microsoft, 2026ab) and
// File.WriteAllText (Microsoft, 2026aa).

namespace Smart_X_API.Services
{
    public class SensorService
    {

        // ConcurrentDictionary<TKey, TValue> supports safe simultaneous
        // reads/writes from multiple threads without needing to lock
        // manually (Microsoft, 2026d).
        private readonly ConcurrentDictionary<int, Sensor> _sensors = new();
        private int _nextId = 0;

        // The sensors are saved in a "data" folder next to the application.
        // In Docker this folder is mounted as a volume so the file is kept
        // when the container is rebuilt. The enum converter writes the
        // category as text, matching the API's JSON (Microsoft, 2026ab).
        private readonly string _storePath = Path.Combine(AppContext.BaseDirectory, "data", "sensors.json");
        private readonly object _saveLock = new();
        private static readonly JsonSerializerOptions JsonOptions = new()
        {
            WriteIndented = true,
            Converters = { new JsonStringEnumConverter() }
        };

        // Loads the saved sensors if a file exists, otherwise seeds the
        // sample data.
        public SensorService()
        {
            if (!Load())
            {
                SeedData();
            }
        }

        // LINQ's OrderBy/ToList (Enumerable class) is used throughout this
        // service to filter and sort the in-memory collection
        // (Microsoft, 2026g).
        public List<Sensor> GetAll() => _sensors.Values.OrderBy(s => s.Id).ToList();

        public Sensor? GetById(int id) =>
            _sensors.TryGetValue(id, out var sensor) ? sensor : null;

        public List<Sensor> GetByCategory(SensorCategory category) =>
            _sensors.Values.Where(s => s.Category == category).ToList();

        public List<Sensor> GetByZone(string zone) =>
            _sensors.Values
                .Where(s => s.Zone.Equals(zone, StringComparison.OrdinalIgnoreCase))
                .ToList();

        public Sensor Add(Sensor sensor)
        {
            // Interlocked.Increment atomically increases _nextId, avoiding
            // two concurrent requests being handed the same Id
            // (Microsoft, 2026l).
            sensor.Id = Interlocked.Increment(ref _nextId);
            sensor.RegisteredAt = DateTime.UtcNow;
            _sensors[sensor.Id] = sensor;
            Save();
            return sensor;
        }

        // An edit form does not send the attachment list, so the existing
        // attachments are kept instead of being replaced by an empty list.
        public bool Update(int id, Sensor updated)
        {
            if (!_sensors.ContainsKey(id)) return false;

            updated.Id = id;
            updated.RegisteredAt = _sensors[id].RegisteredAt;
            updated.AttachmentPaths = _sensors[id].AttachmentPaths;
            _sensors[id] = updated;
            Save();
            return true;
        }

        public bool Remove(int id)
        {
            var removed = _sensors.TryRemove(id, out _);
            if (removed) Save();
            return removed;
        }

        public bool AddAttachment(int sensorId, string relativePath)
        {
            if (!_sensors.TryGetValue(sensorId, out var sensor)) return false;
            sensor.AttachmentPaths.Add(relativePath);
            Save();
            return true;
        }

        public List<string>? GetAttachments(int sensorId)
        {
            return _sensors.TryGetValue(sensorId, out var sensor)
                ? sensor.AttachmentPaths
                : null;
        }

        // Reads the saved sensors back into the dictionary and continues the
        // Id numbering after the highest saved Id. It returns false when
        // there is nothing usable to load, so the sample data is seeded
        // instead (Microsoft, 2026ab).
        private bool Load()
        {
            try
            {
                if (!File.Exists(_storePath)) return false;

                var saved = JsonSerializer.Deserialize<List<Sensor>>(
                    File.ReadAllText(_storePath), JsonOptions);

                if (saved == null || saved.Count == 0) return false;

                foreach (var sensor in saved)
                {
                    _sensors[sensor.Id] = sensor;
                }

                _nextId = saved.Max(s => s.Id);
                return true;
            }
            catch
            {
                return false;
            }
        }

        // Writes every sensor to the JSON file. A lock makes sure two
        // requests never write the file at the same time, and a failed write
        // is ignored so a read-only disk cannot crash the API
        // (Microsoft, 2026aa; Microsoft, 2026ab).
        private void Save()
        {
            lock (_saveLock)
            {
                try
                {
                    Directory.CreateDirectory(Path.GetDirectoryName(_storePath)!);
                    File.WriteAllText(_storePath, JsonSerializer.Serialize(GetAll(), JsonOptions));
                }
                catch (IOException) { }
                catch (UnauthorizedAccessException) { }
            }
        }

        // Generates ten sample sensors on startup so the API/UI has data to
        // display before any real device registers. Random is seeded (42)
        // so the sample data is reproducible between runs (Microsoft, 2026u).
        private void SeedData()
        {
            var random = new Random(42);
            var zones = new[] { "Zone A", "Zone B", "Zone C", "Zone D" };
            var rooms = new[] { "Greenhouse-1", "Greenhouse-2", "Utility-Room", "Grid-Node" };
            var categories = Enum.GetValues<SensorCategory>();

            for (int i = 0; i < 24; i++)
            {
                var sensor = new Sensor
                {
                    DeviceIdentifier = $"ESP32-{i:0000}",
                    MacAddress = GenerateMac(random),
                    Room = rooms[random.Next(rooms.Length)],
                    Zone = zones[random.Next(zones.Length)],
                    NodeId = $"Node-{random.Next(1, 50)}",
                    Category = categories[random.Next(categories.Length)],
                    Active = random.NextDouble() > 0.1
                };
                Add(sensor);
            }
        }

        // Builds a random-looking MAC address string. "X2" is a standard
        // numeric format string meaning "two-digit uppercase hexadecimal"
        // (Microsoft, 2026x).
        private static string GenerateMac(Random random)
        {
            var bytes = new byte[6];
            random.NextBytes(bytes);
            return string.Join(":", bytes.Select(b => b.ToString("X2")));
        }
    }
}

// References:
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025].
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
// Microsoft, 2026d. ConcurrentDictionary<TKey,TValue> Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.concurrent.concurrentdictionary-2> [Accessed 10 September 2026].
// Microsoft, 2026g. Enumerable Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.linq.enumerable> [Accessed 10 September 2026].
// Microsoft, 2026l. Interlocked.Increment Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.threading.interlocked.increment> [Accessed 10 September 2026].
// Microsoft, 2026u. Random Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.random> [Accessed 8 September 2026].
// Microsoft, 2026x. Standard numeric format strings. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/standard/base-types/standard-numeric-format-strings> [Accessed 10 September 2026].
// Microsoft, 2026aa. File.WriteAllText Method (System.IO). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.io.file.writealltext> [Accessed 19 October 2026].
// Microsoft, 2026ab. How to serialize JSON in C# - .NET. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/standard/serialization/system-text-json/how-to> [Accessed 19 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.