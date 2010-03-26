using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using NPanday.ProjectImporter;

/// Author: Joe Ocaba

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class MultipleFolderLevelTest : AbstractProjectImportTest
    {
        public override string SolutionFileRelativePath
        {
            get { return @"MultipleFolderLevelTest\MultipleFolderLevelExample.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(3, GeneratedPomFiles);
        }

        public override string TestResourcePath
        {
            get { return @"\src\test\resource\MultipleFolderLevelTest\"; }
        }
    }
}
