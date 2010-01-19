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
using System.IO;
using System.Text;

namespace NPanday.Artifact
{
    public class PathUtil
    {
        public static FileInfo GetPrivateApplicationBaseFileFor(Artifact artifact, DirectoryInfo localRepository)
        {
            return new FileInfo(localRepository.Parent.FullName + @"\pab\gac_msil\" + artifact.ArtifactId + @"\" + artifact.Version + "__" +
                artifact.GroupId + @"\" + artifact.ArtifactId + "." + artifact.Extension);
        }

        public static FileInfo GetUserAssemblyCacheFileFor(Artifact artifact, DirectoryInfo localRepository)
        {
            return new FileInfo(localRepository.Parent.FullName + @"\uac\gac_msil\" + artifact.ArtifactId + @"\" + artifact.Version + "__" +
                artifact.GroupId + @"\" + artifact.ArtifactId + "." + artifact.Extension);
        }


        public static string[] GetRelativePathTokens(DirectoryInfo parentPath, FileInfo path)
        {
            return GetRelativePathTokens(parentPath.FullName, path.FullName);
        }

        public static string[] GetRelativePathTokens(DirectoryInfo parentPath, DirectoryInfo path)
        {
            return GetRelativePathTokens(parentPath.FullName, path.FullName);
        }

        public static string[] GetRelativePathTokens(FileInfo parentPath, FileInfo path)
        {
            return GetRelativePathTokens(parentPath.FullName, path.FullName);
        }


        public static string[] GetRelativePathTokens(string parentPath, string path)
        {   
            string[] parent = TokenizePath(parentPath);
            string[] child = TokenizePath(path);

            List<string> list = new List<string>();

            for (int i = 0; i < parent.Length - 1; i++)
            {
                if (!parent[i].Equals(child[i], StringComparison.OrdinalIgnoreCase))
                {
                    throw new Exception(string.Format("Path {0} is not a child path of {1}", path, parentPath));
                }
            }


            list.AddRange(child);
            list.RemoveRange(0, parent.Length - 1);

            return list.ToArray();

        }


        public static string[] TokenizePath(FileInfo fileInfo)
        {
            return TokenizePath(fileInfo.FullName);
        }

        public static  string[] tokenizePath(DirectoryInfo directoryInfo)
        {
            return TokenizePath(directoryInfo.FullName);
        }

        public static string[] TokenizePath(string filename)
        {
            string path = Path.GetFullPath(filename);
            path = path.Replace('/', '\\');

            return path.Split('\\');

        }


    }
}
