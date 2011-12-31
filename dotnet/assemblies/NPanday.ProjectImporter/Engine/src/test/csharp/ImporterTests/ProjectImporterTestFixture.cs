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

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.ImporterTests
{

    [SetUpFixture]
    public class ProjectImporterTestFixture
    {
        #region Properties

        static string sampleProjectsPath = null;
        public static string SampleProjectsPath
        {
            get { return sampleProjectsPath; }
            set { sampleProjectsPath = value; }
        }

        #endregion



        [SetUp]
        public void PrepareProjects()
        {
            string baseProjectPath = FileUtil.GetBaseDirectory();

            ProjectImporterTestFixture.SampleProjectsPath = Path.Combine(baseProjectPath, @"target\test_sample_projects");

            // delete the sample projects from target folder
            FileUtil.DeleteDirectory(SampleProjectsPath);

            // copy sample projects from resource
            FileUtil.CopyDirectory(Path.Combine(baseProjectPath, @"src\test\resource"), SampleProjectsPath);
        }


        [TearDown]
        public void FinalizationProcess()
        {
            // just incase we need to finilize after running some tests
        }

    }
}
