#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion
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

        public abstract string[] ImportProjectType(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, bool writePom, List<Reference> missingReferences);

        #endregion

        #region Helper Methods

        public string[] GenerateChildPoms(ProjectDigest[] prjDigests, string groupId, string parentPomFilename, NPanday.Model.Pom.Model parentPomModel, bool writePom, string scmTag, List<Reference> missingReferences)
        {
            List<string> generatedPoms = new List<string>();

            // make the child pom
            NPanday.Model.Pom.Model[] models = PomConverter.ConvertProjectsToPomModels(prjDigests, parentPomFilename, parentPomModel, groupId, writePom, scmTag, missingReferences);

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
