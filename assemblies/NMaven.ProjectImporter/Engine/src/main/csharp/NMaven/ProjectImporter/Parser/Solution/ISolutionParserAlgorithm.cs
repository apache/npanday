using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Parser
{
    public interface ISolutionParserAlgorithm
    {
        List<Dictionary<string, object>> Parse(FileInfo solutionFile);
    }
}
