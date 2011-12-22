using System.Windows.Forms;

namespace NPanday.VisualStudio.Addin
{
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
}