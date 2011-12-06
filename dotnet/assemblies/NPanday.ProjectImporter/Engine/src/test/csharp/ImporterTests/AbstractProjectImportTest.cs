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

        [Test]
        [TestFixtureSetUp]
        public void ShouldBeAbleImportProject()
        {
            // would be nicer if this could just be a setup check that ran first - if this method weren't a setup itself
            CheckFrameworkVersion();

			Assert.IsNotNull(ProjectImporterTestFixture.SampleProjectsPath, "ProjectImporterTestFixture.SampleProjectsPath must not be null");
			Assert.IsNotNull(SolutionFileRelativePath, "SolutionFileRelativePath must not be null");

            string solutionFile = Path.Combine(ProjectImporterTestFixture.SampleProjectsPath, SolutionFileRelativePath);
            try
            {
                string warnMsg = string.Empty;
                generatedPomFiles = NPandayImporter.ImportProject(solutionFile, "test.group", "test-parent", "1.2.3-SNAPSHOT", string.Empty, false, ref warnMsg);

            }
            catch (Exception e)
            {
                Assert.Fail(
                    "\n\n*******************************************\n"
                    + string.Format(
                        "There is an error in importing {0}",
                        SolutionFileRelativePath
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
                    "Generated Pom Files of  {0} is null", 
                    SolutionFileRelativePath
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
		
        public void CheckWebMVC()
        {
            // check MVC 2 installed
            string name = "System.Web.MVC, Version=2.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35, processorArchitecture=MSIL";
            Assembly a = Assembly.ReflectionOnlyLoad(new System.Reflection.AssemblyName(name).FullName);
            if (a == null)
            {
                Assert.Ignore("Test only runs with MVC 2 installed");
            }
        }

		[Test]
        public void CheckPomFileElementValues()
        {
            ProjectImporterAssertions.AssertPomElementValues(TestResourcePath, GeneratedPomFiles);
        }

        public virtual void CheckFrameworkVersion()
        {
            // designed to override
        }
    }
}
