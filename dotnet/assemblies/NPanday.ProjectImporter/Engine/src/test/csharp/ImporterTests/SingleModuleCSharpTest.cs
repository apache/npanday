using System;
using System.Collections.Generic;
using System.Text;


using NUnit.Framework;
using NPanday.ProjectImporter;
using NPanday.ProjectImporter.ImporterTests;

namespace NPanday.ProjectImporter.ImporterTests
{
    [TestFixture]
    public class SingleModuleCSharpTest : AbstractProjectImportTest
    {

        public override string SolutionFileRelativePath
        {
            get { return @"SingleModuleCSharp\SingleModuleCSharp.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(2, GeneratedPomFiles);
        }
		
		
		public override string TestResourcePath
        {
            get { return @"\src\test\resource\SingleModuleCSharp\"; }
        }
    }
}
