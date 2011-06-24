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
using System.Xml;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Security.Cryptography;

using NPanday.Utils;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.ImporterTests
{
    public class FileUtil
    {
		const string MSG_ERROR_EXPECTEDFILE_NOTFOUND = "The Expected File is not in its location. {0}";
        const string MSG_ERROR_ACTUALFILE_NOTFOUND = "The Pom File is not in its location. {0}";
        const string MSG_ERROR_NOXPATH = "No XPath to cross reference pom file created.";
        const string MSG_EXPECTEDXMLELEMENT_NOTFOUND = "Epected Pom and Actual Pom don't have the same elements.";


        public static bool IsSameFile(string dir1, string dir2)
        {
            string str1 = NPanday.Utils.PomHelperUtility.NormalizeFileToWindowsStyle(Path.GetFullPath(dir1));
            string str2 = NPanday.Utils.PomHelperUtility.NormalizeFileToWindowsStyle(Path.GetFullPath(dir2));
            return str1.Equals(str2, StringComparison.OrdinalIgnoreCase);
        }

        public static void DeleteDirectory(string dir)
        {
            DeleteDirectory(new DirectoryInfo(dir));
        }


        public static void DeleteDirectory(DirectoryInfo dir)
        {
            try
            {
                if (dir.Exists)
                {
                    dir.Delete(true);
                }
            }
            catch (Exception e)
            {

                throw new Exception(string.Format("Error In Deleting Directory: {0}", dir.FullName), e);
            }

        }


        public static void CopyDirectory(String source, String destination)
        {
            CpDir(source, destination);
            
        }


        public static void CopyDirectory(DirectoryInfo source, DirectoryInfo destination)
        {
            CpDir(source, destination);
            
        }




        static void CpDir(String source, String destination)
        {

            // argument validation goes here, CBA to do it now

            DirectoryInfo destDir = new DirectoryInfo(destination);

            // do not copy subversion information
            if (destDir.Name == ".svn" || destDir.Name == "_svn")
                return;

            if (!destDir.Exists)
            {
                destDir.Create();
            }

            DirectoryInfo dir = new DirectoryInfo(source);
            FileInfo[] files = dir.GetFiles();
            foreach (FileInfo filePath in files)
            {
				if (filePath.Name != null && !filePath.Name.EndsWith(".test"))
					filePath.CopyTo(Path.Combine(destination, filePath.Name));
            }



            DirectoryInfo[] subDirectories = dir.GetDirectories();
            foreach (DirectoryInfo dirPath in subDirectories)
            {
                CpDir(Path.Combine(source, dirPath.Name), Path.Combine(destination, dirPath.Name));
            }


        }


        static void CpDir(DirectoryInfo source, DirectoryInfo destination)
        {
            CpDir(source.FullName, destination.FullName);
        }
		
		
		public static string[] GetTestPomFiles(string rootPath, string[] actualPomFileLocations)
        {
            
            List<string> outPut = new List<string>();
            //File.Delete(filename);
            
            if (actualPomFileLocations.Length < 1) return outPut.ToArray();

            string basePath = parseBasePath(rootPath, actualPomFileLocations[0]);
            foreach (string actualPomFileLocation in actualPomFileLocations)
            {
                outPut.Add(actualPomFileLocation.Replace(".xml", ".test").Replace(basePath, rootPath));
            }
            return outPut.ToArray();
        }
        static string parseBasePath(string replaceWith, string basePath)
        {
            string returnValue = basePath;
            if (string.IsNullOrEmpty(returnValue)) return returnValue;
            int endIndex = returnValue.LastIndexOf('\\');
            if (endIndex < 0) return returnValue;
            returnValue = returnValue.Substring(0, endIndex + 1);
            return returnValue;
        }
        public static string CrossCheckPomElement(string refLocation, string pomPath)
        {
            if (!File.Exists(refLocation))
            {
                return string.Format(MSG_ERROR_EXPECTEDFILE_NOTFOUND, refLocation);
            }
            if (!File.Exists(pomPath))
            {
                return string.Format(MSG_ERROR_ACTUALFILE_NOTFOUND, pomPath);
            }

            FileInfo first = new FileInfo(pomPath);
            FileInfo second = new FileInfo(refLocation);

            using (FileStream fs1 = first.OpenRead())
            using (FileStream fs2 = second.OpenRead())
            {
                for (int i = 0; i < first.Length; i++)
                {
                    if (fs1.ReadByte() != fs2.ReadByte())
                        return string.Format("POMs {0} and {1} do not match @ byte {2}", pomPath, refLocation, i);
                }
            }
            return null;
        }

        public static string GetBaseDirectory()
        {
            return new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target"))).FullName;
        }
    }
}
