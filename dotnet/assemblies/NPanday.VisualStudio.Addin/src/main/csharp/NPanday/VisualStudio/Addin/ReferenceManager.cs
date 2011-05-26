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
using NPanday.Model.Pom;
using System.Windows.Forms;
using EnvDTE;
using EnvDTE80;

namespace NPanday.VisualStudio.Addin
{
    #region Interfaces
    
    /// <summary>
    /// A custom Reference Manager that will handle all artifact reference that can be easily transferable.
    /// </summary>
    public interface IReferenceManager
    {
        Artifact.Artifact Add(IReferenceInfo reference);
        void Remove(IReferenceInfo reference);
        void Initialize(VSLangProj80.VSProject2 project);
        string ReferenceFolder { get; }
        void CopyArtifact(Artifact.Artifact artifact, NPanday.Logging.Logger logger);
        void ResyncArtifacts(NPanday.Logging.Logger logger);
        event EventHandler<ReferenceErrorEventArgs> OnError;
    }

    public interface IReferenceInfo
    {
        string Path { get;set;}
        string FileName { get;set;}
        string Version { get; set;}
        Artifact.Artifact Artifact { get; set; }
    }

    #endregion

    public class ReferenceManager : IReferenceManager
    {
        bool initialized = false;
        string pomFile;
        string projectPath;
        Solution solution;

        #region IReferenceManager Members

        public Artifact.Artifact Add(IReferenceInfo reference)
        {
            if (!initialized)
                throw new Exception("Reference manager not initialized.");

            string artifactFileName = copyToReferenceFolder(reference.Artifact, referenceFolder);

            Artifact.Artifact a = reference.Artifact;
            
            a.FileInfo = new FileInfo(artifactFileName);
            return a;
        }

        public void Remove(IReferenceInfo reference)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public void Initialize(VSLangProj80.VSProject2 project)
        {
            solution = project.Project.DTE.Solution;
            projectPath = Path.GetDirectoryName(project.Project.FileName);
            referenceFolder = Path.Combine( projectPath,".references");
            pomFile =  Path.Combine(projectPath, "pom.xml");

            initialized = true;
            if (!pomExist())
            {
                throw new Exception("Project has no valid pom file.");
            }
            createReferenceFolder();
        }

        string referenceFolder;
        public string ReferenceFolder
        {
            get
            {
                return referenceFolder;
            }
            //for testing purposes
            set
            {
                initialized = true;
                referenceFolder = value;
            }
        }

        public void CopyArtifact(Artifact.Artifact artifact, NPanday.Logging.Logger logger)
        {
            if (!initialized)
                throw new Exception("Reference manager not initialized.");

            if (!artifact.FileInfo.Exists || artifact.Version.EndsWith("SNAPSHOT"))
            {
                if (!NPanday.ProjectImporter.Digest.Model.Reference.DownloadArtifact(artifact,logger))
                {
                    ReferenceErrorEventArgs e = new ReferenceErrorEventArgs();
                    e.Message = string.Format("Unable to get the artifact {0} from any of your repositories.", artifact.ArtifactId);
                    onError(e);
                    return;
                }
            }

            copyToReferenceFolder(artifact, referenceFolder);
        }

        public void ResyncArtifacts(NPanday.Logging.Logger logger)
        {
            if (!initialized)
                throw new Exception("Reference manager not initialized.");
            getReferencesFromPom(logger);
        }

        #endregion

        #region privates

