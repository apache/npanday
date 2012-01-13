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

using System;
using System.IO;
using System.Windows.Forms;
using EnvDTE;
using NPanday.Artifact;
using NPanday.Utils;
using VSLangProj;

namespace NPanday.VisualStudio.Addin
{
    public partial class NPandaySignAssembly : Form
    {

        private ArtifactContext artifactContext;
        private Project project;
        private PomHelperUtility pomUtility;
        private FileInfo pom;
        public NPandaySignAssembly()
        {
            InitializeComponent();
        }

        public NPandaySignAssembly(Project project, ArtifactContext container, FileInfo pom)
        {
            this.project = project;
            this.artifactContext = container;

            InitializeComponent();
            this.pom = pom;
            this.pomUtility = new PomHelperUtility(pom);

            this.txtBrowseAssemblySignKey.Text = pomUtility.CompilerPluginConfigurationKeyfile;
            
        }


        private void btnOk_Click(object sender, EventArgs e)
        {
            try
            {
                // set pom keyfile
                pomUtility.CompilerPluginConfigurationKeyfile = txtBrowseAssemblySignKey.Text;
                

                // add the key to the vs project
                VSProject vsProject = (VSProject)project.Object;
                if (!string.IsNullOrEmpty(txtBrowseAssemblySignKey.Text))
                {
                    try
                    {
                        vsProject.Project.ProjectItems.AddFromFileCopy(txtBrowseAssemblySignKey.Text);
                    }
                    catch (Exception) 
                    { }
                    
                    vsProject.Project.Properties.Item("AssemblyOriginatorKeyFile").Value = txtBrowseAssemblySignKey.Text;
                    vsProject.Project.Properties.Item("SignAssembly").Value = true;
                }
                else
                {
                    vsProject.Project.Properties.Item("AssemblyOriginatorKeyFile").Value = null;
                    vsProject.Project.Properties.Item("SignAssembly").Value = false;
                }
            }
            catch (Exception err)
            {

                MessageBox.Show(err.Message);
            }

            this.DialogResult = DialogResult.OK;
            this.Close();
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void btnBrowse_Click(object sender, EventArgs e)
        {
            OpenFileDialog ofd = new OpenFileDialog();

            if (!"".Equals(System.IO.File.Exists(txtBrowseAssemblySignKey.Text)) && System.IO.File.Exists(txtBrowseAssemblySignKey.Text))
            {
                ofd.FileName = txtBrowseAssemblySignKey.Text;
            }
            else
            {
                txtBrowseAssemblySignKey.Text = "";
                txtBrowseAssemblySignKey.Text = "";
                ofd.InitialDirectory = (new FileInfo(project.FileName)).Directory.ToString();
            }

            ofd.Filter = "sign key files (*.snk)|*.snk|All files (*.*)|*.*";
            ofd.FilterIndex = 1;
            ofd.CheckFileExists = true;
            ofd.RestoreDirectory = true;

            if (ofd.ShowDialog() == DialogResult.OK)
            {
                txtBrowseAssemblySignKey.Text = ofd.FileName;
            }
        }
    }
}