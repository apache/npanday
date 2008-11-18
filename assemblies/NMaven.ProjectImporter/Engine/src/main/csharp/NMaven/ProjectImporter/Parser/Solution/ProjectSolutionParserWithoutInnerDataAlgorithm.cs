using System;
using System.Collections.Generic;
using System.Text;

using System.IO;

using System.Text.RegularExpressions;
using NMaven.ProjectImporter.Parser.VisualStudioProjectTypes;
using Microsoft.Build.BuildEngine;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Parser.Solution
{
    class ProjectSolutionParserWithoutInnerDataAlgorithm : AbstractSolutionParserAlgorithm
    {

        private static string REGEX;
        
        static ProjectSolutionParserWithoutInnerDataAlgorithm()
        {
            REGEX = GetRegexString();

            
        }

        public static string GetRegexString()
        {
            StringBuilder strNormalRegex = new StringBuilder();
            strNormalRegex.Append(@"\s*Project");
            strNormalRegex.Append(@"\(""{(?<ProjectTypeGuid>([^}])*)}""\)");
            strNormalRegex.Append(@"\s*=\s*");
            strNormalRegex.Append(@"""(?<ProjectName>([^""])*)""");
            strNormalRegex.Append(@",\s*");
            strNormalRegex.Append(@"""(?<ProjectPath>([^""])*)""");
            strNormalRegex.Append(@",\s*");
            strNormalRegex.Append(@"""{(?<ProjectGUID>([^}])*)}""\s*");

            strNormalRegex.Append(@"(\n|\s*)*");
            strNormalRegex.Append(@"\n\s*EndProject");

            return strNormalRegex.ToString();
        }

        #region ISolutionParserAlgorithm Members

        public override List<Dictionary<string, object>> Parse(FileInfo solutionFile)
        {
            List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();

            StreamReader sr = new StreamReader(solutionFile.FullName);
            string contents = sr.ReadToEnd();
            sr.Close();


            Regex regex = new Regex(REGEX, RegexOptions.Multiline | RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(contents);


            foreach (Match match in matches)
            {
                Dictionary<string, object> dictionary = new Dictionary<string, object>();


                string projectTypeGuid = match.Groups["ProjectTypeGuid"].ToString();
                dictionary.Add("ProjectTypeGuid", projectTypeGuid);
                dictionary.Add("ProjectType", VisualStudioProjectType.GetVisualStudioProjectType(projectTypeGuid));

                dictionary.Add("ProjectName", match.Groups["ProjectName"].ToString());
                dictionary.Add("ProjectPath", match.Groups["ProjectPath"].ToString());
                dictionary.Add("ProjectGUID", match.Groups["ProjectGUID"].ToString());

                string fullpath = Path.Combine(solutionFile.DirectoryName, match.Groups["ProjectPath"].ToString());
                dictionary.Add("ProjectFullPath", fullpath);


                //Project project = new Project(BUILD_ENGINE);
                //project.Load(fullpath);

                //dictionary.Add("Project", project);



                // this is for web projects
                if ((VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Web_Site)
                {

                    string[] assemblies = GetWebConfigAssemblies(Path.Combine(fullpath, "web.config"));
                    dictionary.Add("WebConfigAssemblies", assemblies);

                    string[] binAssemblies = GetBinAssemblies(Path.Combine(fullpath, @"bin"));
                    dictionary.Add("BinAssemblies", binAssemblies);

                    ParseInnerData(dictionary, match.Groups["projectInnerData"].ToString());
                    ParseProjectReferences(dictionary, contents, solutionFile);
                }
                // this is for normal projects
                else if (
                    (VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Windows__CSharp
                    || (VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Windows__VbDotNet
                    )
                {
                    Project project = new Project(BUILD_ENGINE);
                    project.Load(fullpath);
                    ParseInnerData(dictionary, match.Groups["projectInnerData"].ToString());
                    dictionary.Add("Project", project);
                }


                list.Add(dictionary);


            }


            return list;
        }

        #endregion
    }
}