        static string copyToReferenceFolder(Artifact.Artifact artifact, string referenceFolder)
        {
            //modified artifactFolder to match the .dll searched in NPanday.ProjectImporter.Digest.Model.Reference.cs
            string artifactFolder = Path.Combine(referenceFolder, string.Format("{0}\\{1}-{2}", artifact.GroupId, artifact.ArtifactId, artifact.Version));
            //string artifactFolder = Path.Combine(referenceFolder, string.Format("{0}\\{1}", artifact.GroupId, artifact.ArtifactId));
            
            DirectoryInfo di = new DirectoryInfo(artifactFolder);
            if (!di.Exists)
            {
                di.Create();
            }
            
            //string artifactFileName = Path.Combine(artifactFolder, artifact.FileInfo.Name);
            string artifactFileName = Path.Combine(artifactFolder, artifact.ArtifactId+".dll");

            // TODO: Probably we should use value of 
            // <metadata>/<versioning>/<lastUpdated> node from maven metadata xml file 
            // as an artifactTimestamp
            DateTime artifactTimestamp = new FileInfo(artifact.FileInfo.FullName).LastWriteTime;

            if (!File.Exists(artifactFileName) ||
                (artifactTimestamp.CompareTo(new FileInfo(artifactFileName).LastWriteTime) > 0))
            {
                try
                {
                    byte[] contents = File.ReadAllBytes(artifact.FileInfo.FullName);
                    File.WriteAllBytes(artifactFileName, contents);
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.ToString());
                }
            }
            return artifactFileName;
        }


        void getReferencesFromPom(NPanday.Logging.Logger logger)
        {
            Artifact.ArtifactRepository repository = new NPanday.Artifact.ArtifactContext().GetArtifactRepository();
            NPanday.Model.Pom.Model m = NPanday.Utils.PomHelperUtility.ReadPomAsModel(new FileInfo(pomFile));

            if (m.dependencies != null)
            {
                foreach (Dependency d in m.dependencies)
                {
                    // check if intra-project reference and copy
                    // artifacts from remote repository only
                    if (!isIntraProject(m, d) && d.classifier == null)
                    {
                        CopyArtifact(repository.GetArtifact(d),logger);
                    }
                }
            }
        }

        bool isIntraProject(NPanday.Model.Pom.Model m, Dependency d)
        {
            string pomGroupId = (m.parent != null) ? m.parent.groupId : m.groupId;
            if (d.groupId == pomGroupId)
            {
                // loop through VS projects (instead of modules in parent POM) because
                // we need real-time list of project names in the solution
                foreach (Project project in solution.Projects)
                {
                    if (d.artifactId == project.Name)
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        bool pomExist()
        {
            return File.Exists(pomFile);
        }

        void createReferenceFolder()
        {
            DirectoryInfo di = new DirectoryInfo(referenceFolder);
            if (!di.Exists)
            {
                di.Create();
                di.Attributes = FileAttributes.Hidden | FileAttributes.Directory;
            }
        }

        #endregion


        #region IReferenceManager Members


        public event EventHandler<ReferenceErrorEventArgs> OnError;

        void onError(ReferenceErrorEventArgs e)
        {
            if (OnError != null)
            {
                OnError(this, e);
            }
        }

        #endregion
    }

    public class ReferenceErrorEventArgs : EventArgs
    {
        string message;
        public string Message
        {
            get { return message; }
            set { message = value; }
        }
    }

    public class ReferenceInfo : IReferenceInfo
    {
        public ReferenceInfo(){}
        public ReferenceInfo(Artifact.Artifact artifact)
        {
            path = artifact.FileInfo.FullName;
            fileName = artifact.FileInfo.Name;
            version = artifact.Version;
            this.artifact = artifact;
        }

        #region IReferenceInfo Members

        string path;
        public string Path
        {
            get
            {
                return path;
            }
            set
            {
                path = value;
            }
        }

        string fileName;
        public string FileName
        {
            get
            {
                return fileName;
            }
            set
            {
                fileName = value;
            }
        }

        string version;
        public string Version
        {
            get
            {
                return version;
            }
            set
            {
                version = value;
            }
        }

        Artifact.Artifact artifact;
        public NPanday.Artifact.Artifact Artifact
        {
            get
            {
                return artifact;
            }
            set
            {
                artifact = value;
            }
        }

        #endregion

    }

    
}
