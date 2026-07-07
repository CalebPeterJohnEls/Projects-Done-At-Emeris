using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Speech.Synthesis;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace PROG2A_POE_PART3_CalebEls
{
   
    public class Methods
    {
        
        
            
        UserData userData = new UserData();
        // Class the classes containing the other menus//(Troelsen and Japikse, 2022)
        HowAreYou howAreYou = new HowAreYou();
        Introduction introduction = new Introduction();
        Threats threats = new Threats();
        Defend defend = new Defend();
        TaskManagement taskManagement= new TaskManagement();
        AddReminder addReminder = new AddReminder();
        ActivityLogger activityLogger = new ActivityLogger();   
        private bool expectingName = false;  // track if we are expecting a name next
                                             // // (Oracle, 2016)
        private bool expectingDefend;
        private bool expectingThreats;
        private bool expectingIntroduction;
        private bool expectingHowAreYou;
        private bool expectingTasks;
        private bool expectingReminder;
        // // (Oracle, 2016)
        public string GetResponse(string input)
        {


            // If expecting a name, treat the input as the name and reset flag
            if (expectingName)
            {
                expectingName = false; // reset the flag// (Oracle, 2016)
                userData.name = input; // capture the name as-is
                return "Nice to meet you, " + userData.name + ". How are you feeling today?";
            }

            // Detect greetings and then set the flag to expect a name next
            if (input.Contains("hello") || input.Contains("hi"))
            {
                expectingName = true;  // next input should be the name
                return "Hello there! What's your name?";
            }

            // Handle feelings/emotions ann displays a menu of what the user can ask
            if (input.Contains("great") || input.Contains("good") || input.Contains("happy"))
            {
                userData.Emotion = input;//sasves emtion to userdata class
                return "That's awesome to hear! Let's keep the good vibes going!\n" +
                     "\nHeres what you can ask me" +
                    "\n- Questions about me" +
                    "\n- Introduction to cyber-security" +
                    "\n- Different threats"
                    + "\n- Defending yourself against threats"
                    + "\n- Task Management" +
                    "\n - Set Reminder"
                    + "\n - Activity log"
                     + "\n - Enter 'Back' to return to the main menu"
                    + "\n- Exit"; ;
            }
            if (input.Contains("okay") || input.Contains("fine"))
            {
                userData.Emotion = input;
                return "Glad to hear you're doing alright. Let's make today even better!" +
                     "\nHeres what you can ask me" +
                    "\n- Questions about me" +
                    "\n- Introduction to cyber-security" +
                    "\n- Different threats"
                    + "\n- Defending yourself against threats"
                    + "\n- Task Management" +
                    "\n - Set Reminder"
                    + "\n - Activity log"
                    + "\n - Enter 'Back' to return to the main menu"
                    + "\n- Exit"; ;
            }
            if (input.Contains("sad") || input.Contains("tired") || input.Contains("bad"))
            {
                userData.Emotion = input;
                return "Sorry to hear that. Hopefully, this session will brighten things up." +
                    "\nHeres what you can ask me" +
                    "\n- Questions about me" +
                    "\n- Introduction to cyber-security" +
                    "\n- Different threats"
                    + "\n- Defending yourself against threats"
                    + "\n- Task Management" +
                    "\n - Set Reminder"
                    + "\n - Activity log"
                     + "\n - Enter 'Back' to return to the main menu"
                    + "\n- Exit"; ;
            }
            // Follow-up input for specific conversations
            if (expectingDefend)
            {
                expectingDefend = false; // Reset flag after handling (Oracle, 2016)
                return defend.choice4(input, null, userData); // Handle related question
            }

            if (expectingThreats)
            {
                expectingThreats = false; 
                return threats.choice3(input, null, userData); 
            }

            if (expectingIntroduction)
            {
                expectingIntroduction = false; 
                return introduction.choice2(input, null, userData); 
            }

            if (expectingHowAreYou)
            {
                expectingHowAreYou = false;
                return howAreYou.choice1(input, null, userData); 
            }

            if (expectingTasks)
            {
                if (input.Contains("back"))
                {
                    expectingTasks = false; // Exits  if user says back
                    return "Exiting Task Management.";
                }

                return taskManagement.taskChoice(input); 
            }

            if (expectingReminder)
            {
                if (input.Contains("back"))
                {
                    expectingReminder = false; 
                    return "Exiting Reminder.";
                }

                return addReminder.reminderChoice(input); 
            }
           // (Oracle, 2016)

            //A switch that enters into a 'menu' based on what the users asks and the chat bot will give responces based on the relavant classes
            switch (input)
            {
                // User asks a question contain what words are in the case
                case "questions":
                case "about you":
                    expectingHowAreYou = true; // Set flag to route next response
                    return "Sure! What would you like to know about me?";//responces based on question

                //Responces based on cyber sercurity
                case "intro":
                case "introduction":
                    expectingIntroduction = true;
                    return "Sure! What would you like to know about cybersecurity.";

                //Responces based on cyber threats
                case "threat":
                case "threats":
                case "danger":
                case "dangers":
                    expectingThreats = true;
                    return "Sure! What would you like to know about the different cybersecurity threats.";

                //Responces based on threats
                case "defending":
                case "defend":
                case "defense":
                    expectingDefend = true;
                    return "Sure! What would you like to know about defending yourself from cyber threats?";

                // Task management 
                case "tasks":
                case "task":
                case "add a task":
                    expectingTasks = true;
                    return "Sure! You're now in Task Management. What would you like to do?" +
                           "\n- Add Tasks" +
                           "\n- View Tasks" +
                           "\n- Delete Task" +
                           "\n- Type 'back' to exit task management.";

                // Reminder management 
                case "reminder":
                case "reminders":
                case "set reminder":
                    expectingReminder = true; 
                    return "Sure! You're now in Reminder Management. What would you like to do?" +
                           "\n- Add Reminder" +
                           "\n- View Reminders" +
                           "\n- Delete Reminder" +
                           "\n- Type 'back' to exit reminder management.";

                // View activity log
                case "activity":
                case "activity log":

                case "activities":
                case "view activities":
                    return "Sure! Here's your activity log:\n" +
                           ActivityLogger.GetLog(); // Retrieve all stored activity logs

                // Exit the app
                case "exit":
                    Application.Exit(); // Close the application
                    return "Exiting..."; 

                // If input is unrecognized 
                default:
                    return "I didn't quite understand that, " + userData.name + ". Can you say it differently?";
            }
        }
    }
}
//(TseplyaevAF, 2017)
//(Medium, 2019)(Microsoft, 2025d)
//(Troelsen and Japikse, 2022)
/*
 Reference list
PART 1
•	Programming is Fun, 2025. C# Console – Center text using PadLeft. [online] Available at:<https://programmingisfun.com/c-sharp-console-center-text-padleft/> [Accessed 19 April 2025].
•	Stack Overflow, 2012. Centering text in C# console app only working with some input. [online] Available at:<https://stackoverflow.com/questions/12847960/centering-text-in-c-sharp-console-app-only-working-with-some-input> [Accessed 19 April 2025].
•	Stack Overflow, 2014. How to make text be typed out in console application. [online] Available at:<https://stackoverflow.com/questions/25337336/how-to-make-text-be-typed-out-in-console-application> [Accessed 20 April 2025].
•	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress. 

PART 2
•	Stack Overflow, 2016. How to determine if string contains specific substring within the first X characters. [online] Available at: <https://stackoverflow.com/questions/501026/how-to-determine-if-string-contains-specific-substring-within-the-first-x-charac> [Accessed 24 May 2025].
•	Stack Overflow, 2023a. Calling a variable from another class. [online] Available at: <https://stackoverflow.com/questions/1392017/calling-a-variable-from-another-class> [Accessed 24 May 2025].
•	Stack Overflow, 2023b. C# check if a string has a certain word. [online] Available at: <https://stackoverflow.com/questions/40247979/c-sharp-check-if-a-string-has-a-certain-word> [Accessed 24 May 2025].
•	Treehouse, 2019. C#: Ending while loop after X number of times. [online] Available at: <https://teamtreehouse.com/community/c-ending-while-loop-after-x-number-of-times> [Accessed 23 May 2025].
•	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress. 
•	Unity Discussions, 2015. How to get random string from array. [online] Available at: <https://discussions.unity.com/t/how-to-get-random-string-from-array/569644/3> [Accessed 23 May 2025].

PART 3
•	Adnan Zameer, 2015. How to get random items from array or list in C#. [online] Available at: <https://www.adnanzameer.com/2015/05/how-to-get-random-items-from-array-or.html> [Accessed 6 June 2025].
•	Medium, 2019. C# – How to Use RichTextBox in Visual Studio Windows Form. [online] Available at: <https://medium.com/@bdragonpp/c-how-to-use-richtextbox-in-visual-studio-windows-form-cc8799429755> [Accessed 20 June 2025].
•	Microsoft, 2025a. List<T>.Remove Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.list-1.remove?view=net-8.0> [Accessed 20 June 2025].
•	Microsoft, 2025b. Microsoft VisualStudio Shell ActivityLog Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/microsoft.visualstudio.shell.activitylog?view=visualstudiosdk-2022>[Accessed 20 June 2025].
•	Microsoft, 2025c. System.Collections.ArrayList.Add Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.collections.arraylist.add?view=net-9.0>[Accessed 6 June 2025].
•	Microsoft, 2025d. System.Windows.Forms.RichTextBox Class. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.windows.forms.richtextbox?view=windowsdesktop-8.0> [Accessed 20 June 2025].
•	MooICT, 2020. WPF C# Tutorial – Create a Simple Quiz Game in Visual Studio. [online] Available at: <https://www.mooict.com/wpf-c-tutorial-create-a-simple-quiz-game-in-visual-studio/#google_vignette> [Accessed 6 June 2025].
•	Oracle, 2016. Using Flag Variables. [online] Available at: <https://docs.oracle.com/en/applications/jd-edwards/development-tools/9.2.x/eotds/using-flag-variables.html> [Accessed 26 June 2025].
•	Pastebin, 2023. C# Code Snippet NAudio[online] Available at: <https://pastebin.com/hZSZaXci>[Accessed 15 June 2025].
•	Stack Overflow, 2011. C# – ArrayList User Input. [online] Available at: <https://stackoverflow.com/questions/7633152/c-sharp-arraylist-user-input> [Accessed 6 June 2025].
•	Stack Overflow, 2014. C# – How to Create a Log of User Activities. [online] Available at: <https://stackoverflow.com/questions/22872074/c-sharp-how-to-create-a-log-of-a-user-activities>[Accessed 20 June 2025].
•	Stack Overflow, 2017. How to enable a disabled button in code-behind in ASP.NET. [online] Available at: <https://stackoverflow.com/questions/23379976/how-to-enable-a-disabled-button-in-code-behind-in-asp-net> [Accessed 6 June 2025].
•	Stack Overflow, 2018. How Do I Remove a String Array From a List in C#? [online] Available at: <https://stackoverflow.com/questions/49963930/how-do-i-remove-a-string-array-from-a-list-in-c>[Accessed 20 June 2025].
•	Stack Overflow, 2020. C# Windows Form Application – How to take the user’s input from text boxes. [online] Available at: <https://stackoverflow.com/questions/60007694/c-sharp-windows-form-application-how-to-take-the-users-input-from-text-boxes> [Accessed 18 June 2025].
•	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
•	TseplyaevAF, 2017. ChatBot/Form1.cs on GitHub. [online] Available at: <https://github.com/TseplyaevAF/ChatBot/blob/master/Chat%20Bot/Form1.cs>[Accessed 10 June 2025].




 */