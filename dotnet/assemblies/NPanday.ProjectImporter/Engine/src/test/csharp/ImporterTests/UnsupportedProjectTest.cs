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
        [TestFixtureSetUp]
        public void TestSetUp()
        {
            NPanday.ProjectImporter.Converter.Algorithms.AbstractPomConverter.UseTestingArtifacts(new List<Artifact.Artifact>());
        }

        [Test]
        public void TestProjectImporterWithNunitAndCheckedTestProject()
        {
            string[] generatedPoms = null;
            try
            {
                string slnFile = Path.Combine(ProjectImporterTestFixture.SampleProjectsPath, "UnsupportedProjectAboveSolution\\SampleApp\\SampleApp.sln");
                string warnMsg = string.Empty;
                generatedPoms = NPandayImporter.ImportProject(slnFile, "test", "test-plugin", "1.0", "", false, ref warnMsg);
            }
            catch
            {
                Assert.IsNull(generatedPoms);
            }
        }
    }
}
