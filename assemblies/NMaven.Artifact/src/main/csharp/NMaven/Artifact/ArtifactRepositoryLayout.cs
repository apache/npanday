using System;
using System.Collections.Generic;
using System.Text;

namespace NMaven.Artifact
{
    public interface ArtifactRepositoryLayout
    {
        String pathOf(Artifact artifact);
    }
}