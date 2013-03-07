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
            this.cmdOK.Location = new System.Drawing.Point(272, 234);
            this.cmdOK.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.cmdOK.Name = "cmdOK";
            this.cmdOK.Size = new System.Drawing.Size(62, 23);
            this.cmdOK.TabIndex = 5;
            this.cmdOK.Text = "&OK";
            this.cmdOK.UseVisualStyleBackColor = true;
            this.cmdOK.Click += new System.EventHandler(this.cmdOK_Click);
            // 
            // chkListTestUnits
            // 
            this.chkListTestUnits.FormattingEnabled = true;
            this.chkListTestUnits.Location = new System.Drawing.Point(25, 36);
            this.chkListTestUnits.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.chkListTestUnits.Name = "chkListTestUnits";
            this.chkListTestUnits.Size = new System.Drawing.Size(309, 184);
            this.chkListTestUnits.TabIndex = 0;
            // 
            // cmdReset
            // 
            this.cmdReset.Location = new System.Drawing.Point(204, 234);
            this.cmdReset.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.cmdReset.Name = "cmdReset";
            this.cmdReset.Size = new System.Drawing.Size(63, 23);
            this.cmdReset.TabIndex = 4;
            this.cmdReset.Text = "&Reset";
            this.cmdReset.UseVisualStyleBackColor = true;
            this.cmdReset.Click += new System.EventHandler(this.cmdReset_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(22, 12);
            this.label1.Margin = new System.Windows.Forms.Padding(2, 0, 2, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(225, 13);
            this.label1.TabIndex = 3;
            this.label1.Text = "Select Projects that will be used as Unit Tests:";
            // 
            // cmdSelectAll
            // 
            this.cmdSelectAll.Location = new System.Drawing.Point(134, 234);
            this.cmdSelectAll.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.cmdSelectAll.Name = "cmdSelectAll";
            this.cmdSelectAll.Size = new System.Drawing.Size(66, 22);
            this.cmdSelectAll.TabIndex = 3;
            this.cmdSelectAll.Text = "Select &All";
            this.cmdSelectAll.UseVisualStyleBackColor = true;
            this.cmdSelectAll.Click += new System.EventHandler(this.cmdSelectAll_Click);
            // 
            // cmdSelectNone
            // 
            this.cmdSelectNone.Location = new System.Drawing.Point(50, 234);
            this.cmdSelectNone.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.cmdSelectNone.Name = "cmdSelectNone";
            this.cmdSelectNone.Size = new System.Drawing.Size(79, 21);
            this.cmdSelectNone.TabIndex = 2;
            this.cmdSelectNone.Text = "Select &None";
            this.cmdSelectNone.UseVisualStyleBackColor = true;
            this.cmdSelectNone.Click += new System.EventHandler(this.cmdSelectNone_Click);
            // 
            // VerifyUnitTestsForm
            // 
            this.AcceptButton = this.cmdOK;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(353, 277);
            this.Controls.Add(this.cmdSelectNone);
            this.Controls.Add(this.cmdSelectAll);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.cmdReset);
            this.Controls.Add(this.chkListTestUnits);
            this.Controls.Add(this.cmdOK);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
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