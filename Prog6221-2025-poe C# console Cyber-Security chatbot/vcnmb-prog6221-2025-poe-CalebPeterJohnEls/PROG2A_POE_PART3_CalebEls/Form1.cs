using System.Speech.Synthesis;
using System.Threading;
using System.Xml.Linq;
using javax.sound.midi;
using javax.sound.sampled;
using NAudio.Wave;

namespace PROG2A_POE_PART3_CalebEls
{
    public partial class Form1 : Form
    {
     
        // Declaring core components
        private Quiz quiz; // Calling the Quiz form for the cybersecurity quiz
        private Methods methods; // Calling Methods class for processing user input
        private SpeechSynthesizer synth; // Used for converting text to speech

        // Declaring audio-related objects
        private WaveStream stream1Intro; // Stream for intro voice file
        private WaveOut out1; // Player for background music
        private WaveStream stream2Music; // Stream for background music
        private WaveOut out2; // Player for voice for introduction
        public Form1()
        {
          
            InitializeComponent();
            methods = new Methods();
            quiz = new Quiz(this);//pass reference to main form
            // Initialize the speech synthesizer
            synth = new SpeechSynthesizer();
            synth.SelectVoiceByHints(VoiceGender.Male);
            try
            {
                //https://pastebin.com/hZSZaXci
                //takes the wav file and uses it (Hadley, 2022)(Microsoft, 2025)
                // Loads background music from file
                stream2Music = new WaveFileReader("POE Voice files\\Cyberpunk 2077 Lofi Mix Never Fade Away Lofi (background music).wav");
                out1 = new(); // Outputs device for background music
                out1.Init(stream2Music); // Links player to stream
                out1.PlaybackStopped += (sender, args) =>
                {
                    stream2Music.Position = 0; // Rewind to the beginning
                    out1.Play();               // Play again
                };//(Stack Overflow, 2012)
                // Loads welcome voice from file
                stream1Intro = new WaveFileReader("POE Voice files\\Welcome - Johnny.wav");
                out2 = new(); // Outputs device for welcome voice
                out2.Init(stream1Intro); // Links player to stream
            }
            catch (Exception ex)
            {
                //displays message if audio loading failed
                MessageBox.Show("Audio loading failed: " + ex.Message);
            }

        }
        // Plays both the introduction voice and background music
        private void PlaySoundsClick(object sender, EventArgs e)
        {
            // Stop if currently playing
            if (out1.PlaybackState is PlaybackState.Playing) out1.Stop();
            if (out2.PlaybackState is PlaybackState.Playing) out2.Stop();

            // Reset audio positions to beginning
            stream1Intro.CurrentTime = new TimeSpan(0L);
            stream2Music.CurrentTime = new TimeSpan(0L);

            // Play both sounds
            out1.Play(); // Background music
            out2.Play(); // Voice
        }

        // Handles send button click and proccesses user input and chatbot responce
        private async void btnSend_Click(object sender, EventArgs e)
        {
            string input = txtUserInput.Text.ToLower(); // Get and lowercase input
            if (string.IsNullOrEmpty(input)) return; // Ignore if empty

            rtbAnswers.AppendText("You: " + input + "\n"); // Shows user input in rich text box(Medium, 2019)(Microsoft, 2025d)
            txtUserInput.Clear(); // Clear textbox

            string response = methods.GetResponse(input); // Gets response from methods class

            synth.SpeakAsync(response); // Speak the response with synth

            await TypeLineInRichTextBoxAsync("JOHNNY: " + response + "\n"); // Types repsonce in typing animation
        }
        //(Medium, 2019)(Microsoft, 2025d)
        private void listAIAnswers_SelectedIndexChanged(object sender, EventArgs e)
        {
            
        }

        public async Task TypeLineInRichTextBoxAsync(string line)
        {
            string currentText = "";

            for (int i = 0; i < line.Length; i++)
            {
                currentText += line[i];
                rtbAnswers.AppendText(line[i].ToString()); // Add one letter at a time
                rtbAnswers.SelectionStart = rtbAnswers.Text.Length;
                rtbAnswers.ScrollToCaret(); // Keep scroll at bottom
                await Task.Delay(70); // Delay per character
            }

            rtbAnswers.AppendText(Environment.NewLine); // Add a new line after typing is done
        }
        // (Medium, 2019)(Microsoft, 2025d)
        //(Stack Overflow, 2014)
        //(Stack Overflow, 2014)
        //(TseplyaevAF, 2017)
        //(Troelsen and Japikse, 2022)

        private void bntQuiz_Click(object sender, EventArgs e)
        {

            Quiz quizForm = new Quiz(this);
            quizForm.Show();
            this.Hide();


        }

        private async Task TypeLineIntroAsync(string line)
        {
            // Display text with typing animation while voice is playing
            lblIntro.Text = ""; // Clear previous text
            for (int i = 0; i < line.Length; i++)
            {
                lblIntro.Text += line[i];
                await Task.Delay(70); // Delay 70 milliseconds
            }
        }
        //(Stack Overflow, 2014)
        //(Stack Overflow, 2014)

        private void label2_Click(object sender, EventArgs e)
        {

        }

        private async void Form1_Load(object sender, EventArgs e)
        {
            PlaySoundsClick(null, null);//Calls the PlaySounds Method and starts playing music and intro voice

            //Displays welocme message with typing animation
            await TypeLineIntroAsync("Hey there. The name's Johnny Silverhand, and welcome to The Cyber Security Bot." +
                "\nI'm here to help you stay safe online from any cyber threats."
                +"\n Say hello to start....");
        }
    }

}
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
    Stack Overflow, 2012. NAudio – Looping an audio file. [online] Available at: https://stackoverflow.com/questions/8792456/naudio-looping-an-audio-file [Accessed 26 June 2025].
•	Stack Overflow, 2014. C# – How to Create a Log of User Activities. [online] Available at: <https://stackoverflow.com/questions/22872074/c-sharp-how-to-create-a-log-of-a-user-activities>[Accessed 20 June 2025].
•	Stack Overflow, 2017. How to enable a disabled button in code-behind in ASP.NET. [online] Available at: <https://stackoverflow.com/questions/23379976/how-to-enable-a-disabled-button-in-code-behind-in-asp-net> [Accessed 6 June 2025].
•	Stack Overflow, 2018. How Do I Remove a String Array From a List in C#? [online] Available at: <https://stackoverflow.com/questions/49963930/how-do-i-remove-a-string-array-from-a-list-in-c>[Accessed 20 June 2025].
•	Stack Overflow, 2020. C# Windows Form Application – How to take the user’s input from text boxes. [online] Available at: <https://stackoverflow.com/questions/60007694/c-sharp-windows-form-application-how-to-take-the-users-input-from-text-boxes> [Accessed 18 June 2025].
•	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.
•	TseplyaevAF, 2017. ChatBot/Form1.cs on GitHub. [online] Available at: <https://github.com/TseplyaevAF/ChatBot/blob/master/Chat%20Bot/Form1.cs>[Accessed 10 June 2025].


 */
