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
    public class FlatMultiModuleProject : AbstractProjectAlgorithm
    {
        public override string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, bool writePom)
        {
            List<string> generatedPoms = new List<string>();

            string pomFileName = Path.GetFullPath(Path.GetDirectoryName(solutionFile) + @"\parent-pom.xml");
            // write the parent pom
            NPanday.Model.Pom.Model mainModel = PomConverter.MakeProjectsParentPomModel(prjDigests, pomFileName, groupId, artifactId, version, scmTag, true);
            generatedPoms.Add(pomFileName);


            generatedPoms.AddRange(
                GenerateChildPoms(prjDigests, groupId, pomFileName, mainModel, writePom, scmTag)
            );

            return generatedPoms.ToArray();
        }
    }
}
