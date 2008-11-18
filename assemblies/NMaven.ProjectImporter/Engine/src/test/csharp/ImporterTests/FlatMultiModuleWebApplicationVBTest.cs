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
    public class FlatMultiModuleWebApplicationVBTest : AbstractProjectImportTest
    {
        public override string SolutionFileRelativePath
        {
            get { return @"flat-multi-module-web-application-vb\NMaven.WebApplication_WithVBProject.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(3, GeneratedPomFiles);
        }

		public override string TestResourcePath
        {
            get { return @"\src\test\resource\flat-multi-module-web-application-vb\"; }
        }
    }
}
