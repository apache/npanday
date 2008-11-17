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
using System.IO;
using System.Reflection;
using System.Text;
using System.Xml.Serialization;

using Microsoft.Build.BuildEngine;

using NMaven.Artifact;
using NMaven.Solution;
using NMaven.Model.Pom;

namespace NMaven.Solution.Impl
{
	/// <summary>
	/// Implementation of the IProjectGenerator.
	/// </summary>
	internal sealed class ProjectGeneratorImpl : IProjectGenerator
	{
        private Dictionary<String, String> directoryToFileNameExtensionMapping;

        private Dictionary<String, String> directoryToImportProject;
		
        /// <summary>
        /// Constructor
        /// </summary>
		internal ProjectGeneratorImpl()
		{
            directoryToFileNameExtensionMapping = new Dictionary<string,string>();
            directoryToFileNameExtensionMapping.Add("csharp", ".csproj");
            directoryToFileNameExtensionMapping.Add("vb", ".vbproj");

            directoryToImportProject = new Dictionary<string, string>();
            directoryToImportProject.Add("csharp", @"$(MSBuildBinPath)\Microsoft.CSharp.Targets");
            directoryToImportProject.Add("vb", @"$(MSBuildBinPath)\Microsoft.VisualBasic.Targets");
		}
		
	    public IProjectReference GenerateProjectFor(NMaven.Model.Pom.Model model, 
		                                  DirectoryInfo sourceFileDirectory,
		                                  String projectFileName,
		                                  ICollection<IProjectReference> projectReferences,
		                                  DirectoryInfo localRepository,
                                          DirectoryInfo currentDirectory)
	    {		
			Guid projectGuid = Guid.NewGuid();

            if (projectReferences == null)
            {
                projectReferences = new List<IProjectReference>();
            }
            

			Project project = GetProjectFromPomModel(model, 
			                                         sourceFileDirectory,
			                                         projectFileName, 
			                                         projectGuid,
			                                         currentDirectory + @"\target\bin\Debug\", 
			                                         currentDirectory + @"\target\obj\",
			                                         projectReferences,
			                                         localRepository);
            String fileNameExtension = directoryToFileNameExtensionMapping[sourceFileDirectory.Name];
            FileInfo fileInfo = new FileInfo(sourceFileDirectory.FullName + @"\" + projectFileName + fileNameExtension);
		    project.Save(fileInfo.FullName);

            IProjectReference projectReference = Factory.createDefaultProjectReference();
		    projectReference.ProjectFile = fileInfo;
		    projectReference.ProjectGuid = projectGuid;
		    projectReference.ProjectName = projectFileName;
			return projectReference;	    	
	    }

        public void GenerateSolutionFor(FileInfo fileInfo, ICollection<IProjectReference> projectReferences)
		{
			TextWriter writer = 
				new StreamWriter(fileInfo.FullName, false, System.Text.Encoding.UTF8);
			writer.WriteLine("");
			writer.WriteLine("Microsoft Visual Studio Solution File, Format Version 9.00");
			writer.WriteLine("# Visual Studio 2005");
			writer.WriteLine("# SharpDevelop 2.1.0.2376");

			Guid solutionGuid = Guid.NewGuid();
			foreach(IProjectReference projectReference in projectReferences) 
			{
                string projectType = null;
                string projectFileExtension = projectReference.ProjectFile.Extension;
                if (projectFileExtension == ".csproj")
                {
                    projectType = "FAE04EC0-301F-11D3-BF4B-00C04F79EFBC";
                }
                else if (projectFileExtension == ".vbproj")
                {
                    projectType = "F184B08F-C81C-45F6-A57F-5ABD9991F28F";
                }

                writer.Write("Project(\"{");
                writer.Write(projectType);
				writer.Write("}\") = \"");
				writer.Write(projectReference.ProjectName);
				writer.Write("\", \"");
				writer.Write(projectReference.ProjectFile.FullName);
				writer.Write("\", \"{");
				writer.Write(projectReference.ProjectGuid.ToString());
				writer.WriteLine("}\"");
				writer.WriteLine("EndProject");
				
			}
			writer.Flush();
			writer.Close();
			Console.WriteLine("NMAVEN-000-000: Generate solution file: File Name = " + fileInfo.FullName);
		}
					
		public NMaven.Model.Pom.Model CreatePomModelFor(String fileName)
		{
			TextReader reader = new StreamReader(fileName);
		    XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Pom.Model));
			return (NMaven.Model.Pom.Model) serializer.Deserialize(reader);	
		}
		
