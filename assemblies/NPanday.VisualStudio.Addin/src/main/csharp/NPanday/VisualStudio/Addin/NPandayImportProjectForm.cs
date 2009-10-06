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
using System.Xml;

namespace NPanday.VisualStudio.Addin
{
    public partial class NPandayImportProjectForm : Form
    {
        private DTE2 applicationObject;
        private OutputWindowPane outputWindowPane;

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
                    groupId = ConvertToPascalCase(groupId);
                    groupId = FilterID(groupId) + "." + FilterID(ConvertToPascalCase(new FileInfo(applicationObject.Solution.FileName).Name.Replace(".sln", "")));
                    txtGroupId.Text = groupId;
                    string scmTag =  getSCMTag(applicationObject.Solution.FileName);
                    if(scmTag!=string.Empty && scmTag!=null)
                    {
                        txtSCMTag.Text = scmTag;
                    }
                    
                }
                catch { /*do nothing*/}

            }

        }

        public static string ConvertToPascalCase(string str)
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

        public void SetOutputWindowPane(OutputWindowPane pane)
        {
            outputWindowPane = pane;
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

        private string getSCMTag(string filePath)
        {
            string pomFilePath = string.Empty;
            string scmTag = string.Empty;
            try
            {
                
                //construct the path for the pom file and check for file existance.
                //return if file does not exist.
                pomFilePath = filePath.Substring(0, filePath.LastIndexOf("\\"));
                pomFilePath += "\\pom.xml";
                if (!File.Exists(pomFilePath))
                {
                    return scmTag;
                }

                XmlDocument doc = new XmlDocument();
                doc.Load(pomFilePath);

                XmlNodeList devCon = doc.GetElementsByTagName("developerConnection");
                
                foreach (XmlNode item in devCon)
                {
                    scmTag = item.InnerText;
                }
                
               
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }

            return scmTag;
        }

        private void btnGenerate_Click(object sender, EventArgs e)
        {
            string warningMsg = string.Empty;
            try
            {
                if (!String.Empty.Equals(txtBrowseDotNetSolutionFile.Text) && System.IO.File.Exists(txtBrowseDotNetSolutionFile.Text)
                       && (!String.Empty.Equals(txtGroupId.Text.Trim())))
                {
                    // Generate here                

                    FileInfo file = new FileInfo(txtBrowseDotNetSolutionFile.Text);

                    string artifactId = FilterID(ConvertToPascalCase(file.Name.Replace(".sln", ""))) + "-parent";
                    //string groupId = FilterID(ConvertToPascalCase(txtGroupId.Text));
                    string groupId = FilterID(txtGroupId.Text);
                    string scmTag = txtSCMTag.Text;

                    if (scmTag == null)
                    {
                        scmTag = string.Empty;
                    }

                    if (scmTag.ToUpper().Contains("OPTIONAL"))
                    {
                        scmTag = string.Empty;
                    }

                    if (scmTag.Contains("scm:svn:"))
                    {
                        scmTag = scmTag.Remove(scmTag.IndexOf("scm:svn:"), 8);
                    }

                    try
                    {
                        if (!scmTag.Equals(string.Empty))
                        {
                            if (!scmTag.Contains(@"://"))
                                scmTag = string.Format(@"http://{0}", scmTag);

                            System.Net.WebClient webClient = new System.Net.WebClient();
                            webClient.DownloadData(new Uri(scmTag));
                            //repoUri = new Uri(scmTag);
                        }
                    }
                    catch (Exception)
                    {
                        warningMsg = string.Format("\n    SCM Tag {0} was not accessible", scmTag);
                    }

                    validateSolutionStructure();
                    resyncAllArtifacts();
                    string[] generatedPoms = ProjectImporter.NPandayImporter.ImportProject(file.FullName, groupId, artifactId, "1.0-SNAPSHOT", scmTag, true, ref warningMsg);
                    string str = string.Format("NPanday Import Project has Successfully Generated Pom Files!\n");

                    foreach (string pom in generatedPoms)
                    {
                        str = str + string.Format("\n    Generated Pom XML File: {0} ", pom);
                    }

                    if (!string.IsNullOrEmpty(warningMsg))
                    {
                        str = string.Format("{0}\n\nwith Warning(s):{1}", str, warningMsg);
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

        private void txtSCMTag_TextChanged(object sender, EventArgs e)
        {
           
        }

        private void txtSCMTag_Click(object sender, EventArgs e)
        {
            //removed of clearing scmTag in order for users to verify the scmtag generated
            //txtSCMTag.Text = string.Empty;
        }

        private void txtSCMTag_DoubleClick(object sender, EventArgs e)
        {
            //removed of clearing scmTag in order for users to verify the scmtag generated
            //txtSCMTag.Text = string.Empty;
        }

        private void resyncAllArtifacts()
        {
            if (applicationObject.Solution != null)
            {
                Solution2 solution = (Solution2)applicationObject.Solution;
                IList<IReferenceManager> refManagers = new List<IReferenceManager>();
                foreach (Project project in solution.Projects)
                {
                    if (!isWebProject(project) && !isFolder(project) && project.Object != null)
                    {
                        IReferenceManager mgr = new ReferenceManager();
                        try
                        {
                            mgr.Initialize((VSLangProj80.VSProject2)project.Object);
                            refManagers.Add(mgr);
                        }
                        catch
                        {
                            // suppressing...
                        }
                    }
                }

                // if POM file exists in any of the projects, commence resync
                if (refManagers.Count > 0)
                {
                    refManagerHasError = false;
                    outputWindowPane.OutputString("\n[INFO] Re-syncing artifacts... ");
                    try
                    {
                        foreach (IReferenceManager mgr in refManagers)
                        {
                            mgr.OnError += new EventHandler<ReferenceErrorEventArgs>(refmanager_OnError);
                            mgr.ResyncArtifacts();
                        }

                        if (!refManagerHasError)
                        {
                            outputWindowPane.OutputString(string.Format("done [{0}]", DateTime.Now.ToString("hh:mm tt")));
                        }
                    }
                    catch (Exception ex)
                    {
                        if (refManagerHasError)
                        {
                            outputWindowPane.OutputString(string.Format("\n[WARNING] {0}\n\n{1}\n\n", ex.Message, ex.StackTrace));
                        }
                        else
                        {
                            outputWindowPane.OutputString(string.Format("failed: {0}\n\n{1}\n\n", ex.Message, ex.StackTrace));
                        }
                    }
                }
            }
        }

        private void validateSolutionStructure()
        {
            Solution2 solution = (Solution2)applicationObject.Solution;
            string solutionDir = Path.GetDirectoryName(solution.FullName);
            bool isFlatSingleModule = (solution.Projects.Count == 1 
                && Path.GetExtension(solution.Projects.Item(1).FullName).EndsWith("proj")
                && solutionDir == Path.GetDirectoryName(solution.Projects.Item(1).FullName));

            foreach (Project project in solution.Projects)
            {
                string projPath = string.Empty;
                try { projPath = project.FullName; }
                catch { } //missing project, do nothing

                if (Path.GetExtension(projPath).EndsWith("proj"))
                {
                    string projDir = Path.GetDirectoryName(projPath);
                    if ((isFlatSingleModule && solutionDir == projDir) || (!isFlatSingleModule && solutionDir == Path.GetDirectoryName(projDir)))
                    {
                        continue;
                    }
                    else
                    {
                        throw new Exception("Project Importer failed with project " + project.Name + ". Project directory structure may not be supported.");
                    }
                }
            }
        }

        private bool refManagerHasError = false;
        void refmanager_OnError(object sender, ReferenceErrorEventArgs e)
        {
            refManagerHasError = true;
            outputWindowPane.OutputString("\n[WARNING] " + e.Message);
        }

        private const string WEB_PROJECT_KIND_GUID = "{E24C65DC-7377-472B-9ABA-BC803B73C61A}";
        private static bool isWebProject(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // compare the project kind to the web project guid
            return (String.Compare(project.Kind, WEB_PROJECT_KIND_GUID, true) == 0);
        }

        private const string FOLDER_KIND_GUID = "{66A26720-8FB5-11D2-AA7E-00C04F688DDE}";
        private static bool isFolder(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // compare the project kind to the folder guid
            return (String.Compare(project.Kind, FOLDER_KIND_GUID, true) == 0);
        }
    }
}
