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
    /// <summary>
    /// This algorithm class is for genating a single pom.xml project
    /// </summary>
    public class NormalSingleProject : AbstractProjectAlgorithm
    {
        public override string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom)
        {
            List<string> generatedPoms = new List<string>();

            string pomFileName = Path.GetFullPath(Path.GetDirectoryName(solutionFile) + @"\pom.xml");
            // write the parent pom
            NPanday.Model.Pom.Model mainModel = PomConverter.MakeProjectsParentPomModel(prjDigests, pomFileName, groupId, artifactId, version, true);
            generatedPoms.Add(pomFileName);


            generatedPoms.AddRange(
                GenerateChildPoms(prjDigests, groupId, pomFileName, mainModel, writePom)
            );

            return generatedPoms.ToArray();
        }
    }
}
