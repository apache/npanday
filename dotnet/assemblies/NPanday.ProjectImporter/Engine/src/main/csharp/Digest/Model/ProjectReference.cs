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

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{

    public class ProjectReference : IncludeBase
    {
        public ProjectReference(string projectBasePath) 
            : base(projectBasePath)
        {
        }

        private string name;
        public string Name
        {
            get { return name; }
            set { name = value; }
        }

        private string projectPath;
        public string ProjectPath
        {
            get { return projectPath; }
            set { projectPath = value; }
        }

        private string roleType;
        public string RoleType
        {
            get { return roleType; }
            set { roleType = value; }
        }

        public string ProjectFullPath
        {
            get
            {
                if (Path.IsPathRooted(projectPath))
                {
                    return Path.GetFullPath(projectPath);
                }
                else
                {
                    return Path.GetFullPath(projectBasePath + @"\" + projectPath);
                }

            }
        }

        private ProjectDigest projectReferenceDigest;
        public ProjectDigest ProjectReferenceDigest
        {
            get { return projectReferenceDigest; }
            set { projectReferenceDigest = value; }
        }
    }

}
