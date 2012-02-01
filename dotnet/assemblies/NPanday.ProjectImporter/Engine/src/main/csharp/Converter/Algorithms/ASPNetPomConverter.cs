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
using System.IO;
using NPanday.Model.Pom;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;
using System.Collections.Generic;

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class ASPNetPomConverter : NormalPomConverter
    {
        public ASPNetPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            // just call the base, but dont write it we still need some minor adjustments for it
            base.ConvertProjectToPomModel(false,scmTag);

            List<string> goals = new List<string>();
            goals.Add("assemble-package-files");
            goals.Add("process-configs");

            Plugin aspnetPlugin = AddPlugin("org.apache.npanday.plugins", "aspnet-maven-plugin", null, false);
            AddPluginExecution(aspnetPlugin, "prepare-package", goals.ToArray(), null);

            if (!string.IsNullOrEmpty(projectDigest.TargetFramework))
                AddPluginConfiguration(aspnetPlugin, "frameworkVersion", projectDigest.TargetFramework);

            if (!string.IsNullOrEmpty(projectDigest.WebConfig))
            {
                AddPluginConfiguration(aspnetPlugin, "transformationHint", projectDigest.WebConfig);
            }

            Plugin msdeployPlugin = AddPlugin("org.apache.npanday.plugins", "msdeploy-maven-plugin", null, false);
            AddPluginExecution(msdeployPlugin, "create-msdeploy-package", new string[] { "create-package" }, null);

            if (projectDigest.SilverlightApplicationList != null)
            {
                // add silverlight dependencies and copy them into the desired location
                foreach (SilverlightApplicationReference app in projectDigest.SilverlightApplicationList)
                {
                    Dependency dep = CreateInterProjectDependency(app.Project.AssemblyName, app.Project);
                    dep.type = "silverlight-application";
                    dep.scope = "runtime";
                    AddDependency(dep);
                }
            }

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }
        }
    }
}
