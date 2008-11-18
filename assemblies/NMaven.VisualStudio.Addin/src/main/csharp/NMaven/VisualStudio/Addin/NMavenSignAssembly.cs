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

using Extensibility;
using EnvDTE;
using EnvDTE80;

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.IO;
using System.Net;
using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Forms;

using VSLangProj;

using NMaven.Artifact;
using NMaven.Logging;
using NMaven.Model.Pom;
using NMaven.Model.Setting;

using NMaven.Utils;

namespace NMaven.VisualStudio.Addin
{
    public partial class NMavenSignAssembly : Form
    {

        private ArtifactContext artifactContext;
        private Project project;
        private NMaven.Logging.Logger logger;
        private NMavenPomHelperUtility pomUtility;
        private FileInfo pom;
        public NMavenSignAssembly()
        {
            InitializeComponent();
        }

        public NMavenSignAssembly(Project project, ArtifactContext container, Logger logger, FileInfo pom)
        {
            this.project = project;
            this.logger = logger;
            this.artifactContext = container;

            InitializeComponent();
            this.pom = pom;
            this.pomUtility = new NMavenPomHelperUtility(pom);

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