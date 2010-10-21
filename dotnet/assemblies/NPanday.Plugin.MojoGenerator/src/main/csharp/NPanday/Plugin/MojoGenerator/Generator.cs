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
using System.Runtime.Remoting;
using System.Resources;
using System.Text;
using System.Xml.Serialization;

using NPanday.Plugin;
using NPanday.Plugin.Generator;

namespace NPanday.Plugin.MojoGenerator
{
	/// <summary>
	/// Provides methods for loading plugins and for creating Mojos.
	/// </summary>
	internal sealed class Generator
	{
		internal Generator()
		{
		}

		/// <summary>
		/// Loads the specified plugin assembly file into the returned plugin application domain.
		/// </summary>
		/// <param name="pluginAssemblyFile">the.NET maven plugin</param>
		/// <returns>application domain for .NET maven plugin</returns>
		/// 
		internal AppDomain GetApplicationDomainFor(FileInfo pluginAssemblyFile)
		{
			Console.WriteLine("Loading Generator: " + pluginAssemblyFile.DirectoryName);
			AppDomainSetup setup = new AppDomainSetup();
			setup.ApplicationBase = pluginAssemblyFile.DirectoryName;

			AppDomain applicationDomain = AppDomain.CreateDomain("Loader", null, setup);
			PluginDomainManager pluginDomainManager = (PluginDomainManager) applicationDomain.DomainManager;
            pluginDomainManager.LoadPlugin(pluginAssemblyFile);
			return applicationDomain;
		}
		
		/// <summary>
		/// Creates an instance of the specified mojo name within the specified application domain.
		/// </summary>
		/// <param name="mojoName">the name of the mojo to create</param>
		/// <param name="pluginAssemblyFile">the.NET maven plugin</param>
		/// <param name="paramFile">the file containing the parameters to inject into an instance 
		/// of the specified mojo</param>
		/// <param name="applicationDomain">
		/// the application domain used to create the specified mojo name instance</param>
		/// <returns>an instance of the specified mojo name within the specified application domain</returns>
		internal int BuildPluginProject(AppDomain applicationDomain, FileInfo pluginArtifact, 
		                                FileInfo outputDirectory, string groupId, string artifactId, 
		                                string version)
		{
            ObjectHandle objectHandle = 
            	applicationDomain.CreateInstanceFrom(@pluginArtifact.FullName, 
            	                                     "NPanday.Plugin.Generator.JavaClassUnmarshaller");
			JavaClassUnmarshaller jcuRemote = (JavaClassUnmarshaller) objectHandle.Unwrap();
			List<JavaClass> javaClasses = jcuRemote.GetMojosFor(artifactId, groupId);
			JavaClassUnmarshaller jcuLocal = new JavaClassUnmarshaller();
			
			char[] delim = {'.'};
			DirectoryInfo sourceDirectory = new DirectoryInfo(@outputDirectory.FullName + "/src/main/java/" 
			                                                  + artifactId.Replace('.', '/'));
            sourceDirectory.Create();
			if(javaClasses.Count == 0)
			{
				Console.WriteLine("NPanday-000-000: There are no Mojos within the assembly: Artifact Id = " 
				                  + artifactId);
				return 1;
			}
			
			foreach(JavaClass javaClass in javaClasses)
			{
				string[] tokens = javaClass.ClassName.Split(delim);
				string classFileName = tokens[tokens.Length - 1];
				FileInfo fileInfo = new FileInfo(sourceDirectory.FullName + "/" 
				                                 + classFileName + ".java");
                jcuLocal.unmarshall(javaClass, fileInfo);
			}
            try
            {		    

            TextReader reader = new StreamReader(Assembly.GetExecutingAssembly().
            GetManifestResourceStream(Assembly.GetExecutingAssembly().GetManifestResourceNames()[0]));
			XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Pom.Model));
			NPanday.Model.Pom.Model model = (NPanday.Model.Pom.Model) serializer.Deserialize(reader);	
			model.artifactId = artifactId + ".JavaBinding";
			model.groupId = groupId;
			model.version = version;
			model.name = artifactId + ".JavaBinding";

            FileInfo outputPomXml = new FileInfo(@outputDirectory.FullName + "/pom-java.xml");
			TextWriter textWriter = new StreamWriter(@outputPomXml.FullName);
            serializer.Serialize(textWriter, model);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
			return 0;
		}
		
		public static int Main(string[] args)
		{
		
			string targetAssemblyFile = GetArgFor("targetAssemblyFile", args);
			string outputDirectory = GetArgFor("outputDirectory", args);
			string pluginArtifactPath = GetArgFor("pluginArtifactPath", args);
			string groupId = GetArgFor("groupId", args);
			string artifactId = GetArgFor("artifactId", args);
			string version = GetArgFor("artifactVersion", args); 
			
            
			Generator generator = new Generator();
			AppDomain applicationDomain = 
				generator.GetApplicationDomainFor(new FileInfo(targetAssemblyFile));
			return generator.BuildPluginProject(applicationDomain, new FileInfo(pluginArtifactPath), 
			                             new FileInfo(outputDirectory), 
			                             groupId, artifactId, version);
		}
		
		private static string GetArgFor(string name, string[] args)
		{
			char[] delim = {'='};
			foreach(string arg in args)
			{
                string[] tokens = arg.Split(delim);
                if (tokens[0].Equals(name)) return tokens[1];
			}
            return null;
		}		
	}
}
