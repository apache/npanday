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
using System.Net;
using NPanday.ProjectImporter;

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
            log.Debug("Projects: " + applicationObject.Solution.Projects.Count);
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
                        node = doc.SelectSingleNode("/pom:project/pom:groupId", xmlnsManager);
                        if (node != null)
                        {
                            groupId = node.InnerText;
                        }
                    }

                    if (!string.IsNullOrEmpty(scmTag))
                    {
                        txtSCMTag.Text = scmTag;
                    }

                    txtGroupId.Text = groupId;
                    txtVersion.Text = version;

                    // assuming just one cloud project for now
                    List<string> cloudConfigurations = new List<string>();

                    // these could be per project, but assume they match up for now
                    List<string> availableConfigurations = new List<string>();

                    bool hasWebProjects = false, hasCloudProjects = false;
                    Solution2 solution = (Solution2)applicationObject.Solution;

                    foreach (Project project in Connect.GetAllProjects(solution.Projects))
                    {
                        bool web = isWebProject(project);
                        bool cloud = isCloudProject(project);

                        foreach (object c in ((object[])project.ConfigurationManager.ConfigurationRowNames))
                        {
                            string configuration = (string)c;
                            if (!availableConfigurations.Contains(configuration))
                            {
                                availableConfigurations.Add(configuration);
                            }
                        }

                        if (web)
                        {
                            hasWebProjects = true;
                        }
                        if (cloud)
                        {
                            hasCloudProjects = true;

                            foreach (EnvDTE.ProjectItem item in project.ProjectItems)
                            {
                                if (item.Name.EndsWith(".cscfg", StringComparison.OrdinalIgnoreCase))
                                {
                                    cloudConfigurations.Add(item.Name);
                                }
                            }
                        }

                        log.DebugFormat("Project: {0} (Web = {1}, Cloud = {2}, Kind = {3})", project.Name, web, cloud, project.Kind);
                    }
                    // disabled if there are cloud projects (must be on), or if there are no web projects (not useful)
                    useMsDeployCheckBox.Enabled = hasWebProjects && !hasCloudProjects;

                    // TODO: remember this, or have a default
                    // force to false if no web projects, force to true if cloud projects
                    useMsDeployCheckBox.Checked = hasWebProjects || hasCloudProjects;

                    cloudConfigComboBox.Enabled = hasCloudProjects && cloudConfigurations.Count > 0;
                    cloudConfigComboBox.Items.Add("(Default)");
                    cloudConfigComboBox.Items.AddRange(cloudConfigurations.ToArray());
                    cloudConfigComboBox.SelectedItem = "(Default)";
                    configComboBox.Enabled = availableConfigurations.Count > 0;
                    configComboBox.Items.Add("(Default)");
                    configComboBox.Items.AddRange(availableConfigurations.ToArray());
                    configComboBox.SelectedItem = "(Default)";

                    log.Debug("Configurations: " + string.Join(", ", availableConfigurations.ToArray()));
                    if (hasCloudProjects)
                        log.Debug("Cloud configuration files: " + string.Join(", ", cloudConfigurations.ToArray()));
                }
                catch (Exception e)
                {
                    log.Debug("Error constructing the import form: " + e.Message, e);
                }

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
            string configuration = null;
            if (configComboBox.SelectedItem != "(Default)")
            {
                configuration = (string) configComboBox.SelectedItem;
            }

            string cloudConfig = null;
            if (cloudConfigComboBox.SelectedItem != "(Default)")
            {
                cloudConfig = (string)cloudConfigComboBox.SelectedItem;
            }

            DependencySearchConfiguration depSearchConfig = new DependencySearchConfiguration();
            depSearchConfig.SearchFramework = searchFrameworkCheckBox.Checked;
            depSearchConfig.SearchReferenceAssemblies = searchRefAssembliesCheckBox.Checked;
            depSearchConfig.SearchGac = searchGacCheckBox.Checked;

            //Refactored code for easier Unit Testing
            try
            {
                GeneratePom(txtBrowseDotNetSolutionFile.Text, txtGroupId.Text.Trim(), txtVersion.Text.Trim(), txtSCMTag.Text, useMsDeployCheckBox.Checked, configuration, cloudConfig, depSearchConfig);
            }
            catch (Exception exception)
            {
                log.Debug("Import error", exception);
                MessageBox.Show(exception.Message, "NPanday Import Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        protected void GeneratePom(String solutionFile, String groupId, String version, String scmTag, bool useMsDeploy)
        {
            GeneratePom(solutionFile, groupId, version, scmTag, useMsDeploy, null, null, null);
        }

        protected void GeneratePom(String solutionFile, String groupId, String version, String scmTag, bool useMsDeploy, string configuration, string cloudConfig, DependencySearchConfiguration depSearchConfig)
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

                        verifyRemoteAccess(scmTag, null);
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
                string[] generatedPoms = ProjectImporter.NPandayImporter.ImportProject(file.FullName, groupId, artifactId, version, scmTag, true, useMsDeploy, configuration, cloudConfig, depSearchConfig, ref warningMsg);
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

        private void verifyRemoteAccess(string url, CredentialCache credentials)
        {
            try
            {
                System.Net.HttpWebRequest request = (System.Net.HttpWebRequest)System.Net.WebRequest.Create(url);
                if (credentials != null)
                    request.Credentials = credentials;
                request.Method = "HEAD";
                System.Net.WebResponse response = request.GetResponse();
                if (response.ResponseUri.AbsoluteUri.Contains("url=")) // verify if just forwarded to a external DNS server (e.g. openDNS.com)
                    throw new Exception(string.Format("SCM tag {0} is not accessible", url));
            }
            catch (WebException ex)
            {
                // ask for user credentials then try again
                if (ex.Response != null && (ex.Response as HttpWebResponse).StatusCode == HttpStatusCode.Unauthorized)
                {
                    LoginForm dialog = new LoginForm("Enter SCM credentials:");
                    if (dialog.ShowDialog(this) == DialogResult.OK)
                    {
                        CredentialCache cache = new CredentialCache();
                        cache.Add(new Uri(url), "Basic", new NetworkCredential(dialog.Username, dialog.Password));
                        verifyRemoteAccess(url, cache);
                    }
                    else
                        throw new Exception("Sorry, but you are not authorized to access the specified URL.");
                }
                else
                {
                    throw new Exception("Sorry, but you have entered an invalid URL.");
                }
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
                foreach (Project project in Connect.GetAllProjects(solution.Projects))
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
            Projects projects = solution.Projects;
            bool isFlatSingleModule = (projects.Count == 1
                && Path.GetExtension(projects.Item(1).FullName).EndsWith("proj")
                && solutionDir == Path.GetDirectoryName(projects.Item(1).FullName));

            foreach (Project project in Connect.GetAllProjects(projects))
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

        private static bool isWebProject(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // TODO: better location for utility
            return Connect.IsWebProject(project);
        }

        private static bool isCloudProject(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // TODO: better location for utility
            return Connect.IsCloudProject(project);
        }

        private static bool isFolder(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // TODO: better location for utility
            return Connect.IsFolder(project);
        }
    }
}
