using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


using NUnit.Framework;
using NPanday.ProjectImporter;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class FlatMultiModuleProjectTest : AbstractProjectImportTest
    {

        public override string SolutionFileRelativePath
        {
            get { return @"flat-multi-module\flatVB_proj.sln"; }
        }



        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(3, GeneratedPomFiles);
        }

        public override string TestResourcePath
        {
            get { return @"\src\test\resource\flat-multi-module\"; }
        }
    }
}
