using Microsoft.AspNetCore.Mvc;
using Smart_X_API.Models;
using Smart_X_API.Services;

// DeploymentController: Exposes endpoints for validating a hierarchical
// (tree-shaped) deployment of facility -> zone -> sub-zone -> device nodes.
// The tree structure and the recursive traversal used to validate it follow
// the Composite design pattern, where individual objects and compositions of
// objects are treated the same way (Refactoring Guru, 2024). The actual
// recursion is performed in DeploymentValidationService (Code-Maze, 2026; The
// IIE, 2026).

namespace Smart_X_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DeploymentController : ControllerBase
    {
        // Injected via the DI container configured in Program.cs
        // (Microsoft, 2026c).
        private readonly DeploymentValidationService _validationService;

        public DeploymentController(DeploymentValidationService validationService)
        {
            _validationService = validationService;
        }

        // POST api/deployment/validate
        // Accepts a DeploymentNode tree in the request body and recursively
        // checks that every node (and all of its children) is configured.
        [HttpPost("validate")]
        public IActionResult ValidateDeployment([FromBody] DeploymentNode root)
        {
            if (root == null)
            {
                return BadRequest(new { message = "A deployment node tree is required." });
            }

            // Delegates the recursive walk of the tree to the service layer
            // (Code-Maze, 2026).
            bool isValid = _validationService.ValidateDeployment(root);

            return Ok(new
            {
                nodeName = root.Name,
                isValid,
                message = isValid
                    ? "Deployment tree is fully configured."
                    : "One or more nodes in this tree are not configured."
            });
        }

        // GET api/deployment/sample
        // Returns a small hard-coded tree so the client can demonstrate the
        // validation logic without needing a real deployment first.
        [HttpGet("sample")]
        public IActionResult GetSampleTree()
        {
            return Ok(_validationService.GetSampleTree());
        }
    }
}

// References:
// Refactoring Guru, 2024. Composite Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/composite> [Accessed 15 March 2026].
// Code-Maze, 2026. C# Basics: Recursion. [online] Available at: <https://code-maze.com/csharp-basics-recursion/> [Accessed 9 September 2026].
// The IIE, 2026. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished.
// Microsoft, 2026c. ASP.NET Core Dependency Injection. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/dependency-injection> [Accessed 1 June 2026].
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