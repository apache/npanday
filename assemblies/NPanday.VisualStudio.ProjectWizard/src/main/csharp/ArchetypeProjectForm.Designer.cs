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

namespace WindowsApplication2
{
    partial class ArchetypeProjectForm
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
            this.groupIdTextBox = new System.Windows.Forms.TextBox();
            this.artifactIdTextBox = new System.Windows.Forms.TextBox();
            this.versionTextBox = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.button1 = new System.Windows.Forms.Button();
            this.label5 = new System.Windows.Forms.Label();
            this.listBoxArtifactType = new System.Windows.Forms.ListBox();
            this.SuspendLayout();
            // 
            // groupIdTextBox
            // 
            this.groupIdTextBox.Location = new System.Drawing.Point(121, 54);
            this.groupIdTextBox.Name = "groupIdTextBox";
            this.groupIdTextBox.Size = new System.Drawing.Size(246, 22);
            this.groupIdTextBox.TabIndex = 0;
            // 
            // artifactIdTextBox
            // 
            this.artifactIdTextBox.Location = new System.Drawing.Point(121, 98);
            this.artifactIdTextBox.Name = "artifactIdTextBox";
            this.artifactIdTextBox.Size = new System.Drawing.Size(246, 22);
            this.artifactIdTextBox.TabIndex = 1;
            // 
            // versionTextBox
            // 
            this.versionTextBox.Location = new System.Drawing.Point(121, 137);
            this.versionTextBox.Name = "versionTextBox";
            this.versionTextBox.Size = new System.Drawing.Size(246, 22);
            this.versionTextBox.TabIndex = 2;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(26, 54);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(65, 17);
            this.label1.TabIndex = 3;
            this.label1.Text = "Group ID";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(26, 98);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(69, 17);
            this.label2.TabIndex = 4;
            this.label2.Text = "Artifact ID";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(26, 137);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(56, 17);
            this.label3.TabIndex = 5;
            this.label3.Text = "Version";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label4.Location = new System.Drawing.Point(20, 9);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(136, 25);
            this.label4.TabIndex = 6;
            this.label4.Text = "Create Project";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(303, 233);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 7;
            this.button1.Text = "Finish";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(26, 178);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(88, 17);
            this.label5.TabIndex = 9;
            this.label5.Text = "Artifact Type";
            this.label5.Click += new System.EventHandler(this.label5_Click);
            // 
            // listBoxArtifactType
            // 
            this.listBoxArtifactType.AllowDrop = true;
            this.listBoxArtifactType.FormattingEnabled = true;
            this.listBoxArtifactType.ItemHeight = 16;
            this.listBoxArtifactType.Location = new System.Drawing.Point(121, 178);
            this.listBoxArtifactType.Name = "listBoxArtifactType";
            this.listBoxArtifactType.Size = new System.Drawing.Size(134, 20);
            this.listBoxArtifactType.TabIndex = 10;
            this.listBoxArtifactType.SelectedIndexChanged += new System.EventHandler(this.listBoxArtifactType_SelectedIndexChanged);
            // 
            // ArchetypeProjectForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(402, 279);
            this.Controls.Add(this.listBoxArtifactType);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.versionTextBox);
            this.Controls.Add(this.artifactIdTextBox);
            this.Controls.Add(this.groupIdTextBox);
            this.Name = "ArchetypeProjectForm";
            this.Text = "Form1";
            this.Load += new System.EventHandler(this.ArchetypeProjectForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox groupIdTextBox;
        private System.Windows.Forms.TextBox artifactIdTextBox;
        private System.Windows.Forms.TextBox versionTextBox;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.ListBox listBoxArtifactType;
    }
}

