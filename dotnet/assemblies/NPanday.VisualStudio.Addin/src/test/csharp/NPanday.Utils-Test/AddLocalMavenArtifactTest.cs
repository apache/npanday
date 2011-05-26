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
using System.IO;
using NUnit.Framework;
using NPanday.VisualStudio.Addin;
using NPanday.Artifact;
using EnvDTE;
using EnvDTE80;
using NPanday.Logging;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class AddLocalMavenArtifactTest
    {
        private DirectoryInfo testProject;
        private DirectoryInfo testProjectCopy;
        private DirectoryInfo repo;
        private DirectoryInfo repoCopy;
        private ReferenceManager refManager;
        private Artifact testArtifact;

        public AddLocalMavenArtifactTest()
        {
            testProject = new DirectoryInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\TestProject");
            testProjectCopy = new DirectoryInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\TestProjectCopy");

            repo = new DirectoryInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\m2");
            repoCopy = new DirectoryInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\m2Copy");
        }

        [TestFixtureSetUp]
        public void TestSetUp()
        {
            FileUtils.CopyDirectory(testProject, testProjectCopy);
            FileUtils.CopyDirectory(repo, repoCopy);

            refManager = new ReferenceManager();
            refManager.ReferenceFolder = testProjectCopy.FullName + "\\TestProject\\.references";

            testArtifact = new Artifact();
            testArtifact.GroupId = "npanday.test";
            testArtifact.ArtifactId = "NPanday.Test";
            testArtifact.Version = "1.0";
            testArtifact.Extension = "dll";
        }

        [Test]
        public void addMavenArtifact()
        {
            testArtifact.FileInfo = new FileInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\m2Copy\\ClassLibrary1.dll");
            refManager.CopyArtifact(testArtifact, null);
            Assert.IsTrue(new FileInfo(refManager.ReferenceFolder + "\\npanday.test\\NPanday.Test-1.0\\NPanday.Test.dll").Exists);
        }

        [Test]
        public void addExistingMavenArtifact()
        {
            testArtifact.FileInfo = new FileInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\m2Copy\\ClassLibrary1.dll");
            refManager.CopyArtifact(testArtifact, null);
            FileInfo copiedArtifact = new FileInfo(refManager.ReferenceFolder + "\\npanday.test\\NPanday.Test-1.0\\NPanday.Test.dll");

            Assert.IsTrue(copiedArtifact.Exists);
            Assert.AreEqual(testArtifact.FileInfo.Length, copiedArtifact.Length);

            testArtifact.FileInfo = new FileInfo(FileUtils.getBaseDirectory() + "\\src\\test\\resource\\m2Copy\\ClassLibrary2.dll");

            Assert.IsFalse(copiedArtifact.Length == testArtifact.FileInfo.Length);
            //so that new artifact will have a newer timestamp
            File.SetLastWriteTime(testArtifact.FileInfo.FullName, copiedArtifact.LastWriteTime.AddMinutes(1));

            refManager.CopyArtifact(testArtifact, null);
            FileInfo copiedArtifact2 = new FileInfo(refManager.ReferenceFolder + "\\npanday.test\\NPanday.Test-1.0\\NPanday.Test.dll");

            Assert.IsTrue(copiedArtifact2.Exists);
            Assert.AreEqual(testArtifact.FileInfo.Length, copiedArtifact2.Length);
        }

        [TestFixtureTearDown]
        public void TestTearDown()
        {
            Directory.Delete(testProjectCopy.FullName, true);
            Directory.Delete(repoCopy.FullName, true);
        }
    }
}