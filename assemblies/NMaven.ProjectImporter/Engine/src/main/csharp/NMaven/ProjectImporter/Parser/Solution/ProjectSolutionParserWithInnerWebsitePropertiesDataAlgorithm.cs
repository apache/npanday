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
    public class ProjectSolutionParserWithInnerWebsitePropertiesDataAlgorithm : AbstractSolutionParserAlgorithm
    {
        private static string WEB_REGEX;
        
        static ProjectSolutionParserWithInnerWebsitePropertiesDataAlgorithm()
        {
            WEB_REGEX = GetWebRegexString();

        }


        private static string GetWebRegexString()
        {
            StringBuilder strWebRegex = new StringBuilder();
            strWebRegex.Append(@"\s*Project");
            strWebRegex.Append(@"\(""{(?<ProjectTypeGuid>.*)}""\)");
            strWebRegex.Append(@"\s*=\s*");
            strWebRegex.Append(@"""(?<ProjectName>.*)""");
            strWebRegex.Append(@",\s*");
            strWebRegex.Append(@"""(?<ProjectPath>.*)""");
            strWebRegex.Append(@",\s*");
            strWebRegex.Append(@"""{(?<ProjectGUID>.*)}""\s*");
            strWebRegex.Append(@"(\n\s*)*");



            strWebRegex.Append(@"\s*ProjectSection\(WebsiteProperties\)\s*=\s*preProject\s*");
            strWebRegex.Append(@"(?<projectInnerData>((\n\s*.*\s*=\s*"".*""\s*)|(\n\s*))*)");
            strWebRegex.Append(@"\n\s*EndProjectSection\s*");
            strWebRegex.Append(@"(\n\s*)*");


            strWebRegex.Append(@"\nEndProject\s*");

            return strWebRegex.ToString();
        }


        public override List<Dictionary<string, object>> Parse(FileInfo solutionFile)
        {

            List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();


            StreamReader sr = new StreamReader(solutionFile.FullName);
            string contents = sr.ReadToEnd();
            sr.Close();





            Regex regex = new Regex(WEB_REGEX, RegexOptions.Multiline | RegexOptions.IgnoreCase);
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


                // this is for web projects
                if ((VisualStudioProjectTypeEnum)dictionary["ProjectType"] == VisualStudioProjectTypeEnum.Web_Site)
                {

                    string[] assemblies = GetWebConfigAssemblies(Path.Combine(fullpath, "web.config"));
                    dictionary.Add("WebConfigAssemblies", assemblies);

					 //@001 SERNACIO START retrieving webreference
                    Digest.Model.WebReferenceUrl[] webReferences = getWebReferenceUrls(fullpath);
                    dictionary.Add("WebReferencesUrl", webReferences);
                    //@001 SERNACIO END retrieving webreference

					
                    string[] binAssemblies = GetBinAssemblies(Path.Combine(fullpath, @"bin"));
                    dictionary.Add("BinAssemblies", binAssemblies);

                    ParseInnerData(dictionary, match.Groups["projectInnerData"].ToString());
                    ParseProjectReferences(dictionary, contents, solutionFile);
                }
                // this is for normal projects
                else if(
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

		Digest.Model.WebReferenceUrl[] getWebReferenceUrls(string projectPath)
        {
            List<Digest.Model.WebReferenceUrl> returnList = new List<Digest.Model.WebReferenceUrl>();
            string webPath = Path.GetFullPath(Path.Combine(projectPath, "App_WebReferences"));
            if (Directory.Exists(webPath))
            {
                DirectoryInfo dirInfo = new DirectoryInfo(webPath);
                foreach (DirectoryInfo folders in dirInfo.GetDirectories())
                {
                    if (folders.Equals(".svn")) continue;
                    returnList.AddRange(getWebReferenceUrls(folders, "App_WebReferences"));
                }
            }
            return returnList.ToArray();
        }

        Digest.Model.WebReferenceUrl[] getWebReferenceUrls(DirectoryInfo folder, string currentPath)
        {
            string relPath = Path.Combine(currentPath, folder.Name);
            string url = string.Empty;
            List<Digest.Model.WebReferenceUrl> webReferenceUrls = new List<Digest.Model.WebReferenceUrl>();

            FileInfo[] fileInfo = folder.GetFiles("*.discomap");
            if (fileInfo != null && fileInfo.Length > 0)
            {
                System.Xml.XPath.XPathDocument xDoc = new System.Xml.XPath.XPathDocument(fileInfo[0].FullName);
                System.Xml.XPath.XPathNavigator xNav = xDoc.CreateNavigator();
                string xpathExpression = @"DiscoveryClientResultsFile/Results/DiscoveryClientResult[@referenceType='System.Web.Services.Discovery.ContractReference']/@url";
                System.Xml.XPath.XPathNodeIterator xIter = xNav.Select(xpathExpression);
                if (xIter.MoveNext())
                {
                    url = xIter.Current.TypedValue.ToString();
                }
            }
            if(!string.IsNullOrEmpty(url))
            {
                Digest.Model.WebReferenceUrl newWebReferenceUrl = new Digest.Model.WebReferenceUrl();
                newWebReferenceUrl.RelPath = relPath;
                newWebReferenceUrl.UpdateFromURL = url;
                webReferenceUrls.Add(newWebReferenceUrl);
            }
            foreach (DirectoryInfo dirInfo in folder.GetDirectories())
            {
                webReferenceUrls.AddRange(getWebReferenceUrls(dirInfo, relPath));
            }
            return webReferenceUrls.ToArray();
        }



    }
}
