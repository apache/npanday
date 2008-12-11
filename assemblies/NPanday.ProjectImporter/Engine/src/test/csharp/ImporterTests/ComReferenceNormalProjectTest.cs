using NUnit.Framework;
using NPanday.ProjectImporter;

/// Author: Joe Ocaba

namespace NPanday.ProjectImporter.ImporterTests
{

    [TestFixture]
    public class ComReferenceNormalProjectTest : AbstractProjectImportTest
    {
        public override string SolutionFileRelativePath
        {
            get { return @"Shell32Example\Shell32Example.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(2, GeneratedPomFiles);
        }
		
		public override string TestResourcePath
        {
            get { return @"\src\test\resource\Shell32Example\"; }
        }
    }
}
