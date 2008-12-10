using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using NUnit.Framework;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.ImporterTests
{

    [SetUpFixture]
    public class ProjectImporterTestFixture
    {
        #region Properties

        static string sampleProjectsPath = null;
        public static string SampleProjectsPath
        {
            get { return sampleProjectsPath; }
            set { sampleProjectsPath = value; }
        }

        #endregion



        [SetUp]
        public void PrepareProjects()
        {
            
            string baseProjectPath = Path.GetFullPath(Directory.GetCurrentDirectory() + @"\..\..");
            ProjectImporterTestFixture.SampleProjectsPath = Path.Combine(baseProjectPath, @"target\test_sample_projects");

            // delete the sample projects from target folder
            FileUtil.DeleteDirectory(SampleProjectsPath);

            // copy sample projects from resource
            FileUtil.CopyDirectory(Path.Combine(baseProjectPath, @"src\test\resource"), SampleProjectsPath);
        }


        [TearDown]
	    public void FinalizationProcess()
	    {
	      // just incase we need to finilize after running some tests
	    }

    }
}
