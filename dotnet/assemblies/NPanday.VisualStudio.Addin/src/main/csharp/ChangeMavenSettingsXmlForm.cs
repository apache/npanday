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
/// Date: March 28, 2008
/// Author: Jan Ancajas

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.IO;

namespace NPanday.VisualStudio.Addin
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
                string[] settingsFileArray = txtBrowseSettingsXmlFile.Text.Split("\\".ToCharArray());
                string settingsFile = settingsFileArray[settingsFileArray.Length - 1];
                if (!settingsFile.Equals("settings.xml"))
                {
                    MessageBox.Show("Sorry, but you have entered an incorrect settings file.", "Change Maven Settings", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }
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
                    + "does not exists!";

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

            string M2 = System.Environment.GetEnvironmentVariable("M2_HOME");
            string m2HomeConf = Path.GetFullPath( Path.Combine( M2,  @"conf"));


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
