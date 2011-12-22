using System.Windows.Forms;

namespace NPanday.VisualStudio.Addin
{
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