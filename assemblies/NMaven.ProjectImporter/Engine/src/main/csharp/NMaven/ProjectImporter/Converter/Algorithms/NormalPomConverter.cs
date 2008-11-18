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

using NMaven.ProjectImporter.Validator;


/// Author: Leopoldo Lee Agdeppa III


namespace NMaven.ProjectImporter.Converter.Algorithms
{
    public class NormalPomConverter : AbstractPomConverter
    {

        public NormalPomConverter(ProjectDigest projectDigest, string mainPomFile, NMaven.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }



        public override void ConvertProjectToPomModel(bool writePom)
        {

            
            GenerateHeader("library");


            Model.build.sourceDirectory = GetSourceDir();

            // Add NMaven compile plugin 
            Plugin compilePlugin = AddPlugin(
                "org.apache.maven.dotnet.plugins",
                "maven-compile-plugin",
                null,
                true
            );
            

            if(projectDigest.Language.Equals("vb",StringComparison.OrdinalIgnoreCase))
            {
                AddPluginConfiguration(compilePlugin, "language", "VB");
            }

            AddPluginConfiguration(compilePlugin, "rootNamespace", projectDigest.RootNamespace);
            AddPluginConfiguration(compilePlugin, "main", projectDigest.StartupObject);
            AddPluginConfiguration(compilePlugin, "doc", projectDigest.DocumentationFile);
            //AddPluginConfiguration(compilePlugin, "noconfig", "true");
            AddPluginConfiguration(compilePlugin, "imports", "import", projectDigest.GlobalNamespaceImports);


            // add include list for the compiling
            DirectoryInfo baseDir = new DirectoryInfo(Path.GetDirectoryName(projectDigest.FullFileName));
            List<string> compiles = new List<string>();
            foreach (Compile compile in projectDigest.Compiles)
            {
                string compilesFile = NMavenPomHelperUtility.GetRelativePath(baseDir, new FileInfo(compile.IncludeFullPath));
                compiles.Add(compilesFile);
            }
            AddPluginConfiguration(compilePlugin, "includeSources", "includeSource", compiles.ToArray());


            string define = GetDefineConfigurationValue();
            if(!string.IsNullOrEmpty(define))
            {
                AddPluginConfiguration(compilePlugin, "define", define);
            }

            if ("true".Equals(projectDigest.SignAssembly, StringComparison.OrdinalIgnoreCase)
                && !string.IsNullOrEmpty(projectDigest.AssemblyOriginatorKeyFile)
                )
            {
                if (Path.IsPathRooted(projectDigest.AssemblyOriginatorKeyFile))
                {
                    AddPluginConfiguration(compilePlugin, "keyfile", NMavenPomHelperUtility.GetRelativePath(baseDir, new FileInfo(projectDigest.AssemblyOriginatorKeyFile)));
                }
                else
                {
                    AddPluginConfiguration(compilePlugin, "keyfile", NMavenPomHelperUtility.GetRelativePath(baseDir, new FileInfo(baseDir.FullName + @"\" + projectDigest.AssemblyOriginatorKeyFile)));
                }
                
            }
            
            
            // add integration test plugin if project is a test
            if (projectDigest.UnitTest)
            {
                Plugin testPlugin = AddPlugin(
                    "org.apache.maven.dotnet.plugins",
                    "maven-test-plugin",
                    null,
                    true
                );
                AddPluginConfiguration(testPlugin, "integrationTest", "true");
                
            }

            // Add Com Reference Dependencies
            if (projectDigest.ComReferenceList.Length > 0)
            {
                AddComReferenceDependency();
            }
            

			//Add Project WebReferences
            AddWebReferences();

            // Add Project Inter-dependencies
            AddInterProjectDependenciesToList();


            // filter the rsp included assemblies
            FilterRSPIncludedReferences();
            // Add Project Reference Dependencies
            AddProjectReferenceDependenciesToList();

            
            if (writePom)
            {
                NMavenPomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }

        }



        protected void FilterRSPIncludedReferences()
        {
            List<Reference> list = new List<Reference>();

            foreach (Reference reference in projectDigest.References)
            {
                if (!string.IsNullOrEmpty(projectDigest.Language))
                {
                    if (!gacUtil.IsRspIncluded(reference.Name, projectDigest.Language))
                    {
                        list.Add(reference);
                    }
                }
                else
                {
                    list.Add(reference);
                }
            }
            projectDigest.References = list.ToArray();
        }


    }
}
