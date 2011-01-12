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
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


using NUnit.Framework;
using NPanday.ProjectImporter;

namespace NPanday.ProjectImporter.ImporterTests
{
    public abstract class AbstractProjectImportTest
    {
        string[] generatedPomFiles = null;

		public AbstractProjectImportTest()
        {
            XpathValues.Add("modelVersion", "project/modelVersion");
            XpathValues.Add("ArtifactId", "project/groupId");
            XpathValues.Add("GroupId", "project/artifactId");
            XpathValues.Add("packaging", "project/packaging");
            XpathValues.Add("name", "project/packaging");
            XpathValues.Add("version", "project/version");
            XpathValues.Add("parent", "project/parent");
            XpathValues.Add("parent.artifactId", "project/parent/artifactId");
            XpathValues.Add("parent.groupId", "project/parent/groupId");
            XpathValues.Add("parent.version", "project/parent/version");
            XpathValues.Add("parent.relativePath", "project/parent/relativePath");
            XpathValues.Add("modules", "project/modules");
            XpathValues.Add("modules.module", "project/modules/module");
            XpathValues.Add("build", "/project/build");
            XpathValues.Add("build.sourceDirectory", "/project/build/sourceDirectory");
            XpathValues.Add("build.plugins", "/project/build/plugins");
            XpathValues.Add("build.plugins.plugin", "/project/build/plugins/plugin");
            XpathValues.Add("build.plugins.plugin.groupId", "/project/build/plugins/plugin/groupId");
            XpathValues.Add("build.plugins.plugin.artifactId", "/project/build/plugins/plugin/artifactId");
            XpathValues.Add("build.plugins.plugin.extensions", "/project/build/plugins/plugin/extensions");
            XpathValues.Add("build.plugins.plugin.configuration", "/project/build/plugins/plugin/configuration");
            XpathValues.Add("build.plugins.plugin.configuration/rootNamespace", "/project/build/plugins/plugin/congiguration/rootNamespace");
            XpathValues.Add("build.plugins.plugin.configuration/includeSources", "/project/build/plugins/plugin/congiguration/includeSources");
            XpathValues.Add("build.plugins.plugin.configuration/includeSources/includeSource", "/project/build/plugins/plugin/congiguration/includeSources/includeSource");
            XpathValues.Add("build.plugins.plugin.configuration/define", "/project/build/plugins/plugin/congiguration/define");
            XpathValues.Add("build.plugins.plugin.configuration/language", "/project/build/plugins/plugin/congiguration/language");
            XpathValues.Add("build.plugins.plugin.configuration/doc", "/project/build/plugins/plugin/congiguration/doc");
            XpathValues.Add("build.plugins.plugin.configuration/imports", "/project/build/plugins/plugin/congiguration/imports");
            XpathValues.Add("build.plugins.plugin.configuration/imports/import", "/project/build/plugins/plugin/congiguration/imports/import");
            XpathValues.Add("dependencies", "/project/dependencies");
            XpathValues.Add("dependencies.dependency", "/project/dependencies/dependency");
            XpathValues.Add("dependencies.dependency.groupId", "/project/dependencies/dependency/groupId");
            XpathValues.Add("dependencies.dependency.artifactId", "/project/dependencies/dependency/artifactId");
            XpathValues.Add("dependencies.dependency.version", "/project/dependencies/dependency/version");
            XpathValues.Add("dependencies.dependency.type", "/project/dependencies/dependency/type");
            
        }
        public string[] GeneratedPomFiles
        {
            get { return generatedPomFiles; }
        }


        public abstract string SolutionFileRelativePath
        {
            get;
        }

		public abstract string TestResourcePath
        {
            get;
        }

        public Dictionary<string, string> XpathValues = new Dictionary<string,string>();
        
		string groupId;
        public string GroupId
        {
            get{return groupId;}
        }

        string artifactId;
        public string ArtifactId
        {
            get { return artifactId; }
        }

        [Test]
        [TestFixtureSetUp]
        public void ShouldBeAbleImportProject()
        {
            groupId = "test.group." + Guid.NewGuid().ToString();
            artifactId = "test.artfact." + Guid.NewGuid().ToString();
			Assert.IsNotNull(ProjectImporterTestFixture.SampleProjectsPath, "ProjectImporterTestFixture.SampleProjectsPath must not be null");
			Assert.IsNotNull(SolutionFileRelativePath, "SolutionFileRelativePath must not be null");

            string solutionFile = Path.Combine(ProjectImporterTestFixture.SampleProjectsPath, SolutionFileRelativePath);
            try
            {
                string warnMsg = string.Empty;
                generatedPomFiles = NPandayImporter.ImportProject(solutionFile, GroupId, ArtifactId, "1.2.3-SNAPSHOT", string.Empty, false, ref warnMsg);

            }
            catch (Exception e)
            {
                Assert.Fail(
                    "\n\n*******************************************\n"
                    + string.Format(
                        "There is an error in importing {0}, with GroupId: {1}, ArtifactId {2}",
                        SolutionFileRelativePath, GroupId, ArtifactId
                        )

                    + "\n\n*******************************************\n"
                    + e
                    );
            }
        }


        /*[Test]
        public void ShouldBeAbleToReImportProject()
        {
            try
            {
                string solutionFile = Path.Combine(ProjectImporterTestFixture.SampleProjectsPath, SolutionFileRelativePath);

                string warnMsg = string.Empty;
                generatedPomFiles = NPandayImporter.ReImportProject(solutionFile,ref warnMsg);

            }
            catch (Exception e)
            {
                Assert.Fail(
                    "\n\n*******************************************\n"
                    + string.Format(
                        "Their is an error in re-importing {0}",
                        SolutionFileRelativePath
                        )

                    + "\n\n*******************************************\n"
                    + e
                    );
            }
        }*/




        public abstract void ShouldGenerateTheExpectedNumberOfPoms();



        [Test]
        public void GeneratedShouldNotBeNull()
        {
            Assert.IsNotNull(GeneratedPomFiles,

                string.Format(
                    "Generated Pom Files of  {0}, with GroupId: {1}, ArtifactId {2} is null", 
                    SolutionFileRelativePath, GroupId, ArtifactId
                )
                
                );
        }
        [Test]
        public void GeneratedPomFilesShouldExists()
        {
            ProjectImporterAssertions.AssertPomFilesExists(GeneratedPomFiles);
        }

        [Test]
        public void ThereShouldBeNoOverlappingPomFiles()
        {
            ProjectImporterAssertions.AssertHasNoOverlappingPomFiles(GeneratedPomFiles);
        }
		
		[Test]
        public void CheckPomFileElementValues()
        {
            ProjectImporterAssertions.AssertPomElementValues(TestResourcePath, GeneratedPomFiles, XpathValues);
        }

    }
}
