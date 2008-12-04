using System;
using System.Collections.Generic;
using System.Text;
using NMaven.ProjectImporter.SlnParser;
using NMaven.ProjectImporter.SlnParser.Model;
using System.IO;
using NMaven.ProjectImporter.Parser.VisualStudioProjectTypes;
using System.Text.RegularExpressions;

namespace NMaven.ProjectImporter.Parser.Solution
{
    public class ProjectSolutionParser : AbstractSolutionParserAlgorithm
    {
        public override List<Dictionary<string, object>> Parse(FileInfo solutionFile)
        {

            List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();
            NMaven.ProjectImporter.SlnParser.Model.Solution solution = SolutionFactory.GetSolution(solutionFile);


            foreach (Project project in solution.Projects)
            {
                Dictionary<string, object> dictionary = new Dictionary<string, object>();


                dictionary.Add("ProjectTypeGuid", project.ProjectTypeGUID);
                dictionary.Add("ProjectType", VisualStudioProjectType.GetVisualStudioProjectType(project.ProjectTypeGUID));

                dictionary.Add("ProjectName", project.ProjectName);
                dictionary.Add("ProjectPath", project.ProjectPath);
                dictionary.Add("ProjectGUID", project.ProjectGUID);

                string fullpath = Path.Combine(solutionFile.DirectoryName, project.ProjectPath);
                dictionary.Add("ProjectFullPath", fullpath);






                // this is for web projects
                if ((VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Web_Site)
                {

                    string[] assemblies = GetWebConfigAssemblies(Path.Combine(fullpath, "web.config"));
                    dictionary.Add("WebConfigAssemblies", assemblies);

                    string[] binAssemblies = GetBinAssemblies(Path.Combine(fullpath, @"bin"));
                    dictionary.Add("BinAssemblies", binAssemblies);

                    //ParseInnerData(dictionary, match.Groups["projectInnerData"].ToString());
                    ParseProjectReferences(dictionary, project, solution);
                }
                // this is for normal projects
                else if (
                    (VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Windows__CSharp
                    || (VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Windows__VbDotNet
                    )
                {
                    Microsoft.Build.BuildEngine.Project prj = new Microsoft.Build.BuildEngine.Project(BUILD_ENGINE);
                    prj.Load(fullpath);
                    //ParseInnerData(dictionary, match.Groups["projectInnerData"].ToString());
                    dictionary.Add("Project", prj);
                }


                list.Add(dictionary);


            }

            return list;
        }



        protected void ParseProjectReferences(Dictionary<string, object> dictionary, Project project, NMaven.ProjectImporter.SlnParser.Model.Solution solution)
        {
            if (project.ProjectSections != null)
            {
                List<Microsoft.Build.BuildEngine.Project> projectReferenceList = new List<Microsoft.Build.BuildEngine.Project>();
                foreach (ProjectSection ps in project.ProjectSections)
                {
                    if ("WebsiteProperties".Equals(ps.Name))
                    {
                        // ProjectReferences = "{11F2FCC8-5941-418A-A0E7-42D250BA9D21}|SampleInterProject111.dll;{9F37BA7B-06F9-4B05-925D-B5BC16322E8B}|BongClassLib.dll;"

                        Regex regex = new Regex(PROJECT_REFERENCE_REGEX, RegexOptions.Multiline | RegexOptions.IgnoreCase);
                        MatchCollection matches = regex.Matches(ps.Map["ProjectReferences"]);


                        foreach (Match match in matches)
                        {
                            string projectReferenceGUID = match.Groups["ProjectReferenceGUID"].ToString();
                            string projectReferenceDll = match.Groups["ProjectReferenceDll"].ToString();
                            string projectReferenceName = null;
                            string projectReferencePath = null;
                            string projectReferenceFullPath = null;

                            Microsoft.Build.BuildEngine.Project prj = GetMSBuildProject(solution, projectReferenceGUID);
                            if (prj != null)
                            {
                                projectReferenceList.Add(prj);
                            }
                        }




                    }
                    else if("ProjectDependencies".Equals(ps.Name))
                    {
                        // TODO: implemtation here

                        //{0D80BE11-F1CE-409E-B9AC-039D3801209F} = {0D80BE11-F1CE-409E-B9AC-039D3801209F}

                        foreach (string key in ps.Map.Keys)
                        {
                            Microsoft.Build.BuildEngine.Project prj = GetMSBuildProject(solution, "{" + key + "}");
                            if (prj != null)
                            {
                                projectReferenceList.Add(prj);
                            }
                        }

                    }
                }

                dictionary.Add("InterProjectReferences", projectReferenceList.ToArray());
            }


            
        }

        Microsoft.Build.BuildEngine.Project GetMSBuildProject(NMaven.ProjectImporter.SlnParser.Model.Solution solution, string projectGuid)
        {

            foreach (Project p in solution.Projects)
            {

                if (p.ProjectGUID.Equals("{" + projectGuid + "}", StringComparison.OrdinalIgnoreCase))
                {
                    string projectReferenceName = p.ProjectName;
                    string projectReferencePath = p.ProjectPath;
                    string projectReferenceFullPath = null;

                    if (Path.IsPathRooted(projectReferencePath))
                    {
                        projectReferenceFullPath = Path.GetFullPath(projectReferencePath);
                    }
                    else
                    {
                        projectReferenceFullPath = Path.Combine(solution.File.Directory.FullName, projectReferencePath);
                    }


                    Microsoft.Build.BuildEngine.Project prj = new Microsoft.Build.BuildEngine.Project(BUILD_ENGINE);
                    prj.Load(projectReferenceFullPath);

                    return prj;

                }
            }

            return null;
        }


    }
}
