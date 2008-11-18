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
    public class FlatSingleModuleTest : AbstractProjectImportTest
    {

        public override string SolutionFileRelativePath
        {
            get { return @"flat-single-module-cs\FlatSingleModule.sln"; }
        }

        [Test]
        public override void ShouldGenerateTheExpectedNumberOfPoms()
        {
            ProjectImporterAssertions.AssertPomCount(1, GeneratedPomFiles);
        }
		
		public override string TestResourcePath
        {
            get 
            {
                return @"\src\test\resource\flat-single-module-cs\"; 
            }
        }
    }
}
