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
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.configComboBox = new System.Windows.Forms.ComboBox();
            this.cloudConfigComboBox = new System.Windows.Forms.ComboBox();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.searchGacCheckBox = new System.Windows.Forms.CheckBox();
            this.searchAssemblyFoldersExCheckBox = new System.Windows.Forms.CheckBox();
            this.searchFrameworkCheckBox = new System.Windows.Forms.CheckBox();
            this.copyToMavenCheckBox = new System.Windows.Forms.CheckBox();
            this.groupBox1.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnBrowse
            // 
            this.btnBrowse.Location = new System.Drawing.Point(435, 12);
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
            this.lblBrowseDotNetSolutionFile.Location = new System.Drawing.Point(9, 17);
            this.lblBrowseDotNetSolutionFile.Margin = new System.Windows.Forms.Padding(2, 0, 2, 0);
            this.lblBrowseDotNetSolutionFile.Name = "lblBrowseDotNetSolutionFile";
            this.lblBrowseDotNetSolutionFile.Size = new System.Drawing.Size(67, 13);
            this.lblBrowseDotNetSolutionFile.TabIndex = 8;
            this.lblBrowseDotNetSolutionFile.Text = "Solution File:";
            // 
            // txtBrowseDotNetSolutionFile
            // 
            this.txtBrowseDotNetSolutionFile.Location = new System.Drawing.Point(119, 14);
            this.txtBrowseDotNetSolutionFile.Margin = new System.Windows.Forms.Padding(2);
            this.txtBrowseDotNetSolutionFile.Name = "txtBrowseDotNetSolutionFile";
            this.txtBrowseDotNetSolutionFile.Size = new System.Drawing.Size(312, 20);
            this.txtBrowseDotNetSolutionFile.TabIndex = 7;
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(369, 245);
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
            this.btnGenerate.Location = new System.Drawing.Point(261, 245);
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
            this.label1.Location = new System.Drawing.Point(11, 49);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(53, 13);
            this.label1.TabIndex = 10;
            this.label1.Text = "Group ID:";
            // 
            // txtGroupId
            // 
            this.txtGroupId.Location = new System.Drawing.Point(119, 46);
            this.txtGroupId.Margin = new System.Windows.Forms.Padding(2);
            this.txtGroupId.Name = "txtGroupId";
            this.txtGroupId.Size = new System.Drawing.Size(171, 20);
            this.txtGroupId.TabIndex = 11;
            // 
            // lblSCM
            // 
            this.lblSCM.AutoSize = true;
            this.lblSCM.Location = new System.Drawing.Point(9, 113);
            this.lblSCM.Name = "lblSCM";
            this.lblSCM.Size = new System.Drawing.Size(55, 13);
            this.lblSCM.TabIndex = 12;
            this.lblSCM.Text = "SCM Tag:";
            // 
            // txtSCMTag
            // 
            this.txtSCMTag.Location = new System.Drawing.Point(119, 110);
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
            this.label2.Location = new System.Drawing.Point(11, 81);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(45, 13);
            this.label2.TabIndex = 14;
            this.label2.Text = "Version:";
            // 
            // txtVersion
            // 
            this.txtVersion.Location = new System.Drawing.Point(119, 78);
            this.txtVersion.Margin = new System.Windows.Forms.Padding(2);
            this.txtVersion.Name = "txtVersion";
            this.txtVersion.Size = new System.Drawing.Size(171, 20);
            this.txtVersion.TabIndex = 15;
            // 
            // useMsDeployCheckBox
            // 
            this.useMsDeployCheckBox.AutoSize = true;
            this.useMsDeployCheckBox.Location = new System.Drawing.Point(119, 208);
            this.useMsDeployCheckBox.Name = "useMsDeployCheckBox";
            this.useMsDeployCheckBox.Size = new System.Drawing.Size(264, 17);
            this.useMsDeployCheckBox.TabIndex = 16;
            this.useMsDeployCheckBox.Text = "Use Web Deploy 2.0 to package web applications";
            this.useMsDeployCheckBox.UseVisualStyleBackColor = true;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(10, 145);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(72, 13);
            this.label3.TabIndex = 17;
            this.label3.Text = "Configuration:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(10, 177);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(101, 13);
            this.label4.TabIndex = 18;
            this.label4.Text = "Cloud configuration:";
            // 
            // configComboBox
            // 
            this.configComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.configComboBox.FormattingEnabled = true;
            this.configComboBox.Location = new System.Drawing.Point(119, 142);
            this.configComboBox.Name = "configComboBox";
            this.configComboBox.Size = new System.Drawing.Size(171, 21);
            this.configComboBox.TabIndex = 19;
            // 
            // cloudConfigComboBox
            // 
            this.cloudConfigComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cloudConfigComboBox.FormattingEnabled = true;
            this.cloudConfigComboBox.Location = new System.Drawing.Point(119, 174);
            this.cloudConfigComboBox.Name = "cloudConfigComboBox";
            this.cloudConfigComboBox.Size = new System.Drawing.Size(312, 21);
            this.cloudConfigComboBox.TabIndex = 20;
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.searchGacCheckBox);
            this.groupBox1.Controls.Add(this.searchAssemblyFoldersExCheckBox);
            this.groupBox1.Controls.Add(this.searchFrameworkCheckBox);
            this.groupBox1.Location = new System.Drawing.Point(534, 12);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(186, 100);
            this.groupBox1.TabIndex = 21;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Dependency Search Locations";
            // 
            // searchGacCheckBox
            // 
            this.searchGacCheckBox.AutoSize = true;
            this.searchGacCheckBox.Checked = true;
            this.searchGacCheckBox.CheckState = System.Windows.Forms.CheckState.Checked;
            this.searchGacCheckBox.Location = new System.Drawing.Point(15, 70);
            this.searchGacCheckBox.Name = "searchGacCheckBox";
            this.searchGacCheckBox.Size = new System.Drawing.Size(137, 17);
            this.searchGacCheckBox.TabIndex = 2;
            this.searchGacCheckBox.Text = "Global Assembly Cache";
            this.searchGacCheckBox.UseVisualStyleBackColor = true;
            // 
            // searchAssemblyFoldersExCheckBox
            // 
            this.searchAssemblyFoldersExCheckBox.AutoSize = true;
            this.searchAssemblyFoldersExCheckBox.Location = new System.Drawing.Point(15, 47);
            this.searchAssemblyFoldersExCheckBox.Name = "searchAssemblyFoldersExCheckBox";
            this.searchAssemblyFoldersExCheckBox.Size = new System.Drawing.Size(134, 17);
            this.searchAssemblyFoldersExCheckBox.TabIndex = 1;
            this.searchAssemblyFoldersExCheckBox.Text = "Extra Assembly Folders";
            this.searchAssemblyFoldersExCheckBox.UseVisualStyleBackColor = true;
            // 
            // searchFrameworkCheckBox
            // 
            this.searchFrameworkCheckBox.AutoSize = true;
            this.searchFrameworkCheckBox.Location = new System.Drawing.Point(15, 24);
            this.searchFrameworkCheckBox.Name = "searchFrameworkCheckBox";
            this.searchFrameworkCheckBox.Size = new System.Drawing.Size(78, 17);
            this.searchFrameworkCheckBox.TabIndex = 0;
            this.searchFrameworkCheckBox.Text = "Framework";
            this.searchFrameworkCheckBox.UseVisualStyleBackColor = true;
            // 
            // copyToMavenCheckBox
            // 
            this.copyToMavenCheckBox.AutoSize = true;
            this.copyToMavenCheckBox.Location = new System.Drawing.Point(549, 118);
            this.copyToMavenCheckBox.Name = "copyToMavenCheckBox";
            this.copyToMavenCheckBox.Size = new System.Drawing.Size(180, 17);
            this.copyToMavenCheckBox.TabIndex = 3;
            this.copyToMavenCheckBox.Text = "Copy to Local Maven Repository";
            this.copyToMavenCheckBox.UseVisualStyleBackColor = true;
            // 
            // NPandayImportProjectForm
            // 
            this.AcceptButton = this.btnGenerate;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(734, 280);
            this.ControlBox = false;
            this.Controls.Add(this.copyToMavenCheckBox);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.cloudConfigComboBox);
            this.Controls.Add(this.configComboBox);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
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
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
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
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.ComboBox configComboBox;
        private System.Windows.Forms.ComboBox cloudConfigComboBox;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.CheckBox searchGacCheckBox;
        private System.Windows.Forms.CheckBox searchAssemblyFoldersExCheckBox;
        private System.Windows.Forms.CheckBox searchFrameworkCheckBox;
        private System.Windows.Forms.CheckBox copyToMavenCheckBox;
    }
}
