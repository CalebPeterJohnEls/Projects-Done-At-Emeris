using Microsoft.AspNetCore.Mvc;
using Smart_X_API.Models;
using Smart_X_API.Services;

// SensorsController: A standard REST/CRUD controller for the Sensor resource:
// list, get one, create, update and delete. Business logic and storage live in
// SensorService; this controller is only responsible for HTTP concerns (status
// codes, input validation, routing) (The IIE, 2026; IIE Emeris School of
// Computer Science, 2025b).

namespace Smart_X_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class SensorsController : ControllerBase
    {
        private readonly SensorService _sensorService;

        public SensorsController(SensorService sensorService)
        {
            _sensorService = sensorService;
        }

        // GET api/sensors
        [HttpGet]
        public IActionResult GetSensors()
        {
            return Ok(_sensorService.GetAll());
        }

        // GET api/sensors/{id}
        [HttpGet("{id}")]
        public IActionResult GetSensor(int id)
        {
            var sensor = _sensorService.GetById(id);

            if (sensor == null)
            {
                return NotFound(new
                {
                    message = "Sensor was not found."
                });
            }

            return Ok(sensor);
        }

        // POST api/sensors
        // Basic manual validation before handing the sensor to the service
        // layer to be stored and assigned an Id.
        [HttpPost]
        public IActionResult CreateSensor([FromBody] Sensor sensor)
        {
            if (string.IsNullOrWhiteSpace(sensor.DeviceIdentifier))
            {
                return BadRequest(new
                {
                    message = "Device identifier is required."
                });
            }

            if (string.IsNullOrWhiteSpace(sensor.MacAddress))
            {
                return BadRequest(new
                {
                    message = "MAC address is required."
                });
            }

            var createdSensor = _sensorService.Add(sensor);

            // CreatedAtAction returns a 201 response with a Location header
            // pointing back at GetSensor, following standard REST practice.
            return CreatedAtAction(
                nameof(GetSensor),
                new { id = createdSensor.Id },
                createdSensor
            );
        }

        // PUT api/sensors/{id}
        [HttpPut("{id}")]
        public IActionResult UpdateSensor(int id, [FromBody] Sensor sensor)
        {
            if (string.IsNullOrWhiteSpace(sensor.DeviceIdentifier))
            {
                return BadRequest(new
                {
                    message = "Device identifier is required."
                });
            }

            if (string.IsNullOrWhiteSpace(sensor.MacAddress))
            {
                return BadRequest(new
                {
                    message = "MAC address is required."
                });
            }

            var updated = _sensorService.Update(id, sensor);

            if (!updated)
            {
                return NotFound(new
                {
                    message = "Sensor was not found."
                });
            }

            return Ok(_sensorService.GetById(id));
        }

        // DELETE api/sensors/{id}
        [HttpDelete("{id}")]
        public IActionResult DeleteSensor(int id)
        {
            var removed = _sensorService.Remove(id);

            if (!removed)
            {
                return NotFound(new
                {
                    message = "Sensor was not found."
                });
            }

            return NoContent();
        }
    }
}

// References:
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
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