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
        public List<Artifact> GetArtifactsFor()
        {
            return (GetArtifactsFromDirectories(localRepository.GetDirectories()));
        }

        public void Init(ArtifactContext artifactContext, DirectoryInfo localRepository)
        {
            this.artifactContext = artifactContext;
            this.localRepository = localRepository;
        }

        private List<Artifact> GetArtifactsFromDirectories(DirectoryInfo[] directories)
        {
            List<Artifact> artifacts = new List<Artifact>();
            foreach (DirectoryInfo directoryInfo in directories)
            {
                foreach(FileInfo fileInfo in directoryInfo.GetFiles())
                {
                    if (fileInfo.Name.EndsWith("pom"))
                    {
                        XmlReader reader = XmlReader.Create(fileInfo.FullName);
		                XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Model));
                        if (serializer.CanDeserialize(reader))
                        {
                            Artifact artifact = artifactContext.GetArtifactFor(
                                (NMaven.Model.Model)serializer.Deserialize(reader));
                            if (artifact.Extension.Equals("dll"))
                            {
                                artifacts.Add(artifact);
                            }                           
                        }
                    }
                }
                artifacts.AddRange(GetArtifactsFromDirectories(directoryInfo.GetDirectories()));
            }
            return artifacts;
        }

        private ArtifactContext artifactContext;
        private DirectoryInfo localRepository;
    }
}
