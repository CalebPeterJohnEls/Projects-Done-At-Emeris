using Microsoft.AspNetCore.Mvc;
using Smart_X_API.Models;
using Smart_X_API.Services;
using static Smart_X_API.Models.StreamModels;

// TelemetryController: Receives and returns telemetry readings for three
// metric types (temperature, power and actuator state). Each type reuses the
// same generic TelemetryPacket<T> model (Microsoft, 2026j; Troelsen and
// Japikse, 2022), so this controller only differs in the concrete type
// argument (float, int, bool) it plugs into that generic. Batch endpoints
// accept a jagged array (T[][]) of raw readings, which is processed by
// TelemetryHistoryService (Microsoft, 2026n).
//
// Part 2: after a reading is stored for Part 1, it is also passed to
// StreamService, which puts it into a Queue (normal readings) or a
// PriorityQueue (critical alerts) for processing (Microsoft, 2026ah;
// Microsoft, 2026ag; The IIE, 2026). A moisture endpoint was also added,
// because moisture readings are only needed by the live stream.

namespace Smart_X_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class TelemetryController : ControllerBase
    {
        private readonly TelemetryService _telemetryService;
        private readonly TelemetryHistoryService _telemetryHistoryService;

        // Part 2: the shared StreamService is supplied through the
        // constructor, in the same way as the Part 1 services
        // (Troelsen and Japikse, 2022).
        private readonly StreamService _stream;

        public TelemetryController(
            TelemetryService telemetryService,
            TelemetryHistoryService telemetryHistoryService,
            StreamService stream)
        {
            _telemetryService = telemetryService;
            _telemetryHistoryService = telemetryHistoryService;
            _stream = stream;
        }

        // POST api/telemetry/temperature
        // Accepts a single temperature reading, using the generic
        // TelemetryPacket<float> (Microsoft, 2026j).
        [HttpPost("temperature")]
        public IActionResult ReceiveTemperature(
            [FromBody] TelemetryPacket<float> packet)
        {
            if (string.IsNullOrWhiteSpace(packet.DeviceIdentifier))
            {
                return BadRequest(new
                {
                    message = "Device identifier is required."
                });
            }

            _telemetryService.AddTemperature(packet);

            // Part 2: send the same reading to the live stream. An object
            // initializer sets each property while the object is created
            // (Troelsen and Japikse, 2022). The category tells the dashboard
            // what kind of device this is.
            _stream.AddReading(new StreamReading
            {
                DeviceId = packet.DeviceIdentifier,
                Category = "Environmental",
                Metric = packet.MetricName,
                Value = packet.Value,
                Unit = packet.Unit,
                Timestamp = packet.Timestamp
            });

            return Ok(new
            {
                message = "Temperature telemetry received successfully.",
                packet
            });
        }

        // GET api/telemetry/temperature
        [HttpGet("temperature")]
        public IActionResult GetTemperature()
        {
            return Ok(_telemetryService.GetTemperature());
        }

        // POST api/telemetry/temperature/batch
        // Accepts a jagged array (float[][]) of readings - one row per
        // device - and flattens it into a list of TelemetryPacket<float>
        // objects via the history service (Microsoft, 2026n).
        [HttpPost("temperature/batch")]
        public IActionResult ReceiveTemperatureBatch(
            [FromBody] float[][] rawTelemetry)
        {
            var processed = _telemetryHistoryService.ProcessTemperatureHistory(rawTelemetry);

            foreach (var packet in processed)
            {
                _telemetryService.AddTemperature(packet);
            }

            return Ok(new
            {
                message = "Temperature batch processed successfully.",
                count = processed.Count
            });
        }

        // POST api/telemetry/moisture
        // Added for Part 2. Moisture readings reuse TelemetryPacket<float>
        // (Microsoft, 2026j). They go straight to the live stream, where a
        // reading below 20% becomes a critical alert in the priority queue
        // (Microsoft, 2026ag).
        [HttpPost("moisture")]
        public IActionResult ReceiveMoisture([FromBody] TelemetryPacket<float> packet)
        {
            if (string.IsNullOrWhiteSpace(packet.DeviceIdentifier))
            {
                return BadRequest(new
                {
                    message = "Device identifier is required."
                });
            }

            _stream.AddReading(new StreamReading
            {
                DeviceId = packet.DeviceIdentifier,
                Category = "Environmental",
                Metric = "Moisture",
                Value = packet.Value,
                Unit = packet.Unit,
                Timestamp = packet.Timestamp
            });

            return Ok(new
            {
                message = "Moisture telemetry received successfully.",
                packet
            });
        }

        // POST api/telemetry/power
        // Same pattern as temperature, but using TelemetryPacket<int> for
        // whole-watt power readings (Microsoft, 2026j).
        [HttpPost("power")]
        public IActionResult ReceivePower(
            [FromBody] TelemetryPacket<int> packet)
        {
            if (string.IsNullOrWhiteSpace(packet.DeviceIdentifier))
            {
                return BadRequest(new
                {
                    message = "Device identifier is required."
                });
            }

            _telemetryService.AddPower(packet);

            // Part 2: send the reading to the live stream. A power reading
            // above 3000 W goes into the priority queue as a critical alert
            // (Microsoft, 2026ag).
            _stream.AddReading(new StreamReading
            {
                DeviceId = packet.DeviceIdentifier,
                Category = "PowerConsumption",
                Metric = packet.MetricName,
                Value = packet.Value,
                Unit = packet.Unit,
                Timestamp = packet.Timestamp
            });

            return Ok(new
            {
                message = "Power telemetry received successfully.",
                packet
            });
        }

        // GET api/telemetry/power
        [HttpGet("power")]
        public IActionResult GetPower()
        {
            return Ok(_telemetryService.GetPower());
        }

        // POST api/telemetry/actuator
        // Same pattern again, using TelemetryPacket<bool> for on/off
        // actuator (e.g. valve) state.
        [HttpPost("actuator")]
        public IActionResult ReceiveActuator(
            [FromBody] TelemetryPacket<bool> packet)
        {
            if (string.IsNullOrWhiteSpace(packet.DeviceIdentifier))
            {
                return BadRequest(new
                {
                    message = "Device identifier is required."
                });
            }

            _telemetryService.AddActuator(packet);

            // Part 2: send the state to the live stream. The bool is changed
            // to 1 (on) or 0 (off), so every kind of device can be stored
            // with a numeric value (Troelsen and Japikse, 2022).
            _stream.AddReading(new StreamReading
            {
                DeviceId = packet.DeviceIdentifier,
                Category = "Actuator",
                Metric = packet.MetricName,
                Value = packet.Value ? 1 : 0,
                Unit = packet.Unit,
                Timestamp = packet.Timestamp
            });

            return Ok(new
            {
                message = "Actuator telemetry received successfully.",
                packet
            });
        }

        // GET api/telemetry/actuator
        [HttpGet("actuator")]
        public IActionResult GetActuator()
        {
            return Ok(_telemetryService.GetActuator());
        }

        // POST api/telemetry/meters/aggregate
        // Demonstrates operator overloading: Meter defines a custom '+'
        // operator (see Models/Meter.cs), so two Meter instances can be
        // added directly with "+" instead of a named method
        // (Microsoft, 2026q).
        [HttpPost("meters/aggregate")]
        public IActionResult AggregateMeters([FromBody] MeterAggregateRequest request)
        {
            var total = request.First + request.Second;

            return Ok(total);
        }

        // GET api/telemetry/count
        // Simple diagnostic endpoint returning how many telemetry packets
        // have been received in total across all three metric types.
        [HttpGet("count")]
        public IActionResult GetTelemetryCount()
        {
            return Ok(new
            {
                totalTelemetry =
                    _telemetryService.GetTotalTelemetryCount()
            });
        }
    }
}

