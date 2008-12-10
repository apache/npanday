using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{

    public class ProjectReference : IncludeBase
    {
        public ProjectReference(string projectBasePath) 
            : base(projectBasePath)
        {
        }

        private string name;
        public string Name
        {
            get { return name; }
            set { name = value; }
        }

        private string projectPath;
        public string ProjectPath
        {
            get { return projectPath; }
            set { projectPath = value; }
        }

        public string ProjectFullPath
        {
            get
            {
                if (Path.IsPathRooted(projectPath))
                {
                    return Path.GetFullPath(projectPath);
                }
                else
                {
                    return Path.GetFullPath(projectBasePath + @"\" + projectPath);
                }

            }
        }

        private ProjectDigest projectReferenceDigest;
        public ProjectDigest ProjectReferenceDigest
        {
            get { return projectReferenceDigest; }
            set { projectReferenceDigest = value; }
        }
    }

}
