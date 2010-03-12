using System.Collections.Generic;
using System.IO;
using System.Windows.Forms;

using Microsoft.Build.BuildEngine;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Digest.Algorithms;

using NPanday.ProjectImporter.Parser.VisualStudioProjectTypes;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest
{
    public sealed class ProjectDigester
    {
        public delegate ProjectDigest DigestProject(Dictionary<string, object> project);
        private static readonly Dictionary<VisualStudioProjectTypeEnum, DigestProject> _digestAlgoritms;

        static ProjectDigester()
        {
            _digestAlgoritms = new Dictionary<VisualStudioProjectTypeEnum, DigestProject>();

            _digestAlgoritms.Add(VisualStudioProjectTypeEnum.Windows__CSharp, new NormalProjectDigestAlgorithm().DigestProject);
            _digestAlgoritms.Add(VisualStudioProjectTypeEnum.Windows__VbDotNet, new NormalProjectDigestAlgorithm().DigestProject);
            _digestAlgoritms.Add(VisualStudioProjectTypeEnum.Web_Site, new WebProjectDigestAlgorithm().DigestProject);
            
            
            // combined Projects
            _digestAlgoritms.Add(
                    VisualStudioProjectTypeEnum.Web_Site | VisualStudioProjectTypeEnum.Windows__CSharp, 
                    new NormalProjectDigestAlgorithm().DigestProject
                );
            
            _digestAlgoritms.Add(
                    VisualStudioProjectTypeEnum.Web_Site | VisualStudioProjectTypeEnum.Windows__VbDotNet, 
                    new NormalProjectDigestAlgorithm().DigestProject
                );


            _digestAlgoritms.Add(
                    VisualStudioProjectTypeEnum.Web_Application | VisualStudioProjectTypeEnum.Windows__CSharp,
                    new NormalProjectDigestAlgorithm().DigestProject
                );

            _digestAlgoritms.Add(
                    VisualStudioProjectTypeEnum.Web_Application | VisualStudioProjectTypeEnum.Windows__VbDotNet,
                    new NormalProjectDigestAlgorithm().DigestProject
                );



            

        }

        

        public static ProjectDigest[] DigestProjects(List<Dictionary<string, object>> projects, ref string warningMsg)
        {
            List<ProjectDigest> projectDigests = new List<ProjectDigest>();
            Dictionary<string, ProjectDigest> projDigestDictionary = new Dictionary<string, ProjectDigest>();

            foreach (Dictionary<string, object> project in projects)
            {
                DigestProject digestProject = _digestAlgoritms[(VisualStudioProjectTypeEnum)project["ProjectType"]];
                ProjectDigest projDigest = digestProject(project);
                projectDigests.Add(projDigest);
                projDigestDictionary.Add(projDigest.AssemblyName, projDigest);
            }

            List<ProjectDigest> tobeIncluded = new List<ProjectDigest>();

            // verify if all project dependencies are in the solution
            foreach (ProjectDigest projectDigest in projectDigests)
            {
                foreach (ProjectReference projectReference in projectDigest.ProjectReferences)
                {
                    if (string.IsNullOrEmpty(projectReference.Name) 
                        || !projDigestDictionary.ContainsKey(projectReference.Name))
                    {
                        Project prjRef = GetProject(projectReference.ProjectFullPath);
                        if (prjRef == null)
                        {
                            // this might not be possible
                            warningMsg = string.Format(
                            "{0}\n    Missing Project Reference {1} located at {2}!"+
                            "\n        Note this might cause Missing Artifact Dependency!", 
                                warningMsg,
                                projectReference.Name,
                                projectReference.ProjectFullPath);
                            continue;
                        }

                        DigestProject digestProject = _digestAlgoritms[VisualStudioProjectTypeEnum.Windows__CSharp];

                        Dictionary<string, object> projectMap = new Dictionary<string, object>();
                        projectMap.Add("Project", prjRef);

                        ProjectDigest prjRefDigest = digestProject(projectMap);
                        string errMsg = string.Format(
                            "Project \"{0}\"  Requires \"{1}\" which is not included in the Solution File, "
                            + "\nWould you like to include \"{1}\" Generating NPanday Project Poms?"
                            + "\nNote: Not adding \"{1}\" will result to a missing Artifact Dependency \"{1}\"",
                            projectDigest.AssemblyName,
                            prjRefDigest.AssemblyName);

                        DialogResult includeResult = MessageBox.Show(errMsg, "Include Project in Pom Generation:",
                            MessageBoxButtons.YesNo,
                            MessageBoxIcon.Question);

                        if (includeResult == DialogResult.Yes)
                        {
                            projDigestDictionary.Add(prjRefDigest.AssemblyName, prjRefDigest);
                            tobeIncluded.Add(prjRefDigest);
                        }
                        else
                        {
                            warningMsg = string.Format(
                                "{0}\n    Please Make sure that Artifact[GroupId: {1}, ArtifactId: {1}] exists in your NPanday Repository, " +
                                "\n        Or an error will occur during NPanday-Build due to Missing Artifact Dependency!",
                                warningMsg, prjRefDigest.AssemblyName);
                        }
                    }
                }
            }

            // add tobe included
            projectDigests.AddRange(tobeIncluded);



            // insert the projectRererences
            foreach (ProjectDigest prjDigest in projectDigests)
            {
                if (prjDigest.ProjectReferences == null)
                {
                    // if no project references proceed to the next loop
                    continue;
                }

                for (int i = 0; i < prjDigest.ProjectReferences.Length; i++)
                {
                    ProjectReference prjRef = prjDigest.ProjectReferences[i];
                    if (projDigestDictionary.ContainsKey(prjRef.Name))
                    {
                        ProjectDigest pd = projDigestDictionary[prjRef.Name];
                        prjRef.ProjectReferenceDigest = pd;
                    }
                }
            }

            
            // sort by inter-project dependency
            projectDigests.Sort(CompareByDependency);

            return projectDigests.ToArray();
        }


        // dependency sorter
        private static int CompareByDependency(ProjectDigest x, ProjectDigest y)
        {
            // Less than 0, x is less than y. (x is referring to y)
            // 0, x equals y. (not refering to each other)
            // Greater than 0, x is greater than y. (x is reffered by y)

            foreach (ProjectReference prjRef in y.ProjectReferences)
            {
                if (x.AssemblyName.Equals(prjRef.Name))
                {
                    // Greater than 0, x is greater than y. (x is reffered by y)
                    return -1;
                }
            }

            foreach (ProjectReference prjRef in x.ProjectReferences)
            {
                if (y.AssemblyName.Equals(prjRef.Name))
                {
                    // Less than 0, x is less than y. (x is referring to y)
                    return 1;
                }
            }
            // x equals y.
            return 0;

        }




        public static Project GetProject(string projectFile)
        {
            if (!new FileInfo(projectFile).Exists)
            {
                return null;
            }

            if (!(projectFile.ToUpper().EndsWith(".CSPROJ") || projectFile.ToUpper().EndsWith(".VBPROJ")))
            {
                return null;
            }

            // gets the directory path of mscorlib using the System.String Type Assembly path
            string msBuildPath = Path.GetDirectoryName(System.Reflection.Assembly.GetAssembly(typeof(string)).Location);
            Engine engine = new Engine(msBuildPath);

            Project project = new Project(engine);
            project.Load(projectFile);

            return project;
        }


        
    }
}
