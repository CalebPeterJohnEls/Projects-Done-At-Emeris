// DeploymentNode: Represents one node in a facility deployment tree (e.g.
// Facility -> Zone -> Sub-Zone -> Device). Each node can contain a list of
// child nodes of the same type, which is the defining feature of the Composite
// design pattern: a "part" (leaf, no children) and a "whole" (branch, with
// children) share the same interface/shape (Refactoring Guru, 2024). This is
// what allows DeploymentValidationService to walk the tree recursively without
// needing to know how deep it is (Code-Maze, 2026).

namespace Smart_X_API.Models
{
    public class DeploymentNode
    {
        public string Name { get; set; } = string.Empty;

        public string NodeType { get; set; } = string.Empty;

        public bool IsConfigured { get; set; }

        // Recursive composition: a DeploymentNode contains a list of more
        // DeploymentNode objects (Refactoring Guru, 2024).
        public List<DeploymentNode> Children { get; set; } = new();
    }
}

// References:
// Refactoring Guru, 2024. Composite Design Pattern. [online] Available at: <https://refactoring.guru/design-patterns/composite> [Accessed 15 March 2026].
// Code-Maze, 2026. C# Basics: Recursion. [online] Available at: <https://code-maze.com/csharp-basics-recursion/> [Accessed 9 September 2026].
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