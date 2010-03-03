using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using NUnit.Framework;
using NPanday.ProjectImporter;

/// Author: Joe Ocaba

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class EmbeddedResourcesTest : AbstractProjectImportTest
    {

        public override string SolutionFileRelativePath
        {
           get { return @"EmbeddedResourcesTest\EmbeddedResourcesTest.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(2, GeneratedPomFiles);
        }

        public override string TestResourcePath
        {
            get { return @"\src\test\resource\EmbeddedResourcesTest\"; }
        }
    }
}
