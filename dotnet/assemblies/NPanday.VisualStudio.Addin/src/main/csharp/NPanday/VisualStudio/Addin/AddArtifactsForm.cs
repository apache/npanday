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

using NPanday.Artifact;
using NPanday.Logging;
using NPanday.Model.Pom;
using NPanday.Model.Setting;

using NPanday.Utils;

namespace NPanday.VisualStudio.Addin
{
    public partial class AddArtifactsForm : Form
    {
        private List<NPanday.Artifact.Artifact> localArtifacts = new List<NPanday.Artifact.Artifact>();
        private ArtifactContext artifactContext;
        private Project project;
        private NPanday.Logging.Logger logger;
        private FileInfo pom;
        private WebClient webClient = new WebClient();
        public bool fileProtocol = false;

        private string settingsPath;
        private Settings settings;
        private string defaultProfileID = "NPanday.id";
        private NPanday.Model.Setting.Profile defaultProfile;
        private NPanday.Model.Setting.Repository selectedRepo;
        private string prevSelectedRepoUrl = string.Empty;
        
        /// <summary>
        /// For Testing
        /// </summary>
        public AddArtifactsForm()
        {
            //InitializeForm();
            InitializeComponent();
            addArtifact.Show();
        }

        public AddArtifactsForm(Project project, ArtifactContext container, Logger logger, FileInfo pom)
        {
            this.project = project;
            this.logger = logger;
            InitializeForm();
            InitializeComponent();
            addArtifact.Visible = true;
            localListView.View = View.Details;           
            artifactContext = container;
            this.pom = pom;
        }

