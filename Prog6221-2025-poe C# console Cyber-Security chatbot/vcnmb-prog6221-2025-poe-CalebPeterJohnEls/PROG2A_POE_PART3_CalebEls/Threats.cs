using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualBasic.ApplicationServices;

namespace PROG2A_POE_PART3_CalebEls
{
    public class Threats
    {
        public delegate string TipSelector(List<string> tips);// called as a delegate
        int questionCounter = 0;   // initialize counter
        int maxQuestions = 3; //

        //this method is used to let the user ask the chat bot questions based on cyber theats (Troelsen and Japikse, 2022)
        public string choice3(string input, object value, UserData user)
        {
            TipSelector tipSelector = GetRandomTip;//link with method
            //(Unity Discussions, 2015)
            //array list of responces
            List<string> MalwareInfo = new List<string>
            {
                "Malware is software that can damage your device, steal your data, or take control of your system — often without you knowing.",
                "Ransomware locks your files and demands money to unlock them. It's one of the most dangerous types of malware.",
                "Trojans pretend to be useful programs, but once installed, they open a backdoor for other threats.",
                "Spyware quietly watches what you do online and can steal things like passwords or card info.",
                "Worms spread from one device to another without help. They're sneaky and can cause a lot of harm fast."
            };

            List<string> SocialEngineeringInfo = new List<string>
            {
                "Social engineering is when attackers trick you into giving away personal info or doing something risky.",
                "Phishing is the most common type — fake emails or messages that steal passwords or install malware.",
                "Spear phishing targets specific people using personal details to sound more convincing.",
                "Whaling targets high-profile people like executives using realistic scams.",
                "Business email scams use fake company emails to trick employees into sending money or info.",
                "Domain spoofing is when fake websites or email addresses look almost identical to real ones.",
                "These tricks work because they rely on trust — staying alert is the best defense."
            };

            List<string> DoSInfo = new List<string>
            {
                "A DoS (Denial-of-Service) attack floods a system with traffic so real users can't access it.",
                "A DDoS (Distributed DoS) uses many hacked devices — a botnet — to launch a massive attack from different places.",
                "Think of it like a traffic jam that blocks all other cars from reaching their destination."
            };

            List<string> PasswordAttackInfo = new List<string>
            {
                "Password attacks try to break into your accounts by stealing or guessing your login details.",
                "Some attacks trick you into giving up your password without realizing it — like phishing scams.",
                "Others use brute-force attacks — trying many password combinations until one works.",
                "Whether by trick or persistence, the goal is to get access. Strong, unique passwords help stop them."
            };
            string response = "";
            Random random = new Random();
            //if the user enters something with a specific keyword, the program will out a output responce based on that keyword //(Stack Overflow, 2016)(Stack Overflow, 2023b)
            //If the user is feeling negative about the topic, the program will output a a sympathetic message
            if (!string.IsNullOrEmpty(input) &&
            (input.Contains("malware") || input.Contains("virus") || input.Contains("trojan") || input.Contains("worm") || input.Contains("ransomware") || 
            input.Contains("spyware") || input.Contains("rootkit") || input.Contains("adware")) &&
            (input.Contains("worried") || input.Contains("scared") || input.Contains("anxious") || input.Contains("concerned")))
            {
               
                response = "It's ok, I completely understand. Here's a tip on what malware is:\n" +  GetRandomTip(MalwareInfo);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("malware") || input.Contains("virus") || input.Contains("trojan") || input.Contains("worm") ||
             input.Contains("ransomware") || input.Contains("spyware") || input.Contains("rootkit") || input.Contains("adware")))
            {
                //if the user gave a favourite tip and it contains a keyword from their current responce, it will output a message, calling back to the favourite tip 
                if (!string.IsNullOrEmpty(user.FavouriteTip))
                {
                    response = "Since you liked " + user.FavouriteTip + " here's another helpful one:\n";
                }
                response = GetRandomTip(MalwareInfo);//(Unity Discussions, 2015)
                questionCounter++;//( Treehouse, 2019)
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("phishing") || input.Contains("emails") || input.Contains("social engineering") || input.Contains("scam") || input.Contains("spoofing")) &&
             (input.Contains("worried") || input.Contains("scared") || input.Contains("anxious") || input.Contains("concerned")))
            {
                //it will pick a random responce to output from the array list //(Unity Discussions, 2015)
                response = "It's ok, I completely understand. Here's a tip on what phishing is:\n" + GetRandomTip(SocialEngineeringInfo);
                questionCounter++;// adds to counter
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("phishing") || input.Contains("emails") || input.Contains("social engineering") || input.Contains("scam") || input.Contains("spoofing")))
            {
                if (!string.IsNullOrEmpty(user.FavouriteTip))
                {
                    response = "Since you liked "+user.FavouriteTip+" here's another helpful one:\n";
                }
                response = GetRandomTip(SocialEngineeringInfo);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("dos") || input.Contains("ddos") || input.Contains("denial of service") || input.Contains("botnet")) &&
             (input.Contains("worried") || input.Contains("scared") || input.Contains("anxious") || input.Contains("concerned")))
            {
                response = "It's ok, I completely understand. Here's a tip on what DoS attacks are:\n" + GetRandomTip(DoSInfo);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("dos") || input.Contains("ddos") || input.Contains("denial of service") || input.Contains("botnet")))
            {
                if (!string.IsNullOrEmpty(user.FavouriteTip))
                {
                    response = "Since you liked " + user.FavouriteTip + " here's another helpful one:\n";
                   
                }
                response = GetRandomTip(DoSInfo);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("password") || input.Contains("login") || input.Contains("brute force") || input.Contains("passwords")) &&
             (input.Contains("worried") || input.Contains("scared") || input.Contains("anxious") || input.Contains("concerned")))
            {
                response = "It's ok, I completely understand. Here's a tip on what password attacks are:\n" + GetRandomTip(PasswordAttackInfo);
                questionCounter++;
                questionLimit(user, input, value);
            }
            else if (!string.IsNullOrEmpty(input) &&
             (input.Contains("password") || input.Contains("login") || input.Contains("brute force") || input.Contains("passwords")))
            {
                if (!string.IsNullOrEmpty(user.FavouriteTip))
                {
                    response = "Since you liked " + user.FavouriteTip + " here's another helpful one:\n";
                }
                questionCounter++;
                questionLimit(user, input, value);
                response = GetRandomTip(PasswordAttackInfo);
            }
            else if (!string.IsNullOrEmpty(input) && input.Contains("back"))
            {
                response = "Back to menu...";
                questionCounter = 0;
            }
            else
            {
                response = $"Sorry, I didn't understand that,"+user.Name + "Say again?";
            }

            return response;

        }
        //(Troelsen and Japikse, 2022) //(Stack Overflow, 2016)(Stack Overflow, 2023b)
        //( Treehouse, 2019) //(Unity Discussions, 2015)


        // Method to return a random cybersecurity tip from a given list
        private string GetRandomTip(List<string> tips)
        {
            // Create a new instance of the Random class for generating a random number
            var random = new Random();

            // Generate a random index between 0 and the number of tips (exclusive)
            int index = random.Next(tips.Count);

            // Return the randomly selected tip, plus a helpful instruction message
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
                    response= "That's great to hear that your feeling better!";

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
