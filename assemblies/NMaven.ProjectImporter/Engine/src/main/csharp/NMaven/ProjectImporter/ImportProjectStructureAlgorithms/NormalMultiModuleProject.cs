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
    public class NormalMultiModuleProject : AbstractProjectAlgorithm
    {
        public override string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom)
        {
            List<string> generatedPoms = new List<string>();

            string pomFileName = Path.GetFullPath(Path.GetDirectoryName(solutionFile) + @"\pom.xml");
            // write the parent pom
            NMaven.Model.Pom.Model mainModel = PomConverter.MakeProjectsParentPomModel(prjDigests, pomFileName, groupId, artifactId, version, true);
            generatedPoms.Add(pomFileName);


            generatedPoms.AddRange(
                GenerateChildPoms(prjDigests, groupId, pomFileName, mainModel, writePom)
            );

            return generatedPoms.ToArray();
        }
    }
}
