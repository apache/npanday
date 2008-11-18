using System;
using System.Collections.Generic;
using System.Text;

using NMaven.ProjectImporter.Digest.Model;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Digest.Algorithms
{
    public interface IProjectDigestAlgorithm
    {
        ProjectDigest DigestProject(Dictionary<string, object> projectMap);
    }
}
