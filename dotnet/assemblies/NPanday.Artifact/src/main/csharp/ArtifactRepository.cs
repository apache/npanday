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
using log4net;

namespace NPanday.Artifact
{
    public sealed class ArtifactRepository
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(ArtifactRepository));

        public string Tokenize(string id)
        {
            return id.Replace(".", Path.DirectorySeparatorChar.ToString());
        }

        public string GetLocalRepositoryPath(Artifact artifact, string ext)
        {
            return string.Format(@"{0}\{1}\{2}\{3}\{2}-{3}{4}", localRepository.FullName, artifact.GroupId.Replace(@".", @"\"), artifact.ArtifactId, artifact.Version, ext);
        }

        public string GetRemoteRepositoryPath(Artifact artifact, string url, string ext)
        {
            return string.Format("{0}/{1}/{2}/{3}/{2}-{3}{4}", url, artifact.GroupId.Replace('.', '/'), artifact.ArtifactId, artifact.Version, ext);
        }

        public string GetRemoteRepositoryPath(Artifact artifact, string timeStampVersion, string url, string ext)
        {
            return string.Format("{0}/{1}/{2}/{3}/{2}-{4}{5}", url, artifact.GroupId.Replace('.', '/'), artifact.ArtifactId, artifact.Version, timeStampVersion, ext);
        }

        public Artifact GetArtifactFor(String uri)
        {
            Artifact artifact = new Artifact();

            String[] tokens = uri.Split("/".ToCharArray(), Int32.MaxValue, StringSplitOptions.RemoveEmptyEntries);
            int size = tokens.Length;
            if (size < 3)
            {
                System.Reflection.Assembly a = System.Reflection.Assembly.LoadFile(uri);
                string[] info = a.FullName.Split(",".ToCharArray(), Int32.MaxValue, StringSplitOptions.RemoveEmptyEntries);
                artifact.ArtifactId = info[0];
                artifact.GroupId = info[0];
                artifact.Version = info[1].Split(new char[] { '=' })[1];
                artifact.Extension = tokens[0].Split(new char[] { '.' })[1];

                if (artifact.Version == null)
                {
                    artifact.Version = "1.0.0.0";
                }
            }

            else
            {
                artifact.ArtifactId = tokens[size - 3];
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < size - 3; i++)
                {
                    buffer.Append(tokens[i]);
                    if (i != size - 4)
                    {
                        buffer.Append(".");
                    }
                }
                artifact.GroupId = buffer.ToString();
                artifact.Version = tokens[size - 2];
                String[] extToken = tokens[size - 1].Split(".".ToCharArray());
                artifact.Extension = extToken[extToken.Length - 1];
            }
            artifact.FileInfo = new FileInfo(localRepository.FullName + Path.DirectorySeparatorChar + Tokenize(artifact.GroupId) + Path.DirectorySeparatorChar + artifact.ArtifactId + Path.DirectorySeparatorChar
                + artifact.Version + Path.DirectorySeparatorChar + artifact.ArtifactId + "-" + artifact.Version + ".dll");
            return artifact;
        }

        public List<Artifact> GetArtifacts()
        {
            List<Artifact> artifacts = new List<Artifact>();
            try
            {
                List<FileInfo> fileInfos = GetArtifactsFromDirectory(localRepository);

                foreach (FileInfo fileInfo in fileInfos)
                {
                    try
                    {
                        Artifact artifact = GetArtifact(localRepository, fileInfo);
                        artifacts.Add(artifact);
                    }
                    catch
                    {

                    }
                }
            }
            catch (Exception e)
            {
                log.Error(e.Message, e);
            }
            return artifacts;
        }

        #region Repository Artifact Info Helper

        public Artifact GetArtifact(FileInfo artifactFile)
        {
            return GetArtifact(localRepository, artifactFile);
        }

        public Artifact GetArtifact(NPanday.Model.Pom.Dependency dependency)
        {
            Artifact artifact = new Artifact();
            artifact.ArtifactId = dependency.artifactId;
            artifact.GroupId = dependency.groupId;
            artifact.Version = dependency.version;
            artifact.FileInfo = new FileInfo(GetLocalRepositoryPath(artifact, ".dll"));
            return artifact;
        }

        public Artifact GetArtifact(DirectoryInfo uacDirectory, FileInfo artifactFile)
        {
            string[] tokens;
            try
            {
                tokens = PathUtil.GetRelativePathTokens(uacDirectory, artifactFile);
            }
            catch
            {
                List<string> tk = new List<string>(artifactFile.FullName.Split(@"\".ToCharArray()));
                tk.RemoveRange(0, tk.Count - 3);
                tokens = tk.ToArray();
            }


            //artifact for system path
            if (!artifactFile.FullName.Contains(".m2"))
            {
                return null;
            }

            string fileName = tokens[tokens.Length - 1];
            int index = fileName.LastIndexOf(".");

            string ext = fileName.Substring(index);
            string version = tokens[tokens.Length - 2];
            string artifactId = tokens[tokens.Length - 3];

            StringBuilder group = new StringBuilder();

            for (int i = 0; i < tokens.Length - 3; i++)
            {
                group.Append(tokens[i]).Append(".");
            }

            string groupId = group.ToString(0, group.Length - 1);

            Artifact artifact = new Artifact();
            artifact.ArtifactId = artifactId;
            artifact.Version = version;
            artifact.GroupId = groupId;
            artifact.FileInfo = new FileInfo(GetLocalRepositoryPath(artifact, ext));



            return artifact;
        }

        #endregion



        public void Init(ArtifactContext artifactContext, DirectoryInfo localRepository)
        {
            this.artifactContext = artifactContext;
            this.localRepository = localRepository;
        }

        private List<FileInfo> GetArtifactsFromDirectory(DirectoryInfo baseDirectoryInfo)
        {
            List<FileInfo> fileInfos = new List<FileInfo>();

            DirectoryInfo[] directories = baseDirectoryInfo.GetDirectories();
            foreach (DirectoryInfo directoryInfo in directories)
            {
                foreach (FileInfo fileInfo in directoryInfo.GetFiles())
                {
                    if (fileInfo.Name.EndsWith(".dll") || fileInfo.Name.EndsWith(".exe") || fileInfo.Name.EndsWith(".netmodule"))
                    {
                        fileInfos.Add(fileInfo);
                    }
                }
                fileInfos.AddRange(GetArtifactsFromDirectory(directoryInfo));
            }
            return fileInfos;
        }

        private ArtifactContext artifactContext;
        private DirectoryInfo localRepository;
    }
}
