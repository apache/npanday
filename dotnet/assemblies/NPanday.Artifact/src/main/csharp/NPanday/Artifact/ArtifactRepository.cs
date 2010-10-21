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
using System.Xml;
using System.Xml.Serialization;
using System.Windows.Forms;
using NPanday.Model.Setting;

namespace NPanday.Artifact
{
    public sealed class ArtifactRepository
    {

        public string GetLocalUacPath(Artifact artifact, string ext)
        {
            return Path.Combine(SettingsUtil.GetLocalRepositoryPath(), string.Format(@"{0}\{1}\{1}{2}-{3}", Tokenize(artifact.GroupId), artifact.ArtifactId, artifact.Version, ext));
        }
        
        public string Tokenize(string id)
        {
            return id.Replace(".",Path.DirectorySeparatorChar.ToString());
        }
        

        public string GetLocalRepositoryPath(Artifact artifact, string ext)
        {
            return Path.Combine(localRepository.FullName, string.Format(@"repository\{0}\{1}\{2}\{1}-{2}{3}", artifact.GroupId.Replace(@".",@"\"), artifact.ArtifactId, artifact.Version,ext));
        }

        public string GetRemoteRepositoryPath(Artifact artifact, string url, string ext)
        {
            return string.Format("{0}/{1}/{2}/{3}/{2}-{3}{4}", url, artifact.GroupId.Replace('.','/'), artifact.ArtifactId, artifact.Version, ext);
        }

        public string GetRemoteRepositoryPath(Artifact artifact, string timeStampVersion, string url, string ext)
        {
            return string.Format("{0}/{1}/{2}/{3}/{2}-{4}{5}", url, artifact.GroupId.Replace('.', '/'), artifact.ArtifactId, artifact.Version, timeStampVersion, ext);
        }

        public Artifact GetArtifactFor(String uri)
        {
            Artifact artifact = new Artifact();

            DirectoryInfo uac = new DirectoryInfo(localRepository.FullName);

            String[] tokens = uri.Split("/".ToCharArray(), StringSplitOptions.RemoveEmptyEntries);
            int size = tokens.Length;
            if (size < 3)
            {
                System.Reflection.Assembly a = System.Reflection.Assembly.LoadFile(uri);
                string[] info = a.FullName.Split(",".ToCharArray(), StringSplitOptions.RemoveEmptyEntries);
                artifact.ArtifactId = info[0];
                artifact.GroupId = info[0];
                artifact.Version = info[1].Split(new char[] { '='})[1];
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

            artifact.FileInfo = new FileInfo(uac.FullName + Tokenize( artifact.GroupId )+ Path.DirectorySeparatorChar + artifact.ArtifactId + Path.DirectorySeparatorChar 
                + artifact.Version + Path.DirectorySeparatorChar + artifact.ArtifactId+ "-" + artifact.Version+ ".dll");
            return artifact;
        }

        public List<Artifact> GetArtifacts()
        {
            List<Artifact> artifacts = new List<Artifact>();
            try
            {
                DirectoryInfo uac = new DirectoryInfo(localRepository.FullName);
                int directoryStartPosition = uac.FullName.Length;

                List<FileInfo> fileInfos = GetArtifactsFromDirectory(uac);

                foreach (FileInfo fileInfo in fileInfos)
                {
                    try
                    {
                        Artifact artifact = GetArtifact(uac, fileInfo);
                        artifacts.Add(artifact);
                    }
                    catch
                    {
                        
                    }
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.StackTrace, e.Message);
            }
            return artifacts;
        }

        #region Repository Artifact Info Helper

        public Artifact GetArtifact(FileInfo artifactFile)
        {
            DirectoryInfo uacDirectory = new DirectoryInfo( localRepository.FullName );
            return GetArtifact(uacDirectory, artifactFile);
        }

        public Artifact GetArtifact(NPanday.Model.Pom.Dependency dependency)
        {
            Artifact artifact = new Artifact();
            artifact.ArtifactId = dependency.artifactId;
            artifact.GroupId = dependency.groupId;
            artifact.Version = dependency.version;
            artifact.FileInfo = new FileInfo( GetLocalRepositoryPath(artifact, ".dll"));
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

            string ext = Path.GetExtension(tokens[2]);


            // first file token is the artifact
            // eg. NPanday.VisualStudio.Addin\1.1.1.1__NMaven.VisualStudio\NPanday.VisualStudio.Addin.dll
            string artifactId = tokens[0];
            string groupId = getGroupId(tokens[1]);
            string version = getVersion(tokens[1]);


            Artifact artifact = new Artifact();
            artifact.ArtifactId = artifactId;
            artifact.Version = version;
            artifact.GroupId = groupId;
            artifact.FileInfo = new FileInfo(GetLocalUacPath(artifact, ext));
            
            
            
            return artifact;
        }



        string getVersion(string versionAndGroupDirectoryName)
        {
           try
            {
                // 1.1.1.1__NMaven.VisualStudio from index 0 to __ is the version number
                int index = versionAndGroupDirectoryName.IndexOf("__", 0);
                string str = versionAndGroupDirectoryName.Substring(0, index);
                return str;
            }
            catch
            {
                return string.Empty;
            }
        }

        string getGroupId(string versionAndGroupDirectoryName)
        {
            try
            {
                int index = versionAndGroupDirectoryName.IndexOf("__", 0) + 2;
                // 1.1.1.1__NMaven.VisualStudio from (next to index of __) to last is the groupid
                string str = versionAndGroupDirectoryName.Substring(index, (versionAndGroupDirectoryName.Length - index));
                return str;
            }
            catch
            {
                return string.Empty;
            }
        }

        #endregion



        public void Init(ArtifactContext artifactContext, DirectoryInfo localRepository)
        {
            this.artifactContext = artifactContext;
            this.localRepository = localRepository;
        }

        private List<FileInfo> GetArtifactsFromDirectory(DirectoryInfo baseDirectoryInfo)
        {
            DirectoryInfo[] directories = baseDirectoryInfo.GetDirectories();
            List<FileInfo> fileInfos = new List<FileInfo>();
            foreach (DirectoryInfo directoryInfo in directories)
            {
                foreach (FileInfo fileInfo in directoryInfo.GetFiles())
                {
                    if (fileInfo.Name.EndsWith(".dll") || fileInfo.Name.EndsWith(".exe") || fileInfo.Name.EndsWith(".netmodule") )
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