// References:
// Microsoft, 2026ag. PriorityQueue<TElement,TPriority> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.priorityqueue-2> [Accessed 17 October 2026].
// Microsoft, 2026ah. Queue<T> Class (System.Collections.Generic). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.queue-1> [Accessed 17 October 2026].
// Microsoft, 2026j. Generic types and methods. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/fundamentals/types/generics> [Accessed 9 September 2026].
// Microsoft, 2026n. Jagged Arrays (C# Programming Guide). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/arrays/jagged-arrays> [Accessed 8 September 2026].
// Microsoft, 2026q. Operator overloading. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/operator-overloading> [Accessed 8 September 2026].
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.
/*IIE Emeris School of Computer Science, 2025a. CLDV6211 -
Playlist. [video online] Available at:
<https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK>
[Accessed 18 March 2025].



IIE Emeris School of Computer Science, 2025b. CLDV6212 -
Playlist. [video online] Available at:
<https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7>
[Accessed 20 September 2025].



Nakov, S. and Kolev, V. et al., 2013. Fundamentals of
Computer Programming with C#. Sofia. [ebook] Available at:
<https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf>
[Accessed 9 September 2026].



The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The
Independent Institute of Education: Unpublished.



Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6:
Foundational Principles and Practices in Programming. 8th ed. New York: Apress.*/