using System;
using System.Collections;
using System.Windows.Forms;

namespace PROG2A_POE_PART3_CalebEls
{
    public partial class Quiz : Form
    {
      
        //https://www.mooict.com/c-tutorial-create-a-simple-multiple-choice-quiz-game-in-visual-studio/
        //https://stackoverflow.com/questions/60007694/c-sharp-windows-form-application-how-to-take-the-users-input-from-text-boxes?utm_source=chatgpt.com
        //https://stackoverflow.com/questions/23379976/how-to-enable-a-disabled-button-in-code-behind-in-asp-net
        private Form1 mainFormInstance; // Reference to main form (used for returning or sharing data)
        private ArrayList questions = new ArrayList(); // List of quiz questions
        private ArrayList answers = new ArrayList(); // List of correct answers (same index as question)
        //(	Microsoft, 2025c)

        private int currentQuestion = 0; // Tracks the current question index
        private int score = 0; // Tracks user's score

        //(MooICT, 2020)
        public Quiz(Form1 form1)
        {
            InitializeComponent();
            mainFormInstance = form1; 

            LoadQuestions(); // Loads all quiz questions and answers
            DisplayQuestion(); // Displays the question

            // When this form is closed, return to the main form
            this.FormClosed += Quiz_FormClosed;
        }

        // Loads cybersecurity questions and answers
        private void LoadQuestions()
        {
           //(Microsoft, 2025c)
            questions.Add("1. Multiple Choice: Malware Types\n" +
                "Which type of malware pretends to be useful software \nbut actually opens a backdoor for threats?" +
                "\nA. Worm\nB. Ransomware\nC. Trojan\nD. Spyware");
            answers.Add("c");

            questions.Add("2. True or False: Password Strength\n" +
                "Using common passwords like “123456” or “password” \nis considered safe if you change them frequently.");
            answers.Add("false");

            questions.Add("3. Multiple Choice – Social Engineering\n" +
                "What makes spear phishing different from regular phishing?" +
                "\nA. It targets random people with fake emails\nB. It uses malware to spread between devices\nC. It targets specific people using personal details to sound\n more convincing\nD. It always comes from trusted domains");
            answers.Add("c");

            questions.Add("4. True or False: Safe Browsing" +
                "\nYou should avoid websites that show certificate warnings.");
            answers.Add("true");

            questions.Add("5. Multiple Choice – DoS Attacks" +
                "\nWhich statement best describes a DDoS \n(Distributed Denial-of-Service) attack?" +
                "\nA. It tricks users into giving personal data\nB. It uses one device to slow down a website\nC. It uses a botnet to flood a system with traffic\nD. It pretends to be a helpful app to gain access");
            answers.Add("c");

            questions.Add("6. True or False: Suspicious Emails" +
                "\nIt’s safe to click a link in an email if it appears to come from your\n bank.");
            answers.Add("false");

            questions.Add("7. Multiple Choice: Password Attacks" +
                "\nWhich of the following is an example of a brute-force attack?" +
                "\nA. Sending a fake login page\nB. Guessing thousands of passwords until one works\nC. Watching your screen as you type\nD. Tricking you into revealing your password");
            answers.Add("b");

            questions.Add("8. Multiple Choice: Password Tips" +
                "\nWhat is one benefit of using a passphrase like\n “StuckInCyberSpace#69Gmail”?\nA. It uses special symbols only\nB. It’s short and easy to type\nC. It’s long, memorable, and harder to crack\nD. It’s a default password");
            answers.Add("c");

            questions.Add("9. True or False: System Updates" +
                "\nKeeping software up to date can help prevent cyberattacks.");
            answers.Add("true");

            questions.Add("10. Multiple Choice: Social Engineering Tactics" +
                "\nWhy do social engineering scams often succeed?\nA. They are done by professional hackers only\nB. They rely on weak internet connections\nC. They exploit trust and trick people into risky actions\nD. They require high-level coding");
            answers.Add("c");
        }

        // Display current question
        private void DisplayQuestion()
        {
            if (currentQuestion < questions.Count)
            {
                // Show the question and reset input/feedback
                lblQuestions.Text = questions[currentQuestion].ToString();
                txtAnswer.Clear();
                lblResult.Text = "";

                // Enable/disable  buttons
                btnSubmit.Enabled = true;
                btnNext.Enabled = false;
            }
            else
            {
                // Quiz is finished
                lblQuestions.Text = "Quiz Finished!";
                lblScore.Text = "Score: " + score + "/" + questions.Count;

                // Show final result message depending on performance
                if (score < 6)
                {
                    lblResult.Text = "Your final score is: " + score + "/" + questions.Count +
                        "\nI think you should keep learning to stay safe online....";
                }
                else
                {
                    lblResult.Text = "Your final score is: " + score + "/" + questions.Count +
                        "\nGREAT JOB!!! You're a cybersecurity pro!!\nNow you're able to protect yourself online!";
                }

                // Logs the quiz result to activity logger
                //(Stack Overflow, 2014)
                //(Microsoft, 2025c)
                //(	Microsoft, 2025b)
                ActivityLogger.LogThis("Your quizz score was " + score + "/10");

                // Disable both buttons at the end
                btnSubmit.Enabled = false;
                btnNext.Enabled = false;
            }
        }

        // Handles the Submit button click
        private void btnSubmit_Click(object sender, EventArgs e)
        {
            //(	Stack Overflow, 2020) 
            string userAnswer = txtAnswer.Text.Trim().ToLower(); // user input
            string correct = answers[currentQuestion].ToString().ToLower(); // Gets correct answer

            if (!string.IsNullOrEmpty(userAnswer))
            {
                if (userAnswer == correct)
                {
                    lblResult.Text = "Correct!"; // Right answer
                    score++;//adds to score
                }
                else
                {
                    lblResult.Text = "Oops! Incorrect. The correct answer is: " + correct.ToUpper();
                }

                lblScore.Text = "Score: " + score + "/" + questions.Count;

                // Lock answer and enable next question
                btnSubmit.Enabled = false;
                btnNext.Enabled = true;
            }
            else
            {
                // Prevent submitting empty answer
                MessageBox.Show("Please enter an answer before submitting.", "No Answer", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        // Handles the Next button click to go to the next question
        private void btnNext_Click(object sender, EventArgs e)
        {
            currentQuestion++; // Goes to next question
            DisplayQuestion(); // Displays next question


        }
        private void lblQuestions_Click(object sender, EventArgs e) { }

        private void lblScore_Click(object sender, EventArgs e) { }

        private void lblResult_Click(object sender, EventArgs e) { }

        private void txtAnswer_TextChanged(object sender, EventArgs e) { }

        private void Quiz_Load(object sender, EventArgs e) { }

        //Closes quiz and returns back to main form
        private void btnBack_Click(object sender, EventArgs e)
        {
            this.Close();

            if (mainFormInstance != null)
            {
                mainFormInstance.Show();
            }
        }

        //(Troelsen and Japikse, 2022)
        private void Quiz_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (mainFormInstance != null && !mainFormInstance.Visible)
            {
                mainFormInstance.Show();
            }
        }
        //(Troelsen and Japikse, 2022)
        private void lblQuizTitle_Click(object sender, EventArgs e)
        {

        }
    }
}
//(MooICT, 2020)
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