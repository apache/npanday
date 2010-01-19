using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using NUnit.Framework;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.ImporterTests
{
    public class ProjectImporterAssertions
    {

        #region Assert Helper Methods

        public static void AssertPomFilesExists(string[] pomFiles)
        {
            foreach (string pomfile in pomFiles)
            {
                FileInfo file = new FileInfo(pomfile);

                if (!file.Exists)
                {
                    Assert.Fail(string.Format("Pom File Doesnot Exist: {0}", pomfile));
                }
            }
        }



        public static void AssertHasNoOverlappingPomFiles(string[] pomFiles)
        {
            List<string> files = new List<string>();
            bool overlapping = false;
            foreach (string pomfile in pomFiles)
            {

                foreach (string file in files)
                {
                    if (FileUtil.IsSameFile(pomfile, file))
                    {
                        overlapping = true;
                    }
                }


                files.Add(pomfile);
            }


            if (overlapping)
            {
                Assert.Fail("Generated Files are overlapping, generated files: "
                    + "\n***************************\n"
                    + string.Join("\n", pomFiles) 
                    + "\n***************************\n"
                    );
            }

        }

        public static void AssertPomCount(int expected, string[] poms)
        {
            if (poms.Length != expected)
            {
                Assert.Fail(string.Format("Expecting {0} pom files tobe generated, but only {1} are generated!!!", expected, poms.Length));
            }
        }
		
		public static void AssertPomElementValues(string testPomLocation, string[] pomFiles, Dictionary<string, string> testXPaths)
        {
            string[] testPomFiles = FileUtil.GetTestPomFiles(Path.GetFullPath(Directory.GetCurrentDirectory() + @"\..\..") + testPomLocation, pomFiles);
            Assert.AreEqual(testPomFiles.Length, pomFiles.Length);
            int pomCount = testPomFiles.Length;
            for (int index = 0; index < pomCount; index++)
            {

                string returnMsg = FileUtil.CrossCheckPomElement(testPomFiles[index], pomFiles[0], testXPaths);
                if (!string.IsNullOrEmpty(returnMsg))
                {
                    Assert.Fail(returnMsg);
                }
            }
        }
        #endregion

    }
}
