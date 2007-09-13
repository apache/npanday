using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

namespace NMaven.Artifact
{
    public sealed class ArtifactRepository
    {
        public Artifact GetArtifactFor(String uri)
        {
            DirectoryInfo uac = new DirectoryInfo(localRepository.FullName + @"\uac\gac_msil\");

            String[] tokens = uri.Split("/".ToCharArray());
            int size = tokens.Length;
            if (size < 3)
            {
                throw new IOException("Invalid Artifact Path = " + uri);
            }
            Artifact artifact = new Artifact();
            artifact.ArtifactId = tokens[size - 3];
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < size - 3; i++)
            {
                buffer.Append(tokens[i]);
                if (i != size - 4)
                {
                    buffer.Append(".");
                }
            }
            artifact.GroupId = buffer.ToString();
            artifact.Version = tokens[size - 2];
            artifact.Extension = tokens[size - 1].Split(".".ToCharArray())[1];
            artifact.FileInfo = new FileInfo(uac.FullName + artifact.ArtifactId + @"\" +
                artifact.Version + "__" + artifact.GroupId + @"\" + artifact.ArtifactId + ".dll" );
            return artifact;
        }

        public List<Artifact> GetArtifacts()
        {
            List<Artifact> artifacts = new List<Artifact>();
            DirectoryInfo uac = new DirectoryInfo(localRepository.FullName + @"\uac\gac_msil\");
            int directoryStartPosition = uac.FullName.Length;

            List<FileInfo> fileInfos = GetArtifactsFromDirectory(uac);

            foreach (FileInfo fileInfo in fileInfos)
            {
                Artifact artifact = new Artifact();
                String relativePath = fileInfo.FullName.Substring(directoryStartPosition,
                    (fileInfo.FullName.Length - directoryStartPosition));
                String[] tokens = relativePath.Split(new char[1] { '\\' });
                artifact.ArtifactId = tokens[0];
                String[] tokens2 = tokens[1].Split(new char[2]{'_', '_'});
                artifact.Version = tokens2[0];
                artifact.GroupId = tokens2[2];
                artifact.FileInfo = fileInfo;
                artifacts.Add(artifact);
            }
            return artifacts;
        }

        public void Init(ArtifactContext artifactContext, DirectoryInfo localRepository)
        {
            this.artifactContext = artifactContext;
            this.localRepository = localRepository;
        }

        private List<FileInfo> GetArtifactsFromDirectory(DirectoryInfo baseDirectoryInfo)
        {
            DirectoryInfo[] directories = baseDirectoryInfo.GetDirectories();
            List<FileInfo> fileInfos = new List<FileInfo>();
            foreach (DirectoryInfo directoryInfo in directories)
            {
                foreach (FileInfo fileInfo in directoryInfo.GetFiles())
                {
                    fileInfos.Add(fileInfo);                 
                }
                fileInfos.AddRange(GetArtifactsFromDirectory(directoryInfo));
            }
            return fileInfos;
        }

        private ArtifactContext artifactContext;
        private DirectoryInfo localRepository;
    }
}
