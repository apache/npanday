using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace NMaven.Artifact
{
    public class PathUtil
    {
        public static FileInfo GetPrivateApplicationBaseFileFor(Artifact artifact, DirectoryInfo localRepository)
        {
            return new FileInfo(localRepository.Parent.FullName + @"\pab\gac_msil\" + artifact.ArtifactId + @"\" + artifact.Version + "__" +
                artifact.GroupId + @"\" + artifact.ArtifactId + "." + artifact.Extension);
        }

        public static FileInfo GetUserAssemblyCacheFileFor(Artifact artifact, DirectoryInfo localRepository)
        {
            return new FileInfo(localRepository.Parent.FullName + @"\uac\gac_msil\" + artifact.ArtifactId + @"\" + artifact.Version + "__" +
                artifact.GroupId + @"\" + artifact.ArtifactId + "." + artifact.Extension);
        }
    }
}
