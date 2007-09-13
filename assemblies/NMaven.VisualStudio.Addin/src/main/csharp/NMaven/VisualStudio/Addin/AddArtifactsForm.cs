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
using NMaven.Model.Pom;
using NMaven.Model.Setting;
using Castle.Windsor;

namespace NMaven.VisualStudio.Addin
{
    public partial class AddArtifactsForm : Form
    {
        /// <summary>
        /// For Testing
        /// </summary>
        public AddArtifactsForm()
        {
            //InitializeForm();
            InitializeComponent();
           // localListView.View = View.Details;
        }

        public AddArtifactsForm(Project project, IWindsorContainer container)
        {
            this.project = project;
            InitializeForm();
            InitializeComponent();
            localListView.View = View.Details;
            artifactContext = (ArtifactContext) container[typeof(ArtifactContext)];         
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
                System.Windows.Forms.ListViewItem item = new System.Windows.Forms.ListViewItem(new string[] {
                    artifact.ArtifactId, artifact.Version}, -1);
                item.Tag = artifact;
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

            String url = null;

            foreach (NMaven.Model.Setting.Profile profile in settings.profiles)
            {
                foreach (NMaven.Model.Setting.Repository repository in profile.repositories)
                {
                    if (repository.id.Equals("nmaven.id"))
                    {
                        url = repository.url;
                    }
                }
            }

            SetUnsafeHttpHeaderParsing();

            List<TreeNode> treeNodes = GetNodesFor(url);
            treeView1.Nodes.AddRange(treeNodes.ToArray());
            treeView1.MouseClick += new System.Windows.Forms.MouseEventHandler(treeView_MouseUp);
        }

        private void addArtifact_Click(object sender, EventArgs e)
        {
            String pomFileName = 
                (new FileInfo(project.FileName).Directory).FullName + @"\pom.xml";
            if (!new FileInfo(pomFileName).Exists)
            {
                MessageBox.Show("Could not add reference. Missing pom file: File = " + pomFileName);
                return;
            }

            XmlReader reader = XmlReader.Create(pomFileName);
            XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Pom.Model));
            if (!serializer.CanDeserialize(reader))
            {
                MessageBox.Show("Could not add reference. Corrupted pom file: File = " + pomFileName);
                return;
            }

            NMaven.Model.Pom.Model model = (NMaven.Model.Pom.Model)serializer.Deserialize(reader);
            List<Dependency> dependencies = new List<Dependency>();
            if (model.dependencies != null)
            {
                dependencies.AddRange(model.dependencies);
            }
 
            ListView.SelectedListViewItemCollection selectedItems = localListView.SelectedItems;
            if (selectedItems != null)
            {
                foreach (ListViewItem item in selectedItems)
                {
                    NMaven.Artifact.Artifact artifact = (NMaven.Artifact.Artifact)item.Tag;
                    Dependency dependency = new Dependency();
                    dependency.artifactId = artifact.ArtifactId;
                    dependency.groupId = artifact.GroupId;
                    dependency.version = artifact.Version;
                    dependency.type = "library";
                    //dependency.scope = artifact.ArtifactScope;

                    dependencies.Add(dependency);
                    VSProject vsProject = (VSProject)project.Object;
                    vsProject.References.Add(artifact.FileInfo.FullName);
                }
            }

            TreeNode treeNode = treeView1.SelectedNode;
            if (treeNode != null)
            {
                String uri = (String)treeNode.Tag;
                int length = uri.Length - uri.LastIndexOf("/maven2/") - 8;
                String paths = uri.Substring(uri.LastIndexOf("/maven2/") + 8, length);
                NMaven.Artifact.Artifact artifact1 = 
                    artifactContext.GetArtifactRepository().GetArtifactFor(paths);
                Dependency dependency1 = new Dependency();
                dependency1.artifactId = artifact1.ArtifactId;
                dependency1.groupId = artifact1.GroupId;
                dependency1.version = artifact1.Version;
                dependency1.type = "library";
                dependencies.Add(dependency1);

                //Download
                artifact1.FileInfo.Directory.Create();
                WebClient client = new WebClient();
                byte[] assembly = client.DownloadData(uri);
                FileStream stream = new FileStream(artifact1.FileInfo.FullName, FileMode.Create);
                stream.Write(assembly, 0, assembly.Length);
                stream.Close();
                
                VSProject vsProject1 = (VSProject)project.Object;
                //File must exist
                vsProject1.References.Add(artifact1.FileInfo.FullName);
            }    
            reader.Close();

            model.dependencies = dependencies.ToArray();
            TextWriter writer = new StreamWriter(pomFileName);
            serializer.Serialize(writer, model);
            writer.Close();
            this.Close();
        }

        private List<NMaven.Artifact.Artifact> localArtifacts = new List<NMaven.Artifact.Artifact>();
        private ArtifactContext artifactContext;
        private Project project;

        private Boolean IsIncluded(String name, String uri)
        {
            if (name.StartsWith(".") || name.Equals("Parent Directory") || name.Equals("Terms of Use"))
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


        private List<TreeNode> GetNodesFor(String url)
        {
            List<TreeNode> treeNodes = new List<TreeNode>();

            WebClient webClient = new WebClient();
            byte[] page = webClient.DownloadData(url);
            String pattern =
                (@"<a[^>]*href\s*=\s*[\""\']?(?<URI>[^""'>\s]*)[\""\']?[^>]*>(?<Name>[^<]+|.*?)?</a\s*>");
            MatchCollection matches = Regex.Matches(Encoding.ASCII.GetString(page), pattern, RegexOptions.IgnoreCase);

            treeView1.ImageList = imageList1;
            foreach (Match match in matches)
            {
                String name = match.Groups["Name"].Value;
                String uri = match.Groups["URI"].Value; 
                if (IsIncluded(name, uri))
                {
                    TreeNode node = new TreeNode(name);
                    if (!IsDirectory(name))
                    {
                        node.ImageIndex = 1;
                    }

                    node.Tag = url + "/" + uri.TrimEnd("/".ToCharArray());
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
                TreeNode node = treeView1.GetNodeAt(point);
                List<TreeNode> treeNodes = GetNodesFor( (String) node.Tag);
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

    }
}