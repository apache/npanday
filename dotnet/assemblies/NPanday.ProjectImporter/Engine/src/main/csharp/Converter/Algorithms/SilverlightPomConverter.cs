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
    public class SilverlightPomConverter : NormalPomConverter
    {
        public SilverlightPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            string packaging = projectDigest.SilverlightApplication ? "silverlight-application" : "silverlight-library";
            GenerateHeader(packaging);

            Model.build.sourceDirectory = GetSourceDir();

            //Add SCM Tag
            if (scmTag != null && scmTag != string.Empty && Model.parent == null)
            {
                Scm scmHolder = new Scm();
                scmHolder.connection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.developerConnection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.url = scmTag;

                Model.scm = scmHolder;
            }

            // don't configure framework version for MSBuild since it's the Silverlight version, not the .NET Framework and not needed

            // add for types only
            AddPlugin("org.apache.npanday.plugins", "maven-compile-plugin", null, true);

            // add integration test plugin if project is a test
            if (projectDigest.UnitTest)
            {
                Plugin testPlugin = AddPlugin(
                    "org.apache.npanday.plugins",
                    "maven-test-plugin",
                    null,
                    false
                );
                AddPluginConfiguration(testPlugin, "integrationTest", "true");

                // for running .net framework 4.0 unit tests add new parameter in order to tell NUnit which runtime to use. If there is a way to get this 
                // parameter from maven-compile-plugin use it
                if (projectDigest.TargetFramework == "4.0")
                {
                    AddPluginConfiguration(testPlugin, "executionFrameworkVersion", "4.0");
                }
            }

            // Add Com Reference Dependencies
            if (projectDigest.ComReferenceList.Length > 0)
            {
                AddComReferenceDependency();
            }

            //Add Project WebReferences
            AddWebReferences();

            //Add EmbeddedResources maven-resgen-plugin
            AddEmbeddedResources();


            // Add Project Inter-dependencies
            AddInterProjectDependenciesToList();

            // filter the rsp and SDK included assemblies
            //  This includes just a subset of the provided libraries that are automatically available to a Silverlight application.
            //  Setting them to 'provided' scope may be more accurate, if NPanday core were to support it. The motivation is to avoid
            //  Adding incorrect dependencies referring to the GAC which are likely to be inconsistent across generations (including
            //  making the unit tests for this class fail).
            List<string> sdkReferences = new List<string>();
            sdkReferences.Add("System.Windows");
            sdkReferences.Add("System.Windows.Browser");
            sdkReferences.Add("System.Net");
            FilterSdkReferences(sdkReferences);

            // Add Project Reference Dependencies
            AddProjectReferenceDependenciesToList();

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }
        }
    }
}