        public static void SetUnsafeHttpHeaderParsing()
        {
            Assembly assembly = Assembly.GetAssembly(typeof(System.Net.Configuration.SettingsSection));
            Type settingsSectionType = assembly.GetType("System.Net.Configuration.SettingsSectionInternal");

            object settingsSection = settingsSectionType.InvokeMember("Section",
                BindingFlags.Static | BindingFlags.GetProperty | BindingFlags.NonPublic,
                null, null, new object[] { });

            FieldInfo fieldInfo = settingsSectionType.GetField("useUnsafeHeaderParsing",
                BindingFlags.NonPublic | BindingFlags.Instance);
            fieldInfo.SetValue(settingsSection, true);
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
            localListView_Refresh();
            loadSettings();

            if (settings == null)
            {
                this.Close();
                return;
            }

            if (settings.profiles == null || settings.profiles.Length < 1)
            {
                MessageBox.Show("No Profile Found. Please Configure your Repository. ", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            defaultProfile = getDefaultProfile();
            selectedRepo = getDefaultRepository();

            if (selectedRepo == null || string.IsNullOrEmpty(selectedRepo.url))
            {
                MessageBox.Show("Remote repository not yet set: Please set your Remote Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
            else
            {
                remoteTreeView_Refresh();
            }

            if (selectedRepo == null)
            {
                repoCombo_Refresh(null);
            }
            else
            {
                repoCombo_Refresh(selectedRepo.url);
            }
            
        }

        private void localListView_Refresh()
        {
            localListView.Items.Clear();
            localArtifacts = artifactContext.GetArtifactRepository().GetArtifacts();
            foreach (NPanday.Artifact.Artifact artifact in localArtifacts)
            {
                LocalArtifactItem item = new LocalArtifactItem(new string[] {
                    artifact.ArtifactId, artifact.Version}, -1);
                item.Artifact = artifact;
                localListView.Items.Add(item);
            }
        }

        private void remoteTreeView_Refresh()
        {
            SetUnsafeHttpHeaderParsing();

            treeView1.Nodes.Clear();
            List<TreeNode> treeNodes = getNodesFor(selectedRepo.url);
            treeView1.Nodes.AddRange(treeNodes.ToArray());            

            prevSelectedRepoUrl = selectedRepo.url;
        }

        private bool isSelectedRepoModified()
        {
            if (selectedRepo == null)
            {
                return false;
            }

            if (selectedRepo.snapshots == null || selectedRepo.releases == null ||
                checkBoxSnapshot.Checked != selectedRepo.snapshots.enabled || checkBoxRelease.Checked != selectedRepo.releases.enabled)
            {
                return true;
            }

            // check if URL is already in NPanday.id profile
            if (defaultProfile != null)
            {
                foreach (NPanday.Model.Setting.Repository repo in defaultProfile.repositories)
                {
                    if (repo.url == RepoCombo.Text)
                    {
                        return false;
                    }
                }
            }

            return true;
        }

        private bool isIncluded(string name, string uri)
        {
            if (name.StartsWith(".") || name.Equals("Parent Directory") || name.Equals("Terms of Use"))
            {
                return false;
            }
            
            if (uri.StartsWith(".."))
            {
                return false;
            }
            
            if (uri.Contains("."))
            {
                string[] tokens = name.Split(".".ToCharArray());
                string extension = tokens[tokens.Length - 1];
                if (extension.Equals("txt") || extension.Equals("pom") ||
                    extension.Equals("md5") || extension.Equals("sha1") ||
                    extension.Equals("xml") || extension.Equals("tar") ||
                    extension.Equals("gz") || extension.Equals("rb") || 
                    extension.Equals("htm") || extension.Equals("html") ||
                    extension.Equals("jsp"))
                {
                    return false;
                }
            }
            if (uri.ToLower().StartsWith("http") || uri.ToLower().StartsWith("mailto"))
            {
                return false;
            }
            return true;
        }

        private bool isDirectory(string name)
        {
            if (name.Contains("."))
            {
                string[] tokens = name.Split(".".ToCharArray());
                string extension = tokens[tokens.Length - 1];
   
                if (extension.Equals("dll") || extension.Equals("jar") ||
                    extension.Equals("exe"))
                {
                    return false;
                }
            }
            return true;
        }

        private void verifyRemoteAccess(string url)
        {
            while (true)
            {
                try
                {
                    webClient.DownloadData(url);
                    break;
                }
                catch (WebException ex)
                {
                    // ask for user credentials then try again
                    if (ex.Response != null && (ex.Response as HttpWebResponse).StatusCode == HttpStatusCode.Unauthorized)
                    {
                        LoginForm dialog = new LoginForm();
                        if (dialog.ShowDialog(this) == DialogResult.OK)
                        {
                            CredentialCache cache = new CredentialCache();
                            cache.Add(new Uri(url), "Basic", new NetworkCredential(dialog.Username, dialog.Password));
                            webClient.Credentials = cache;
                            continue;
                        }

                        throw new Exception("Sorry, but you are not authorized to access the specified URL.");

                    }
                    else
                    {
                        throw new Exception("Sorry, but you have entered an invalid URL for the Remote Repository.");
                    }
                }
                catch (Exception)
                {
                    throw new Exception("Sorry, but you have entered an invalid URL for the Remote Repository.");
                }
            }
        }

        private void verifyFileProtocol(string url)
        {
            string chkDir = url.Replace("file:///", "");
            if (!Directory.Exists(chkDir))
            {
                throw new Exception("Sorry, but you have entered an invalid URL for the Remote Repository.");
            }
        }

        List<TreeNode> getNodesFromRemote(string url)
        {
            List<TreeNode> treeNodes = new List<TreeNode>();

            byte[] page = null;

            //prevent VS crash
            try
            {
                page = webClient.DownloadData(url);
            }
            catch (Exception)
            {
                //MessageBox.Show("Cannot read remote repository: " + url + " " + ex.Message + ex.StackTrace);
                MessageBox.Show("Cannot read remote repository: " + url, "Configure Repository", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return treeNodes;
            }

            string pattern = (@"<a[^>]*href\s*=\s*[\""\']?(?<URI>[^""'>\s]*)[\""\']?[^>]*>(?<Name>[^<]+|.*?)?<");
            MatchCollection matches = Regex.Matches(Encoding.ASCII.GetString(page), pattern, RegexOptions.IgnoreCase);

            // treeView1.ImageList = imageList1;
            foreach (Match match in matches)
            {
                string name = match.Groups["Name"].Value;
                string uri = match.Groups["URI"].Value;

                // this will convert absolute URI to relative
                if (uri.ToLower().StartsWith("http"))
                {
                    uri = Regex.Replace(uri, url, "", RegexOptions.IgnoreCase);
                }

                if (isIncluded(name, uri))
                {
                    RemoteArtifactNode node = new RemoteArtifactNode(name);  // new TreeNode(name);
                    if (!isDirectory(name))
                    {
                        node.ImageIndex = 1;
                    }
                    node.IsFileSystem = false;
                    string ext = Path.GetExtension(name).ToLower();
                    if (ext == ".dll" || ext == ".exe" || ext == ".netmodule" || ext == ".ocx")
                    {
                        node.IsAssembly = true;
                    }
                    else
                    {
                        node.IsAssembly = false;
                    }

                    node.ArtifactUrl = url + "/" + uri.TrimEnd("/".ToCharArray()); ;

                    treeNodes.Add(node);
                }
            }
            return treeNodes;
        }

        private bool isValidRemoteRepositoryUrl(string repoUrl)
        {
            byte[] page = null;

            try
            {
                page = webClient.DownloadData(repoUrl);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Cannot read remote repository: " + repoUrl + " " + ex.Message + ex.StackTrace);
                return false;
            }
            return true;
        }

        private string normalizePath(string path)
        {
            //
            // e.g.: Castle/./Castle.Core/./2.0-rc2/./Castle.Core-2.0-rc2.dll
            // transform it to: Castle/Castle.Core/2.0-rc2/Castle.Core-2.0-rc2.dll
            // 
            //
            return path.Replace("/./", "/");
        }

        void addArtifactToPom(Artifact.Artifact artifact)
        {
            try
            {
                if (pom != null)
                {
                    PomHelperUtility pomUtil = new PomHelperUtility(pom);
                    pomUtil.AddPomDependency(artifact.GroupId, artifact.ArtifactId, artifact.Version);
                }
            }
            catch (Exception err)
            {
                MessageBox.Show(this, err.Message, "NPanday Add Dependency Warning:", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

        }

        bool addVSProjectReference(Artifact.Artifact artifact, string name)
        {
            VSProject vsProject = (VSProject)project.Object;
            if (vsProject.References.Find(name) != null)
            {
                MessageBox.Show(this, "A version of artifact is already added to the project, please remove it first before adding this version.", this.Text);
                return false;
            }

            try
            {
                Assembly a = Assembly.LoadFile(artifact.FileInfo.FullName);
                //if (a.ToString().Split(",".ToCharArray())[0].ToLower().StartsWith("interop."))
                //{
                //    MessageBox.Show("Cannot add COM Interop reference from a Maven Artifact, just use Add Reference if you wish to add a COM reference.", "Add Maven Artifact", MessageBoxButtons.OK, MessageBoxIcon.Information);
                //    return false;
                //}

                addArtifactToPom(artifact);
                vsProject.References.Add(artifact.FileInfo.FullName);
                return true;
            }
            catch
            {
                MessageBox.Show("Cannot add COM reference from a Maven Artifact.", "Add Maven Artifact", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return false;
            }

        }

        bool addVSWebProjectReference(Artifact.Artifact artifact, string name)
        {
            try
            {
                VsWebSite.VSWebSite website = (VsWebSite.VSWebSite)project.Object;
                
                Assembly a = Assembly.LoadFile(artifact.FileInfo.FullName);
                if (a.ToString().Split(",".ToCharArray())[0].ToLower().StartsWith("interop."))
                {
                    MessageBox.Show("Cannot add COM Interop reference from a Maven Artifact, just use Add Reference if you wish to add a COM reference.", "Add Maven Artifact", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    return false;
                }
                
                bool referenced = false;
                try
                {
                    referenced = (website.References.Item(name) != null);
                }
                catch
                {
                    referenced = false;
                }

                if (referenced)
                {
                    MessageBox.Show(this, "A version of artifact is already added to the project, please remove it first before adding this version.", this.Text);
                    return false;
                }

                // not need to written in pom anymore
                //addArtifactToPom(artifact);
                
                website.References.AddFromFile(artifact.FileInfo.FullName);
                return true;
            }
            catch
            {
                MessageBox.Show("Cannot add COM reference from a Maven Artifact.", "Add Maven Artifact", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return false;
            }

        }

        void addLocalArtifact(LocalArtifactItem item)
        {
            NPanday.Artifact.Artifact artifact = item.Artifact;
            addReferenceToProject(ref artifact, item.Text);
        }

        private void addReferenceToProject(ref NPanday.Artifact.Artifact artifact, string text)
        {
            if (project.Object is VSProject)
            {
                IReferenceManager refMgr = new ReferenceManager();
                refMgr.Initialize((VSLangProj80.VSProject2)project.Object);
                artifact = refMgr.Add(new ReferenceInfo(artifact));

                if (!addVSProjectReference(artifact, text))
                    return;
            }
            else if (Connect.IsWebProject(project))
            {
                if (!addVSWebProjectReference(artifact, text))
                    return;
            }
            else
            {
                MessageBox.Show(this, "Cannot add artifact to none VS projects.", this.Text);
                return;
            }
        }

        void addRemoteArtifact(RemoteArtifactNode node)
        {
            string uri = node.ArtifactUrl;
            string paths;
            string repoUrl = selectedRepo.url;
            if (node.IsFileSystem)
            {
                //Uri repoUri = new Uri(repoUrl);
                //paths = uri.Substring(repoUri.LocalPath.Length).Replace(@"\",@"/");
                paths = uri;
            }
            else 
            {
                paths = normalizePath(uri.Substring(repoUrl.Length));
            }

            NPanday.Artifact.Artifact artifact =
                artifactContext.GetArtifactRepository().GetArtifactFor(paths);

            //Download
            artifact.FileInfo.Directory.Create();
            if (node.IsFileSystem)
            {
                if (!File.Exists(artifact.FileInfo.FullName))
                {
                    File.Copy(node.ArtifactUrl, artifact.FileInfo.FullName);
                }
            }
            else
            {
                if (!File.Exists(artifact.FileInfo.FullName))
                {
                    byte[] assembly = webClient.DownloadData(uri);
                    FileStream stream = new FileStream(artifact.FileInfo.FullName, FileMode.Create);
                    stream.Write(assembly, 0, assembly.Length);
                    stream.Flush();
                    stream.Close();
                    stream.Dispose();
                    webClient.Dispose();
                }
                //make sure that file is properly closed before adding it to the reference
                System.Threading.Thread.Sleep(1000);
            }

            addReferenceToProject(ref artifact, artifact.ArtifactId);

            //if (project.Object is VSProject)
            //{
            //    if (!addVSProjectReference(artifact, artifact.ArtifactId))
            //        return;
            //}
            //else if (Connect.IsWebProject(project))
            //{
            //    if (!addVSWebProjectReference(artifact, artifact.ArtifactId))
            //        return;
            //}
            //else
            //{
            //    MessageBox.Show(this, "Cannot add artifact to none VS projects.", this.Text);
            //    return;
            //}

        }

        private void executeRepoUpdate()
        {
            if (string.IsNullOrEmpty(RepoCombo.Text))
            {
                MessageBox.Show("Sorry, Repository cannot be blank.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
            else
            {
                XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Setting.Settings));
                TextWriter writer;

                string selectedUrl = RepoCombo.Text;

                // verify if URL is accessible
                try
                {

                    if (selectedUrl.Contains("file:///"))
                    {
                        verifyFileProtocol(selectedUrl);
                    }
                    else
                    {
                        verifyRemoteAccess(selectedUrl);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    artifactTabControl.SelectedIndex = 2;
                    RepoCombo.Text = string.Empty;
                    return;
                }

                if (defaultProfile == null)
                {
                    defaultProfile = getDefaultProfile();
                }

                // if NPanday profile is not found, create it
                if (defaultProfile != null)
                {
                    selectedRepo = getRepositoryFromProfile(defaultProfile, selectedUrl);

                    // if found, remove then add
                    if (selectedRepo != null)
                    {
                        removeRepositoryFromProfile(selectedRepo, defaultProfile);
                        updateRepository(defaultProfile, selectedRepo);
                    }
                    else
                    {
                        selectedRepo = new NPanday.Model.Setting.Repository();
                        selectedRepo.url = selectedUrl;
                        updateRepository(defaultProfile, selectedRepo);
                    }
                    addRepositoryToProfile(selectedRepo, defaultProfile);
                }
                else
                {
                    // create new profile
                    defaultProfile = new NPanday.Model.Setting.Profile();
                    defaultProfile.id = defaultProfileID;

                    // create new repo
                    NPanday.Model.Setting.Repository newRepo = new NPanday.Model.Setting.Repository();
                    newRepo.url = selectedUrl;
                    updateRepository(defaultProfile, newRepo);
                    defaultProfile.repositories = new NPanday.Model.Setting.Repository[] { newRepo };

                    if (settings.profiles == null)
                    {
                        settings.profiles = new NPanday.Model.Setting.Profile[] { defaultProfile };
                    }
                    else
                    {
                        List<NPanday.Model.Setting.Profile> profiles = new List<NPanday.Model.Setting.Profile>();
                        profiles.AddRange(settings.profiles);
                        profiles.Add(defaultProfile);
                        settings.profiles = profiles.ToArray();
                    }

                    selectedRepo = newRepo;
                }

                // make NPanday.id profile active
                addActiveProfile(defaultProfileID);

                // write to Settings.xml
                writer = new StreamWriter(settingsPath);
                serializer.Serialize(writer, settings);
                writer.Close();

                // do not specify SelectedUrl to suppress SelectedIndexChanged event
                repoCombo_Refresh(null);
                MessageBox.Show(this, "Successfully Changed Remote Repository.", "Repository Configuration");
                //localListView_Refresh(); 
            }
        }

        private void addActiveProfile(string profileID)
        {
            bool isActive = false;
            if (settings.activeProfiles == null)
            {
                settings.activeProfiles = new string[] { profileID };
                isActive = true;
            }
            else
            {
                foreach (string id in settings.activeProfiles)
                {
                    if (id == profileID)
                    {
                        isActive = true;
                        break;
                    }
                }
            }

            if (!isActive)
            {
                string[] newActiveProfiles = new string[settings.activeProfiles.Length + 1];
                settings.activeProfiles.CopyTo(newActiveProfiles, 0);
                newActiveProfiles[settings.activeProfiles.Length] = profileID;
                settings.activeProfiles = newActiveProfiles;
            }
        }

        private void repoCombo_Refresh(string selectedUrl)
        {
            RepoCombo.Items.Clear();
            foreach (NPanday.Model.Setting.Repository repo in getAllRepositories())
            {
                if (!RepoCombo.Items.Contains(repo.url))
                {
                    RepoCombo.Items.Add(repo.url);
                }
            }

            if (RepoCombo.Items.Count > 0 && !string.IsNullOrEmpty(selectedUrl))
            {
                RepoCombo.SelectedIndex = RepoCombo.Items.IndexOf(selectedUrl);
            }
        }

        private void repoCheckboxes_Refresh()
        {
            checkBoxRelease.Checked = (selectedRepo.releases != null)? selectedRepo.releases.enabled: false;
            checkBoxSnapshot.Checked = (selectedRepo.snapshots != null)? selectedRepo.snapshots.enabled: false;
        }

        #region GUI Events

        private void localListView_DoubleClick(object sender, EventArgs e)
        {
            try
            {
                LocalArtifactItem item = localListView.SelectedItems[0] as LocalArtifactItem;

                addLocalArtifact(item);
            }
            catch (Exception ex)
            {
                MessageBox.Show(this, ex.Message, "NPanday Add Dependency Warning:", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void treeView1_NodeMouseDoubleClick(object sender, TreeNodeMouseClickEventArgs e)
        {
            try
            {
                RemoteArtifactNode node = e.Node as RemoteArtifactNode;
                if (node.IsAssembly)
                {
                    addRemoteArtifact(node);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(this, ex.Message, "NPanday Add Dependency Warning:", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void addArtifact_Click(object sender, EventArgs e)
        {
            try
            {
                ListView.SelectedListViewItemCollection selectedItems = localListView.SelectedItems;
                if (selectedItems != null)
                {
                    foreach (ListViewItem item in selectedItems)
                    {
                        addLocalArtifact(item as LocalArtifactItem);
                    }
                }

                if (treeView1.SelectedNode != null)
                {

                    RemoteArtifactNode treeNode = treeView1.SelectedNode as RemoteArtifactNode;

                    if (treeNode.IsAssembly)
                    {
                        addRemoteArtifact(treeNode);
                    }
                    else
                    {
                        MessageBox.Show(this, string.Format("Cannot add {0} not an artifact assembly.", treeNode.FullPath), this.Text);
                        return;
                    }
                }
            }
            catch (Exception err)
            {
                MessageBox.Show(this, err.Message, "NPanday Add Dependency Warning:", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            this.Close();
        }

        private void update_Click(object sender, EventArgs e)
        {
            executeRepoUpdate();
        }

        private void RepoListBox_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void ConfigureTab_Click(object sender, EventArgs e)
        {
            addArtifact.Hide();
        }

        private void treeView1_AfterSelect(object sender, TreeViewEventArgs e)
        {
            RemoteArtifactNode node = e.Node as RemoteArtifactNode;
            if (node.IsAssembly)
                return;

            List<TreeNode> treeNodes = getNodesFor(node.ArtifactUrl);
            node.Nodes.Clear();
            node.Nodes.AddRange(treeNodes.ToArray());

            addArtifact.Show();
        }

        private void remoteTabPage_Click(object sender, EventArgs e)
        {
            addArtifact.Show();
        }

        private void localTabPage_Click(object sender, EventArgs e)
        {
            addArtifact.Show();
        }

        private void localListView_SelectedIndexChanged(object sender, EventArgs e)
        {
            addArtifact.Show();
        }

        private void RepoCombo_SelectedIndexChanged(object sender, EventArgs e)
        {
            selectedRepo = getRepository(RepoCombo.Text);
            repoCheckboxes_Refresh();
        }

        private void artifactTabControl_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (artifactTabControl.SelectedIndex == 2)
            {
                RepoCombo.Focus();
                addArtifact.Hide();
            }
            else if (artifactTabControl.SelectedIndex == 1)
            {
                //check if there is an existing settings.xml file 
                if (settings == null)
                {
                    loadSettings();
                }

                if (settings == null)
                {
                    MessageBox.Show("Sorry, but you cannot Access Remote Repository without a Settings.xml file", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    artifactTabControl.SelectedIndex = 0;
                }
                else
                {
                    if (selectedRepo == null)
                    {
                        selectedRepo = getDefaultRepository();
                    }

                    if (selectedRepo != null)
                    {
                        if (isSelectedRepoModified())
                        {
                            executeRepoUpdate();
                        }

                        if (prevSelectedRepoUrl != selectedRepo.url)
                        {
                            remoteTreeView_Refresh();
                        }
                        treeView1.Focus();
                        addArtifact.Show();
                    }
                }
            }
            else
            {
                localListView.Focus();
                addArtifact.Show();
            }
        }

        #endregion

        #region GUI Operations

        List<TreeNode> getNodesFor(string url)
        {
            try
            {
                Uri repoUri = new Uri(url);
                if (repoUri.IsFile)
                {
                    return getNodesFromLocal(repoUri.LocalPath);
                }
                else
                {
                    return getNodesFromRemote(url);
                }
            }
            catch (Exception e)
            {
                MessageBox.Show("There was a problem with the provided URL. \nStack Trace:" + e.Message, "Get Artifacts from Remote Repository Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            return null;
        }

        List<TreeNode> getNodesFromLocal(string repoFolder)
        {
            List<TreeNode> nodes = new List<TreeNode>();
            if (!Directory.Exists(repoFolder))
            {
                MessageBox.Show(this, "Local repository path not found.", "Local Repository");
                return nodes;
            }

            foreach (FileSystemInfo fsi in (new DirectoryInfo(repoFolder).GetFileSystemInfos()))
            {
                if (fsi is FileInfo)
                {
                    string ext = Path.GetExtension(fsi.FullName).ToLower();
                    if (ext != ".dll" && ext != ".exe" && ext != ".netmodule" && ext != ".ocx")
                        continue;
                }
                RemoteArtifactNode node = new RemoteArtifactNode(fsi.Name);
                node.IsFileSystem = true;
                node.ArtifactUrl = Path.Combine(repoFolder, fsi.Name);
                node.IsAssembly = (fsi is FileInfo);
                nodes.Add(node);
            }

            return nodes;
        }

        #endregion

        #region Settings.xml Operations

        private void loadSettings()
        {
            settingsPath = SettingsUtil.GetUserSettingsPath();
            try
            {
                if (File.Exists(settingsPath))
                {
                    settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
                }
                else
                {
                    throw new Exception("Sorry, but no settings.xml file was found in your Local Repository.");
                }

            }
            catch (Exception ex)
            {
                MessageBox.Show("Invalid settings.xml File:" + " " + ex.Message, "Configuration Warning", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }
        }

        private NPanday.Model.Setting.Profile getDefaultProfile()
        {
            if (settings == null)
            {
                loadSettings();
            }

            if (settings != null)
            {
                foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
                {
                    if (defaultProfileID.Equals(profile.id))
                    {
                        return profile;
                    }
                }
            }
            return null;
        }

        private NPanday.Model.Setting.Repository getRepository(string url)
        {
            if (string.IsNullOrEmpty(url))
            {
                return null;
            }

            if (defaultProfile == null)
            {
                defaultProfile = getDefaultProfile();
            }

            // extract from NPanday repositories first
            if (defaultProfile != null)
            {
                foreach (NPanday.Model.Setting.Repository repo in defaultProfile.repositories)
                {
                    if (url.Equals(repo.url))
                    {
                        return repo;
                    }
                }
            }

            // extract from NON-NPanday repositories
            foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
            {
                if (profile.id != defaultProfileID)
                {
                    foreach (NPanday.Model.Setting.Repository repo in profile.repositories)
                    {
                        if (url.Equals(repo.url))
                        {
                            return repo;
                        }
                    }
                }
            }

            return null;
        }

        private NPanday.Model.Setting.Repository getDefaultRepository()
        {
            if (defaultProfile == null)
            {
                defaultProfile = getDefaultProfile();
            }

            if (defaultProfile != null && defaultProfile.repositories.Length > 0)
            {
                return defaultProfile.repositories[0];
            }
            return null;
        }

        private List<NPanday.Model.Setting.Repository> getAllRepositories()
        {
            List<NPanday.Model.Setting.Repository> repos = new List<NPanday.Model.Setting.Repository>();

            if (defaultProfile == null)
            {
                defaultProfile = getDefaultProfile();
            }

            foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
            {
                repos.AddRange(profile.repositories);
            }
            return repos;
        }

        private NPanday.Model.Setting.Repository getRepositoryFromProfile(NPanday.Model.Setting.Profile profile, string url)
        {
            foreach (NPanday.Model.Setting.Repository repo in profile.repositories)
            {
                if (url.Equals(repo.url))
                {
                    return repo;
                }
            }
            return null;
        }

        private void addRepositoryToProfile(NPanday.Model.Setting.Repository repository, NPanday.Model.Setting.Profile profile)
        {
            // activate profile
            NPanday.Model.Setting.Activation activation = new NPanday.Model.Setting.Activation();
            activation.activeByDefault = true;
            profile.activation = activation;

            // add repository to profile
            if (profile.repositories == null)
            {
                profile.repositories = new NPanday.Model.Setting.Repository[] { repository };
            }
            else
            {
                List<NPanday.Model.Setting.Repository> repositories = new List<NPanday.Model.Setting.Repository>();
                repositories.AddRange(profile.repositories);
                repositories.Insert(0, repository);
                profile.repositories = repositories.ToArray();
            }
        }

        private void removeRepositoryFromProfile(NPanday.Model.Setting.Repository repository, NPanday.Model.Setting.Profile profile)
        {
            List<NPanday.Model.Setting.Repository> repositories = new List<NPanday.Model.Setting.Repository>();
            repositories.AddRange(profile.repositories);
            repositories.Remove(repository);
            profile.repositories = repositories.ToArray();
        }

        private void updateRepository(NPanday.Model.Setting.Profile profile, NPanday.Model.Setting.Repository repository)
        {
            NPanday.Model.Setting.Activation activation = new NPanday.Model.Setting.Activation();
            activation.activeByDefault = true;
            profile.activation = activation;

            NPanday.Model.Setting.RepositoryPolicy releasesPolicy = new NPanday.Model.Setting.RepositoryPolicy();
            NPanday.Model.Setting.RepositoryPolicy snapshotsPolicy = new NPanday.Model.Setting.RepositoryPolicy();
            releasesPolicy.enabled = checkBoxRelease.Checked;
            snapshotsPolicy.enabled = checkBoxSnapshot.Checked;
            repository.releases = releasesPolicy;
            repository.snapshots = snapshotsPolicy;
            repository.id = repository.url;
        }

        #endregion
    }

    class LocalArtifactItem : ListViewItem
    {
        public LocalArtifactItem() { }
        public LocalArtifactItem(string name)
            : base(name)
        {
        }

        public LocalArtifactItem(string[] items) : base(items) { }

        public LocalArtifactItem(string[] items, int imageIndex) : base(items, imageIndex) { }

        private NPanday.Artifact.Artifact artifact;

        public NPanday.Artifact.Artifact Artifact
        {
            get { return artifact; }
            set { artifact = value; }
        }


    }


    class RemoteArtifactNode : TreeNode
    {
        public RemoteArtifactNode() { }
        public RemoteArtifactNode(string name) : base(name)
        {
        }
        private bool isAssembly;

        public bool IsAssembly
        {
            get { return isAssembly; }
            set { isAssembly = value; }
        }

        private string artifactUrl;

        public string ArtifactUrl
        {
            get { return artifactUrl; }
            set { artifactUrl = value; }
        }

        private bool isFileSystem;

        public bool IsFileSystem
        {
            get { return isFileSystem; }
            set { isFileSystem = value; }
        }

    }
}