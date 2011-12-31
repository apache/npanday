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
            this.lblSCM = new System.Windows.Forms.Label();
            this.txtSCMTag = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.txtVersion = new System.Windows.Forms.TextBox();
            this.useMsDeployCheckBox = new System.Windows.Forms.CheckBox();
            this.SuspendLayout();
            // 
            // btnBrowse
            // 
            this.btnBrowse.Location = new System.Drawing.Point(434, 37);
            this.btnBrowse.Margin = new System.Windows.Forms.Padding(2);
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
            this.lblBrowseDotNetSolutionFile.Location = new System.Drawing.Point(8, 43);
            this.lblBrowseDotNetSolutionFile.Margin = new System.Windows.Forms.Padding(2, 0, 2, 0);
            this.lblBrowseDotNetSolutionFile.Name = "lblBrowseDotNetSolutionFile";
            this.lblBrowseDotNetSolutionFile.Size = new System.Drawing.Size(67, 13);
            this.lblBrowseDotNetSolutionFile.TabIndex = 8;
            this.lblBrowseDotNetSolutionFile.Text = "Solution File:";
            // 
            // txtBrowseDotNetSolutionFile
            // 
            this.txtBrowseDotNetSolutionFile.Location = new System.Drawing.Point(118, 40);
            this.txtBrowseDotNetSolutionFile.Margin = new System.Windows.Forms.Padding(2);
            this.txtBrowseDotNetSolutionFile.Name = "txtBrowseDotNetSolutionFile";
            this.txtBrowseDotNetSolutionFile.Size = new System.Drawing.Size(312, 20);
            this.txtBrowseDotNetSolutionFile.TabIndex = 7;
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(265, 172);
            this.btnCancel.Margin = new System.Windows.Forms.Padding(2);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(104, 21);
            this.btnCancel.TabIndex = 6;
            this.btnCancel.Text = "&Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // btnGenerate
            // 
            this.btnGenerate.Location = new System.Drawing.Point(157, 172);
            this.btnGenerate.Margin = new System.Windows.Forms.Padding(2);
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
            this.label1.Location = new System.Drawing.Point(10, 78);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(53, 13);
            this.label1.TabIndex = 10;
            this.label1.Text = "Group ID:";
            // 
            // txtGroupId
            // 
            this.txtGroupId.Location = new System.Drawing.Point(118, 74);
            this.txtGroupId.Margin = new System.Windows.Forms.Padding(2);
            this.txtGroupId.Name = "txtGroupId";
            this.txtGroupId.Size = new System.Drawing.Size(171, 20);
            this.txtGroupId.TabIndex = 11;
            // 
            // lblSCM
            // 
            this.lblSCM.AutoSize = true;
            this.lblSCM.Location = new System.Drawing.Point(8, 13);
            this.lblSCM.Name = "lblSCM";
            this.lblSCM.Size = new System.Drawing.Size(55, 13);
            this.lblSCM.TabIndex = 12;
            this.lblSCM.Text = "SCM Tag:";
            // 
            // txtSCMTag
            // 
            this.txtSCMTag.Location = new System.Drawing.Point(118, 6);
            this.txtSCMTag.Name = "txtSCMTag";
            this.txtSCMTag.Size = new System.Drawing.Size(396, 20);
            this.txtSCMTag.TabIndex = 13;
            this.txtSCMTag.Text = "<OPTIONAL: svn url>";
            this.txtSCMTag.Click += new System.EventHandler(this.txtSCMTag_Click);
            this.txtSCMTag.TextChanged += new System.EventHandler(this.txtSCMTag_TextChanged);
            this.txtSCMTag.DoubleClick += new System.EventHandler(this.txtSCMTag_DoubleClick);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(10, 110);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(45, 13);
            this.label2.TabIndex = 14;
            this.label2.Text = "Version:";
            // 
            // txtVersion
            // 
            this.txtVersion.Location = new System.Drawing.Point(118, 107);
            this.txtVersion.Margin = new System.Windows.Forms.Padding(2);
            this.txtVersion.Name = "txtVersion";
            this.txtVersion.Size = new System.Drawing.Size(171, 20);
            this.txtVersion.TabIndex = 15;
            // 
            // useMsDeployCheckBox
            // 
            this.useMsDeployCheckBox.AutoSize = true;
            this.useMsDeployCheckBox.Location = new System.Drawing.Point(118, 141);
            this.useMsDeployCheckBox.Name = "useMsDeployCheckBox";
            this.useMsDeployCheckBox.Size = new System.Drawing.Size(264, 17);
            this.useMsDeployCheckBox.TabIndex = 16;
            this.useMsDeployCheckBox.Text = "Use Web Deploy 2.0 to package web applications";
            this.useMsDeployCheckBox.UseVisualStyleBackColor = true;
            // 
            // NPandayImportProjectForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(526, 204);
            this.ControlBox = false;
            this.Controls.Add(this.useMsDeployCheckBox);
            this.Controls.Add(this.txtVersion);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.txtSCMTag);
            this.Controls.Add(this.lblSCM);
            this.Controls.Add(this.txtGroupId);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btnBrowse);
            this.Controls.Add(this.lblBrowseDotNetSolutionFile);
            this.Controls.Add(this.txtBrowseDotNetSolutionFile);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnGenerate);
            this.Margin = new System.Windows.Forms.Padding(2);
            this.Name = "NPandayImportProjectForm";
            this.ShowInTaskbar = false;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
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
        private System.Windows.Forms.Label lblSCM;
        private System.Windows.Forms.TextBox txtSCMTag;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox txtVersion;
        private System.Windows.Forms.CheckBox useMsDeployCheckBox;
    }
}
