using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.IO;

using Microsoft.Build.BuildEngine;

using NPanday.Utils;
using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Parser;
using NPanday.ProjectImporter.Converter;
using NPanday.ProjectImporter.Converter.Algorithms;
using NPanday.ProjectImporter.Validator;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.ImportProjectStructureAlgorithms
{
    public class FlatSingleModuleProject : AbstractProjectAlgorithm
    {
        public override string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, bool writePom)
        {
            return GenerateChildPoms(prjDigests, groupId, null, null, writePom, scmTag);
        }
    }
}
