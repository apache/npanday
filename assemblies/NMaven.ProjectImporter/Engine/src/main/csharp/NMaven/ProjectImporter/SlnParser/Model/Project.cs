using System;
using System.Collections.Generic;
using System.Text;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.SlnParser.Model
{
    public class Project
    {
        string projectTypeGUID;
        public string ProjectTypeGUID
        {
            get { return projectTypeGUID; }
            set { projectTypeGUID = value; }
        }

        string projectName;
        public string ProjectName
        {
            get { return projectName; }
            set { projectName = value; }
        }

        string projectPath;
        public string ProjectPath
        {
            get { return projectPath; }
            set { projectPath = value; }
        }

        string projectGUID;
        public string ProjectGUID
        {
            get { return projectGUID; }
            set { projectGUID = value; }
        }

        List<ProjectSection> projectSections = new List<ProjectSection>();
        public List<ProjectSection> ProjectSections
        {
            get { return projectSections; }
            set { projectSections = value; }
        }

    }
}
