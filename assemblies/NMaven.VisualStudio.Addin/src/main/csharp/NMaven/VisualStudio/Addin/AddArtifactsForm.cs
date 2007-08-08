using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;



namespace NMaven.VisualStudio.Addin
{
    public partial class AddArtifactsForm : Form
    {
        public AddArtifactsForm()
        {
            InitializeForm();
            localListView.View = View.Details;
         //   IWindsorContainer container = new WindsorContainer(new XmlInterpreter(@"C:\Documents and Settings\shane\nmaven-apache\trunk-fix\assemblies\NMaven.VisualStudio.Addin\src\main\resources\components.xml"));
         //   ArtifactContext artifactContext = (ArtifactContext) container[typeof(ArtifactContext)];
         //   artifactContext.GetArtifactRepository();

        }

        private void remoteTabPage_Click(object sender, EventArgs e)
        {

        }

        private void listView1_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void localTabPage_Click(object sender, EventArgs e)
        {
            ListViewItem item = new ListViewItem("Test");
          //  item.SubItems.Add("1.2.3.4");
           // localListView.Items.Add(item);
           
        }

        private void InitializeForm()
        {
            this.SuspendLayout();
            // 
            // AddArtifactsForm
            // 
            this.ClientSize = new System.Drawing.Size(292, 260);
            this.Name = "AddArtifactsForm";
            this.Load += new System.EventHandler(this.AddArtifactsForm_Load);
            this.ResumeLayout(false);

        }

        private void AddArtifactsForm_Load(object sender, EventArgs e)
        {

        }
    }
}