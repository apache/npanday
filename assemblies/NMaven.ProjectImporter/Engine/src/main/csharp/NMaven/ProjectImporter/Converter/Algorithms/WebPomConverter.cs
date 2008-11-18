using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.IO;

using NMaven.ProjectImporter.Digest;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.Utils;
using NMaven.Model.Pom;

using NMaven.Artifact;

using System.Reflection;

using NMaven.ProjectImporter.Converter;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Converter.Algorithms
{
    public class WebPomConverter : AbstractPomConverter
    {

        public WebPomConverter(ProjectDigest projectDigest, string mainPomFile, NMaven.Model.Pom.Model parent, string groupId) 
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

		
        public override void ConvertProjectToPomModel(bool writePom)
        {
            GenerateHeader("asp");


            Model.build.sourceDirectory = ".";
            
			
            // Add NMaven compile plugin 
            Plugin aspxPlugin = AddPlugin("org.apache.maven.dotnet.plugins", "maven-aspx-plugin");

			//Add Project WebReferences
            AddWebReferences();


            // Add Project Inter-dependencies
            AddInterProjectDependenciesToList();


            // Add Project Reference Dependencies
            AddProjectReferenceDependenciesToList();


            if (writePom)
            {
                NMavenPomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }

        }


        // override to insert only an nmaven artifact
        protected override void AddProjectReferenceDependency(Reference reference)
        {

            Dependency refDependency = ResolveDependency(reference);
            if (refDependency == null)
            {
                return;
            }

            if (!"library".Equals(refDependency.type, StringComparison.OrdinalIgnoreCase))
            {
                // ignore gac if already in the RSP 
                if (gacUtil.IsRspIncluded(refDependency.artifactId, projectDigest.Language))
                {
                    return;
                }

                // insert only NMaven Artifact
                AddDependency(refDependency);
                return;
            }

        }

    }
}
