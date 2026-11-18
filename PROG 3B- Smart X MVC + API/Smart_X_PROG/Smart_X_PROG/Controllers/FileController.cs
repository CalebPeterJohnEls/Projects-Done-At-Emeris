using Microsoft.AspNetCore.Mvc;
using Smart_X_API.Services;

// FileController: Handles uploading attachment files (e.g. sensor
// configuration/log files) and linking them to a Sensor, and lists the
// attachments already stored for a given sensor. The actual file-system work
// is delegated to FileService, following the ASP.NET Core guidance on
// accepting IFormFile uploads in a controller action (Microsoft, 2026z;
// kudvenkat, 2024).

namespace Smart_X_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class FileController : ControllerBase
    {
        private readonly FileService _fileService;
        private readonly SensorService _sensorService;

        public FileController(FileService fileService, SensorService sensorService)
        {
            _fileService = fileService;
            _sensorService = sensorService;
        }

        // POST api/file/{sensorId}
        // Accepts a multipart/form-data upload (IFormFile) for an existing
        // sensor, validates the sensor exists, then hands the file to
        // FileService for saving to disk (Microsoft, 2026z).
        [HttpPost("{sensorId}")]
        public async Task<IActionResult> Upload(int sensorId, IFormFile file)
        {
            var sensor = _sensorService.GetById(sensorId);
            if (sensor == null)
            {
                return NotFound(new
                {
                    message = $"Sensor {sensorId} was not found."
                });
            }

            if (file == null || file.Length == 0)
            {
                return BadRequest(new
                {
                    message = "No file was provided."
                });
            }

            try
            {
                // SaveFileAsync performs the extension/size checks and
                // writes the file under wwwroot/uploads (Microsoft, 2026z).
                var path = await _fileService.SaveFileAsync(file, sensorId);

                // Record the relative path against the sensor so it can be
                // listed/downloaded later.
                _sensorService.AddAttachment(sensorId, path);

                return Ok(new
                {
                    message = "File uploaded and linked to sensor successfully.",
                    sensorId,
                    path
                });
            }
            catch (ArgumentException ex)
            {
                // FileService throws ArgumentException for validation
                // failures (wrong extension, file too large, etc.).
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/file/{sensorId}
        // Returns the list of attachment paths already stored for a sensor.
        [HttpGet("{sensorId}")]
        public IActionResult GetAttachments(int sensorId)
        {
            var sensor = _sensorService.GetById(sensorId);
            if (sensor == null)
            {
                return NotFound(new
                {
                    message = $"Sensor {sensorId} was not found."
                });
            }

            var attachments = _sensorService.GetAttachments(sensorId) ?? new List<string>();

            return Ok(new
            {
                sensorId,
                count = attachments.Count,
                attachments
            });
        }
    }
}

// References:
// Microsoft, 2026z. Upload files in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
// kudvenkat, 2024. File Upload in ASP.NET Core MVC. [video online] Available at: <https://www.youtube.com/watch?v=aoxEJii70_I> [Accessed 8 June 2026].
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