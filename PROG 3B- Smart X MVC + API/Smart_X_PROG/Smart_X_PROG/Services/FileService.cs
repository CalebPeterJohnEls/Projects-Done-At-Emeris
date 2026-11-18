// FileService: Handles saving uploaded IFormFile attachments safely to disk,
// following Microsoft's guidance for accepting uploads in ASP.NET Core
// (Microsoft, 2026z) and the walkthrough in kudvenkat (2024). Files are
// validated by extension and size, then written into a per-sensor folder under
// wwwroot/uploads with a GUID-based file name so that two uploads can never
// collide (Microsoft, 2026k).

namespace Smart_X_API.Services
{
    public class FileService
    {
        // IWebHostEnvironment gives access to the app's web root
        // (wwwroot) and content root paths (Microsoft, 2026m).
        private readonly IWebHostEnvironment _environment;

        // Whitelist of accepted file extensions - anything else is
        // rejected before it touches the file system. Image formats and
        // the common device configuration formats (.cfg, .conf, .ini,
        // .yaml, .yml) are accepted alongside logs and documents.
        private readonly string[] _allowedExtensions =
        {
        ".jpg",
        ".jpeg",
        ".png",
        ".gif",
        ".webp",
        ".pdf",
        ".txt",
        ".log",
        ".json",
        ".xml",
        ".csv",
        ".cfg",
        ".conf",
        ".ini",
        ".yaml",
        ".yml"
    };

        private const long MaxFileSize = 20 * 1024 * 1024;

        public FileService(IWebHostEnvironment environment)
        {
            _environment = environment;
        }

        public async Task<string> SaveFileAsync(IFormFile file, int sensorId)
        {
            if (file == null || file.Length == 0)
                throw new ArgumentException("No file was provided.");

            if (file.Length > MaxFileSize)
                throw new ArgumentException(
                    "File size cannot exceed 20 MB.");

            // Path.GetExtension pulls out just the ".ext" portion of the
            // file name so it can be checked against the whitelist
            // (Microsoft, 2026t).
            var extension =
                Path.GetExtension(file.FileName).ToLowerInvariant();

            if (!_allowedExtensions.Contains(extension))
                throw new ArgumentException(
                    "This file type is not supported.");

            var webRootPath = _environment.WebRootPath;

            if (string.IsNullOrWhiteSpace(webRootPath))
            {
                // Falls back to ContentRootPath/wwwroot if WebRootPath was
                // not configured, using Path.Combine so the correct
                // directory separator is used on any OS (Microsoft, 2026s).
                webRootPath = Path.Combine(
                    _environment.ContentRootPath,
                    "wwwroot");
            }

            // Each sensor gets its own sub-folder, named after its Id
            // (Microsoft, 2026s).
            var uploadsFolder = Path.Combine(
                webRootPath,
                "uploads",
                sensorId.ToString());

            Directory.CreateDirectory(uploadsFolder);

            // A new GUID guarantees a unique file name even if two users
            // upload files with the same original name at the same time
            // (Microsoft, 2026k).
            var uniqueFileName =
                $"{Guid.NewGuid()}{extension}";

            var filePath = Path.Combine(
                uploadsFolder,
                uniqueFileName);

            // The file is copied to disk in chunks through an asynchronous
            // FileStream, so a large upload does not block the request
            // thread or sit fully in memory (Microsoft, 2026aa).
            await using var stream = new FileStream(
                filePath,
                FileMode.Create,
                FileAccess.Write,
                FileShare.None,
                81920,
                useAsync: true);

            await file.CopyToAsync(stream);

            // Returns a web-friendly relative path (forward slashes) that
            // the client can append to the API's base URL to download the
            // file later.
            return Path.Combine(
                "uploads",
                sensorId.ToString(),
                uniqueFileName
            ).Replace("\\", "/");
        }
    }
}

// References:
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025].
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025].
// kudvenkat, 2024. File Upload in ASP.NET Core MVC. [video online] Available at: <https://www.youtube.com/watch?v=aoxEJii70_I> [Accessed 8 June 2026].
// Microsoft, 2026k. Guid.NewGuid Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.guid.newguid> [Accessed 1 June 2026].
// Microsoft, 2026m. IWebHostEnvironment Interface. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.aspnetcore.hosting.iwebhostenvironment> [Accessed 1 June 2026].
// Microsoft, 2026s. Path.Combine Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.io.path.combine> [Accessed 1 June 2026].
// Microsoft, 2026t. Path.GetFileName Method (System.IO). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.io.path.getfilename> [Accessed 10 September 2026].
// Microsoft, 2026z. Upload files in ASP.NET Core. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/mvc/models/file-uploads> [Accessed 10 April 2026].
// Microsoft, 2026aa. FileStream Class (System.IO). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.io.filestream> [Accessed 19 October 2026].
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.