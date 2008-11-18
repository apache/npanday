using System;
using System.Xml;
using System.Collections.Generic;
using System.Text;
using System.IO;

using NMaven.Utils;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.ImporterTests
{
    public class FileUtil
    {
		const string MSG_ERROR_EXPECTEDFILE_NOTFOUND = "The Expected File is not in its location. {0}";
        const string MSG_ERROR_ACTUALFILE_NOTFOUND = "The Pom File is not in its location. {0}";
        const string MSG_ERROR_NOXPATH = "No XPath to cross reference pom file created.";
        const string MSG_EXPECTEDXMLELEMENT_NOTFOUND = "Epected Pom and Actual Pom don't have the same elements.";


        public static bool IsSameFile(string dir1, string dir2)
        {
            string str1 = NMaven.Utils.NMavenPomHelperUtility.NormalizeFileToWindowsStyle(Path.GetFullPath(dir1));
            string str2 = NMaven.Utils.NMavenPomHelperUtility.NormalizeFileToWindowsStyle(Path.GetFullPath(dir2));
            return str1.Equals(str2, StringComparison.OrdinalIgnoreCase);
        }

        public static void DeleteDirectory(string dir)
        {
            DeleteDirectory(new DirectoryInfo(dir));
        }


        public static void DeleteDirectory(DirectoryInfo dir)
        {
            try
            {
                if (dir.Exists)
                {
                    dir.Delete(true);
                }
            }
            catch (Exception e)
            {

                throw new Exception(string.Format("Error In Deleting Directory: {0}", dir.FullName), e);
            }

        }


        public static void CopyDirectory(String source, String destination)
        {
            CpDir(source, destination);
            
        }


        public static void CopyDirectory(DirectoryInfo source, DirectoryInfo destination)
        {
            CpDir(source, destination);
            
        }




        static void CpDir(String source, String destination)
        {

            // argument validation goes here, CBA to do it now

            DirectoryInfo destDir = new DirectoryInfo(destination);
            if (!destDir.Exists)
            {
                destDir.Create();
            }

            DirectoryInfo dir = new DirectoryInfo(source);
            FileInfo[] files = dir.GetFiles();
            foreach (FileInfo filePath in files)
            {
				if (filePath.Name != null && !filePath.Name.EndsWith(".test"))
					filePath.CopyTo(Path.Combine(destination, filePath.Name));
            }



            DirectoryInfo[] subDirectories = dir.GetDirectories();
            foreach (DirectoryInfo dirPath in subDirectories)
            {
                CpDir(Path.Combine(source, dirPath.Name), Path.Combine(destination, dirPath.Name));
            }


        }


        static void CpDir(DirectoryInfo source, DirectoryInfo destination)
        {
            CpDir(source.FullName, destination.FullName);
        }
		
		
		public static string[] GetTestPomFiles(string rootPath, string[] actualPomFileLocations)
        {
            string filename = @"c:\build\shein1.txt";
            List<string> outPut = new List<string>();
            //File.Delete(filename);
            TextWriter w = new StreamWriter(filename, true);
            if (actualPomFileLocations.Length < 1) return outPut.ToArray();

            string basePath = parseBasePath(rootPath, actualPomFileLocations[0]);
            foreach (string actualPomFileLocation in actualPomFileLocations)
            {
                outPut.Add(actualPomFileLocation.Replace(".xml", ".test").Replace(basePath, rootPath));
            }
            return outPut.ToArray();
        }
        static string parseBasePath(string replaceWith, string basePath)
        {
            string returnValue = basePath;
            if (string.IsNullOrEmpty(returnValue)) return returnValue;
            int endIndex = returnValue.LastIndexOf('\\');
            if (endIndex < 0) return returnValue;
            returnValue = returnValue.Substring(0, endIndex + 1);
            return returnValue;
        }
        public static string CrossCheckPomElement(string refLocation, string pomPath, Dictionary<string, string> testXPaths)
        {
            if (!File.Exists(refLocation))
            {
                return string.Format(MSG_ERROR_EXPECTEDFILE_NOTFOUND, refLocation);
            }
            if (!File.Exists(pomPath))
            {
                return string.Format(MSG_ERROR_ACTUALFILE_NOTFOUND, pomPath);
            }
            if (testXPaths == null || testXPaths.Count == 0)
            {
                return MSG_ERROR_NOXPATH;
            }
            XmlDocument xDocRef = new XmlDocument();
            xDocRef.Load(refLocation);
            XmlDocument xDocPom = new XmlDocument();
            xDocPom.Load(pomPath);
            XmlNamespaceManager xmlNamespace = new XmlNamespaceManager(xDocRef.NameTable);

            xmlNamespace.AddNamespace("foo", "http://maven.apache.org/POM/4.0.0");
            xmlNamespace.AddNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
            xmlNamespace.AddNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            foreach (KeyValuePair<string, string> testXPath in testXPaths)
            {
                if (testXPath.Key.Equals("Name") || testXPath.Key.Equals("GroupId"))
                    continue;
                XmlNodeList nodeRefList = xDocRef.SelectNodes(testXPath.Value, xmlNamespace);
                XmlNodeList nodePomList = xDocPom.SelectNodes(testXPath.Value, xmlNamespace);
                if(nodePomList.Count != nodeRefList.Count)
                {
                    return string.Format("Actual number of element NOT equal to Expected number of the element {0}: {1} should be {2}.", testXPath.Key, nodePomList.Count, nodeRefList.Count);
                }

                for (int index = 0; index < nodeRefList.Count; index++)
                {
                    XmlNode nodeRef = nodeRefList[index];
                    XmlNode nodePom = nodePomList[index];

                    string actualValue = nodePom.InnerXml;
                    string expectedValue = nodeRef.InnerXml;
                    if (actualValue != expectedValue)
                    {
                        return string.Format("Actual value NOT equal to Expected value of the element {0}: {1} should be {2}.", nodePom.Name, actualValue, expectedValue);
                    }
                };
            }
            return null;
        }
    }
}
