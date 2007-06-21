using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace WindowsApplication2
{
    public partial class ArchetypeProjectForm : Form
    {
        private String artifactId;

        private String version;

        private String groupId;

        public ArchetypeProjectForm()
        {
            InitializeComponent();
        }

        public String Version
        {
            get
            {
                return version;
            }

            set
            {
                version = value;
            }
        }

        public String ArtifactId
        {
            get
            {
                return artifactId;
            }

            set
            {
                artifactId = value;
            }
        }

        public String GroupId
        {
            get
            {
                return groupId;
            }

            set
            {
                groupId = value;
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            artifactId = artifactIdTextBox.Text;
            groupId = groupIdTextBox.Text;
            version = versionTextBox.Text;
            this.Dispose();            
        }

        private void ArchetypeProjectForm_Load(object sender, EventArgs e)
        {
            groupIdTextBox.Text = this.GroupId;
            artifactIdTextBox.Text = this.ArtifactId;
            versionTextBox.Text = this.Version;
        }
    }
}