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
using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using NPanday.Utils;
using NPanday.VisualStudio.Addin;
using System.IO;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class ProjectImporterValidationTest
    {


        private class NPandayImportProjectFormTest : NPandayImportProjectForm
        {
            public void GeneratePomTest(String solutionFile, String groupId, String version, String scmTag)
            {
                this.GeneratePom(solutionFile, groupId, version, scmTag, false);
            }
        }

        private NPandayImportProjectFormTest importerTest;
        private String solutionSample;

        [SetUp]
        public void ProjectImporterValidationTestSetup()
        {
            importerTest = new NPandayImportProjectFormTest();
            solutionSample = new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1.sln").FullName;
        }
        
        [Test]
        public void ImporterInvalidGroupIdTest()
        {
            try
            {
                importerTest.GeneratePomTest(solutionSample, "", "1.0-SNAPSHOT", "");
            }
            catch (Exception e)
            {
                Assert.AreEqual("Group Id is empty.", e.Message);
            }
        }

        [Test]
        public void ImporterEmptyVersionTest()
        {
            try
            {
                importerTest.GeneratePomTest(solutionSample, "npanday", "", "");
            }
            catch (Exception e)
            {
                Assert.AreEqual("\r\nVersion is empty.", e.Message);
            }
        }
        
        [Test]
        public void ImporterInvalidVersionTest()
        {
            try
            {
                importerTest.GeneratePomTest(solutionSample, "npanday", "123--.", "");
            }
            catch (Exception e)
            {
                Assert.AreEqual("\r\nVersion should be in the form major.minor.build.revision-SNAPSHOT", e.Message);
            }
        }

        [Test]
        public void ImporterInvalidSolutionFileTest()
        {
            try
            {
                importerTest.GeneratePomTest("", "npanday", "1.0-SNAPSHOT", "");
            }
            catch (Exception e)
            {
                Assert.AreEqual("Solution File Not Found:  \r\n", e.Message);
            }
        } 
    }
}
