using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.IO;


using Extensibility;
using EnvDTE;
using EnvDTE80;
using Microsoft.Win32;


using NPanday.ProjectImporter;

namespace NPanday.VisualStudio.Addin
{
    public partial class NPandayImportProjectForm : Form
    {
        private DTE2 applicationObject;

		public static string FilterID(string partial)
        {
            string filtered = string.Empty;
            if (partial.EndsWith("."))
            {
                partial = partial.Substring(0, partial.Length - 1);
            }
            char before = '*';
            foreach (char item in partial)
            {

                if ((Char.IsNumber(item) || Char.IsLetter(item)) || ((item == '.' && before != '.') || (item == '-' && before != '-')))
                {
                    filtered += item;
                }
                before = item;
            }

            return filtered;
        }
		
        public NPandayImportProjectForm(DTE2 applicationObject)
        {
            this.applicationObject = applicationObject;
            InitializeComponent();

            if (applicationObject != null && applicationObject.Solution != null && applicationObject.Solution.FileName != null)
            {
                txtBrowseDotNetSolutionFile.Text = applicationObject.Solution.FileName;
                try
                {                    
                    string groupId = Registry.LocalMachine.OpenSubKey(@"Software\Microsoft\Windows NT\CurrentVersion").GetValue("RegisteredOrganization","mycompany").ToString();
                    groupId = FilterID(groupId) + "." + FilterID(ConvertToPascalCase(new FileInfo(applicationObject.Solution.FileName).Name.Replace(".sln", "")));
                    txtGroupId.Text = groupId;
                    
                }
                catch { /*do nothing*/}

            }

        }

        static string ConvertToPascalCase(string str)
        {
            if (string.IsNullOrEmpty(str))
            {
                return str;
            }

            string[] words = str.Split(new char[] { '_', ' ' });
            StringBuilder strBuild = new StringBuilder();

            foreach (string word in words)
            {
                if (word.Length > 0)
                {
                    char firstLetter = char.ToUpper(word[0]);
                    strBuild.Append(firstLetter);

                    if (word.Length > 1)
                    {
                        strBuild.Append(word.Substring(1));
                    }
                }
            }
            return strBuild.ToString();
        }

        private void btnBrowse_Click(object sender, EventArgs e)
        {

            OpenFileDialog ofd = new OpenFileDialog();

            if (!"".Equals(txtBrowseDotNetSolutionFile.Text) && System.IO.File.Exists(txtBrowseDotNetSolutionFile.Text))
            {
                ofd.FileName = txtBrowseDotNetSolutionFile.Text;
            }
            else
            {
                txtBrowseDotNetSolutionFile.Text = "";
                ofd.InitialDirectory = @"c:\";
            }

            ofd.Filter = "Solution Files (*.sln)|*.sln|All Files (*.*)|*.*";
            ofd.FilterIndex = 1;
            ofd.CheckFileExists = true;
            ofd.RestoreDirectory = true;

            if (ofd.ShowDialog() == DialogResult.OK)
            {
                txtBrowseDotNetSolutionFile.Text = ofd.FileName;
            }

        }
        private void btnGenerate_Click(object sender, EventArgs e)
        {
            try
            {
                if (!"".Equals(txtBrowseDotNetSolutionFile.Text) && System.IO.File.Exists(txtBrowseDotNetSolutionFile.Text)
                        && (!String.IsNullOrEmpty(txtGroupId.Text.Trim()))
                       )
                {
                    // Generate here                

                    FileInfo file = new FileInfo(txtBrowseDotNetSolutionFile.Text);



                    string artifactId = FilterID(file.Name.Replace(".sln", "")) + "-parent";
                    string groupId = FilterID(txtGroupId.Text);

                    //NPandayImporter importer = new NPandayImporter(new String[2] {txtBrowseDotNetSolutionFile.Text, "-DgroupId=" + txtGroupId.Text });
                    //importer.GeneratePom();

                    string[] generatedPoms = ProjectImporter.NPandayImporter.ImportProject(file.FullName, groupId, artifactId, "1.0-SNAPSHOT", true);

                    string str = string.Format("NPanday Import Project has Successfully Generated Pom Files!!!\n");


                    foreach (string pom in generatedPoms)
                    {
                        str = str + string.Format("\n    Generated Pom XML File: {0} ", pom);
                    }

                    MessageBox.Show(str, "NPanday Import Done:");


                    // Close the Dialog Here
                    this.DialogResult = DialogResult.OK;
                    this.Close();
                }
                else
                {
                    string message = (!(!"".Equals(txtBrowseDotNetSolutionFile.Text) && System.IO.File.Exists(txtBrowseDotNetSolutionFile.Text))) ? string.Format("Solution File Not Found: {0} ", txtBrowseDotNetSolutionFile.Text) : "";

                    if (String.IsNullOrEmpty(message))
                    {
                        message = message + (String.IsNullOrEmpty(txtGroupId.Text.Trim()) ? "Group Id is empty." : "");
                    }
                    else
                    {
                        message = message + Environment.NewLine + (String.IsNullOrEmpty(txtGroupId.Text.Trim()) ? "Group Id is empty." : "");
                    }


                    MessageBox.Show(message, "NPanday Import Error:", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message, "NPanday Import Error:", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }

        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

    }
}