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
    public abstract class AbstractProjectAlgorithm : IProjectTypeImporter
    {


        #region IProjectTypeImporter Members

        public abstract string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom);

        #endregion

        #region Helper Methods

        public string[] GenerateChildPoms(ProjectDigest[] prjDigests, string groupId, string parentPomFilename, NPanday.Model.Pom.Model parentPomModel, bool writePom)
        {
            List<string> generatedPoms = new List<string>();

            // make the child pom
            NPanday.Model.Pom.Model[] models = PomConverter.ConvertProjectsToPomModels(prjDigests, parentPomFilename, parentPomModel, groupId, writePom);

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
