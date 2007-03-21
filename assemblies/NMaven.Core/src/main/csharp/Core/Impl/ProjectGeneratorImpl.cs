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

using System;
using NMaven.Core;
using NMaven.Model;
using System.IO;
using Microsoft.Build.BuildEngine;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Reflection;
using System.Text;

namespace NMaven.Core.Impl
{
	/// <summary>
	/// Implementation of the IProjectGenerator.
	/// </summary>
	public class ProjectGeneratorImpl : IProjectGenerator
	{
		
        /// <summary>
        /// Constructor
        /// </summary>
		public ProjectGeneratorImpl()
		{
		}
		
	    public IProjectReference GenerateProjectFor(NMaven.Model.Model model, 
		                                  DirectoryInfo sourceFileDirectory,
		                                  string projectFileName,
		                                  List<IProjectReference> projectReferences)
	    {		
			Guid projectGuid = Guid.NewGuid();
			
			if(projectReferences == null) projectReferences = new List<IProjectReference>();
			Project project = GetProjectFromPomModel(model, 
			                                         sourceFileDirectory,
			                                         projectFileName, 
			                                         projectGuid,
			                                         @"..\..\..\target\bin\Debug\", 
			                                         @"..\..\..\target\obj\",
			                                         projectReferences);
			FileInfo fileInfo = new FileInfo(sourceFileDirectory.FullName + @"\" + projectFileName + ".csproj");
		    project.Save(fileInfo.FullName);	
		    
		    IProjectReference projectReference = new ProjectReferenceImpl();
		    projectReference.CsProjFile = fileInfo;
		    projectReference.ProjectGuid = projectGuid;
		    projectReference.ProjectName = projectFileName;
			return projectReference;	    	
	    }
		
		public void GenerateSolutionFor(FileInfo fileInfo, List<IProjectReference> projectReferences)
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
				writer.Write("Project(\"{");
				writer.Write("FAE04EC0-301F-11D3-BF4B-00C04F79EFBC");
				writer.Write("}\") = \"");
				writer.Write(projectReference.ProjectName);
				writer.Write("\", \"");
				writer.Write(projectReference.CsProjFile.FullName);
				writer.Write("\", \"{");
				writer.Write(projectReference.ProjectGuid.ToString());
				writer.WriteLine("}\"");
				writer.WriteLine("EndProject");
				
			}
			writer.Flush();
			writer.Close();
			Console.WriteLine("NMAVEN-000-000: Generate solution file: File Name = " + fileInfo.FullName);
		}
					
