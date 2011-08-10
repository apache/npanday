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
    public class Net40GacImportTest : AbstractProjectImportTest
    {
        public override void CheckFrameworkVersion()
        {
            if (!GetType().Assembly.ImageRuntimeVersion.StartsWith("v4.0"))
            {
                Assert.Ignore("Test only runs on .NET 4.0, but is: " + this.GetType().Assembly.ImageRuntimeVersion);
            }
        }

        public override string SolutionFileRelativePath
        {
            get { return @"NPANDAY-445-NET40GAC\NPANDAY-445-NET40GAC.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(2, GeneratedPomFiles);
        }

        public override string TestResourcePath
        {
            get { return @"\src\test\resource\NPANDAY-445-NET40GAC\"; }
        }
    }
}
