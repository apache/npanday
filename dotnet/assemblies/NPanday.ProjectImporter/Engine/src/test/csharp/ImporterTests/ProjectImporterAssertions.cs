//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
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
                Assert.Fail(string.Format("Expecting {0} POM files to be generated, but {1} were generated!!!", expected, poms.Length));
            }
        }
		
		public static void AssertPomElementValues(string testPomLocation, string[] pomFiles)
        {
            string[] testPomFiles = FileUtil.GetTestPomFiles(Path.Combine(FileUtil.GetBaseDirectory(), testPomLocation), pomFiles);
            Assert.AreEqual(testPomFiles.Length, pomFiles.Length);
            int pomCount = testPomFiles.Length;
            for (int index = 0; index < pomCount; index++)
            {
                string returnMsg = FileUtil.CrossCheckPomElement(testPomFiles[index], pomFiles[index]);
                if (!string.IsNullOrEmpty(returnMsg))
                {
                    Assert.Fail(returnMsg);
                }
            }
        }
        #endregion

    }
}
