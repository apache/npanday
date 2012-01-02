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
    public class AzureWorkerPomConverter : NormalPomConverter
    {
        public AzureWorkerPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId)
            : base(projectDigest, mainPomFile, parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            // just call the base, but dont write it we still need some minor adjustments for it
            base.ConvertProjectToPomModel(false, scmTag);

            List<string> goals = new List<string>();
            goals.Add("assemble-package-files");
            goals.Add("process-app-config");
            goals.Add("package");

            Plugin plugin = AddPlugin("org.apache.npanday.plugins", "application-maven-plugin", null, false);
            AddPluginExecution(plugin, "package-application", goals.ToArray(), null);

            // TODO: until XDT works, just use Web.config itself
            AddPluginConfiguration(plugin, "appConfig", "app.config");

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }
        }
    }
}
