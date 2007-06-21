using System;
using System.Collections.Generic;
using System.Text;

namespace NMaven.Artifact
{
    interface IArtifactContext
    {
        ArtifactRepository GetArtifactRepository();

        Artifact GetArtifactFor(NMaven.Model.Model model);

        Artifact CreateArtifact(String groupId, String artifactId, String version, String packaging);
    }
}
