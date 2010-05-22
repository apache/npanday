using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.IO;

using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;
using NPanday.Model.Pom;

using NPanday.Artifact;

using System.Reflection;

using NPanday.ProjectImporter.Converter;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class WebPomConverter : AbstractPomConverter
    {

        public WebPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }

		/// <summary>
        /// Adds WebReference as Plugin
        /// Author: Joe Ocaba
        /// </summary>
        private string ParsePath(string path)
        {
            List<string> pathCheck = new List<string>();
            string[] items = path.Split((@"\").ToCharArray());
            string realPath = String.Empty;

            for (int i = 0; i < items.Length; i++)
            {
                if (!String.IsNullOrEmpty(items[i]))
                {
                    if (!pathCheck.Contains(items[i]) || items[i] != ".")
                    {
                        realPath += items[i] + @"\" + @"\";
                        pathCheck.Add(items[i]);
                    }
                }
            }
            return realPath;
        }

        private string[] GetWebReferenceValues()
        {
            string[] items = projectDigest.FullDirectoryName.Split((@"\").ToCharArray());

            string currentDirectory = items[items.Length - 2];

            string realPath = ParsePath(projectDigest.FullDirectoryName);


            string webReferenceFolder = "App_WebReferences";

            string webReferenceDirectory = realPath + webReferenceFolder;

            if(Directory.Exists(webReferenceDirectory))
            {
                List<string> values = new List<string>();
                string[] directories = Directory.GetDirectories(webReferenceDirectory);
                foreach (string directory in directories)
                {
                    string[] files = Directory.GetFiles(directory);
                    foreach (string file in files)
                    {
                        if (file.Contains(".wsdl"))
                        {
                            int webDirIndex = file.IndexOf(currentDirectory);

                            string refHolder = file.Substring(webDirIndex).Replace(@"\", "/");
                            values.Add(refHolder);
                        }
                    }
                }

                return values.ToArray();
            }
            else
            {
                return null;
            }
        }

        private string GetOutputDirectory()
        {
            string[] items = projectDigest.FullDirectoryName.Split((@"\").ToCharArray());

            string currentDirectory = items[items.Length - 2];

            string realPath = ParsePath(projectDigest.FullDirectoryName);


            string webReferenceFolder = "App_WebReferences";

            string webReferenceDirectory = realPath + webReferenceFolder;

            string outputDirectory = "." + "/" + currentDirectory + "/" + webReferenceFolder;

            return outputDirectory;
        }

		
        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            GenerateHeader("asp");


            Model.build.sourceDirectory = ".";

            if (scmTag != null && scmTag != string.Empty && Model.parent == null)
            {
                Scm scmHolder = new Scm();
                scmHolder.connection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.developerConnection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.url = scmTag;

                Model.scm = scmHolder;
            }

            // Add NPanday compile plugin 
            Plugin aspxPlugin = AddPlugin("npanday.plugin", "maven-aspx-plugin");
            if(!string.IsNullOrEmpty(projectDigest.TargetFramework))
                AddPluginConfiguration(aspxPlugin, "frameworkVersion", projectDigest.TargetFramework);

			//Add Project WebReferences
            //AddWebReferences();


            // Add Project Inter-dependencies
            //AddInterProjectDependenciesToList();


            // Add Project Reference Dependencies
            //AddProjectReferenceDependenciesToList();


            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml")), Model);
            }

        }


        // override to insert only an NPanday artifact
        protected override void AddProjectReferenceDependency(Reference reference)
        {

            Dependency refDependency = ResolveDependency(reference);
            if (refDependency == null)
            {
                return;
            }

            if (!("library".Equals(refDependency.type, StringComparison.OrdinalIgnoreCase)
                  || "dotnet-library".Equals(refDependency.type, StringComparison.OrdinalIgnoreCase)))
            {
                // ignore gac if already in the RSP 
                if (gacUtil.IsRspIncluded(refDependency.artifactId, projectDigest.Language))
                {
                    return;
                }

                // insert only NPanday Artifact
                AddDependency(refDependency);
                return;
            }

        }

    }
}
