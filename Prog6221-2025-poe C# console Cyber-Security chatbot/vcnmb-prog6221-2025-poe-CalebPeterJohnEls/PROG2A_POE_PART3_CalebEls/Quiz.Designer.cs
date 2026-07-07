namespace PROG2A_POE_PART3_CalebEls
{
    partial class Quiz
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
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
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            btnSubmit = new Button();
            btnNext = new Button();
            txtAnswer = new TextBox();
            lblQuestions = new Label();
            lblResult = new Label();
            lblScore = new Label();
            lblQuizTitle = new Label();
            btnBack = new Button();
            SuspendLayout();
            // 
            // btnSubmit
            // 
            btnSubmit.BackColor = Color.Lime;
            btnSubmit.ForeColor = SystemColors.ActiveCaptionText;
            btnSubmit.Location = new Point(304, 353);
            btnSubmit.Name = "btnSubmit";
            btnSubmit.Size = new Size(75, 23);
            btnSubmit.TabIndex = 0;
            btnSubmit.Text = "Submit";
            btnSubmit.UseVisualStyleBackColor = false;
            btnSubmit.Click += btnSubmit_Click;
            // 
            // btnNext
            // 
            btnNext.BackColor = Color.Lime;
            btnNext.ForeColor = SystemColors.ActiveCaptionText;
            btnNext.Location = new Point(410, 353);
            btnNext.Name = "btnNext";
            btnNext.Size = new Size(75, 23);
            btnNext.TabIndex = 1;
            btnNext.Text = "Next";
            btnNext.UseVisualStyleBackColor = false;
            btnNext.Click += btnNext_Click;
            // 
            // txtAnswer
            // 
            txtAnswer.Location = new Point(344, 324);
            txtAnswer.Name = "txtAnswer";
            txtAnswer.Size = new Size(100, 23);
            txtAnswer.TabIndex = 2;
            txtAnswer.TextChanged += txtAnswer_TextChanged;
            // 
            // lblQuestions
            // 
            lblQuestions.AutoSize = true;
            lblQuestions.BorderStyle = BorderStyle.Fixed3D;
            lblQuestions.Font = new Font("Nirmala UI", 12F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblQuestions.ForeColor = Color.Red;
            lblQuestions.Location = new Point(265, 85);
            lblQuestions.Name = "lblQuestions";
            lblQuestions.Size = new Size(92, 23);
            lblQuestions.TabIndex = 3;
            lblQuestions.Text = "Questions:";
            lblQuestions.Click += lblQuestions_Click;
            // 
            // lblResult
            // 
            lblResult.AutoSize = true;
            lblResult.BorderStyle = BorderStyle.Fixed3D;
            lblResult.Font = new Font("Nirmala UI", 9.75F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblResult.ForeColor = Color.Cyan;
            lblResult.Location = new Point(275, 239);
            lblResult.Name = "lblResult";
            lblResult.Size = new Size(52, 19);
            lblResult.TabIndex = 4;
            lblResult.Text = "Result:";
            lblResult.Click += lblResult_Click;
            // 
            // lblScore
            // 
            lblScore.AutoSize = true;
            lblScore.BorderStyle = BorderStyle.Fixed3D;
            lblScore.Font = new Font("Nirmala UI", 14.25F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblScore.ForeColor = Color.Cyan;
            lblScore.Location = new Point(33, 104);
            lblScore.Name = "lblScore";
            lblScore.Size = new Size(69, 27);
            lblScore.TabIndex = 5;
            lblScore.Text = "Score:";
            lblScore.Click += lblScore_Click;
            // 
            // lblQuizTitle
            // 
            lblQuizTitle.AutoSize = true;
            lblQuizTitle.Font = new Font("Nirmala UI", 21.75F, FontStyle.Bold, GraphicsUnit.Point, 0);
            lblQuizTitle.ForeColor = Color.Lime;
            lblQuizTitle.Location = new Point(249, 24);
            lblQuizTitle.Name = "lblQuizTitle";
            lblQuizTitle.Size = new Size(344, 40);
            lblQuizTitle.TabIndex = 6;
            lblQuizTitle.Text = "CYBER SERCURITY QUIZ";
            lblQuizTitle.Click += lblQuizTitle_Click;
            // 
            // btnBack
            // 
            btnBack.BackColor = Color.Lime;
            btnBack.ForeColor = SystemColors.ActiveCaptionText;
            btnBack.Location = new Point(353, 405);
            btnBack.Name = "btnBack";
            btnBack.Size = new Size(75, 23);
            btnBack.TabIndex = 7;
            btnBack.Text = "Back";
            btnBack.UseVisualStyleBackColor = false;
            btnBack.Click += btnBack_Click;
            // 
            // Quiz
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            BackColor = SystemColors.ActiveCaptionText;
            ClientSize = new Size(800, 450);
            Controls.Add(btnBack);
            Controls.Add(lblQuizTitle);
            Controls.Add(lblScore);
            Controls.Add(lblResult);
            Controls.Add(lblQuestions);
            Controls.Add(txtAnswer);
            Controls.Add(btnNext);
            Controls.Add(btnSubmit);
            ForeColor = SystemColors.ControlLightLight;
            Name = "Quiz";
            Text = "Quiz";
            Load += Quiz_Load;
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private Button btnSubmit;
        private Button btnNext;
        private TextBox txtAnswer;
        private Label lblQuestions;
        private Label lblResult;
        private Label lblScore;
        private Label lblQuizTitle;
        private Button btnBack;
    }
}