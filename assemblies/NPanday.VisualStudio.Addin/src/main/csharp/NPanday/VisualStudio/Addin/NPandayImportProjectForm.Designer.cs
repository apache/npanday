namespace NPanday.VisualStudio.Addin
{
    partial class NPandayImportProjectForm
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
            this.btnBrowse = new System.Windows.Forms.Button();
            this.lblBrowseDotNetSolutionFile = new System.Windows.Forms.Label();
            this.txtBrowseDotNetSolutionFile = new System.Windows.Forms.TextBox();
            this.btnCancel = new System.Windows.Forms.Button();
            this.btnGenerate = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.txtGroupId = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // btnBrowse
            // 
            this.btnBrowse.Location = new System.Drawing.Point(434, 21);
            this.btnBrowse.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.btnBrowse.Name = "btnBrowse";
            this.btnBrowse.Size = new System.Drawing.Size(80, 23);
            this.btnBrowse.TabIndex = 9;
            this.btnBrowse.Text = "&Browse";
            this.btnBrowse.UseVisualStyleBackColor = true;
            this.btnBrowse.Click += new System.EventHandler(this.btnBrowse_Click);
            // 
            // lblBrowseDotNetSolutionFile
            // 
            this.lblBrowseDotNetSolutionFile.AutoSize = true;
            this.lblBrowseDotNetSolutionFile.Location = new System.Drawing.Point(8, 27);
            this.lblBrowseDotNetSolutionFile.Margin = new System.Windows.Forms.Padding(2, 0, 2, 0);
            this.lblBrowseDotNetSolutionFile.Name = "lblBrowseDotNetSolutionFile";
            this.lblBrowseDotNetSolutionFile.Size = new System.Drawing.Size(107, 13);
            this.lblBrowseDotNetSolutionFile.TabIndex = 8;
            this.lblBrowseDotNetSolutionFile.Text = "Dot Net Solution File:";
            // 
            // txtBrowseDotNetSolutionFile
            // 
            this.txtBrowseDotNetSolutionFile.Location = new System.Drawing.Point(118, 24);
            this.txtBrowseDotNetSolutionFile.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.txtBrowseDotNetSolutionFile.Name = "txtBrowseDotNetSolutionFile";
            this.txtBrowseDotNetSolutionFile.Size = new System.Drawing.Size(312, 20);
            this.txtBrowseDotNetSolutionFile.TabIndex = 7;
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(450, 86);
            this.btnCancel.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(64, 21);
            this.btnCancel.TabIndex = 6;
            this.btnCancel.Text = "&Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // btnGenerate
            // 
            this.btnGenerate.Location = new System.Drawing.Point(342, 86);
            this.btnGenerate.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.btnGenerate.Name = "btnGenerate";
            this.btnGenerate.Size = new System.Drawing.Size(104, 21);
            this.btnGenerate.TabIndex = 5;
            this.btnGenerate.Text = "&Generate Poms";
            this.btnGenerate.UseVisualStyleBackColor = true;
            this.btnGenerate.Click += new System.EventHandler(this.btnGenerate_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(10, 59);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(53, 13);
            this.label1.TabIndex = 10;
            this.label1.Text = "Group ID:";
            // 
            // txtGroupId
            // 
            this.txtGroupId.Location = new System.Drawing.Point(118, 55);
            this.txtGroupId.Margin = new System.Windows.Forms.Padding(2);
            this.txtGroupId.Name = "txtGroupId";
            this.txtGroupId.Size = new System.Drawing.Size(171, 20);
            this.txtGroupId.TabIndex = 11;
            // 
            // NPandayImportProjectForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(524, 131);
            this.ControlBox = false;
            this.Controls.Add(this.txtGroupId);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btnBrowse);
            this.Controls.Add(this.lblBrowseDotNetSolutionFile);
            this.Controls.Add(this.txtBrowseDotNetSolutionFile);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnGenerate);
            this.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.Name = "NPandayImportProjectForm";
            this.ShowInTaskbar = false;
            this.Text = "NPanday Import Dot Net Solution";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnBrowse;
        private System.Windows.Forms.Label lblBrowseDotNetSolutionFile;
        private System.Windows.Forms.TextBox txtBrowseDotNetSolutionFile;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.Button btnGenerate;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox txtGroupId;
    }
}