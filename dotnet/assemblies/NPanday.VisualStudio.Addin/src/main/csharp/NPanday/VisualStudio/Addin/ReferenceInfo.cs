namespace NPanday.VisualStudio.Addin
{
    public class ReferenceInfo : IReferenceInfo
    {
        public ReferenceInfo(){}
        public ReferenceInfo(Artifact.Artifact artifact)
        {
            path = artifact.FileInfo.FullName;
            fileName = artifact.FileInfo.Name;
            version = artifact.Version;
            this.artifact = artifact;
        }

        #region IReferenceInfo Members

        string path;
        public string Path
        {
            get
            {
                return path;
            }
            set
            {
                path = value;
            }
        }

        string fileName;
        public string FileName
        {
            get
            {
                return fileName;
            }
            set
            {
                fileName = value;
            }
        }

        string version;
        public string Version
        {
            get
            {
                return version;
            }
            set
            {
                version = value;
            }
        }

        Artifact.Artifact artifact;
        public NPanday.Artifact.Artifact Artifact
        {
            get
            {
                return artifact;
            }
            set
            {
                artifact = value;
            }
        }

        #endregion

    }
}