        /// <summary>
        /// Returns a project binding (xmlns="http://schemas.microsoft.com/developer/msbuild/2003") from the given model 
        /// (pom.xml) file
        /// </summary>
        /// <param name="model">the model binding for a pom.xml file</param>
        /// <param name="sourceFileDirectory">the directory containing the source files</param>
        /// <param name="assemblyName">the name of the assembly: often corresponds to the artifact id from the pom</param>
        /// <param name="projectGuid">the GUID of the project</param>
        /// <param name="assemblyOutputPath">directory where the IDE output files are placed</param>
        /// <param name="baseIntermediateOutputPath">directory where the IDE output files are placed</param>
        /// <param name="projectReferences">references to other projects that this project is dependent upon</param>
        /// <returns>Returns a project binding for the specified model</returns>
		private Project GetProjectFromPomModel(NMaven.Model.Pom.Model model, 
		                                       DirectoryInfo sourceFileDirectory,
		                                       String assemblyName,
		                                       Guid projectGuid,
		                                       String assemblyOutputPath,
		                                       String baseIntermediateOutputPath,
                                               ICollection<IProjectReference> projectReferences,
                                               DirectoryInfo localRepository)
		{
			if(model == null || sourceFileDirectory == null)
			{
				throw new ExecutionException("NMAVEN-000-000: Missing required parameter.");
			}
            Engine engine = new Engine(Environment.GetEnvironmentVariable("SystemRoot") + @"\Microsoft.NET\Framework\v2.0.50727");
            Project project = new Project(engine);

            string outputType = GetOutputType(model.packaging, "test".Equals(sourceFileDirectory.Parent.Name));

            Console.WriteLine("ProjectGuid = " + projectGuid.ToString() + ", RootNameSpace = " +
                model.groupId + ", AssemblyName = " + assemblyName + ", BaseIntPath = " +
                baseIntermediateOutputPath + ", OutputType = " + outputType + 
                ", Packaging = " + model.packaging);
            //Main Properties
            BuildPropertyGroup groupProject = project.AddNewPropertyGroup(false);
            groupProject.AddNewProperty("ProjectGuid", "{" + projectGuid.ToString() + "}");
            BuildProperty buildProperty = groupProject.AddNewProperty("Configuration", "Debug");
            buildProperty.Condition = " '$(Configuration)' == '' ";
            groupProject.AddNewProperty("RootNameSpace", model.groupId);
            groupProject.AddNewProperty("AssemblyName", assemblyName);
            groupProject.AddNewProperty("BaseIntermediateOutputPath", baseIntermediateOutputPath);
            groupProject.AddNewProperty("OutputType", outputType);
            
            //Debug Properties
            groupProject = project.AddNewPropertyGroup(false);
            buildProperty.Condition = " '$(Configuration)' == '' ";
            groupProject.AddNewProperty( "OutputPath", assemblyOutputPath, false);

            project.AddNewImport(directoryToImportProject[sourceFileDirectory.Name], null);
            DirectoryInfo configDirectory = new DirectoryInfo(Environment.CurrentDirectory + @"\src\main\config");
            if(configDirectory.Exists)
            {
            	BuildItemGroup configGroup = project.AddNewItemGroup();
            	foreach(FileInfo fileInfo in configDirectory.GetFiles())
            	{
            		if(fileInfo.Extension.Equals("exe.config"))
            		{
            			configGroup.AddNewItem("None", @"src\main\config\" + fileInfo.Name);
            		}
            	}
            }
            AddProjectDependencies(project, model, sourceFileDirectory, localRepository);
            AddFoldersToProject(project, null, sourceFileDirectory, sourceFileDirectory);
            AddClassFilesToProject(project, null, sourceFileDirectory, sourceFileDirectory);
            AddProjectReferences(project, assemblyName, projectReferences);            
            AddProjectResource(project, null, sourceFileDirectory);
			return project;
			
		}

        private void AddProjectReferences(Project project, String projectName, ICollection<IProjectReference> projectReferences)
		{
			BuildItemGroup itemGroup = project.AddNewItemGroup();
			foreach(IProjectReference projectReference in projectReferences)
			{
				BuildItem buildItem = itemGroup.AddNewItem("ProjectReference", projectReference.ProjectFile.FullName);
				buildItem.SetMetadata("Project", "{" + projectReference.ProjectGuid.ToString() + "}");
				buildItem.SetMetadata("Name", projectName);		
			}
		}
				
		private void AddFoldersToProject(Project project, BuildItemGroup folderGroup, DirectoryInfo rootDirectory, 
            DirectoryInfo sourceFileDirectory) 
		{
            DirectoryInfo[] directoryInfos = rootDirectory.GetDirectories();
            if(directoryInfos != null && directoryInfos.Length > 0)
            {              	
            	if(folderGroup == null) folderGroup = project.AddNewItemGroup();
            	
            	foreach(DirectoryInfo di in directoryInfos) 
            	{
              		if(di.FullName.Contains(".svn") || di.FullName.Contains(@"obj") || di.FullName.Contains(@"bin"))
    					continue;   
            		folderGroup.AddNewItem("Folder", di.FullName.Substring(sourceFileDirectory.FullName.Length));
                	AddFoldersToProject(project, folderGroup, di, sourceFileDirectory);
            	}           	
            }			
		}
		
		private void AddClassFilesToProject(Project project, BuildItemGroup compileGroup, DirectoryInfo rootDirectory, 
            DirectoryInfo sourceFileDirectory) 
		{
	        DirectoryInfo[] directoryInfos = rootDirectory.GetDirectories(); 

            // include classes directly inside  sourceDirectory
            if (rootDirectory.FullName.Equals(sourceFileDirectory.FullName))
            {
                foreach (FileInfo fileInfo in rootDirectory.GetFiles())
                { 
                    if (fileInfo.FullName.EndsWith(".cs", false, null) || fileInfo.FullName.EndsWith(".vb", false, null))
                    {
                        if (compileGroup == null)
                        {
                            compileGroup = project.AddNewItemGroup();
                        }

                        BuildItem buildItem =
                            compileGroup.AddNewItem("Compile",
                                                    fileInfo.FullName.Substring(sourceFileDirectory.FullName.Length));
                    }
                }
            }

            if(directoryInfos != null && directoryInfos.Length > 0)
            {
                if (compileGroup == null)
                {
                    compileGroup = project.AddNewItemGroup();
                }
            	
            	foreach(DirectoryInfo di in directoryInfos) 
            	{
                    if (di.FullName.Contains(".svn") || di.FullName.Contains("obj") || di.FullName.Contains("bin"))
                    {
                        continue; 
                    }					      			
	            	foreach(FileInfo fileInfo in di.GetFiles()) 
	            	{
	            		BuildItem buildItem = 
	            			compileGroup.AddNewItem("Compile", 
	            			                        fileInfo.FullName.Substring(sourceFileDirectory.FullName.Length));
	            	}            		
                	AddClassFilesToProject(project, compileGroup, di, sourceFileDirectory);
            	}           	
            }				
		}

        private void AddProjectResource(Project project, BuildItemGroup resourceGroup, DirectoryInfo sourceFileDirectory)
        {
            if (resourceGroup == null)
            {
                resourceGroup = project.AddNewItemGroup();
            }

            foreach (FileInfo  file  in sourceFileDirectory.GetFiles())
            {                
                if (file.FullName.ToLower().Contains(".resx"))
                {
                    Console.WriteLine("adding EmbeddedResource : " + file.Name);
                    BuildItem buildItem = resourceGroup.AddNewItem("EmbeddedResource", file.Name);

                    buildItem.SetMetadata("SubType", "Designer", false);
                }
            }            
        }
		
		private void AddProjectDependencies(Project project, NMaven.Model.Pom.Model model, DirectoryInfo sourceFileDirectory,
		    DirectoryInfo localRepository)
		{
			BuildItemGroup group = project.AddNewItemGroup();
			group.AddNewItem("Reference", "System.Xml");
			if(model.dependencies != null) 
			{
			    ArtifactContext artifactContext = new ArtifactContext();
				foreach(Dependency dependency in model.dependencies)
				{
					//String artifactExtension = (dependency.type == "module") ? "dll" : GetExtension(dependency.type);
					NMaven.Artifact.Artifact dependencyArtifact = artifactContext.CreateArtifact(dependency.groupId,
					    dependency.artifactId, dependency.version, dependency.type);

					String repoPath = PathUtil.GetUserAssemblyCacheFileFor(dependencyArtifact, localRepository).FullName;
					BuildItem buildItem = group.AddNewItem("Reference", dependency.artifactId);
					//TODO: Fix this. Just because it is in the GAC on the system that builds the .csproj does not mean 
					//it is in the GAC on another system. 
					
					if("system".Equals(dependency.scope))
					{
						buildItem.SetMetadata("HintPath", dependency.systemPath, false);
					}
                    else if (!dependency.GetType().Equals("gac") && !IsInGac(dependency.artifactId))
                    {
                        buildItem.SetMetadata("HintPath", repoPath, false);
                    }
				}				
			}

	        DirectoryInfo[] directoryInfos = sourceFileDirectory.GetDirectories();
            
            ClassParser classParser = new ClassParser();
            List<FileInfo> fileInfos = new List<FileInfo>();
            AddFileInfosFromSourceDirectories(sourceFileDirectory, fileInfos);
            List<String> dependencies = classParser.GetDependencies(fileInfos);
            foreach(String dependency in dependencies)
            {
            	try {
                    String assembly = GetAssemblyFor(dependency);
                    if(IsInGac(assembly)) {
            			group.AddNewItem("Reference", assembly);	
            		} 
            	}
            	catch(Exception e) 
            	{
            		Console.WriteLine("NMAVEN-000-000: Could not find assembly dependency", e.Message);
            	}
            }
		}
		
		private bool IsInGac(String assembly)
		{
			return new DirectoryInfo(Environment.GetEnvironmentVariable("SystemRoot")
			    + @"\assembly\GAC_MSIL\" + assembly).Exists;		
		}

        private String GetAssemblyFor(String dependency)
        {
            return (dependency.Trim().Equals("System.Resources")) ? "System.Windows.Forms" : dependency;
        }
		
		private void AddFileInfosFromSourceDirectories(DirectoryInfo sourceFileDirectory, List<FileInfo> fileInfos ) 
		{
            DirectoryInfo[] directoryInfos = sourceFileDirectory.GetDirectories();
            if(directoryInfos != null && directoryInfos.Length > 0)
            {  	
            	foreach(DirectoryInfo di in directoryInfos) 
            	{
                    if (di.FullName.Contains(".svn") || di.FullName.Contains("obj") || di.FullName.Contains("bin"))
                    {
                        continue;
                    }
              		fileInfos.AddRange(di.GetFiles());
              		AddFileInfosFromSourceDirectories(di, fileInfos);
            	}           	
            }
		}
		
		private String GetOutputType(String type,bool isATest)
        {
            if (type.Equals("library") || type.Equals("netplugin") || type.Equals("visual-studio-addin")
                || type.Equals("sharp-develop-addin") || type.Equals("nar")) return "Library";
            else if (type.Equals("exe")) return isATest ? "Library" : "Exe";
            else if (type.Equals("winexe")) return "WinExe";
            else if (type.Equals("module")) return "Module";
            return null;
        }
		
		private String GetExtension(String type)
		{
			if (type.Equals("library") || type.Equals("netplugin") ) return "dll";
			else if (type.Equals("exe")) return "exe";
			else if (type.Equals("winexe")) return "exe";
			else if (type.Equals("module")) return "netmodule";
			return null;
		}				
		
		private class ClassParser {
			
			public List<String> GetDependencies(List<FileInfo> fileInfos) 
			{
				List<String> dependencies = new List<String>();
				foreach(FileInfo fileInfo in fileInfos) 
				{
					try 
			        {
			            using (StreamReader sr = new StreamReader(fileInfo.FullName)) 
			            {
			                String line;
			                while ((line = sr.ReadLine()) != null) 
			                {
			                	if (line.StartsWith("namespace")) break;
			                	if (line.StartsWith("//")) continue;
			                	if (line.StartsWith("using")) {
			                		String[] tokens = line.Remove(line.Length - 1).Split(new char[1]{' '});
			                		if(!dependencies.Contains(tokens[1]))
			                		{
			                			dependencies.Add(tokens[1]);
			                		}			                		
			                	}
			                }
			            }
			        }
			        catch (Exception e) 
			        {
			            Console.WriteLine(e.Message);
			        }
				}
				return dependencies;
			}		
		}		
	}
}
