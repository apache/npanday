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
using System.Xml;
using System.Xml.Serialization;

using VSLangProj;

using NMaven.Artifact;
using NMaven.Logging;
using NMaven.Model.Pom;
using NMaven.Model.Setting;

namespace NMaven.VisualStudio.Addin
{
    public partial class RemoveArtifactsForm : Form
    {
        private List<NMaven.Artifact.Artifact> localArtifacts = new List<NMaven.Artifact.Artifact>();
        private ArtifactContext artifactContext;
        private Project project;
        private NMaven.Logging.Logger logger;
        private List<Dependency> dependenciesFromPom;

        public RemoveArtifactsForm()
        {
            InitializeComponent();
        }

        public RemoveArtifactsForm(Project project, ArtifactContext container, Logger logger)
        {    
            this.project = project;
            this.logger = logger;
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
            XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Pom.Model));            
            
            if (!serializer.CanDeserialize(reader))
            {
                MessageBox.Show("Could not remove reference. Corrupted pom file: File = " + pomFileName);
                return;
            }

            NMaven.Model.Pom.Model model = (NMaven.Model.Pom.Model)serializer.Deserialize(reader);
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
            XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Pom.Model));
            if (!serializer.CanDeserialize(reader))
            {
                MessageBox.Show("Could not remove reference. Corrupted pom file: File = " + pomFileName);
                return;
            }

            NMaven.Model.Pom.Model model = (NMaven.Model.Pom.Model)serializer.Deserialize(reader);
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