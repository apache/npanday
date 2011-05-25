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
                this.GeneratePom(solutionFile, groupId, version, scmTag);
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
