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
