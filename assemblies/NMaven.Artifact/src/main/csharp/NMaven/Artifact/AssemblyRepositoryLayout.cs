using System;
using System.Text;

namespace NMaven.Artifact
{
    public class AssemblyRepositoryLayout : ArtifactRepositoryLayout 
    {
        public string pathOf(Artifact artifact)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(artifact.GroupId.Replace('.', '\\')).Append(@"\");
            sb.Append(artifact.ArtifactId).Append(@"\");
            sb.Append(artifact.Version).Append(@"\");
            sb.Append(artifact.ArtifactId).Append(".").Append(artifact.Extension);
            return sb.ToString();
        }
    }
}
