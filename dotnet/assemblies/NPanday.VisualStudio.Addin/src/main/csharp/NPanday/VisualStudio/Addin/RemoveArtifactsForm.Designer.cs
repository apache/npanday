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
    partial class RemoveArtifactsForm
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
            this.removeBtn = new System.Windows.Forms.Button();
            this.cancelBtn = new System.Windows.Forms.Button();
            this.artifactsListView = new System.Windows.Forms.ListView();
            this.ArtifactNameHeader = new System.Windows.Forms.ColumnHeader();
            this.versionHeader = new System.Windows.Forms.ColumnHeader();
            this.SuspendLayout();
            // 
            // removeBtn
            // 
            this.removeBtn.Location = new System.Drawing.Point(274, 251);
            this.removeBtn.Name = "removeBtn";
            this.removeBtn.Size = new System.Drawing.Size(80, 22);
            this.removeBtn.TabIndex = 1;
            this.removeBtn.Text = "Remove";
            this.removeBtn.UseVisualStyleBackColor = true;
            this.removeBtn.Click += new System.EventHandler(this.removeBtn_Click);
            // 
            // cancelBtn
            // 
            this.cancelBtn.Location = new System.Drawing.Point(372, 250);
            this.cancelBtn.Name = "cancelBtn";
            this.cancelBtn.Size = new System.Drawing.Size(75, 23);
            this.cancelBtn.TabIndex = 2;
            this.cancelBtn.Text = "Cancel";
            this.cancelBtn.UseVisualStyleBackColor = true;
            this.cancelBtn.Click += new System.EventHandler(this.button2_Click);
            // 
            // artifactsListView
            // 
            this.artifactsListView.BackgroundImageTiled = true;
            this.artifactsListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.ArtifactNameHeader,
            this.versionHeader});
            this.artifactsListView.Location = new System.Drawing.Point(11, 11);
            this.artifactsListView.Margin = new System.Windows.Forms.Padding(2);
            this.artifactsListView.Name = "artifactsListView";
            this.artifactsListView.Size = new System.Drawing.Size(438, 220);
            this.artifactsListView.Sorting = System.Windows.Forms.SortOrder.Ascending;
            this.artifactsListView.TabIndex = 0;
            this.artifactsListView.UseCompatibleStateImageBehavior = false;
            // 
            // ArtifactNameHeader
            // 
            this.ArtifactNameHeader.Text = "Artifact Name";
            this.ArtifactNameHeader.Width = 240;
            // 
            // versionHeader
            // 
            this.versionHeader.Text = "Version";
            this.versionHeader.Width = 120;
            // 
            // RemoveArtifactsForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(465, 280);
            this.Controls.Add(this.artifactsListView);
            this.Controls.Add(this.cancelBtn);
            this.Controls.Add(this.removeBtn);
            this.Name = "RemoveArtifactsForm";
            this.Text = "RemoveArtifactsForm";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button removeBtn;
        private System.Windows.Forms.Button cancelBtn;
        private System.Windows.Forms.ListView artifactsListView;
        private System.Windows.Forms.ColumnHeader ArtifactNameHeader;
        private System.Windows.Forms.ColumnHeader versionHeader;
    }
}