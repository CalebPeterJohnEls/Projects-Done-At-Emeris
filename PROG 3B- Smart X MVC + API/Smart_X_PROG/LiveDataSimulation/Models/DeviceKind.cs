namespace LiveDataSimulation.Models
{
    // An enum is a fixed list of named options. Using DeviceKind.Power is
    // clearer and safer than using a number or a piece of text that could be
    // mistyped (Nakov and Kolev et al., 2013; Troelsen and Japikse, 2022).
    public enum DeviceKind
    {
        Temperature,
        Power,
        Actuator,
        Moisture
    }
}

// References:
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.