using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class SampleVBDependencyTest : AbstractProjectImportTest
    {


        public override string SolutionFileRelativePath
        {
            get { return @"SampleVBDependency\SampleProjectDependecy.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(3, GeneratedPomFiles);
        }

        public override string TestResourcePath
        {
            get { return @"\src\test\resource\SampleVBDependency\"; }
        }
    }
}
