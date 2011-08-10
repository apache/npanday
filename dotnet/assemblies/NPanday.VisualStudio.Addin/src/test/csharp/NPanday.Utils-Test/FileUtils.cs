#region Apache License, Version 2.0
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
#endregion
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace ConnectTest.UtilsTest
{
    public class FileUtils
    {
        public static string getBaseDirectory()
        {
            return new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target"))).FullName;
        }

        public static string getLocalRepository()
        {
            string homePath = string.Empty;
            if (Environment.OSVersion.Platform == PlatformID.Unix || Environment.OSVersion.Platform == PlatformID.MacOSX)
            {
                homePath = Environment.GetEnvironmentVariable("HOME");
            }
            else
            {
                homePath = Environment.ExpandEnvironmentVariables("%HOMEDRIVE%%HOMEPATH%");

                if (homePath == null || homePath == string.Empty)
                {
                    homePath = Environment.GetEnvironmentVariable("USERPROFILE");
                }
            }

            return new FileInfo(homePath + "\\.m2").FullName;

        }

        public static void CopyDirectory(DirectoryInfo source, DirectoryInfo destination)
        {
            if (!destination.Exists)
            {
                destination.Create();
            }

            FileInfo[] files = source.GetFiles();
            foreach (FileInfo filePath in files)
            {
                if (filePath.Name != null && !filePath.Name.EndsWith(".test"))
                    filePath.CopyTo(Path.Combine(destination.FullName, filePath.Name));
            }

            DirectoryInfo[] subDirectories = source.GetDirectories();
            foreach (DirectoryInfo dirPath in subDirectories)
            {
                if (!dirPath.Name.Equals(".svn"))
                {
                    CopyDirectory(new DirectoryInfo(Path.Combine(source.FullName, dirPath.Name)), new DirectoryInfo(Path.Combine(destination.FullName, dirPath.Name)));
                }
            }
        }
    }
}
