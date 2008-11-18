using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.IO;

using Microsoft.Build.BuildEngine;

using NMaven.Utils;
using NMaven.ProjectImporter.Digest;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.ProjectImporter.Parser;
using NMaven.ProjectImporter.Converter;
using NMaven.ProjectImporter.Converter.Algorithms;
using NMaven.ProjectImporter.Validator;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.ImportProjectStructureAlgorithms
{
    public class FlatSingleModuleProject : AbstractProjectAlgorithm
    {
        public override string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom)
        {
            return GenerateChildPoms(prjDigests, groupId, null, null, writePom);
        }
    }
}
