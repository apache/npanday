namespace NMaven.VisualStudio.Addin
{
    partial class ChangeMavenSettingsXmlForm
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
            this.btnOk = new System.Windows.Forms.Button();
            this.btnCancel = new System.Windows.Forms.Button();
            this.txtBrowseSettingsXmlFile = new System.Windows.Forms.TextBox();
            this.lblBrowseSettingsXmlFile = new System.Windows.Forms.Label();
            this.btnBrowse = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // btnOk
            // 
            this.btnOk.Location = new System.Drawing.Point(478, 46);
            this.btnOk.Name = "btnOk";
            this.btnOk.Size = new System.Drawing.Size(90, 25);
            this.btnOk.TabIndex = 0;
            this.btnOk.Text = "&OK";
            this.btnOk.UseVisualStyleBackColor = true;
            this.btnOk.Click += new System.EventHandler(this.btnOk_Click);
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(574, 46);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(85, 25);
            this.btnCancel.TabIndex = 1;
            this.btnCancel.Text = "&Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // txtBrowseSettingsXmlFile
            // 
            this.txtBrowseSettingsXmlFile.Location = new System.Drawing.Point(131, 12);
            this.txtBrowseSettingsXmlFile.Name = "txtBrowseSettingsXmlFile";
            this.txtBrowseSettingsXmlFile.Size = new System.Drawing.Size(415, 22);
            this.txtBrowseSettingsXmlFile.TabIndex = 2;
            // 
            // lblBrowseSettingsXmlFile
            // 
            this.lblBrowseSettingsXmlFile.AutoSize = true;
            this.lblBrowseSettingsXmlFile.Location = new System.Drawing.Point(12, 12);
            this.lblBrowseSettingsXmlFile.Name = "lblBrowseSettingsXmlFile";
            this.lblBrowseSettingsXmlFile.Size = new System.Drawing.Size(113, 17);
            this.lblBrowseSettingsXmlFile.TabIndex = 3;
            this.lblBrowseSettingsXmlFile.Text = "Settings.xml File:";
            // 
            // btnBrowse
            // 
            this.btnBrowse.Location = new System.Drawing.Point(552, 9);
            this.btnBrowse.Name = "btnBrowse";
            this.btnBrowse.Size = new System.Drawing.Size(107, 28);
            this.btnBrowse.TabIndex = 4;
            this.btnBrowse.Text = "&Browse";
            this.btnBrowse.UseVisualStyleBackColor = true;
            this.btnBrowse.Click += new System.EventHandler(this.btnBrowse_Click);
            // 
            // ChangeMavenSettingsXmlForm
            // 
            this.AcceptButton = this.btnOk;
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(671, 81);
            this.ControlBox = false;
            this.Controls.Add(this.btnBrowse);
            this.Controls.Add(this.lblBrowseSettingsXmlFile);
            this.Controls.Add(this.txtBrowseSettingsXmlFile);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnOk);
            this.Name = "ChangeMavenSettingsXmlForm";
            this.Text = "Change Maven settings.xml";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnOk;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.TextBox txtBrowseSettingsXmlFile;
        private System.Windows.Forms.Label lblBrowseSettingsXmlFile;
        private System.Windows.Forms.Button btnBrowse;
    }
}