/// Date: March 28, 2008
/// Author: Jan Ancajas

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace NMaven.VisualStudio.Addin
{
    
    public partial class ChangeMavenSettingsXmlForm : Form 
    {
        private static string settingsXmlFile;
        public static string SettingsXmlFile
        {
            get { return settingsXmlFile; }
            set { settingsXmlFile = value; }
        }

        public ChangeMavenSettingsXmlForm()
        {
            InitializeComponent();

            if (settingsXmlFile != null)
            {
                txtBrowseSettingsXmlFile.Text = settingsXmlFile;
            }
            
        }



        private void btnOk_Click(object sender, EventArgs e)
        {
            
            // assign to the textbox value
            if (System.IO.File.Exists(txtBrowseSettingsXmlFile.Text))
            {
                settingsXmlFile = txtBrowseSettingsXmlFile.Text;
                this.DialogResult = DialogResult.OK;
                this.Close();
            }
            else if ("".Equals(txtBrowseSettingsXmlFile.Text))
            {
                DialogResult res = MessageBox.Show(
                    @"Your Settings File is set to empty, Do you want to use the default settings.xml used by maven?",
                    @"Maven Settings File set to Empty:",
                    MessageBoxButtons.YesNo,
                    MessageBoxIcon.Question);

                if(res == DialogResult.Yes)
                {
                    settingsXmlFile = "";
                    this.DialogResult = DialogResult.OK;
                    this.Close();
                }            }
            else
            {
                string str = "Settings File: "
                    + txtBrowseSettingsXmlFile.Text
                    + "does not exists!!!";

                MessageBox.Show(str,
                    "Settings File Not Found:",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);

            }

            
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void btnBrowse_Click(object sender, EventArgs e)
        {
            OpenFileDialog ofd = new OpenFileDialog();

            if (!"".Equals(System.IO.File.Exists(txtBrowseSettingsXmlFile.Text)) && System.IO.File.Exists(txtBrowseSettingsXmlFile.Text))
            {
                ofd.FileName = txtBrowseSettingsXmlFile.Text;
            }
            else
            {
                txtBrowseSettingsXmlFile.Text = "";
                ofd.InitialDirectory = getInitialDirectory();
            }

            ofd.Filter = "settings.xml file (settings.xml)|settings.xml|xml files (*.xml)|*.xml|All files (*.*)|*.*";
            ofd.FilterIndex = 1;
            ofd.CheckFileExists = true;
            ofd.RestoreDirectory = true;

            if (ofd.ShowDialog() == DialogResult.OK)
            {
                txtBrowseSettingsXmlFile.Text = ofd.FileName;
            }
        }

        private string getInitialDirectory()
        {

            string userProfile = System.Environment.GetEnvironmentVariable("UserProfile");
            string userProfileM2 = userProfile + @"\.m2";

            string m2Home = System.Environment.GetEnvironmentVariable("M2_HOME");
            string m2HomeConf = m2Home + @"\conf";


            if (System.IO.Directory.Exists(userProfileM2))
            {
                return userProfileM2;
            }
            else if (System.IO.Directory.Exists(m2HomeConf))
            {
                return m2HomeConf;
            }
            else
            {
                return @"c:\";
            }

        }


    }
}