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
    [Ignore] // not implemented yet
    [TestFixture]
    public class AzureImportOneWebRoleTest : AbstractProjectImportTest
    {
        public override void CheckFrameworkVersion()
        {
            if (Environment.Version.Major < 4)
            {
                Assert.Ignore("Test only runs on .NET 4.0, but is: " + Environment.Version.ToString());
            }
        }

        public override string SolutionFileRelativePath
        {
            get { return @"NPANDAY_480_AzureSupportOneWebRole\HelloWorld.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(2, GeneratedPomFiles);
        }

        public override string TestResourcePath
        {
            get { return @"src\test\resource\NPANDAY_480_AzureSupportOneWebRole\"; }
        }
    }
}
