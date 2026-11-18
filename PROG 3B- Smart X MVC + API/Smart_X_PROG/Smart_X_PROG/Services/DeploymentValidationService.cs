using Smart_X_API.Models;

// DeploymentValidationService: Validates a DeploymentNode tree by recursively
// checking that a node and every one of its descendants have IsConfigured =
// true. Because DeploymentNode follows the Composite pattern (a node can
// itself contain more nodes), the same recursive method works no matter how
// many levels deep the tree goes (Refactoring Guru, 2024; Code-Maze, 2026).

namespace Smart_X_API.Services
{
    public class DeploymentValidationService
    {

        // Recursive validation: the base case is a node with no children
        // (an empty foreach simply does nothing), and the recursive case
        // calls ValidateDeployment on each child before returning true
        // (Code-Maze, 2026).
        public bool ValidateDeployment(DeploymentNode node)
        {
            if (node == null) return false;
            if (!node.IsConfigured) return false;

            foreach (var child in node.Children)
            {
                if (!ValidateDeployment(child))
                {
                    return false;
                }
            }

            return true;
        }

        // Builds a small four-level sample tree (Facility -> Zone ->
        // Sub-Zone -> Device) purely for demonstration/testing of the
        // validation logic above.
        public DeploymentNode GetSampleTree()
        {
            return new DeploymentNode
            {
                Name = "Facility A",
                NodeType = "Facility",
                IsConfigured = true,
                Children = new List<DeploymentNode>
                {
                    new DeploymentNode
                    {
                        Name = "Zone 1",
                        NodeType = "Zone",
                        IsConfigured = true,
                        Children = new List<DeploymentNode>
                        {
                            new DeploymentNode
                            {
                                Name = "Sub-Zone B",
                                NodeType = "SubZone",
                                IsConfigured = true,
                                Children = new List<DeploymentNode>
                                {
                                    new DeploymentNode
                                    {
                                        Name = "ESP32-0042",
                                        NodeType = "Device",
                                        IsConfigured = false,
                                        Children = new List<DeploymentNode>()
                                    }
                                }
                            }
                        }
                    }
                }
            };
        }
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