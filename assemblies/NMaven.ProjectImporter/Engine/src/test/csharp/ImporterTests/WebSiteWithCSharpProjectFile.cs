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
    public class WebSiteWithCSharpProjectFile : AbstractProjectImportTest
    {

        public override string SolutionFileRelativePath
        {
            get { return @"WebSiteWithCSProj\WebApplication1.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(2, GeneratedPomFiles);
        }
		
		public override string TestResourcePath
        {
            get { return @"\src\test\resource\WebSiteWithCSProj\"; }
        }
    }
}
