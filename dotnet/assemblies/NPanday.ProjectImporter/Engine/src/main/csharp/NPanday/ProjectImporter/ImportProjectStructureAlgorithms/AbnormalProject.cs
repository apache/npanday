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
    public class AbnormalProject : AbstractProjectAlgorithm
    {

        public override string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, bool writePom)
        {
            if (prjDigests.Length.Equals(0))
            {
                throw new Exception("Sorry, but there are no Supported Projects Found");
            }
            else
            {
                throw new Exception("The Project Structure is malformed or abnormal!, Project Importer Could not support this project Structure.");
            }

            
        }
    }
}
