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
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Validator;
using System.IO;
using NPanday.Model.Pom;
using System.Xml;
using NPanday.Utils;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Verifiers
{
    public class VerifyProjectImportSyncronization
    {
        public static void SyncronizePomValues(ref ProjectDigest[] projectDigests, ProjectStructureType structureType, string solutionFile, ref string groupId, ref string artifactId, ref string version)
        {
            // sync parent values
            if (structureType != ProjectStructureType.FlatSingleModuleProject)
            {
                SyncParentValues(structureType, solutionFile, ref groupId, ref artifactId, ref version);
            }
            else
            {
                // get the group id from the solution file
                if (projectDigests[0].ExistingPom != null)
                {
                    groupId = projectDigests[0].ExistingPom.groupId;
                    version = projectDigests[0].ExistingPom.version;
                }
                else
                {
                    throw new Exception("Project Must be Imported atleast once before Re-Importing!");
                }
            }


            // syncronize each project to existing poms
            for (int i=0; i<projectDigests.Length; i++)
            {
                SyncProjectValues(ref projectDigests[i]);
            }


            // show test verification if there is a new project added
            if (IsProjectHavingANewImportedProject(projectDigests))
            {
                VerifyUnitTestsForm verifyForm = new VerifyUnitTestsForm(projectDigests);
                verifyForm.ShowDialog();
            }
        }

        private static void SyncProjectValues(ref ProjectDigest projectDigest)
        {
            FileInfo pomFile = new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml"));

            if (projectDigest.ExistingPom == null || !pomFile.Exists)
            {
                return;
            }

            NPanday.Model.Pom.Model model = NPanday.Utils.PomHelperUtility.ReadPomAsModel(pomFile);

            projectDigest.UnitTest = IsProjectAnIntegrationTest(model);

        }


        static void SyncParentValues(ProjectStructureType structureType, string solutionFile, ref string groupId, ref string artifactId, ref string version)
        {
            string parentPomFileName = Path.GetFullPath(Path.Combine(Path.GetDirectoryName(solutionFile), "pom.xml"));
            if (structureType == ProjectStructureType.FlatMultiModuleProject)
            {
                parentPomFileName = Path.GetFullPath(Path.Combine(Path.GetDirectoryName(solutionFile), @"parent-pom.xml"));
            }

            FileInfo parentPomFile = new FileInfo(parentPomFileName);

            if (!parentPomFile.Exists)
            {
                throw new Exception("Project Must be Imported Atleast once before Re-Importing!");
            }

            NPanday.Model.Pom.Model model = NPanday.Utils.PomHelperUtility.ReadPomAsModel(parentPomFile);

            groupId = model.groupId;
            artifactId = model.artifactId;
            version = model.version;


        }


        static bool IsProjectHavingANewImportedProject(ProjectDigest[] projectDigests)
        {
            foreach (ProjectDigest projectDigest in projectDigests)
            {
                if (projectDigest == null)
                {
                    return true;
                }
                
            }

            return false;

        }

        static bool IsProjectAnIntegrationTest(NPanday.Model.Pom.Model model)
        {
            Plugin plugin = PomHelperUtility.FindPlugin(
                     model, 
                     "org.apache.npanday.plugins",
                     "maven-test-plugin"
                    );

            if (plugin == null)
            {
                return false;
            }

            foreach (XmlElement elem in (ICollection<XmlElement>)plugin.configuration.Any)
	        {
                if ("integrationTest".Equals(elem.Name))
                {
                    if(string.IsNullOrEmpty(elem.InnerText))
                    {
                        return false;
                    }

                    return "true".Equals(elem.InnerText.Trim(), StringComparison.OrdinalIgnoreCase);
                }
	        } 



            return false;

        }
    }
}
