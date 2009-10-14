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
using System.Collections.Generic;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Diagnostics;
using Microsoft.Win32;

using NPanday.Plugin;
using NPanday.Model.Pom;
using NPanday.Model;
using NPanday.Artifact;
using System.Reflection;

namespace NPanday.Plugin.Msbuild
{
	/// <summary>
	/// C# Plugin that will generate the required system reference .dlls
	/// </summary>
	[ClassAttribute(Phase = "validate", Goal = "compile")]
	public sealed class MsbuildMojo : AbstractMojo
	{

        public MsbuildMojo()
		{
		}

		
		[FieldAttribute("mavenProject", Expression = "${project}", Type = "org.apache.maven.project.MavenProject")]
		public NPanday.Model.Pom.Model mavenProject;

		public override Type GetMojoImplementationType()
		{
			return this.GetType();
		}

        private void GenerateDependency(string projectName)
        {
            Directory.SetCurrentDirectory(mavenProject.build.sourceDirectory);
            
            if (File.Exists(projectName + ".csproj"))
            {
                projectName += ".csproj";
            }
            else
            {
                projectName += ".vbproj";
            }
            ProcessStartInfo processStartInfo =
               new ProcessStartInfo("msbuild", projectName);
            processStartInfo.UseShellExecute = true;
            processStartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            System.Diagnostics.Process.Start(processStartInfo);

        }

		private void ExecuteMsBuild()
		{
            try
            {
                Console.WriteLine("[INFO] Executing MsBuild Plugin");
                GenerateDependency(mavenProject.artifactId);
                Console.WriteLine("[INFO] Successfully Executed MsBuild Plugin");
            }
            catch (Exception exe)
            {
                Console.WriteLine("[ERROR] Failed to Execute MsBuild \n"+exe.Message);
            }
		}


        public override void Execute()
        {
            try
            {
				if(mavenProject==null)
				{
					Console.WriteLine("[ERROR] Project was not found by MsBuild Plugin");
				}
				else
				{
                    ExecuteMsBuild();        
				}
            }
            catch (Exception exe)
            {
                Console.WriteLine(exe.Message);
            }
        }
    }
}
