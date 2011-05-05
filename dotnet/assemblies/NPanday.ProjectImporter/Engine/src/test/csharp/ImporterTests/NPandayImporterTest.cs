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
using NPanday.ProjectImporter;
using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Validator;
using NUnit.Framework;

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class NPandayImporterTest
    {
        private String slnFile;
        private String warnMsg = String.Empty;
        private DirectoryInfo withoutNUnitSource;
        private DirectoryInfo withoutNUnitTarget;
        private DirectoryInfo withNUnitSource;
        private DirectoryInfo withNUnitTarget;

        public NPandayImporterTest()
        {
            withoutNUnitSource = new DirectoryInfo(FileUtil.GetBaseDirectory() + "\\src\\test\\resource\\MavenTestProjectWithoutNUnit");
            withoutNUnitTarget = new DirectoryInfo(FileUtil.GetBaseDirectory() + "\\src\\test\\resource\\MavenTestProjectWithoutNUnitCopy");

            withNUnitSource = new DirectoryInfo(FileUtil.GetBaseDirectory() + "\\src\\test\\resource\\MavenTestProjectWithNUnit");
            withNUnitTarget = new DirectoryInfo(FileUtil.GetBaseDirectory() + "\\src\\test\\resource\\MavenTestProjectWithNUnitCopy");
        }

        [TestFixtureSetUp]
        public void TestSetUp()
        {
            //TODO Remove when TestTearDown is fixed
            //Delete manually
            if (withNUnitTarget.Exists)
            {
                withNUnitTarget.Delete(true);
            }

            FileUtil.CopyDirectory(withoutNUnitSource, withoutNUnitTarget);
            FileUtil.CopyDirectory(withNUnitSource, withNUnitTarget); 
        }

        [TestFixtureTearDown]
        public void TestTearDown()
        {
            Directory.Delete(withoutNUnitTarget.FullName, true);

            //TODO
            //Doesn't work because some process is holding the .references folder
            //Directory.Delete(withNUnitTarget.FullName, true);
        }

        [Test]
        public void TestProjectImporterWithNunitAndUncheckedTestProject()
        { 
            slnFile = withNUnitTarget.FullName + "\\MavenTestProjectWithNUnit.sln";
            string[] generatedPoms = NPandayImporter.ImportProject(slnFile, "test", "test-plugin", "1.0", "", UncheckedProject, ref warnMsg);

            Assert.IsNotNull(generatedPoms);
            Assert.AreEqual(2, generatedPoms.Length);
            Assert.IsFalse(ContainsMavenTestPlugin(generatedPoms[1]));
       }

        [Test]
        public void TestProjectImporterWithNunitAndCheckedTestProject()
        {
            slnFile = withNUnitTarget.FullName + "\\MavenTestProjectWithNUnit.sln"; 
            string[] generatedPoms = NPandayImporter.ImportProject(slnFile, "test", "test-plugin", "1.0", "", CheckedProject, ref warnMsg);

            Assert.IsNotNull(generatedPoms);
            Assert.AreEqual(2, generatedPoms.Length);
            Assert.IsTrue(ContainsMavenTestPlugin(generatedPoms[1]));
        }

        [Test]
        public void TestProjectImporterWithOutNunitAndCheckedTestProject()
        {
            slnFile = withoutNUnitTarget.FullName + "\\MavenTestProjectWithoutNUnit.sln";
            string[] generatedPoms = NPandayImporter.ImportProject(slnFile, "test", "test-plugin", "1.0", "", CheckedProject, ref warnMsg);

            Assert.IsNotNull(generatedPoms);
            Assert.AreEqual(2, generatedPoms.Length);
            Assert.IsTrue(ContainsMavenTestPlugin(generatedPoms[1]));
        }

        [Test]
        public void TestProjectImporterWithOutNunitAndUncheckedTestProject()
        {
            slnFile = withoutNUnitTarget.FullName + "\\MavenTestProjectWithoutNUnit.sln";
            string[] generatedPoms = NPandayImporter.ImportProject(slnFile, "test", "test-plugin", "1.0", "", UncheckedProject, ref warnMsg);

            Assert.IsNotNull(generatedPoms);
            Assert.AreEqual(2, generatedPoms.Length);
            Assert.IsFalse(ContainsMavenTestPlugin(generatedPoms[1]));
        }

        public void UncheckedProject(ref ProjectDigest[] projectDigests, ProjectStructureType structureType, string solutionFile, ref string groupId, ref string artifactId, ref string version)
        {
            foreach (ProjectDigest pDigest in projectDigests)
            {
                pDigest.UnitTest = false;
            }
        }

        public void CheckedProject(ref ProjectDigest[] projectDigests, ProjectStructureType structureType, string solutionFile, ref string groupId, ref string artifactId, ref string version)
        {
            foreach (ProjectDigest pDigest in projectDigests)
            {
                pDigest.UnitTest = true;
            }
        }

        private bool ContainsMavenTestPlugin(String pom)
        {
            bool contains = false;
            String line;
            StreamReader strm = new StreamReader(pom);

            while ((line = strm.ReadLine()) != null)
            {
                if (line.ToString().Contains("maven-test-plugin"))
                {
                    contains = true;
                    break;
                }
            }

            strm.Close();
            return contains;
        }
    }
}
