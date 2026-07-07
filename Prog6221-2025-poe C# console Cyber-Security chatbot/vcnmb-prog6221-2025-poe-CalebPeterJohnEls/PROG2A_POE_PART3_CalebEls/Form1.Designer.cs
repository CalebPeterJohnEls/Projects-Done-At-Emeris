namespace PROG2A_POE_PART3_CalebEls
{
    partial class Form1
    {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        ///  Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        ///  Required method for Designer support - do not modify
        ///  the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            splitter1 = new Splitter();
            txtUserInput = new TextBox();
            btnSend = new Button();
            label1 = new Label();
            bntQuiz = new Button();
            pictureBoxASCII = new PictureBox();
            rtbAnswers = new RichTextBox();
            lblTitle = new Label();
            lblSubTitle = new Label();
            lblIntro = new Label();
            ((System.ComponentModel.ISupportInitialize)pictureBoxASCII).BeginInit();
            SuspendLayout();
            // 
            // splitter1
            // 
            splitter1.Location = new Point(0, 0);
            splitter1.Name = "splitter1";
            splitter1.Size = new Size(3, 450);
            splitter1.TabIndex = 0;
            splitter1.TabStop = false;
            // 
            // txtUserInput
            // 
            txtUserInput.Location = new Point(630, 303);
            txtUserInput.Name = "txtUserInput";
            txtUserInput.Size = new Size(100, 23);
            txtUserInput.TabIndex = 2;
            // 
            // btnSend
            // 
            btnSend.BackColor = Color.Cyan;
            btnSend.ForeColor = SystemColors.ActiveCaptionText;
            btnSend.Location = new Point(644, 332);
            btnSend.Name = "btnSend";
            btnSend.Size = new Size(75, 23);
            btnSend.TabIndex = 3;
            btnSend.Text = "SEND";
            btnSend.UseVisualStyleBackColor = false;
            btnSend.Click += btnSend_Click;
            // 
            // label1
            // 
            label1.AutoSize = true;
            label1.Location = new Point(630, 215);
            label1.Name = "label1";
            label1.Size = new Size(0, 15);
            label1.TabIndex = 4;
            // 
            // bntQuiz
            // 
            bntQuiz.BackColor = Color.Cyan;
            bntQuiz.ForeColor = SystemColors.ActiveCaptionText;
            bntQuiz.Location = new Point(644, 381);
            bntQuiz.Name = "bntQuiz";
            bntQuiz.Size = new Size(75, 23);
            bntQuiz.TabIndex = 5;
            bntQuiz.Text = "QUIZ";
            bntQuiz.UseVisualStyleBackColor = false;
            bntQuiz.Click += bntQuiz_Click;
            // 
            // pictureBoxASCII
            // 
            pictureBoxASCII.Image = (Image)resources.GetObject("pictureBoxASCII.Image");
            pictureBoxASCII.Location = new Point(586, 0);
            pictureBoxASCII.Name = "pictureBoxASCII";
            pictureBoxASCII.Size = new Size(187, 268);
            pictureBoxASCII.SizeMode = PictureBoxSizeMode.StretchImage;
            pictureBoxASCII.TabIndex = 6;
            pictureBoxASCII.TabStop = false;
            // 
            // rtbAnswers
            // 
            rtbAnswers.BackColor = SystemColors.MenuText;
            rtbAnswers.Font = new Font("Nirmala UI", 9F, FontStyle.Bold, GraphicsUnit.Point, 0);
            rtbAnswers.ForeColor = Color.Cyan;
            rtbAnswers.Location = new Point(25, 212);
            rtbAnswers.Name = "rtbAnswers";
            rtbAnswers.ReadOnly = true;
            rtbAnswers.Size = new Size(540, 204);
            rtbAnswers.TabIndex = 7;
            rtbAnswers.Text = "";
            // 
            // lblTitle
            // 
            lblTitle.AutoSize = true;
            lblTitle.Font = new Font("Nirmala UI", 27.75F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblTitle.ForeColor = Color.Lime;
            lblTitle.Location = new Point(111, 21);
            lblTitle.Name = "lblTitle";
            lblTitle.Size = new Size(399, 50);
            lblTitle.TabIndex = 8;
            lblTitle.Text = "CYBER SECURITY BOT";
            lblTitle.TextAlign = ContentAlignment.TopCenter;
            lblTitle.Click += label2_Click;
            // 
            // lblSubTitle
            // 
            lblSubTitle.AutoSize = true;
            lblSubTitle.Font = new Font("Nirmala UI", 18F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblSubTitle.ForeColor = Color.Lime;
            lblSubTitle.Location = new Point(193, 71);
            lblSubTitle.Name = "lblSubTitle";
            lblSubTitle.Size = new Size(235, 32);
            lblSubTitle.TabIndex = 9;
            lblSubTitle.Text = "STAY SAFE ONLINE!";
            lblSubTitle.TextAlign = ContentAlignment.TopCenter;
            // 
            // lblIntro
            // 
            lblIntro.AutoSize = true;
            lblIntro.Font = new Font("Nirmala UI", 9F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblIntro.ForeColor = Color.Red;
            lblIntro.Location = new Point(82, 127);
            lblIntro.Name = "lblIntro";
            lblIntro.Size = new Size(0, 15);
            lblIntro.TabIndex = 10;
            lblIntro.TextAlign = ContentAlignment.TopCenter;
            // 
            // Form1
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            BackColor = SystemColors.ActiveCaptionText;
            ClientSize = new Size(800, 450);
            Controls.Add(lblIntro);
            Controls.Add(lblSubTitle);
            Controls.Add(lblTitle);
            Controls.Add(rtbAnswers);
            Controls.Add(pictureBoxASCII);
            Controls.Add(bntQuiz);
            Controls.Add(label1);
            Controls.Add(btnSend);
            Controls.Add(txtUserInput);
            Controls.Add(splitter1);
            ForeColor = SystemColors.ControlLightLight;
            Name = "Form1";
            Text = "Form1";
            Load += Form1_Load;
            ((System.ComponentModel.ISupportInitialize)pictureBoxASCII).EndInit();
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private Splitter splitter1;
        private TextBox txtUserInput;
        private Button btnSend;
        private Label label1;
        private Button bntQuiz;
        private PictureBox pictureBoxASCII;
        private RichTextBox rtbAnswers;
        private Label lblTitle;
        private Label lblSubTitle;
        private Label lblIntro;
    }
}
