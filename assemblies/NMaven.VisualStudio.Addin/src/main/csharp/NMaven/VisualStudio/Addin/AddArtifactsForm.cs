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
            InitializeComponent();
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
    }
}