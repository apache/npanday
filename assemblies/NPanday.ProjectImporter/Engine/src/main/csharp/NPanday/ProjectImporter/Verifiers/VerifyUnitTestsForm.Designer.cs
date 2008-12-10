namespace NPanday.ProjectImporter.Verifiers
{
    partial class VerifyUnitTestsForm
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
            this.cmdOK = new System.Windows.Forms.Button();
            this.chkListTestUnits = new System.Windows.Forms.CheckedListBox();
            this.cmdReset = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.cmdSelectAll = new System.Windows.Forms.Button();
            this.cmdSelectNone = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // cmdOK
            // 
            this.cmdOK.Location = new System.Drawing.Point(362, 288);
            this.cmdOK.Name = "cmdOK";
            this.cmdOK.Size = new System.Drawing.Size(82, 28);
            this.cmdOK.TabIndex = 5;
            this.cmdOK.Text = "&OK";
            this.cmdOK.UseVisualStyleBackColor = true;
            this.cmdOK.Click += new System.EventHandler(this.cmdOK_Click);
            // 
            // chkListTestUnits
            // 
            this.chkListTestUnits.FormattingEnabled = true;
            this.chkListTestUnits.Location = new System.Drawing.Point(33, 44);
            this.chkListTestUnits.Name = "chkListTestUnits";
            this.chkListTestUnits.Size = new System.Drawing.Size(411, 225);
            this.chkListTestUnits.TabIndex = 0;
            // 
            // cmdReset
            // 
            this.cmdReset.Location = new System.Drawing.Point(272, 288);
            this.cmdReset.Name = "cmdReset";
            this.cmdReset.Size = new System.Drawing.Size(84, 28);
            this.cmdReset.TabIndex = 4;
            this.cmdReset.Text = "&Reset";
            this.cmdReset.UseVisualStyleBackColor = true;
            this.cmdReset.Click += new System.EventHandler(this.cmdReset_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(30, 15);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(318, 17);
            this.label1.TabIndex = 3;
            this.label1.Text = "Select Assemblies that will be used as Unit Tests:";
            // 
            // cmdSelectAll
            // 
            this.cmdSelectAll.Location = new System.Drawing.Point(178, 288);
            this.cmdSelectAll.Name = "cmdSelectAll";
            this.cmdSelectAll.Size = new System.Drawing.Size(88, 27);
            this.cmdSelectAll.TabIndex = 3;
            this.cmdSelectAll.Text = "Select &All";
            this.cmdSelectAll.UseVisualStyleBackColor = true;
            this.cmdSelectAll.Click += new System.EventHandler(this.cmdSelectAll_Click);
            // 
            // cmdSelectNone
            // 
            this.cmdSelectNone.Location = new System.Drawing.Point(67, 288);
            this.cmdSelectNone.Name = "cmdSelectNone";
            this.cmdSelectNone.Size = new System.Drawing.Size(105, 26);
            this.cmdSelectNone.TabIndex = 2;
            this.cmdSelectNone.Text = "Select &None";
            this.cmdSelectNone.UseVisualStyleBackColor = true;
            this.cmdSelectNone.Click += new System.EventHandler(this.cmdSelectNone_Click);
            // 
            // VerifyUnitTestsForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(471, 341);
            this.Controls.Add(this.cmdSelectNone);
            this.Controls.Add(this.cmdSelectAll);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.cmdReset);
            this.Controls.Add(this.chkListTestUnits);
            this.Controls.Add(this.cmdOK);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.Name = "VerifyUnitTestsForm";
            this.Text = "Project Unit Tests";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button cmdOK;
        private System.Windows.Forms.CheckedListBox chkListTestUnits;
        private System.Windows.Forms.Button cmdReset;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button cmdSelectAll;
        private System.Windows.Forms.Button cmdSelectNone;
    }
}