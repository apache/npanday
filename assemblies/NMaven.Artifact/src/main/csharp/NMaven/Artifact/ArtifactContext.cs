using System;
using System.IO;
using System.Collections.Generic;
using System.Text;

using NMaven.Model.Setting;

namespace NMaven.Artifact
{
    public sealed class ArtifactContext : IArtifactContext
    {

        public ArtifactRepository GetArtifactRepository()
        {
            ArtifactRepository artifactRepository = new ArtifactRepository();
            artifactRepository.Init(this, new FileInfo(SettingsUtil.GetLocalRepositoryPath()).Directory);
            return artifactRepository;
        }

        public Artifact GetArtifactFor(NMaven.Model.Pom.Model model)
        {
            Artifact artifact = new Artifact();
            artifact.ArtifactId = model.artifactId;
            artifact.GroupId = model.groupId;
            artifact.Version = model.version;
            artifact.Extension = GetExtensionFor(model.packaging);
            return artifact;
        }

        public Artifact CreateArtifact(String groupId, String artifactId, String version, String packaging)
        {
            Artifact artifact = new Artifact();
            artifact.ArtifactId = artifactId;
            artifact.GroupId = groupId;
            artifact.Version = version;
            artifact.Extension = GetExtensionFor(packaging);
            return artifact;
        }

        public String GetExtensionFor(String packaging)
        {
            if (packaging.Equals("library") || packaging.Equals("netplugin")
                || packaging.Equals("visual-studio-addin") || packaging.Equals("sharp-develop-addin"))
            {
                return "dll";
            }
            else if(packaging.Equals("winexe") ||  packaging.Equals("exe"))
            {
                return "exe";
            }
            else if (packaging.Equals("module"))
            {
                return "netmodule";
            }
            return null;
        }
    }
}
