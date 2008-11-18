using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Digest.Model
{
    public abstract class IncludeBase
    {

        protected string projectBasePath;
        public IncludeBase(string projectBasePath)
        {
            this.projectBasePath = projectBasePath;
        }

        private string includePath;
        public string IncludePath
        {
            get { return includePath; }
            set { includePath = value; }
        }

        public string IncludeFullPath
        {
            get 
            {
                if (Path.IsPathRooted(includePath))
                {
                    return Path.GetFullPath(includePath);
                }
                else
                {
                    return Path.GetFullPath(projectBasePath + @"\" + includePath);
                }
            
            }
        }
    }
}
