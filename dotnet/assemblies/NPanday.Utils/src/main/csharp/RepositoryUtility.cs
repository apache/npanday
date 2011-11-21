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

namespace NPanday.Utils
{
    public class RepositoryUtility
    {
        public static bool InstallAssembly(string filename, string groupId, string artifactId, string version)
        {
            return InstallAssembly(filename, groupId, artifactId, version, false);
        }

        public static bool InstallAssembly(string filename, string groupId, string artifactId, string version, bool overwrite)
        {
            try
            {
                string m2Dir = Path.GetFullPath(string.Format("{0}\\..\\.m2", System.Environment.GetFolderPath(Environment.SpecialFolder.Personal)));
                string artifactDir = Path.Combine(m2Dir, string.Format(@"repository\{0}\{1}\{2}", groupId, artifactId, version));
                string artifactFilename = string.Format("{0}-{1}{2}", artifactId, version, Path.GetExtension(filename));

                if (!File.Exists(filename))
                    throw new Exception("Cannot find Assembly to install.");

                if (!Directory.Exists(artifactDir))
                    Directory.CreateDirectory(artifactDir);

                //if assembly already installed skip the copying
                if (File.Exists(Path.Combine(artifactDir, artifactFilename)))
                {
                    if (overwrite)
                    {
                        File.Delete(Path.Combine(artifactDir, artifactFilename));
                    }
                    else
                    {
                        return true;
                    }
                }

                //copy file
                File.Copy(filename, Path.Combine(artifactDir, artifactFilename));
                return true;

            }
            catch
            {
                throw;
            }
        }
    }
}
