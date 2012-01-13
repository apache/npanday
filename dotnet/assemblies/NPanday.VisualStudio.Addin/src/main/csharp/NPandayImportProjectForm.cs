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
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using System.Xml;
using EnvDTE;
using EnvDTE80;
using log4net;
using Microsoft.Win32;

namespace NPanday.VisualStudio.Addin
{
    public partial class NPandayImportProjectForm : Form
    {
        private DTE2 applicationObject;

        private static readonly ILog log = LogManager.GetLogger(typeof(NPandayImportProjectForm));

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

        public NPandayImportProjectForm()
        {
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
                    string companyId = Registry.LocalMachine.OpenSubKey(@"Software\Microsoft\Windows NT\CurrentVersion").GetValue("RegisteredOrganization", "mycompany").ToString();
                    string groupId = string.Empty;

                    if (companyId != string.Empty)
                    {
                        groupId = FilterID(ConvertToPascalCase(companyId)) + ".";
                    }

                    groupId = groupId + FilterID(ConvertToPascalCase(new FileInfo(applicationObject.Solution.FileName).Name.Replace(".sln", "")));
                    txtGroupId.Text = groupId;
                    string scmTag = string.Empty;  //getSCMTag(applicationObject.Solution.FileName);
                    string version = "1.0-SNAPSHOT";
                    string pomFilePath = applicationObject.Solution.FileName.Substring(0, applicationObject.Solution.FileName.LastIndexOf("\\"));
                    pomFilePath += "\\pom.xml";
                    if (File.Exists(pomFilePath))
                    {
                        XmlDocument doc = new XmlDocument();
                        doc.Load(pomFilePath);
                        System.Xml.XmlNamespaceManager xmlnsManager = new System.Xml.XmlNamespaceManager(doc.NameTable);
                        xmlnsManager.AddNamespace("pom", "http://maven.apache.org/POM/4.0.0");
                        XmlNode node = doc.SelectSingleNode("/pom:project/pom:scm/pom:developerConnection", xmlnsManager);
                        if (node != null)
                        {
                            scmTag = node.InnerText;
                        }
                        node = doc.SelectSingleNode("/pom:project/pom:version", xmlnsManager);
                        if (node != null)
                        {
                            version = node.InnerText;
                        }
                    }

                    if (!string.IsNullOrEmpty(scmTag))
                    {
                        txtSCMTag.Text = scmTag;
                    }

                    txtVersion.Text = version;

                    // TODO: remember this, or have a default
                    useMsDeployCheckBox.Checked = true;
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
            //Refactored code for easier Unit Testing
            try
            {
                GeneratePom(txtBrowseDotNetSolutionFile.Text, txtGroupId.Text.Trim(), txtVersion.Text.Trim(), txtSCMTag.Text, useMsDeployCheckBox.Checked);
            }
            catch (Exception exception)
            {
                log.Debug("Import error", exception);
                MessageBox.Show(exception.Message, "NPanday Import Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        protected void GeneratePom(String solutionFile, String groupId, String version, String scmTag, bool useMsDeploy)
        {
            string warningMsg = string.Empty;
            String mavenVerRegex = "^[0-9]+(" + Regex.Escape(".") + "?[0-9]+){0,3}$";
            String hasAlpha = "[a-zA-Z]";
            String singleMavenVer = "[0-9]+";
            bool isMatch = false;

            if (version.Length > 1)
            {

                if (Regex.IsMatch(version, hasAlpha) && version.ToUpper().EndsWith("-SNAPSHOT"))
                {
                    isMatch = Regex.IsMatch(version.ToUpper().Replace("-SNAPSHOT", ""), mavenVerRegex, RegexOptions.Singleline);
                }
                else
                {
                    isMatch = Regex.IsMatch(version, mavenVerRegex, RegexOptions.Singleline);
                }
            }
            else if (version.Length == 1)
            {
                isMatch = Regex.IsMatch(version, singleMavenVer);
            }


            if (!String.Empty.Equals(solutionFile) && System.IO.File.Exists(solutionFile)
                   && (!String.Empty.Equals(groupId)) && (!String.Empty.Equals(version)) && isMatch)
            {
                // Generate here                

                FileInfo file = new FileInfo(solutionFile);

                string artifactId = FilterID(ConvertToPascalCase(file.Name.Replace(".sln", ""))) + "-parent";
                groupId = FilterID(groupId);

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

                        Regex urlValidator = new Regex(@"^((ht|f)tp(s?)\:\/\/|~/|/)?([\w]+:\w+@)?([a-zA-Z]{1}([\w\-]+\.)+([\w]{2,5}))(:[\d]{1,5})?((/?\w+/)+|/?)(\w+\.[\w]{3,4})?((\?\w+=\w+)?(&\w+=\w+)*)?");
                        if (!urlValidator.IsMatch(scmTag))
                            throw new Exception(string.Format("SCM tag {0} is incorrect format", scmTag));

                        System.Net.HttpWebRequest request = (System.Net.HttpWebRequest)System.Net.WebRequest.Create(scmTag);
                        request.Method = "GET";
                        System.Net.WebResponse response = request.GetResponse();
                        if (response.ResponseUri.AbsoluteUri.Contains("url=")) // verify if just forwarded to a external DNS server (e.g. openDNS.com)
                            throw new Exception(string.Format("SCM tag {0} is not accessible", scmTag));
                    }
                }
                catch
                {
                    if (DialogResult.Yes == MessageBox.Show(string.Format("SCM tag {0} was not accessible, would you still like to proceed with Project import?", scmTag), "SCM Tag", MessageBoxButtons.YesNo, MessageBoxIcon.Warning))
                    {
                        warningMsg = string.Format("\n    The SCM URL {0} was added to the POM but could not be resolved and may not be valid.", scmTag);
                    }
                    else
                    {
                        return;
                    }
                }

                validateSolutionStructure();
                resyncAllArtifacts();
                // TODO: nicer to have some sort of structure / flags for the Msdeploy bit, or this dialog will get out of control over time - perhaps a "project configuration" dialog can replace the test popup
                string[] generatedPoms = ProjectImporter.NPandayImporter.ImportProject(file.FullName, groupId, artifactId, version, scmTag, true, useMsDeploy, ref warningMsg);
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
                string message = (!(!"".Equals(solutionFile) && System.IO.File.Exists(solutionFile))) ? string.Format("Solution File Not Found: {0} ", solutionFile) : "";

                if (String.IsNullOrEmpty(message))
                {
                    message = message + (String.IsNullOrEmpty(groupId) ? "Group Id is empty." : "");
                }
                else
                {
                    message = message + Environment.NewLine + (String.IsNullOrEmpty(groupId) ? "Group Id is empty." : "");
                }

                //Adding error message for empty Version field
                if (String.IsNullOrEmpty(version))
                {
                    message += Environment.NewLine + "Version is empty.";
                }

                else if (!isMatch)
                {
                    message += Environment.NewLine + "Version should be in the form major.minor.build.revision-SNAPSHOT";
                }

                throw new Exception(message);
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
                    log.Info("Re-syncing artifacts... ");
                    try
                    {
                        foreach (IReferenceManager mgr in refManagers)
                        {
                            mgr.OnError += new EventHandler<ReferenceErrorEventArgs>(refmanager_OnError);
                            mgr.ResyncArtifacts();
                        }

                        if (!refManagerHasError)
                        {
                            log.InfoFormat("done [{0}]", DateTime.Now.ToString("hh:mm tt"));
                        }
                    }
                    catch (Exception ex)
                    {
                        if (refManagerHasError)
                        {
                            log.Warn(ex.Message, ex);
                        }
                        else
                        {
                            log.Fatal(ex.Message, ex);
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
                    if (isFlatSingleModule)
                    {
                        if (solutionDir != projDir)
                        {
                            throw new Exception("Project Importer failed with project " + project.Name + ". Project directory structure not supported: in a single module project, the solution and project must be in the same directory");
                        }
                    }
                    else
                    {
                        // This check seems too arbitrary - removed for now
                        //                        if (solutionDir != Path.GetDirectoryName(projDir))
                        //                        {
                        //                            throw new Exception("Project Importer failed with project " + project.Name + ". Project directory structure may not be supported: in a multi-module project, the project must be in a direct subdirectory of the solution");
                        //                        }
                    }
                }
            }
        }

        private bool refManagerHasError = false;
        void refmanager_OnError(object sender, ReferenceErrorEventArgs e)
        {
            refManagerHasError = true;
            log.Warn(e.Message);
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
