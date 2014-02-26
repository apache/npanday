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
    public class UnsupportedProjectTest
    {
        private String slnFile;
        private String warnMsg = String.Empty;
        private DirectoryInfo UnsupportedProjectSource;
        private DirectoryInfo UnsupportedProjectTarget;

        public UnsupportedProjectTest()
        {
            UnsupportedProjectSource = new DirectoryInfo(FileUtil.GetBaseDirectory() + "\\src\\test\\resource\\UnsupportedProjectAboveSolution");
            UnsupportedProjectTarget = new DirectoryInfo(FileUtil.GetBaseDirectory() + "\\src\\test\\resource\\UnsupportedProjectAboveSolutionCopy");
        }

        [TestFixtureSetUp]
        public void TestSetUp()
        {
            FileUtil.CopyDirectory(UnsupportedProjectSource, UnsupportedProjectTarget);
        }

        [TestFixtureTearDown]
        public void TestTearDown()
        {
            Directory.Delete(UnsupportedProjectTarget.FullName, true);
        }

        [Test]
        public void TestProjectImporterWithNunitAndCheckedTestProject()
        {
            string[] generatedPoms = null;
            try
            {
                slnFile = UnsupportedProjectTarget.FullName + "\\SampleApp\\SampleApp.sln";
                generatedPoms = NPandayImporter.ImportProject(slnFile, "test", "test-plugin", "1.0", "", UncheckedProject, ref warnMsg);
            }
            catch
            {
                Assert.IsNull(generatedPoms);
            }
        }

        public void UncheckedProject(ref ProjectDigest[] projectDigests, ProjectStructureType structureType, string solutionFile, ref string groupId, ref string artifactId, ref string version)
        {
            foreach (ProjectDigest pDigest in projectDigests)
            {
                pDigest.UnitTest = false;
            }
        }
    }
}
