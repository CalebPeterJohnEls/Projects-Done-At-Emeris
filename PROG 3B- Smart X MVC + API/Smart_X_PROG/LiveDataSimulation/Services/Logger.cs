namespace LiveDataSimulation.Services
{
    // A static class cannot be instantiated: its methods are called directly
    // as Logger.Log(...). That suits a small helper used everywhere in the
    // program (Troelsen and Japikse, 2022; Nakov and Kolev et al., 2013).
    public static class Logger
    {
        // Writes a message to the console with the current time in front, so
        // the output can be matched to what happened on the dashboard.
        public static void Log(string message)
        {
            Console.WriteLine(DateTime.Now.ToString("HH:mm:ss") + "  " + message);
        }
    }
}

// References:
// Nakov, S. and Kolev, V. et al., 2013. Fundamentals of Computer Programming with C#. Sofia. [ebook] Available at: <https://www.introprogramming.info/wp-content/uploads/2013/07/Books/CSharpEn/Fundamentals-of-Computer-Programming-with-CSharp-Nakov-eBook-v2013.pdf> [Accessed 9 September 2026].
// Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational Principles and Practices in Programming. 8th ed. New York: Apress.