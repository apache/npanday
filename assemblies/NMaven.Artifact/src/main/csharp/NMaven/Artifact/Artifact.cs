using System;
using System.IO;

namespace NMaven.Artifact
{
    public sealed class Artifact
    {
        private string artifactId;

        private string groupId;

        private string version;

        private FileInfo fileInfo;

        private int artifactScope;

        private string extension;

        private String remotePath;

        public String RemotePath
        {
            get { return remotePath; }
            set { this.remotePath = value; }
        }

        public string Extension
        {
            get { return extension; }
            set { extension = value; }
        }

        public string ArtifactId
        {
            get { return artifactId; }
            set { artifactId = value; }
        }

        public string GroupId
        {
            get { return groupId; }
            set { groupId = value; }
        }

        public string Version
        {
            get { return version; }
            set { version = value; }
        }

        public FileInfo FileInfo
        {
            get { return fileInfo; }
            set { fileInfo = value; }
        }

        public int ArtifactScope
        {
            get { return artifactScope; }
            set { artifactScope = value; }
        }
    }     
}
