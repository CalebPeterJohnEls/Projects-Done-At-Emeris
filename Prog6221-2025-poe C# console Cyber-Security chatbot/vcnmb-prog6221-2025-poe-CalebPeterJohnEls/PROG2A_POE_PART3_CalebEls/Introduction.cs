using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualBasic.ApplicationServices;

namespace PROG2A_POE_PART3_CalebEls
{
    public class Introduction
    {
        public delegate string TipSelector(List<string> tips);// called as a delegate
        private int questionCounter = 0;
        private int maxQuestions = 3;
        public string choice2(string input, object value, UserData user)
        {
            //(Unity Discussions, 2015)
            TipSelector tipSelector = GetRandomTip;
            List<string> CybersecurityDefinitionTips = new List<string>
            {
            "Cybersecurity is the practice of protecting your computers, networks, and data against theft, damage, and even unauthorised access.",
            "Cybersecurity helps in preventing cyberattacks that have the potential to disrupt services, steal confidential information, or result in financial losses."
            };

            List<string> CybersecurityImportanceTips = new List<string>
            {
            "So many aspects of our lives take place online — from social media to banking to smart devices — making cybersecurity vital.",
            "It helps avoid identity theft, financial loss, and disruptions to daily life by safeguarding our personal information against theft and misuse.",
            "It's important to protect our data and privacy in a connected society."
            };

            List<string> CybersecurityPrinciplesTips = new List<string>
            {
            "Confidentiality, integrity, and availability are the three fundamental principles of cybersecurity.",
            "Confidentiality is one principles of cybersecurity. \n It uses techniques like encryption and access controls to keep private information out of the wrong hands.",
            "Integrity is one principles of cybersecurity. \n It uses regular backups and digital signatures to guarantee that data is correct and unaffected.",
            "Availability is one principles of cybersecurity. \n It uses disaster recovery plans and failover systems to ensure information and systems are accessible when needed — even during disruptions."
            };

            List<string> CybersecurityWhoNeedsItTips = new List<string>
            {
            "Everyone needs does! We all use phones, computers, and the internet every day.",
            "Cybersecurity helps protect our personal information — like passwords, bank details, and photos — from hackers.",
            "Without it Cybersecurity, we could lose money, get our identity stolen, or have private stuff exposed.",
            "Businesses need it to keep customer data safe and operations running smoothly.",
            "Governments need it to protect critical services like hospitals, power stations, and public services.",
            "Basically, if you're online = you need cybersecurity."
            };
            string response = "";

            if (!string.IsNullOrEmpty(input) &&
              (input.Contains("what") || input.Contains("define") || input.Contains("definition") || input.Contains("explain") || input.Contains("meaning")))
            {
                response = GetRandomTip(CybersecurityDefinitionTips);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
                     (input.Contains("important") || input.Contains("importance") || input.Contains("why do") || input.Contains("why is")))
            {
                response = GetRandomTip(CybersecurityImportanceTips);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
                     (input.Contains("principles") || input.Contains("pillars") || input.Contains("rules") || input.Contains("concepts") || input.Contains("ideas")))
            {
                response = GetRandomTip(CybersecurityPrinciplesTips);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
                     (input.Contains("who") || input.Contains("needs") || input.Contains("applies to") || input.Contains("used by") || input.Contains("uses")))
            {
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) && input.Contains("back"))
            {
              
                    response = "Back to menu...";
              
                questionCounter = 0; // reset counter on exit
            }
            else
            {
                response = "Sorry, I didn't understand that,"+user.Name+"Say again?";
            }


            return response;
        }
        //(Troelsen and Japikse, 2022) //(Stack Overflow, 2016)(Stack Overflow, 2023b)
        //( Treehouse, 2019) //(Unity Discussions, 2015)

        private string GetRandomTip(List<string> tips)
        {
            var random = new Random();
            int index = random.Next(tips.Count);
            return tips[index] + "\n\nAsk more or enter 'back' to go back to the menu.";
            //(Adnan Zameer, 2015)
        }
        public void questionLimit(UserData user, string input, object value)
        {
            string response = string.Empty;
            //if the question counter has reached 3 and the user stated that they are feeling a negative emotion, it will ask the user if they are feeling better
            if (questionCounter == maxQuestions
                && !string.IsNullOrEmpty(user.Emotion)
                && (user.Emotion.Contains("sad") || user.Emotion.Contains("tired") || user.Emotion.Contains("bad")))
            //(Stack Overflow, 2023a)
            {

                string newEmotion = input.ToLower();
                Console.WriteLine("\n");

                //if the user is feeling better, it will output a postive message
                if (!string.IsNullOrEmpty(newEmotion) && (newEmotion.StartsWith("better")
                    || newEmotion.StartsWith("good") || newEmotion.StartsWith("great") || newEmotion.StartsWith("happy")))
                {
                    response = "That's great to hear that your feeling better!";

                    questionCounter = 0;
                }
                //else, the program will output a sympathetic message
                else if (!string.IsNullOrEmpty(newEmotion) && newEmotion.StartsWith("sad")
                    || newEmotion.Contains("tired") || newEmotion.Contains("bad") || newEmotion.Contains("still"))
                {
                    response = "Sorry to hear your still feeling that way. Hopefully you'll feel better after this session!";
                    //it will also reset the counter
                    questionCounter = 0;
                }
                else
                {
                    response = "Sorry, I didn’t understand that. " + user.Name + ". Say again?";
                }
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
