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
using System.Collections.Generic;
using System.IO;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Serialization;
using EnvDTE;
using NPanday.Artifact;
using NPanday.Model.Pom;

namespace NPanday.VisualStudio.Addin
{
    public partial class RemoveArtifactsForm : Form
    {
        private List<NPanday.Artifact.Artifact> localArtifacts = new List<NPanday.Artifact.Artifact>();
        private ArtifactContext artifactContext;
        private Project project;
        private List<Dependency> dependenciesFromPom;

        public RemoveArtifactsForm()
        {
            InitializeComponent();
        }

        public RemoveArtifactsForm(Project project, ArtifactContext container)
        {    
            this.project = project;
            InitializeForm();
            InitializeComponent();
            artifactsListView.View = View.Details;           
            artifactContext = container;       
        }

        private void InitializeForm()
        {
            this.SuspendLayout();
            // 
            // RemoveArtifactsForm
            // 
            this.ClientSize = new System.Drawing.Size(292, 260);
            this.Name = "RemoveArtifactsForm";
            this.Load += new System.EventHandler(this.RemoveArtifactsForm_Load);
            this.ResumeLayout(false);
        }

        private void button2_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void RemoveArtifactsForm_Load(object sender, EventArgs e)
        {
            String pomFileName = (new FileInfo(project.FileName).Directory).FullName + @"\..\..\..\pom.xml";
            if (!new FileInfo(pomFileName).Exists)
            {
                MessageBox.Show("Could not remove reference. Missing pom file: File = " + pomFileName);
                return;
            }

            XmlReader reader = XmlReader.Create(pomFileName);
            XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Pom.Model));            
            
            if (!serializer.CanDeserialize(reader))
            {
                MessageBox.Show("Could not remove reference. Corrupted pom file: File = " + pomFileName);
                return;
            }

            NPanday.Model.Pom.Model model = (NPanday.Model.Pom.Model)serializer.Deserialize(reader);
            //List<Dependency> dependencies = new List<Dependency>();

            dependenciesFromPom = new List<Dependency>();
            
            if (model.dependencies != null)
            {
                dependenciesFromPom.AddRange( model.dependencies );
            }
            
            
            foreach (Dependency item in dependenciesFromPom)
            {               
                System.Windows.Forms.ListViewItem viewitem = new System.Windows.Forms.ListViewItem(new string[] {
                    item.artifactId, item.version}, -1);

                viewitem.Tag = item;

                artifactsListView.Items.Add(viewitem);
                                
            }
            //artifactsListBox.ClearSelected();
            reader.Close();
        }

        private void removeBtn_Click(object sender, EventArgs e)
        {
            String pomFileName = (new FileInfo(project.FileName).Directory).FullName + @"\..\..\..\pom.xml";
            if (!new FileInfo(pomFileName).Exists)
            {
                MessageBox.Show("Could not remove reference. Missing pom file: File = " + pomFileName);
                return;
            }

            XmlReader reader = XmlReader.Create(pomFileName);
            XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Pom.Model));
            if (!serializer.CanDeserialize(reader))
            {
                MessageBox.Show("Could not remove reference. Corrupted pom file: File = " + pomFileName);
                return;
            }

            NPanday.Model.Pom.Model model = (NPanday.Model.Pom.Model)serializer.Deserialize(reader);
            List<Dependency> dependencies = new List<Dependency>();  

            ListView.SelectedIndexCollection indices = artifactsListView.SelectedIndices;

            //remove dependencies at the pom
            foreach (int indexItem   in artifactsListView.SelectedIndices)
            {
                
                //MessageBox.Show(dependenciesFromPom[indexItem]);
                dependenciesFromPom.RemoveAt (indexItem);

                //VSProject vsProject1 = (VSProject)project.Object;
                //File must exist
                //VSProject vsProject = (VSProject)project.Object;
                //vsProject.Imports.Re 
                
            }
            
            reader.Close();
            
            model.dependencies = dependenciesFromPom.ToArray();
            TextWriter writer = new StreamWriter(pomFileName);
            serializer.Serialize(writer, model);
            
            writer.Close();
            
            this.Close();  
        }
    }
}