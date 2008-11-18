using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;

using Microsoft.Build.BuildEngine;

using NMaven.ProjectImporter.Parser.VisualStudioProjectTypes;
using System.Windows.Forms;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Parser.Solution
{
    public abstract class AbstractSolutionParserAlgorithm : ISolutionParserAlgorithm
    {

        protected static string INNERDATA_REGEX;

        protected static string PROJECT_REFERENCE_REGEX;

        protected static Engine BUILD_ENGINE;

        static AbstractSolutionParserAlgorithm()
        {
            INNERDATA_REGEX = @"^\s*(?<Key>.*)\s*=\s*""(?<Value>.*)""\s*$";

            PROJECT_REFERENCE_REGEX = @"({(?<ProjectReferenceGUID>([^\}])*)}\|(?<ProjectReferenceDll>([^;])*);)";



            // gets the directory path of mscorlib using the System.String Type Assembly path
            string msBuildPath = Path.GetDirectoryName(System.Reflection.Assembly.GetAssembly(typeof(string)).Location);
            BUILD_ENGINE = new Engine(msBuildPath);
        }



        public abstract List<Dictionary<string, object>> Parse(System.IO.FileInfo solutionFile);
        

        protected void ParseInnerData(Dictionary<string, object> dictionary, string innerData)
        {
            Regex regex = new Regex(INNERDATA_REGEX, RegexOptions.Multiline | RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(innerData);


            foreach (Match match in matches)
            {
                dictionary.Add(match.Groups["Key"].ToString().Trim(), match.Groups["Value"].ToString());
            }


        }



        protected void ParseProjectReferences(Dictionary<string, object> dictionary, string contents, FileInfo solutionFile)
        {
            if (!dictionary.ContainsKey("ProjectReferences"))
            {
                return;
            }

            Regex regex = new Regex(PROJECT_REFERENCE_REGEX, RegexOptions.Multiline | RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(dictionary["ProjectReferences"].ToString());

            List<Project> projectReferenceList = new List<Project>();


            foreach (Match match in matches)
            {
                string projectReferenceGUID = match.Groups["ProjectReferenceGUID"].ToString();
                string projectReferenceDll = match.Groups["ProjectReferenceDll"].ToString();
                string projectReferenceName = null;
                string projectReferencePath = null;
                string projectReferenceFullPath = null;



                string find_project_reference = @"\s*Project\(""{([^}])*}""\)\s*=\s*""(?<ProjectName>([^""])*)""\s*,\s*""(?<ProjectPath>([^""])*)""\s*,\s*""{"
                    + projectReferenceGUID
                    + @"}""\s*";


                Regex regex2 = new Regex(find_project_reference, RegexOptions.Multiline | RegexOptions.IgnoreCase);
                MatchCollection matches2 = regex2.Matches(contents);

                foreach (Match match2 in matches2)
                {
                    projectReferenceName = match2.Groups["ProjectName"].ToString();
                    projectReferencePath = match2.Groups["ProjectPath"].ToString();

                    if (Path.IsPathRooted(projectReferencePath))
                    {
                        projectReferenceFullPath = Path.GetFullPath(projectReferencePath);
                    }
                    else
                    {
                        projectReferenceFullPath = Path.Combine(solutionFile.DirectoryName, projectReferencePath);
                    }


                    Project project = new Project(BUILD_ENGINE);
                    project.Load(projectReferenceFullPath);

                    projectReferenceList.Add(project);

                }

            }

            dictionary.Add("InterProjectReferences", projectReferenceList.ToArray());
        }




        protected string[] GetWebConfigAssemblies(string webconfig)
        {
            List<string> list = new List<string>();

            string xpath_expr = @"//configuration/system.web/compilation/assemblies/add";

            FileInfo webConfigFile = new FileInfo(webconfig);

            if (!webConfigFile.Exists)
            {
                // return empty string array
                return list.ToArray();
            }


            XmlDocument xmldoc = new System.Xml.XmlDocument();
            xmldoc.Load(webConfigFile.FullName);

            XmlNodeList valueList = xmldoc.SelectNodes(xpath_expr);

            foreach (System.Xml.XmlNode val in valueList)
            {
                string assembly = val.Attributes["assembly"].Value;

                if (!string.IsNullOrEmpty(assembly))
                {
                    list.Add(assembly);
                }

            }



            return list.ToArray();

        }


        protected String[] GetBinAssemblies(string webBinDir)
        {
            List<string> list = new List<string>();

            DirectoryInfo dir = new DirectoryInfo(webBinDir);

            if (!dir.Exists)
            {
                // return an empty array string
                return list.ToArray();
            }

            foreach (FileInfo dll in dir.GetFiles("*.dll"))
            {
                list.Add(dll.FullName);

            }



            return list.ToArray();



        }
    
        

    }
}
