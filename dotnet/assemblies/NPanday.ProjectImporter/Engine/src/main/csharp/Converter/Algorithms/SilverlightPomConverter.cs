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
using log4net;
using Microsoft.Win32;
using System;

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class SilverlightPomConverter : NormalPomConverter
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(SilverlightPomConverter));

        public SilverlightPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId)
            : base(projectDigest, mainPomFile, parent, groupId)
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

            // do not resolve any dependencies from the GAC for silverlight projects
            projectDigest.DependencySearchConfig.SearchGac = false;

            // Add Project Inter-dependencies
            AddInterProjectDependenciesToList();

            // Add Project Reference Dependencies
            AddProjectReferenceDependenciesToList();

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }
        }

        protected override Dictionary<string, string> GetTargetFrameworkDirectories()
        {
            if (TargetFrameworkDirectories == null)
            {
                Dictionary<string, string> targetFrameworkDirectories = new Dictionary<string, string>();

                try
                {
                    RegistryKey root = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Microsoft SDKs\Silverlight\" + projectDigest.TargetFrameworkVersion);
                    RegistryKey referenceAssemblies = root.OpenSubKey("ReferenceAssemblies");
                    string value = (string)referenceAssemblies.GetValue("SLRuntimeInstallPath");
                    if (value != null)
                        targetFrameworkDirectories.Add("SilverlightFramework" + projectDigest.TargetFramework.Replace(".", ""), value);
                    else
                        log.Warn("Unable to find Silverlight framework in registry");

                    RegistryKey assemblyFolderEx = root.OpenSubKey("AssemblyFoldersEx");
                    GetTargetFrameworkDirectoriesAssemblyFoldersEx(targetFrameworkDirectories, assemblyFolderEx);
                }
                catch (Exception e)
                {
                    log.Error("Unable to find Silverlight framework in the registry due to an exception: " + e.Message);
                }

                log.InfoFormat("Target framework directories: {0}", string.Join(",", new List<string>(targetFrameworkDirectories.Values).ToArray()));
                TargetFrameworkDirectories = targetFrameworkDirectories;
            }
            return TargetFrameworkDirectories;
        }
    }
}
