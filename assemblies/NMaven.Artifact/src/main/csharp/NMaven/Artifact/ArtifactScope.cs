using System;
using System.Collections.Generic;
using System.Text;

namespace NMaven.Artifact
{
    public enum ArtifactScope
    {
        Runtime = 1,
        Compile = 2,
        Test = 3,
        Provided = 4,
        System = 5
    }
}
