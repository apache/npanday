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

        [Test]
        public void CheckPomFileHasResources()
        {
            XpathValues.Add("build.resources", "project/build/resources");
            XpathValues.Add("build.resources.resource", "project/build/resources/resource");
            XpathValues.Add("build.resources.resource.includes", "project/build/resources/resource/includes");
            XpathValues.Add("build.resources.resource.includes.include", "project/build/resources/resource/includes/include");
            
            ProjectImporterAssertions.AssertPomElementValues(TestResourcePath, GeneratedPomFiles, XpathValues);
        }


    }
}
