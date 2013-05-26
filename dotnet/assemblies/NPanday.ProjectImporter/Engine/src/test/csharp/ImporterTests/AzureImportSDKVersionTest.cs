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
using NUnit.Framework;

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class AzureImportSDKVersionTest : AbstractAzureImportTest
    {
        public override List<Artifact.Artifact> GetTestArtifacts()
        {
            List<Artifact.Artifact> artifacts = new List<Artifact.Artifact>();
            artifacts.Add(createArtifact("Microsoft.WindowsAzure.Configuration", "1.7.0.0"));
            artifacts.Add(createArtifact("Microsoft.WindowsAzure.Diagnostics", "1.7.0.0"));
            artifacts.Add(createArtifact("Microsoft.WindowsAzure.ServiceRuntime", "1.7.0.0"));
            artifacts.Add(createArtifact("Microsoft.WindowsAzure.StorageClient", "1.7.0.0"));
            return artifacts;
        }

        public override void CheckFrameworkVersion()
        {
            if (Environment.Version.Major < 4)
            {
                Assert.Ignore("Test only runs on .NET 4.0, but is: " + Environment.Version.ToString());
            }
        }

        public override string SolutionFileRelativePath
        {
            get { return @"NPANDAY_571\NPANDAY_571_AzureSDKVersionTest.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(3, GeneratedPomFiles);
        }

        public override bool UseMsDeploy
        {
            get { return true; }
        }

        public override string TestResourcePath
        {
            get { return @"src\test\resource\NPANDAY_571\"; }
        }
    }
}
