using System;
using System.Collections.Generic;
using System.Text;

using NPanday.ProjectImporter.Digest.Model;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Algorithms
{
    public interface IProjectDigestAlgorithm
    {
        ProjectDigest DigestProject(Dictionary<string, object> projectMap);
    }
}