		public NMaven.Model.Model CreatePomModelFor(string fileName)
		{
			TextReader reader = new StreamReader(fileName);
		    XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Model));
			return (NMaven.Model.Model) serializer.Deserialize(reader);	
		}
		
		private Project CreateProjectFor(string fileName) 
		{
            Engine engine = new Engine(@"C:\WINDOWS\Microsoft.NET\Framework\v2.0.50727");
            Project project = new Project(engine);
            project.Load(@fileName);
            return project;
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
		private Project GetProjectFromPomModel(NMaven.Model.Model model, 
		                                       DirectoryInfo sourceFileDirectory,
		                                       string assemblyName,
		                                       Guid projectGuid,
		                                       string assemblyOutputPath,
		                                       string baseIntermediateOutputPath,
		                                       List<IProjectReference> projectReferences)
		{
			if(model == null || sourceFileDirectory == null || projectGuid == null)
			{
				throw new ExecutionException("NMAVEN-000-000: Missing required parameter.");
			}
            Engine engine = new Engine(@"C:\WINDOWS\Microsoft.NET\Framework\v2.0.50727");
            Project project = new Project(engine);
            
            
            //Main Properties
            BuildPropertyGroup groupProject = project.AddNewPropertyGroup(false);
            groupProject.AddNewProperty("ProjectGuid", "{" + projectGuid.ToString() + "}");
            BuildProperty buildProperty = groupProject.AddNewProperty("Configuration", "Debug");
            buildProperty.Condition = " '$(Configuration)' == '' ";
            groupProject.AddNewProperty("RootNameSpace", model.groupId);
            groupProject.AddNewProperty("AssemblyName", assemblyName);
            groupProject.AddNewProperty("BaseIntermediateOutputPath", baseIntermediateOutputPath);
            groupProject.AddNewProperty("OutputType", GetOutputType(model.packaging));
            
            //Debug Properties
            groupProject = project.AddNewPropertyGroup(false);
            buildProperty.Condition = " '$(Configuration)' == '' ";
            groupProject.AddNewProperty( "OutputPath", assemblyOutputPath, false);
            
            project.AddNewImport(@"$(MSBuildBinPath)\Microsoft.CSharp.Targets", null);
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
            AddProjectDependencies(project, model, sourceFileDirectory);
            AddFoldersToProject(project, null, sourceFileDirectory, sourceFileDirectory);
            AddClassFilesToProject(project, null, sourceFileDirectory, sourceFileDirectory);
            AddProjectReferences(project, assemblyName, projectReferences);
			return project;
			
		}
		
		private void AddProjectReferences(Project project, string projectName, List<IProjectReference> projectReferences)
		{
			BuildItemGroup itemGroup = project.AddNewItemGroup();
			foreach(IProjectReference projectReference in projectReferences)
			{
				BuildItem buildItem = itemGroup.AddNewItem("ProjectReference", projectReference.CsProjFile.FullName);
				buildItem.SetMetadata("Project", "{" + projectReference.ProjectGuid.ToString() + "}");
				buildItem.SetMetadata("Name", projectName);		
			}
		}
				
		private void AddFoldersToProject(Project project, BuildItemGroup folderGroup, DirectoryInfo rootDirectory, DirectoryInfo sourceFileDirectory) 
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
		
		private void AddClassFilesToProject(Project project, BuildItemGroup compileGroup, DirectoryInfo rootDirectory, DirectoryInfo sourceFileDirectory) 
		{
	        DirectoryInfo[] directoryInfos = rootDirectory.GetDirectories();
            if(directoryInfos != null && directoryInfos.Length > 0)
            {
            	if(compileGroup == null) compileGroup = project.AddNewItemGroup();
            	
            	foreach(DirectoryInfo di in directoryInfos) 
            	{  
              		if(di.FullName.Contains(".svn") || di.FullName.Contains("obj") || di.FullName.Contains("bin"))
    					continue;       			
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
		
		private void AddProjectDependencies(Project project, NMaven.Model.Model model, DirectoryInfo sourceFileDirectory) 
		{
			BuildItemGroup group = project.AddNewItemGroup();
			group.AddNewItem("Reference", "System.Xml");
			if(model.dependencies != null) 
			{
				foreach(Dependency dependency in model.dependencies) {
					String artifactExtension = (dependency.type == "module") ? "dll" : GetExtension(dependency.type);
					String repoPath = Environment.GetEnvironmentVariable("HOMEDRIVE") 
					    + Environment.GetEnvironmentVariable("HOMEPATH") 
						+ @"\.m2\repository\" + dependency.groupId.Replace(".", "\\")
						+ "\\" + dependency.artifactId + "\\" + dependency.version + "\\" + dependency.artifactId + "." 
						+ artifactExtension;
					BuildItem buildItem = group.AddNewItem("Reference", dependency.artifactId);
					//TODO: Fix this. Just because it is in the GAC on the system that builds the .csproj does not mean 
					//it is in the GAC on another system. 
					if(!dependency.GetType().Equals("gac") && !IsInGac(dependency.artifactId)) 
						buildItem.SetMetadata("HintPath", repoPath, false);
				}				
			}

	        DirectoryInfo[] directoryInfos = sourceFileDirectory.GetDirectories();
            
            ClassParser classParser = new ClassParser();
            List<FileInfo> fileInfos = new List<FileInfo>();
            AddFileInfosFromSourceDirectories(sourceFileDirectory, fileInfos);
            List<string> dependencies = classParser.GetDependencies(fileInfos);
            foreach(string dependency in dependencies)
            {
            	try {
                    string assembly = GetAssemblyFor(dependency);
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
		
		private bool IsInGac(string assembly)
		{
			return new DirectoryInfo(@"C:\WINDOWS\assembly\GAC_MSIL\" + assembly).Exists;	
		}

        private string GetAssemblyFor(string dependency)
        {
        if(dependency.Trim().Equals("System.Resources")) return "System.Windows.Forms";
            return dependency;
        }
		
		private void AddFileInfosFromSourceDirectories(DirectoryInfo sourceFileDirectory, List<FileInfo> fileInfos ) 
		{
            DirectoryInfo[] directoryInfos = sourceFileDirectory.GetDirectories();
            if(directoryInfos != null && directoryInfos.Length > 0)
            {  	
            	foreach(DirectoryInfo di in directoryInfos) 
            	{  
              		if(di.FullName.Contains(".svn") || di.FullName.Contains("obj") || di.FullName.Contains("bin"))
              			continue;
              		fileInfos.AddRange(di.GetFiles());
              		AddFileInfosFromSourceDirectories(di, fileInfos);
            	}           	
            }
		}
		
		private string GetOutputType(String type)
		{
			if (type.Equals("library")) return "Library";
			else if (type.Equals("exe")) return "Exe";
			else if (type.Equals("winexe")) return "WinExe";
			else if (type.Equals("module")) return "Module";
			return null;
		}
		
		private string GetExtension(String type)
		{
			if (type.Equals("library")) return "dll";
			else if (type.Equals("exe")) return "exe";
			else if (type.Equals("winexe")) return "exe";
			else if (type.Equals("module")) return "netmodule";
			return null;
		}				
		
		private class ClassParser {
			
			public List<string> GetDependencies(List<FileInfo> fileInfos) 
			{
				List<string> dependencies = new List<string>();
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
			                		string[] tokens = line.Remove(line.Length - 1).Split(new char[1]{' '});
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
