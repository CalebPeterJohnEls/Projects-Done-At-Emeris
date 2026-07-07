using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PROG2A_POE_PART3_CalebEls
{
    public class TaskManagement
    {
        private static List<string> taskList = new List<string>();
        //https://stackoverflow.com/questions/7633152/c-sharp-arraylist-user-input
        //https://stackoverflow.com/questions/49963930/how-do-i-remove-a-string-array-from-a-list-in-c#:~:text=You%20can%20simply%20use%20Remove,with%20the%20supplied%20predicate%20function.
        //https://learn.microsoft.com/en-us/dotnet/api/system.collections.generic.list-1.remove?view=net-8.0
        public string taskChoice(string input)
        {
            string response = "";

            if (!string.IsNullOrWhiteSpace(input))
            {
                input = input.Trim().ToLower();

                if (input.Contains("add") || input.Contains("create"))
                {
                    
                    response = "Please provide a description of the task with reminder. For Example, 'Task: Task name remind in number of days";
                 
                }
                else if (input.Contains("task"))
                {
                    // This means user is providing a task description
                    string taskDescription = input.Substring(5).Trim();
                    if (!string.IsNullOrEmpty(taskDescription))
                    {
                        taskList.Add(taskDescription);
                        response = "Task added: '" + taskDescription + "'";
                        ActivityLogger.LogThis(response);//logs action
                                                         //   //(Stack Overflow, 2014)
                                                         //(Microsoft, 2025c)
                                                         //(	Microsoft, 2025b)
                    }
                    else
                    {
                        response = "Invalid task description.";
                    }
                }
                else if (input.Contains("view")||input.Contains("display"))
                {
                    if (taskList.Count == 0)
                    {
                        response = "No tasks available.";
                    }
                    else
                    {
                        response = "Tasks:\n";
                        for (int i = 0; i < taskList.Count; i++)
                        {
                            response += (i + 1) + ". " + taskList[i] + "\n";
                        }
                        response = response.TrimEnd(); // Remove last newline
                    }
                }
                else if (input.StartsWith("delete")|| input.StartsWith("remove"))
                {
                    string taskToDelete = input.Substring(7).Trim();  // Get the text after "delete"

                    if (taskList.Contains(taskToDelete))
                    {
                        taskList.Remove(taskToDelete);
                        response = "Removed task: '" + taskToDelete + "'";
                        ActivityLogger.LogThis(response);
                    }
                    //(Microsoft, 2025a)(Stack Overflow, 2018)
                    else
                    {
                        response = "Task not found.";
                    }
                }
                else
                {
                    response = "Say again? I didnt understand. Please use 'add', 'view' or provide task as 'task: your description'.";
                }
            }
            else
            {
                response = "Please enter a command such as Add, View or Delete.";
            }

            return response;
        }
    }
    }
//(Medium, 2019)(Microsoft, 2025d)
//(TseplyaevAF, 2017)
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
