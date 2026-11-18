using Microsoft.AspNetCore.Mvc;
using Smart_X_API.Services;
using static Smart_X_API.Models.StreamModels;

namespace Smart_X_API.Controllers
{
    // StreamController is the entry point for the Command Stream page. It
    // does no processing itself: each action passes the request to
    // StreamService and returns the result. Keeping controllers thin and
    // putting the logic in a service is the same pattern used in Part 1
    // (Troelsen and Japikse, 2022; The IIE, 2026).
    [ApiController]
    [Route("api/[controller]")]
    public class StreamController : ControllerBase
    {
        private readonly StreamService _stream;

        // The shared StreamService is supplied through the constructor
        // (Troelsen and Japikse, 2022).
        public StreamController(StreamService stream)
        {
            _stream = stream;
        }

        // GET api/stream/snapshot
        // Returns everything the dashboard shows. The optional parameters
        // are the search and filter choices of the user.
        [HttpGet("snapshot")]
        public IActionResult Snapshot(string? category, bool alertsOnly = false, string? search = null)
        {
            return Ok(_stream.GetSnapshot(category, alertsOnly, search));
        }

        // GET api/stream/history/{deviceId}
        // Returns the sorted readings used to draw the timeline graph.
        [HttpGet("history/{deviceId}")]
        public IActionResult History(string deviceId)
        {
            return Ok(_stream.GetHistory(deviceId));
        }

        // GET api/stream/pending/{deviceId}
        // A device calls this to collect its next waiting command. HTTP 204
        // (NoContent) means there is nothing waiting.
        [HttpGet("pending/{deviceId}")]
        public IActionResult Pending(string deviceId)
        {
            bool? state = _stream.TakePendingCommand(deviceId);
            if (state == null)
                return NoContent();

            return Ok(new { turnOn = state.Value });
        }

        // POST api/stream/command
        // Issues a manual override. A missing device id is rejected with
        // HTTP 400 (BadRequest).
        [HttpPost("command")]
        public IActionResult Command([FromBody] CommandRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.DeviceId))
                return BadRequest(new { message = "Device identifier is required." });

            return Ok(_stream.IssueCommand(request.DeviceId, request.TurnOn));
        }

        // POST api/stream/undo
        // Reverses the most recent command. HTTP 404 (NotFound) is returned
        // when the stack is empty.
        [HttpPost("undo")]
        public IActionResult Undo()
        {
            CommandRecord? undone = _stream.UndoCommand();
            if (undone == null)
                return NotFound(new { message = "Nothing to undo." });

            return Ok(undone);
        }

        // POST api/stream/search
        // Records what the user searched for, so the pattern engine can
        // learn from it.
        [HttpPost("search")]
        public IActionResult LogSearch([FromBody] SearchRequest request)
        {
            if (!string.IsNullOrWhiteSpace(request.Term))
            {
                _stream.LogSearch(request.Term.Trim());
            }


            return Ok();
        }
    }
}

// References:
// The IIE, 2026. Programming 3B [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.