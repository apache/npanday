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

        #region configure repo
        
        private Settings settings;
        private String settingsPath;
        
        #endregion

        /// <summary>
        /// For Testing
        /// </summary>
        public AddArtifactsForm()
        {
            //InitializeForm();
            InitializeComponent();
            addArtifact.Hide();
            // localListView.View = View.Details;
            #region Initialize Configuration Repo
            settingsPath = SettingsUtil.GetUserSettingsPath();
            try
            {
                settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message + e.StackTrace);
            }
            #endregion
        }

        public AddArtifactsForm(Project project, ArtifactContext container, Logger logger, FileInfo pom)
        {
            this.project = project;
            this.logger = logger;
            InitializeForm();
            InitializeComponent();
            addArtifact.Hide();
            localListView.View = View.Details;           
            artifactContext = container;
            this.pom = pom;
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

        private void Refresh()
        {
            localArtifacts = artifactContext.GetArtifactRepository().GetArtifacts();
            foreach (NPanday.Artifact.Artifact artifact in localArtifacts)
            {
                LocalArtifactItem item = new LocalArtifactItem(new string[] {
                    artifact.ArtifactId, artifact.Version}, -1);
                item.Artifact = artifact;
                localListView.Items.Add(item);
            }

            String settingsPath = SettingsUtil.GetUserSettingsPath();
            Settings settings = null;

            try
            {
                if (File.Exists(settingsPath))
                {
                    settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
                }
                else
                {
                    MessageBox.Show("Sorry, but no settings.xml file was found in your Local Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }
            }
            catch(Exception)
            {
                //MessageBox.Show("Invalid Settings File: " + ex.Message + ex.StackTrace);
                MessageBox.Show("Settings.xml could not be read", "Invalid Settings File", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            if (settings == null || settings.profiles == null)
            {
                MessageBox.Show("No Profile Found. Configure repository: ");
                return;
            }

            String url = getRepositoryUrl();

            if (url == null)
            {
                //MessageBox.Show("Remote repository not set: Try 'Add Maven Repository' option from menu. Will" +
                //    " require restart of addin.");
                //return;
                MessageBox.Show("Remote repository not yet set: Please set your Remote Repository.");
                ConfigureTab.Focus();
                return;
            }

            SetUnsafeHttpHeaderParsing();

            List<TreeNode> treeNodes = getNodesFor(url);
            treeView1.Nodes.Clear();
            treeView1.Nodes.AddRange(treeNodes.ToArray());
            treeView1.MouseClick += new System.Windows.Forms.MouseEventHandler(treeView_MouseUp);


            if (settings == null || settings.profiles == null)
            {
                return;
            }

            foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
            {
                foreach (NPanday.Model.Setting.Repository repository in profile.repositories)
                {
                    if (repository.id.Equals("NPanday.id"))
                    {
                        RepoCombo.SelectedIndex = RepoCombo.Items.Add(repository.url);
                    }
                }
            }
        }

        private void AddArtifactsForm_Load(object sender, EventArgs e)
        {
            localArtifacts = artifactContext.GetArtifactRepository().GetArtifacts();
            foreach (NPanday.Artifact.Artifact artifact in localArtifacts)
            {
                LocalArtifactItem item = new LocalArtifactItem(new string[] {
                    artifact.ArtifactId, artifact.Version}, -1);
                item.Artifact = artifact;
                localListView.Items.Add(item);
            }

            String settingsPath = SettingsUtil.GetUserSettingsPath();
            Settings settings = null;
            try
            {
                if (File.Exists(settingsPath))
                {
                    settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
                }
                else
                {
                    MessageBox.Show("Sorry, but no settings.xml file was found in your Local Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }
                
            }
            catch (Exception ex)
            {
                MessageBox.Show("Invalid Settings File: " + ex.Message + ex.StackTrace);
                return;
            }

            if (settings.profiles == null)
            {
                MessageBox.Show("No Profile Found. Please Configure your Repository. ","Repository Configuration",MessageBoxButtons.OK,MessageBoxIcon.Warning);
                return;
            }

            String url = getRepositoryUrl();

            if (url == null)
            {
                //MessageBox.Show("Remote repository not set: Try 'Add Maven Repository' option from menu. Will" +
                //    " require restart of addin.");
                //return;
                MessageBox.Show("Remote repository not yet set: Please set your Remote Repository.","Repository Configuration",MessageBoxButtons.OK,MessageBoxIcon.Warning);
                return;
            }

            SetUnsafeHttpHeaderParsing();

            List<TreeNode> treeNodes = getNodesFor(url);
            treeView1.Nodes.AddRange(treeNodes.ToArray());
            treeView1.MouseClick += new System.Windows.Forms.MouseEventHandler(treeView_MouseUp);


            if (settings == null || settings.profiles == null)
            {
                return;
            }

            foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
            {
                foreach (NPanday.Model.Setting.Repository repository in profile.repositories)
                {
                    if (repository.id.Equals("NPanday.id"))
                    {
                        RepoCombo.SelectedIndex = RepoCombo.Items.Add(repository.url);
                    }
                }
            }
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
                MessageBox.Show(err.Message, "NPanday Add Dependency Error:");
                return;
            }

            this.Close();
        }

        private Boolean IsIncluded(String name, String uri)
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
                String[] tokens = name.Split(".".ToCharArray());
                String extension = tokens[tokens.Length -1];
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

        private Boolean IsDirectory(String name)
        {
            if (name.Contains("."))
            {
                String[] tokens = name.Split(".".ToCharArray());
                String extension = tokens[tokens.Length - 1];
   
                if (extension.Equals("dll") || extension.Equals("jar") ||
                    extension.Equals("exe"))
                {
                    return false;
                }
            }
            return true;
        }

        List<TreeNode> getNodesFor(String url)
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
                MessageBox.Show("There was a problem with the provided URL. \nStack Trace:"+e.Message,"Get Artifacts from Remote Repository Error",MessageBoxButtons.OK,MessageBoxIcon.Error);
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

        //TODO: make a function to check if the url is accessable or not
        /// <summary>
        /// Checks if a remote repository url is accessible or not
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        private bool HasRemoteAccess(string url)
        {
            WebClient webClient = new WebClient();
            byte[] page = null;
            bool hasRemoteAccess;

            try
            {
                page = webClient.DownloadData(url);
                hasRemoteAccess = true;
            }
            catch (Exception ex)
            {
                hasRemoteAccess = false;
            }
            return hasRemoteAccess;

        }

        List<TreeNode> getNodesFromRemote(string url)
        {
            List<TreeNode> treeNodes = new List<TreeNode>();
            WebClient webClient = new WebClient();

            byte[] page = null;

            //prevent VS crash
            try
            {
                page = webClient.DownloadData(url);
            }
            catch (Exception ex)
            {
                //MessageBox.Show("Cannot read remote repository: " + url + " " + ex.Message + ex.StackTrace);
                MessageBox.Show("Cannot read remote repository: " + url,"Configure Repository",MessageBoxButtons.OK,MessageBoxIcon.Warning);
                return treeNodes;
            }


            String pattern =
                (@"<a[^>]*href\s*=\s*[\""\']?(?<URI>[^""'>\s]*)[\""\']?[^>]*>(?<Name>[^<]+|.*?)?<");
            MatchCollection matches = Regex.Matches(Encoding.ASCII.GetString(page), pattern, RegexOptions.IgnoreCase);

            // treeView1.ImageList = imageList1;
            foreach (Match match in matches)
            {
                String name = match.Groups["Name"].Value;
                String uri = match.Groups["URI"].Value;
                if (IsIncluded(name, uri))
                {
                    RemoteArtifactNode node = new RemoteArtifactNode(name);  // new TreeNode(name);
                    if (!IsDirectory(name))
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

        private void treeView_MouseUp(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                Point point = new Point(e.X, e.Y);
                RemoteArtifactNode node = treeView1.GetNodeAt(point) as RemoteArtifactNode;
                if (node.IsAssembly)
                    return;

                List<TreeNode> treeNodes = getNodesFor( node.ArtifactUrl);
                node.Nodes.Clear();
                node.Nodes.AddRange(treeNodes.ToArray());
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            this.Close();
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

        private Boolean isValidRemoteRepositoryUrl(String repoUrl)
        {
            WebClient webClient = new WebClient();
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

        /*
         * e.g.: Castle/./Castle.Core/./2.0-rc2/./Castle.Core-2.0-rc2.dll
         * transform it to: Castle/Castle.Core/2.0-rc2/Castle.Core-2.0-rc2.dll
         * 
        */
        private String normalizePath(String path)
        {
            return path.Replace("/./", "/");
        }

        private String getRepositoryUrl()
        {
            Settings settings = null;
            String settingsPath = SettingsUtil.GetUserSettingsPath();
            try
            {
                settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message + e.StackTrace);
            }

            foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
            {
                foreach (NPanday.Model.Setting.Repository repository in profile.repositories)
                {
                    if (repository.id.Equals("NPanday.id"))
                    {
                        return repository.url;
                    }
                }
            }
            return null;
        }

        private void localListView_DoubleClick(object sender, EventArgs e)
        {
            try
            {
                LocalArtifactItem item = localListView.SelectedItems[0] as LocalArtifactItem;

                addLocalArtifact(item);

            }
            catch (Exception ex)
            {
                MessageBox.Show(this, ex.Message, "Add Artifacts");
            }
        }

        private void treeView1_NodeMouseDoubleClick(object sender, TreeNodeMouseClickEventArgs e)
        {
            RemoteArtifactNode node = e.Node as RemoteArtifactNode;
            if (node.IsAssembly)
            {
                addRemoteArtifact(node);
            }
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
            catch (Exception err1)
            {
                MessageBox.Show(err1.Message, "NPanday Add Dependency Error:");
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

            if (project.Object is VSProject)
            {
                if (!addVSProjectReference(artifact, item.Text))
                    return;
            }
            else if (Connect.IsWebProject(project))
            {
                if (!addVSWebProjectReference(artifact, item.Text))
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
            String uri = node.ArtifactUrl;
            String paths;
            String repoUrl = getRepositoryUrl();
            if (node.IsFileSystem)
            {
                Uri repoUri = new Uri(repoUrl);
                paths = uri.Substring(repoUri.LocalPath.Length).Replace(@"\",@"/");
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
                    WebClient client = new WebClient();
                    byte[] assembly = client.DownloadData(uri);
                    FileStream stream = new FileStream(artifact.FileInfo.FullName, FileMode.Create);
                    stream.Write(assembly, 0, assembly.Length);
                    stream.Flush();
                    stream.Close();
                    stream.Dispose();
                    client.Dispose();
                }
                //make sure that file is properly closed before adding it to the reference
                System.Threading.Thread.Sleep(1000);
            }

            if (project.Object is VSProject)
            {
                if (!addVSProjectReference(artifact, artifact.ArtifactId))
                    return;
            }
            else if (Connect.IsWebProject(project))
            {
                if (!addVSWebProjectReference(artifact, artifact.ArtifactId))
                    return;
            }
            else
            {
                MessageBox.Show(this, "Cannot add artifact to none VS projects.", this.Text);
                return;
            }

        }

        private void UpdateRepositoryFor(NPanday.Model.Setting.Profile profile, NPanday.Model.Setting.Repository repository)
        {
            NPanday.Model.Setting.Activation activation = new NPanday.Model.Setting.Activation();
            activation.activeByDefault = true;
            profile.activation = activation;

            repository.url = RepoCombo.Text;
            repository.id = "NPanday.id";

            NPanday.Model.Setting.RepositoryPolicy releasesPolicy = new NPanday.Model.Setting.RepositoryPolicy();
            NPanday.Model.Setting.RepositoryPolicy snapshotsPolicy = new NPanday.Model.Setting.RepositoryPolicy();
            releasesPolicy.enabled = checkBoxRelease.Checked;
            snapshotsPolicy.enabled = checkBoxSnapshot.Checked;
            repository.releases = releasesPolicy;
            repository.snapshots = snapshotsPolicy;
        }

        private void update_Click(object sender, EventArgs e)
        {
            ExecuteRepoUpdate();

        }

        private string prevRepo=string.Empty;

        private void ExecuteRepoUpdate()
        {
            if (prevRepo.Equals(RepoCombo.Text))
            {
                return;
            }
            if (!string.IsNullOrEmpty(RepoCombo.Text))
            {
                bool hasConfiguration = false;
                XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Setting.Settings));
                if (settingsPath == null)
                {
                    settingsPath = SettingsUtil.GetUserSettingsPath();
                }
                if (settings == null)
                {
                    try
                    {
                        settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
                    }
                    catch (Exception err)
                    {
                        MessageBox.Show(err.Message + err.StackTrace);
                    }
                }

                TextWriter writer = new StreamWriter(settingsPath);
                if (settings.profiles != null)
                {
                    foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
                    {
                        foreach (NPanday.Model.Setting.Repository repository in profile.repositories)
                        {
                            if (repository.id.Equals("NPanday.id"))
                            {
                                Model.Setting.Repository newRepository = new NPanday.Model.Setting.Repository();
                                newRepository.id = repository.id;
                                newRepository.releases = repository.releases;
                                newRepository.snapshots = repository.snapshots;
                                newRepository.url = repository.url;

                                UpdateRepositoryFor(profile, repository);
                                serializer.Serialize(writer, settings);
                                writer.Close();
                                hasConfiguration = true;
                                try
                                {
                                    if (HasRemoteAccess(RepoCombo.Text))
                                    {
                                        Refresh();
                                        MessageBox.Show(this, "Successfully Changed Remote Repository.", "Repository Configuration");
                                        AddUrl(profile, newRepository);
                                        prevRepo = RepoCombo.Text;
                                        RepoCombo.Items.Clear();
                                        UpdateUrlList();
                                        RepoCombo.SelectedIndex = RepoCombo.Items.IndexOf(repository.url); ;
                                    }
                                    else
                                    {
                                        MessageBox.Show("Sorry, but you have entered an invalid URL for the Remote Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                                        artifactTabControl.SelectedIndex = 2;
                                    }
                                    break;
                                }
                                catch (Exception)
                                {
                                    MessageBox.Show("Sorry, but you have entered an invalid URL for the Remote Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                                    artifactTabControl.SelectedIndex = 2;
                                    RepoCombo.Text = string.Empty;
                                }


                            }
                        }
                    }
                }
                if (!hasConfiguration)
                {
                    NPanday.Model.Setting.Profile profile1 = new NPanday.Model.Setting.Profile();
                    NPanday.Model.Setting.Repository repository1 = new NPanday.Model.Setting.Repository();
                    profile1.repositories = new NPanday.Model.Setting.Repository[] { repository1 };
                    UpdateRepositoryFor(profile1, repository1);

                    if (settings.profiles == null)
                    {
                        settings.profiles = new NPanday.Model.Setting.Profile[] { profile1 };
                    }
                    else
                    {
                        List<NPanday.Model.Setting.Profile> profiles = new List<NPanday.Model.Setting.Profile>();
                        profiles.AddRange(settings.profiles);
                        profiles.Add(profile1);
                        settings.profiles = profiles.ToArray();
                    }
                    serializer.Serialize(writer, settings);
                    writer.Close();
                    try
                    {
                        if (HasRemoteAccess(RepoCombo.Text))
                        {
                            Refresh();
                            prevRepo = RepoCombo.Text;
                            MessageBox.Show(this, "Successfully Changed Remote Repository.", "Repository Configuration");
                        }
                        else
                        {
                            MessageBox.Show("Sorry, but you have entered an invalid URL for the Remote Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                            artifactTabControl.SelectedIndex = 2;
                            RepoCombo.Text = string.Empty;
                        }
                    }
                    catch (Exception)
                    {
                        MessageBox.Show("Sorry, but you have entered an invalid URL for the Remote Repository.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                        artifactTabControl.SelectedIndex = 2;
                        RepoCombo.Text = string.Empty;
                    }

                }
            }
            else
            {
                MessageBox.Show("Sorry, Repository cannot be blank.", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void UpdateUrlList()
        {
            List<string> urls = GetUrls();
            foreach (string item in urls)
            {
                if (!RepoCombo.Items.Contains(item))
                {
                    RepoCombo.Items.Add(item);
                }
            }
        }

        private void AddUrl(NPanday.Model.Setting.Profile profile, NPanday.Model.Setting.Repository repository)
        {

            XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Setting.Settings));
            if (settingsPath == null)
            {
                settingsPath = SettingsUtil.GetUserSettingsPath();
            }
            if (settings == null)
            {
                try
                {
                    settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
                }
                catch (Exception err)
                {
                    MessageBox.Show(err.Message + err.StackTrace);
                }
            }

            TextWriter writer = new StreamWriter(settingsPath);


            NPanday.Model.Setting.Activation activation = new NPanday.Model.Setting.Activation();
            activation.activeByDefault = true;
            profile.activation = activation;



            List<NPanday.Model.Setting.Repository> repositories = new List<NPanday.Model.Setting.Repository>();

            bool urlExist = UrlExists(profile, repository);

            if (!urlExist)
            {
                repositories.AddRange(profile.repositories);
                repositories.Add(repository);
                profile.repositories = repositories.ToArray();
            }

            serializer.Serialize(writer, settings);
            writer.Close();


        }

        private bool UrlExists(NPanday.Model.Setting.Profile profile, NPanday.Model.Setting.Repository repository)
        {
            bool urlExist = false;
            foreach (NPanday.Model.Setting.Repository repo in profile.repositories)
            {
                if (repo.url.Equals(repository.url))
                {
                    urlExist = true;
                    break;
                }
            }
            return urlExist;
        }

        private List<string> GetUrls()
        {
            if (settingsPath == null)
            {
                settingsPath = SettingsUtil.GetUserSettingsPath();
            }
            if (settings == null)
            {
                try
                {
                    settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
                }
                catch (Exception err)
                {
                    MessageBox.Show(err.Message + err.StackTrace);
                }
            }

            List<string> urls = new List<string>();
            foreach (NPanday.Model.Setting.Profile profile in settings.profiles)
            {
                foreach (NPanday.Model.Setting.Repository repository in profile.repositories)
                {
                    //if (repository.id.Equals("NPanday.id"))
                    {
                        if (!urls.Contains(repository.url))
                        {
                            urls.Add(repository.url);
                        }
                    }
                }
            }
            return urls;
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
            addArtifact.Hide();
        }

        private void artifactTabControl_SelectedIndexChanged(object sender, EventArgs e)
        {
            String settingsPath = SettingsUtil.GetUserSettingsPath();
        
            if (artifactTabControl.SelectedIndex == 2)
            {
                //check if there is an existing settings.xml file 
                if (settingsPath == null)
                {
                    MessageBox.Show("Sorry, but you cannot Configure Remote Repository without a Settings.xml file", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    AddArtifactsForm.ActiveForm.Hide();
                    artifactTabControl.SelectedIndex = 0;
                }
                else
                {
                    UpdateUrlList();
                    addArtifact.Hide();
                }
            }
            else if (artifactTabControl.SelectedIndex == 1)
            {
                //check if there is an existing settings.xml file 
                if (settingsPath == null)
                {
                    MessageBox.Show("Sorry, but you cannot Access Remote Repository without a Settings.xml file", "Repository Configuration", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    artifactTabControl.SelectedIndex = 0;
                }
                else
                {
                    ExecuteRepoUpdate();
                    
                    treeView1.Focus();
                    addArtifact.Show();
                }
            }
            else
            {
                localListView.Focus();
                addArtifact.Show();
            }
            
        }
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