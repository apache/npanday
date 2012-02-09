#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion
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

using NPanday.ProjectImporter.Validator;


/// Author: Leopoldo Lee Agdeppa III


namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class NormalPomConverter : AbstractPomConverter
    {

        public NormalPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }



        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            string packaging = "dotnet-library";
            if (!string.IsNullOrEmpty(projectDigest.OutputType))
            {
                string type = projectDigest.OutputType.ToLower();
                if (npandayTypeMap.ContainsKey(type))
                    packaging = npandayTypeMap[type];
            }
            GenerateHeader(packaging);

            Model.build.sourceDirectory = GetSourceDir();

            //Add SCM Tag
            if (scmTag != null && scmTag != string.Empty && Model.parent==null)
            {
                Scm scmHolder = new Scm();
                scmHolder.connection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.developerConnection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.url = scmTag;

                Model.scm = scmHolder;
            }



            // Add NPanday compile plugin 
            Plugin compilePlugin = AddPlugin(
                "org.apache.npanday.plugins",
                "maven-compile-plugin",
                null,
                true
            );
            if(!string.IsNullOrEmpty(projectDigest.TargetFramework))
                AddPluginConfiguration(compilePlugin, "frameworkVersion", projectDigest.TargetFramework);
            

            if(projectDigest.Language.Equals("vb",StringComparison.OrdinalIgnoreCase))
            {
                AddPluginConfiguration(compilePlugin, "language", "VB");
                AddPluginConfiguration(compilePlugin, "rootNamespace", projectDigest.RootNamespace);
                string define = GetDefineConfigurationValue();
                if (!string.IsNullOrEmpty(define))
                {
                    AddPluginConfiguration(compilePlugin, "define", define);
                }
            }
            
            AddPluginConfiguration(compilePlugin, "main", projectDigest.StartupObject);
            AddPluginConfiguration(compilePlugin, "doc", projectDigest.DocumentationFile);
            //AddPluginConfiguration(compilePlugin, "noconfig", "true");
            AddPluginConfiguration(compilePlugin, "imports", "import", projectDigest.GlobalNamespaceImports);

            // add include list for the compiling
            DirectoryInfo baseDir = new DirectoryInfo(Path.GetDirectoryName(projectDigest.FullFileName));
            List<Dictionary<string, string>> generatedResourceList = new List<Dictionary<string, string>>();
            List<string> compiles = new List<string>();
            bool msBuildPluginAdded = false, resourceAdded = false;
            foreach (Compile compile in projectDigest.Compiles)
            {
                string compilesFile = PomHelperUtility.GetRelativePath(baseDir, new FileInfo(compile.IncludeFullPath));
                compiles.Add(compilesFile);

                // if it's a xaml file, include the auto-generated file in object\Debug\
                if (compilesFile.EndsWith(".xaml.cs") || compilesFile.EndsWith(".xaml.vb"))
                { 
                    //add the MsBuild plugin to auto generate the .g.cs/g.vb files
                    if (!msBuildPluginAdded)
                    {
                        Plugin msBuildPlugin = AddPlugin("org.apache.npanday.plugins", "NPanday.Plugin.Msbuild.JavaBinding", null, false);
                        AddPluginExecution(msBuildPlugin, "compile", "validate");
                        AddPluginConfiguration(msBuildPlugin, "frameworkVersion", ProjectDigest.TargetFramework);
                        msBuildPluginAdded = true;
                    }                    

                    string prefix;
                    //set the path *.g.cs and *.g.vb files depending on target architecture of WPF projects as this changes path under obj folder
                    switch (projectDigest.Platform)
                    {
                        case "AnyCPU":
                            prefix = @"obj\Debug\";
                            break;
                        case "x64":
                            prefix = @"obj\x64\Debug\";
                            break;
                        case "x86":
                            prefix = @"obj\x86\Debug\";
                            break;
                        case "Itanium":
                            prefix = @"obj\Itanium\Debug\";
                            break;
                        default:
                            prefix = @"obj\Debug\";
                            break;
                    }
                    string sub = compilesFile.Substring(compilesFile.Length - 3);
                    compiles.Add(prefix + compilesFile.Replace(".xaml" + sub, ".g" + sub));

                    if (!resourceAdded)
                    {
                        generatedResourceList.Add(createResourceEntry(prefix + projectDigest.RootNamespace + ".g.resources", projectDigest.RootNamespace + ".g"));
                        resourceAdded = true;
                    }
                }
            }
            AddPluginConfiguration(compilePlugin, "includeSources", "includeSource", compiles.ToArray());

            if ("true".Equals(projectDigest.SignAssembly, StringComparison.OrdinalIgnoreCase)
                && !string.IsNullOrEmpty(projectDigest.AssemblyOriginatorKeyFile)
                )
            {
                if (Path.IsPathRooted(projectDigest.AssemblyOriginatorKeyFile))
                {
                    AddPluginConfiguration(compilePlugin, "keyfile", PomHelperUtility.GetRelativePath(baseDir, new FileInfo(projectDigest.AssemblyOriginatorKeyFile)));
                }
                else
                {
                    AddPluginConfiguration(compilePlugin, "keyfile", PomHelperUtility.GetRelativePath(baseDir, new FileInfo(baseDir.FullName + @"\" + projectDigest.AssemblyOriginatorKeyFile)));
                }
                
            }
            
            
            // add integration test plugin if project is a test
            if (projectDigest.UnitTest)
            {
                Plugin testPlugin = AddPlugin(
                    "org.apache.npanday.plugins",
                    "maven-test-plugin",
                    null,
                    false
                );
                AddPluginConfiguration(testPlugin, "integrationTest", "true");

                // for running .net framework 4.0 unit tests add new parameter in order to tell NUnit which runtime to use. If there is a way to get this 
                // parameter from maven-compile-plugin use it
                if (projectDigest.TargetFramework == "4.0")
                {
                    AddPluginConfiguration(testPlugin, "executionFrameworkVersion", "4.0");
                }
            }

            // Add Com Reference Dependencies
            if (projectDigest.ComReferenceList.Length > 0)
            {
                AddComReferenceDependency();
            }
            

			//Add Project WebReferences
            AddWebReferences();

            //Add EmbeddedResources maven-resgen-plugin
            AddEmbeddedResources(generatedResourceList);
            

            // Add Project Inter-dependencies
            AddInterProjectDependenciesToList();


            // filter the rsp included assemblies
            FilterRSPIncludedReferences();
            // Add Project Reference Dependencies
            AddProjectReferenceDependenciesToList();

            
            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml")), Model);
            }

        }



        protected void FilterRSPIncludedReferences()
        {
            List<Reference> list = new List<Reference>();

            foreach (Reference reference in projectDigest.References)
            {
                if (!string.IsNullOrEmpty(projectDigest.Language))
                {
                    if (!rspUtil.IsRspIncluded(reference.Name, projectDigest.Language))
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
