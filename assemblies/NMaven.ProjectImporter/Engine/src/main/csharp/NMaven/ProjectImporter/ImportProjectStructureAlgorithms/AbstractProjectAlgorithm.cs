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
    public abstract class AbstractProjectAlgorithm : IProjectTypeImporter
    {


        #region IProjectTypeImporter Members

        public abstract string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom);

        #endregion

        #region Helper Methods

        public string[] GenerateChildPoms(ProjectDigest[] prjDigests, string groupId, string parentPomFilename, NMaven.Model.Pom.Model parentPomModel, bool writePom)
        {
            List<string> generatedPoms = new List<string>();

            // make the child pom
            NMaven.Model.Pom.Model[] models = PomConverter.ConvertProjectsToPomModels(prjDigests, parentPomFilename, parentPomModel, groupId, writePom);

            if (models != null && models.Length > 0)
            {
                foreach (ProjectDigest prj in prjDigests)
                {
                    string fileDir = Path.GetDirectoryName(prj.FullFileName);
                    string pomFile = Path.GetFullPath(fileDir + @"\pom.xml");
                    generatedPoms.Add(pomFile);
                }

            }

            return generatedPoms.ToArray();
        }

        #endregion


    }
}
