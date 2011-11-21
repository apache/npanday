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
using System.IO;

using Microsoft.Build.BuildEngine;
using NPanday.Utils;
using NPanday.ProjectImporter.Digest.Model;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Validator
{
    public class ProjectValidator
    {

        static bool IsProjectAbnormal(ProjectDigest[] projectDigests)
        {
            List<string> dirs = new List<string>();

            foreach (ProjectDigest prjDigest in projectDigests)
            {

                foreach (string dir in dirs)
                {
                    if(IsSameDirectory(prjDigest.FullDirectoryName, dir))
                    {
                        // this indecates that 2 or more projects has the same folder
                        return true;
                    }
                }


                dirs.Add(prjDigest.FullDirectoryName);
            }

            return false;

        }

        




        public static ProjectStructureType GetProjectStructureType(string solutionFile, ProjectDigest[] projectDigests)
        {
            if (solutionFile == null)
            {
                throw new NullReferenceException("Solution file must not be null!");
            }

            if (projectDigests == null)
            {
                throw new NullReferenceException("Project Digests Must not be null!");
            }

            string solutonDir = Path.GetDirectoryName(Path.GetFullPath(solutionFile));

            if (IsProjectAbnormal(projectDigests))
            {
                return ProjectStructureType.AbnormalProject;
            }
            else if (projectDigests.Length == 1)
            {

                if (IsSameDirectory(solutonDir, projectDigests[0].FullDirectoryName))
                {
                    // if the project path is the same as the solution path
                    // then it is a flat single module project
                    return ProjectStructureType.FlatSingleModuleProject;
                }

                // else its just a normal project
                return ProjectStructureType.NormalSingleProject;
            }
            else if (projectDigests.Length > 1)
            {
                foreach (ProjectDigest prjDigest in projectDigests)
                {
                    if (IsSameDirectory(prjDigest.FullDirectoryName, solutonDir))
                    {
                        // project has same directory as the solution file
                        return ProjectStructureType.FlatMultiModuleProject;
                    }
                }


                return ProjectStructureType.NormalMultiModuleProject;
            }
            else
            {
                // solutin must have atleast 1 project
                return ProjectStructureType.AbnormalProject;
            }
        }




        static bool IsSameDirectory(string dir1, string dir2)
        {
            string str1 = NPanday.Utils.PomHelperUtility.NormalizeFileToWindowsStyle(Path.GetFullPath(dir1));
            string str2 = NPanday.Utils.PomHelperUtility.NormalizeFileToWindowsStyle(Path.GetFullPath(dir2));
            return str1.Equals(str2, StringComparison.OrdinalIgnoreCase);
        }
    }
}
