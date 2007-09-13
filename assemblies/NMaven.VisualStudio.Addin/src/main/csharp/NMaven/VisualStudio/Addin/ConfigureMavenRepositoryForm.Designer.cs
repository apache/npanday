namespace NMaven.VisualStudio.Addin
{
    partial class ConfigureMavenRepositoryForm
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
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.update = new System.Windows.Forms.Button();
            this.checkBoxRelease = new System.Windows.Forms.CheckBox();
            this.checkBoxSnapshot = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // textBox1
            // 
            this.textBox1.Location = new System.Drawing.Point(28, 61);
            this.textBox1.Name = "textBox1";
            this.textBox1.Size = new System.Drawing.Size(439, 22);
            this.textBox1.TabIndex = 0;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 7.8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(25, 31);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(213, 17);
            this.label1.TabIndex = 1;
            this.label1.Text = "Remote Repository Location";
            // 
            // update
            // 
            this.update.Location = new System.Drawing.Point(401, 141);
            this.update.Name = "update";
            this.update.Size = new System.Drawing.Size(66, 26);
            this.update.TabIndex = 2;
            this.update.Text = "Update";
            this.update.UseVisualStyleBackColor = true;
            this.update.Click += new System.EventHandler(this.update_Click);
            // 
            // checkBoxRelease
            // 
            this.checkBoxRelease.AutoSize = true;
            this.checkBoxRelease.Location = new System.Drawing.Point(28, 103);
            this.checkBoxRelease.Name = "checkBoxRelease";
            this.checkBoxRelease.Size = new System.Drawing.Size(145, 21);
            this.checkBoxRelease.TabIndex = 3;
            this.checkBoxRelease.Text = "Releases Enabled";
            this.checkBoxRelease.UseVisualStyleBackColor = true;
            // 
            // checkBoxSnapshot
            // 
            this.checkBoxSnapshot.AutoSize = true;
            this.checkBoxSnapshot.Location = new System.Drawing.Point(213, 103);
            this.checkBoxSnapshot.Name = "checkBoxSnapshot";
            this.checkBoxSnapshot.Size = new System.Drawing.Size(153, 21);
            this.checkBoxSnapshot.TabIndex = 4;
            this.checkBoxSnapshot.Text = "Snapshots Enabled";
            this.checkBoxSnapshot.UseVisualStyleBackColor = true;
            // 
            // ConfigureMavenRepositoryForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(497, 198);
            this.Controls.Add(this.checkBoxSnapshot);
            this.Controls.Add(this.checkBoxRelease);
            this.Controls.Add(this.update);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.textBox1);
            this.Name = "ConfigureMavenRepositoryForm";
            this.Text = "ConfigureMavenRepositoryForm";
            this.Load += new System.EventHandler(this.ConfigureMavenRepositoryForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button update;
        private System.Windows.Forms.CheckBox checkBoxRelease;
        private System.Windows.Forms.CheckBox checkBoxSnapshot;
    }
}