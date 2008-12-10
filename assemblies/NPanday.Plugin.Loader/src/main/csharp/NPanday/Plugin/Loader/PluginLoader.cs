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
using System.IO;
using System.Reflection;
using System.Runtime.Remoting;
using NPanday.Plugin;

namespace NPanday.Plugin.Loader
{
	/// <summary>
	/// Provides methods for loading plugins and for creating Mojos.
	/// </summary>
	internal sealed class PluginLoader
	{
		internal PluginLoader()
		{
		}

		/// <summary>
		/// Loads the specified plugin assembly file into the returned plugin application domain.
		/// </summary>
		/// <param name="pluginAssemblyFile">the.NET maven plugin</param>
		/// <returns>application domain for .NET maven plugin</returns>
		/// 
		internal AppDomain LoadPlugin(FileInfo pluginAssemblyFile)
		{
			Console.WriteLine("Loading Plugin: " + pluginAssemblyFile.DirectoryName);
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
		internal AbstractMojo CreateAbstractMojoFor(String mojoName, FileInfo pluginAssemblyFile, 
		                                          FileInfo paramFile, AppDomain applicationDomain)
		{
            ObjectHandle objectHandle = 
            	applicationDomain.CreateInstanceFrom(pluginAssemblyFile.FullName, mojoName);
			AbstractMojo abstractMojo = (AbstractMojo) objectHandle.Unwrap();
			abstractMojo.InjectFields(paramFile.FullName);
			return abstractMojo;		
		}
		
		public static int Main(string[] args)
		{
			string paramFilePath = GetArgFor("parameterFile", args);
			string assemblyFilePath = GetArgFor("assemblyFile", args);
			string mojoName = GetArgFor("mojoName", args);
			Console.WriteLine("ParamFile = {0}, AssemblyFile = {1}, MojoName = {2}", 
			                  paramFilePath, assemblyFilePath, mojoName);

			if(paramFilePath == null || assemblyFilePath == null || mojoName == null)
			{
                Console.WriteLine("Missing arguement");
                return 1;
            }

            FileInfo assemblyFile = new FileInfo(assemblyFilePath);
            if (!assemblyFile.Exists)
            {
                Console.WriteLine("Assembly File does not exist: File = " + assemblyFile.FullName);
                return 1;
            }
            FileInfo paramFile = new FileInfo(paramFilePath);
            if (!paramFile.Exists)
            {
                Console.WriteLine("Param File does not exist: File = " + paramFile.FullName);
                return 1;
            }

			PluginLoader pluginLoader = new PluginLoader();
			AppDomain applicationDomain = pluginLoader.LoadPlugin(assemblyFile);
			AbstractMojo abstractMojo = pluginLoader.CreateAbstractMojoFor(mojoName, 
			                                                               assemblyFile, 
			                                                               paramFile,
			                                                               applicationDomain);
			abstractMojo.Execute();
            return 0;
			
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
