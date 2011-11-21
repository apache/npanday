#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion

namespace NPanday.VisualStudio.Addin
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
            this.textBox1.Location = new System.Drawing.Point(21, 50);
            this.textBox1.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.textBox1.Name = "textBox1";
            this.textBox1.Size = new System.Drawing.Size(330, 20);
            this.textBox1.TabIndex = 0;
            this.textBox1.TextChanged += new System.EventHandler(this.textBox1_TextChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 7.8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(19, 25);
            this.label1.Margin = new System.Windows.Forms.Padding(2, 0, 2, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(167, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "Remote Repository Location";
            this.label1.Click += new System.EventHandler(this.label1_Click);
            // 
            // update
            // 
            this.update.Location = new System.Drawing.Point(301, 115);
            this.update.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.update.Name = "update";
            this.update.Size = new System.Drawing.Size(50, 21);
            this.update.TabIndex = 2;
            this.update.Text = "Update";
            this.update.UseVisualStyleBackColor = true;
            this.update.Click += new System.EventHandler(this.update_Click);
            // 
            // checkBoxRelease
            // 
            this.checkBoxRelease.AutoSize = true;
            this.checkBoxRelease.Location = new System.Drawing.Point(21, 84);
            this.checkBoxRelease.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.checkBoxRelease.Name = "checkBoxRelease";
            this.checkBoxRelease.Size = new System.Drawing.Size(112, 17);
            this.checkBoxRelease.TabIndex = 3;
            this.checkBoxRelease.Text = "Releases Enabled";
            this.checkBoxRelease.UseVisualStyleBackColor = true;
            this.checkBoxRelease.CheckedChanged += new System.EventHandler(this.checkBoxRelease_CheckedChanged);
            // 
            // checkBoxSnapshot
            // 
            this.checkBoxSnapshot.AutoSize = true;
            this.checkBoxSnapshot.Location = new System.Drawing.Point(160, 84);
            this.checkBoxSnapshot.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.checkBoxSnapshot.Name = "checkBoxSnapshot";
            this.checkBoxSnapshot.Size = new System.Drawing.Size(118, 17);
            this.checkBoxSnapshot.TabIndex = 4;
            this.checkBoxSnapshot.Text = "Snapshots Enabled";
            this.checkBoxSnapshot.UseVisualStyleBackColor = true;
            this.checkBoxSnapshot.CheckedChanged += new System.EventHandler(this.checkBoxSnapshot_CheckedChanged);
            // 
            // ConfigureMavenRepositoryForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(373, 161);
            this.Controls.Add(this.checkBoxSnapshot);
            this.Controls.Add(this.checkBoxRelease);
            this.Controls.Add(this.update);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.textBox1);
            this.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
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