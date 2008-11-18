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
    public interface IProjectTypeImporter
    {

        /// <summary>
        /// Generates pom for each project
        /// </summary>
        /// <param name="prjDigests"></param>
        /// <param name="solutionFile"></param>
        /// <param name="groupId"></param>
        /// <param name="artifactId"></param>
        /// <param name="version"></param>
        /// <param name="writePom"></param>
        /// <returns>returns a list of pom files generated</returns>
        string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom);
    }
}
