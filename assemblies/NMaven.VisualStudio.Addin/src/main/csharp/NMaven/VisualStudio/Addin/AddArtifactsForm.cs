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

using NMaven.Artifact;
using NMaven.Logging;
using NMaven.Model.Pom;
using NMaven.Model.Setting;

using NMaven.Utils;

namespace NMaven.VisualStudio.Addin
{
    public partial class AddArtifactsForm : Form
    {
        private List<NMaven.Artifact.Artifact> localArtifacts = new List<NMaven.Artifact.Artifact>();
        private ArtifactContext artifactContext;
        private Project project;
        private NMaven.Logging.Logger logger;
        private FileInfo pom;

        /// <summary>
        /// For Testing
        /// </summary>
        public AddArtifactsForm()
        {
            //InitializeForm();
            InitializeComponent();
           // localListView.View = View.Details;
        }

        public AddArtifactsForm(Project project, ArtifactContext container, Logger logger, FileInfo pom)
        {
            this.project = project;
            this.logger = logger;
            InitializeForm();
            InitializeComponent();
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

        private void AddArtifactsForm_Load(object sender, EventArgs e)
        {
            localArtifacts = artifactContext.GetArtifactRepository().GetArtifacts();
            foreach (NMaven.Artifact.Artifact artifact in localArtifacts)
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
                settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
            }
            catch (Exception ex)
            {
                MessageBox.Show("Invalid Settings File: " + ex.Message + ex.StackTrace);
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
                MessageBox.Show("Remote repository not set: Try 'Add Maven Repository' option from menu. Will" +
                    " require restart of addin.");
                return;
            }

            SetUnsafeHttpHeaderParsing();

            List<TreeNode> treeNodes = getNodesFor(url);
            treeView1.Nodes.AddRange(treeNodes.ToArray());
            treeView1.MouseClick += new System.Windows.Forms.MouseEventHandler(treeView_MouseUp);
        }

        private void addArtifact_Click(object sender, EventArgs e)
        {
            try
            {
                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(pom);



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
                MessageBox.Show(err.Message, "NMaven Add Dependency Error:");
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
                RemoteArtifactNode node = new RemoteArtifactNode(fsi.Name);
                node.IsFileSystem = true;
                node.ArtifactUrl = Path.Combine(repoFolder, fsi.Name);
                node.IsAssembly = (fsi is FileInfo);
                nodes.Add(node);
            }

            return nodes;
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
                MessageBox.Show("Cannot read remote repository: " + url + " " + ex.Message + ex.StackTrace);
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
                    if (ext == ".dll" || ext == ".exe" || ext == ".netmodule")
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

            foreach (NMaven.Model.Setting.Profile profile in settings.profiles)
            {
                foreach (NMaven.Model.Setting.Repository repository in profile.repositories)
                {
                    if (repository.id.Equals("nmaven.id"))
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
                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(pom);

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

        void addLocalArtifact(LocalArtifactItem item)
        {
            VSProject vsProject = null;

            NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(pom);
            NMaven.Artifact.Artifact artifact = item.Artifact;

            if (project.Object is VSProject)
            {
                vsProject = (VSProject)project.Object;
                if (vsProject.References.Find(item.Text) != null)
                {
                    MessageBox.Show(this, "A version of artifact is already added to the project, please remove it first before adding this version.", this.Text);
                    return;
                }
                vsProject.References.Add(artifact.FileInfo.FullName);
            }
            else
            {
                if (Connect.IsWebProject(project))
                {
                    VsWebSite.VSWebSite website = (VsWebSite.VSWebSite)project.Object;
                    website.References.AddFromFile(artifact.FileInfo.FullName);
                }
                else
                {
                    MessageBox.Show(this, "Cannot add artifact to none VS projects.", this.Text);
                    return;
                }
            }

            try
            {
                pomUtil.AddPomDependency(artifact.GroupId,
                                    artifact.ArtifactId,
                                    artifact.Version);
            }
            catch (Exception err1)
            {
                MessageBox.Show(err1.Message, "NMaven Add Dependency Error:");
                return;
            }

        }

        void addRemoteArtifact(RemoteArtifactNode node)
        {
            NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(pom);

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

            NMaven.Artifact.Artifact artifact =
                artifactContext.GetArtifactRepository().GetArtifactFor(paths);
            
            VSProject vsProject = null;

            if (project.Object is VSProject)
            {
                vsProject = (VSProject)project.Object;
            }
            else
            {
                MessageBox.Show(this, "Cannot add artifact to none VS projects.", this.Text);
                return;
            }

            if (vsProject.References.Find(artifact.ArtifactId) != null)
            {
                MessageBox.Show(this, "A version of artifact is already added to the project, please remove it first before adding this version.", this.Text);
                return;
            }


            try
            {
                pomUtil.AddPomDependency(artifact.GroupId,
                                artifact.ArtifactId,
                                artifact.Version);
            }
            catch (Exception err2)
            {

                MessageBox.Show(err2.Message, "NMaven Add Dependency Error:");
            }

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
                WebClient client = new WebClient();
                byte[] assembly = client.DownloadData(uri);
                FileStream stream = new FileStream(artifact.FileInfo.FullName, FileMode.Create);
                stream.Write(assembly, 0, assembly.Length);
                stream.Close();
            }
            vsProject.References.Add(artifact.FileInfo.FullName);

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

        private NMaven.Artifact.Artifact artifact;

        public NMaven.Artifact.Artifact Artifact
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