using System;
using System.Collections.Generic;
using System.Text;

using NMaven.Model;

namespace NMaven.Artifact
{
    public class ArtifactContext
    {

        public Artifact GetArtifactFor(NMaven.Model.Model model)
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

        private string GetExtensionFor(string packaging)
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
