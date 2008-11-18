using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


using NUnit.Framework;
using NMaven.ProjectImporter;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class SampleWebProjectsTest : AbstractProjectImportTest
    {

        public override string SolutionFileRelativePath
        {
            get { return @"sample_web_project\SampleWebProjects.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(4, GeneratedPomFiles);
        }

		public override string TestResourcePath
        {
            get { return @"\src\test\resource\sample_web_project\"; }
        }
    }
}
