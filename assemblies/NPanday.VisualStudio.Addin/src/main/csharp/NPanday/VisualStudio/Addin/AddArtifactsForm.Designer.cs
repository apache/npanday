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
    using System.Windows.Forms;

    partial class AddArtifactsForm
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
            this.addArtifact = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.remoteTabPage = new System.Windows.Forms.TabPage();
            this.treeView1 = new System.Windows.Forms.TreeView();
            this.localTabPage = new System.Windows.Forms.TabPage();
            this.localListView = new System.Windows.Forms.ListView();
            this.ArtifactNameHeader = new System.Windows.Forms.ColumnHeader();
            this.versionHeader = new System.Windows.Forms.ColumnHeader();
            this.artifactTabControl = new System.Windows.Forms.TabControl();
            this.ConfigureTab = new System.Windows.Forms.TabPage();
            this.UpdateLabel = new System.Windows.Forms.Label();
            this.RepoCombo = new System.Windows.Forms.ComboBox();
            this.checkBoxSnapshot = new System.Windows.Forms.CheckBox();
            this.checkBoxRelease = new System.Windows.Forms.CheckBox();
            this.update = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.remoteTabPage.SuspendLayout();
            this.localTabPage.SuspendLayout();
            this.artifactTabControl.SuspendLayout();
            this.ConfigureTab.SuspendLayout();
            this.SuspendLayout();
            // 
            // addArtifact
            // 
            this.addArtifact.Location = new System.Drawing.Point(299, 296);
            this.addArtifact.Margin = new System.Windows.Forms.Padding(2);
            this.addArtifact.Name = "addArtifact";
            this.addArtifact.Size = new System.Drawing.Size(83, 26);
            this.addArtifact.TabIndex = 1;
            this.addArtifact.Text = "&Add";
            this.addArtifact.UseVisualStyleBackColor = true;
            this.addArtifact.Click += new System.EventHandler(this.addArtifact_Click);
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(396, 296);
            this.button2.Margin = new System.Windows.Forms.Padding(2);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(86, 26);
            this.button2.TabIndex = 2;
            this.button2.Text = "&Close";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // remoteTabPage
            // 
            this.remoteTabPage.Controls.Add(this.treeView1);
            this.remoteTabPage.Location = new System.Drawing.Point(4, 22);
            this.remoteTabPage.Margin = new System.Windows.Forms.Padding(2);
            this.remoteTabPage.Name = "remoteTabPage";
            this.remoteTabPage.Padding = new System.Windows.Forms.Padding(2);
            this.remoteTabPage.Size = new System.Drawing.Size(469, 249);
            this.remoteTabPage.TabIndex = 1;
            this.remoteTabPage.Text = "Remote";
            this.remoteTabPage.UseVisualStyleBackColor = true;
            this.remoteTabPage.Click += new System.EventHandler(this.remoteTabPage_Click);
            // 
            // treeView1
            // 
            this.treeView1.Location = new System.Drawing.Point(17, 15);
            this.treeView1.Margin = new System.Windows.Forms.Padding(2);
            this.treeView1.Name = "treeView1";
            this.treeView1.Size = new System.Drawing.Size(438, 222);
            this.treeView1.TabIndex = 0;
            this.treeView1.NodeMouseDoubleClick += new System.Windows.Forms.TreeNodeMouseClickEventHandler(this.treeView1_NodeMouseDoubleClick);
            this.treeView1.AfterSelect += new System.Windows.Forms.TreeViewEventHandler(this.treeView1_AfterSelect);
            // 
            // localTabPage
            // 
            this.localTabPage.Controls.Add(this.localListView);
            this.localTabPage.Location = new System.Drawing.Point(4, 22);
            this.localTabPage.Margin = new System.Windows.Forms.Padding(2);
            this.localTabPage.Name = "localTabPage";
            this.localTabPage.Padding = new System.Windows.Forms.Padding(2);
            this.localTabPage.Size = new System.Drawing.Size(469, 249);
            this.localTabPage.TabIndex = 0;
            this.localTabPage.Text = "Local";
            this.localTabPage.UseVisualStyleBackColor = true;
            this.localTabPage.Click += new System.EventHandler(this.localTabPage_Click);
            // 
            // localListView
            // 
            this.localListView.BackgroundImageTiled = true;
            this.localListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.ArtifactNameHeader,
            this.versionHeader});
            this.localListView.Location = new System.Drawing.Point(16, 15);
            this.localListView.Margin = new System.Windows.Forms.Padding(2);
            this.localListView.Name = "localListView";
            this.localListView.Size = new System.Drawing.Size(438, 220);
            this.localListView.Sorting = System.Windows.Forms.SortOrder.Ascending;
            this.localListView.TabIndex = 0;
            this.localListView.UseCompatibleStateImageBehavior = false;
            this.localListView.SelectedIndexChanged += new System.EventHandler(this.localListView_SelectedIndexChanged);
            this.localListView.DoubleClick += new System.EventHandler(this.localListView_DoubleClick);
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
            // artifactTabControl
            // 
            this.artifactTabControl.Controls.Add(this.localTabPage);
            this.artifactTabControl.Controls.Add(this.remoteTabPage);
            this.artifactTabControl.Controls.Add(this.ConfigureTab);
            this.artifactTabControl.Location = new System.Drawing.Point(9, 10);
            this.artifactTabControl.Margin = new System.Windows.Forms.Padding(2);
            this.artifactTabControl.Name = "artifactTabControl";
            this.artifactTabControl.SelectedIndex = 0;
            this.artifactTabControl.Size = new System.Drawing.Size(477, 275);
            this.artifactTabControl.TabIndex = 0;
            this.artifactTabControl.SelectedIndexChanged += new System.EventHandler(this.artifactTabControl_SelectedIndexChanged);
            // 
            // ConfigureTab
            // 
            this.ConfigureTab.Controls.Add(this.UpdateLabel);
            this.ConfigureTab.Controls.Add(this.RepoCombo);
            this.ConfigureTab.Controls.Add(this.checkBoxSnapshot);
            this.ConfigureTab.Controls.Add(this.checkBoxRelease);
            this.ConfigureTab.Controls.Add(this.update);
            this.ConfigureTab.Controls.Add(this.label1);
            this.ConfigureTab.Location = new System.Drawing.Point(4, 22);
            this.ConfigureTab.Name = "ConfigureTab";
            this.ConfigureTab.Padding = new System.Windows.Forms.Padding(3);
            this.ConfigureTab.Size = new System.Drawing.Size(469, 249);
            this.ConfigureTab.TabIndex = 2;
            this.ConfigureTab.Text = "Configure Repository";
            this.ConfigureTab.UseVisualStyleBackColor = true;
            this.ConfigureTab.Click += new System.EventHandler(this.ConfigureTab_Click);
            // 
            // UpdateLabel
            // 
            this.UpdateLabel.AutoSize = true;
            this.UpdateLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.UpdateLabel.Location = new System.Drawing.Point(66, 183);
            this.UpdateLabel.Name = "UpdateLabel";
            this.UpdateLabel.Size = new System.Drawing.Size(0, 20);
            this.UpdateLabel.TabIndex = 11;
            // 
            // RepoCombo
            // 
            this.RepoCombo.FormattingEnabled = true;
            this.RepoCombo.Location = new System.Drawing.Point(56, 77);
            this.RepoCombo.Name = "RepoCombo";
            this.RepoCombo.Size = new System.Drawing.Size(329, 21);
            this.RepoCombo.TabIndex = 10;
            this.RepoCombo.SelectedIndexChanged += new System.EventHandler(this.RepoCombo_SelectedIndexChanged);
            // 
            // checkBoxSnapshot
            // 
            this.checkBoxSnapshot.AutoSize = true;
            this.checkBoxSnapshot.Location = new System.Drawing.Point(194, 138);
            this.checkBoxSnapshot.Margin = new System.Windows.Forms.Padding(2);
            this.checkBoxSnapshot.Name = "checkBoxSnapshot";
            this.checkBoxSnapshot.Size = new System.Drawing.Size(118, 17);
            this.checkBoxSnapshot.TabIndex = 9;
            this.checkBoxSnapshot.Text = "Snapshots Enabled";
            this.checkBoxSnapshot.UseVisualStyleBackColor = true;
            // 
            // checkBoxRelease
            // 
            this.checkBoxRelease.AutoSize = true;
            this.checkBoxRelease.Location = new System.Drawing.Point(55, 138);
            this.checkBoxRelease.Margin = new System.Windows.Forms.Padding(2);
            this.checkBoxRelease.Name = "checkBoxRelease";
            this.checkBoxRelease.Size = new System.Drawing.Size(112, 17);
            this.checkBoxRelease.TabIndex = 8;
            this.checkBoxRelease.Text = "Releases Enabled";
            this.checkBoxRelease.UseVisualStyleBackColor = true;
            // 
            // update
            // 
            this.update.Location = new System.Drawing.Point(335, 135);
            this.update.Margin = new System.Windows.Forms.Padding(2);
            this.update.Name = "update";
            this.update.Size = new System.Drawing.Size(50, 21);
            this.update.TabIndex = 7;
            this.update.Text = "Update";
            this.update.UseVisualStyleBackColor = true;
            this.update.Click += new System.EventHandler(this.update_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 7.8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(53, 45);
            this.label1.Margin = new System.Windows.Forms.Padding(2, 0, 2, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(167, 13);
            this.label1.TabIndex = 6;
            this.label1.Text = "Remote Repository Location";
            // 
            // AddArtifactsForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(508, 336);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.addArtifact);
            this.Controls.Add(this.artifactTabControl);
            this.Margin = new System.Windows.Forms.Padding(2);
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "AddArtifactsForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Add Maven Artifact";
            this.remoteTabPage.ResumeLayout(false);
            this.localTabPage.ResumeLayout(false);
            this.artifactTabControl.ResumeLayout(false);
            this.ConfigureTab.ResumeLayout(false);
            this.ConfigureTab.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button addArtifact;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.TabPage remoteTabPage;
        private System.Windows.Forms.TreeView treeView1;
        private System.Windows.Forms.TabPage localTabPage;
        private System.Windows.Forms.ListView localListView;
        private System.Windows.Forms.ColumnHeader ArtifactNameHeader;
        private System.Windows.Forms.ColumnHeader versionHeader;
        private System.Windows.Forms.TabControl artifactTabControl;
        private TabPage ConfigureTab;
        private CheckBox checkBoxSnapshot;
        private CheckBox checkBoxRelease;
        private Button update;
        private Label label1;
        private ComboBox RepoCombo;
        private Label UpdateLabel;
    }
}