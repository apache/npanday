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

namespace NPanday.Plugin.SysRef
{
	/// <summary>
	/// C# Plugin that will generate the required system reference .dlls
	/// </summary>
	[ClassAttribute(Phase = "SysRef", Goal = "prepare")]
	public sealed class SysRefMojo : AbstractMojo
	{

        public SysRefMojo()
		{
		}

		[FieldAttribute("repository", Expression = "${settings.localRepository}", Type = "java.lang.String")]
		public String localRepository;

		[FieldAttribute("mavenProject", Expression = "${project}", Type = "org.apache.maven.project.MavenProject")]
		public NPanday.Model.Pom.Model mavenProject;

		public override Type GetMojoImplementationType()
		{
			return this.GetType();
		}

        private void GenerateDependency(string filename)
        {
            ProcessStartInfo processStartInfo =
               new ProcessStartInfo("tlbimp", filename);
            processStartInfo.UseShellExecute = true;
            processStartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            System.Diagnostics.Process.Start(processStartInfo);

        }


        private string GetFileName(NPanday.Model.Pom.Dependency dependency)
        {

            RegistryKey root = Registry.ClassesRoot;

            string filename = string.Empty;


            try
            {

                String[] classTokens = dependency.classifier.Split("}".ToCharArray());
                String[] versionTokens = classTokens[1].Split("-".ToCharArray());
                classTokens[1] = classTokens[1].Replace("-", "\\");

                String newClassifier = classTokens[0] + "}" + classTokens[1];

                RegistryKey typeLib = root.OpenSubKey("TypeLib");
                RegistryKey target = typeLib.OpenSubKey(classTokens[0] + "}").OpenSubKey(versionTokens[1]).OpenSubKey(versionTokens[2]).OpenSubKey("win32");

                filename = target.GetValue("").ToString();


            }
            catch (Exception exe)
            {
                Console.WriteLine(exe.Message);
            }
            return filename;

        }

        private void LocalInstall(NPanday.Model.Pom.Dependency dependency, string fileName)
        {
            try
            {
                String[] rawName = GetFileName(dependency).Split("\\".ToCharArray());

                string generatedDLL = rawName[rawName.Length - 1];

                generatedDLL = generatedDLL.Replace("tlb", "dll");

                string directory = localRepository.Replace("\\", "/");


                //Creating of Directories

                directory = directory + "/" + dependency.groupId;


                if (!Directory.Exists(directory))
                {
                    Directory.CreateDirectory(directory);

                }

                directory = directory + "/" + dependency.artifactId;


                if (!Directory.Exists(directory))
                {
                    Directory.CreateDirectory(directory);
                }

                directory = directory + "/" + dependency.version;


                if (!Directory.Exists(directory))
                {
                    Directory.CreateDirectory(directory);
                }

                string newDll = dependency.artifactId + "-" + dependency.version + "-" + dependency.classifier + "." + dependency.type;

                string destinationFile = localRepository + "\\" + dependency.groupId + "\\" + dependency.artifactId + "\\" + dependency.version + "\\" + newDll;

                destinationFile = destinationFile.Replace("\\", "/");

                string tempDir = "c:/Windows/Temp/NPanday";

                if (!Directory.Exists(tempDir))
                {
                    Directory.CreateDirectory(tempDir);
                }
                fileName = "\"" + fileName + "\"";

                fileName = fileName + " /out:" + tempDir + "/" + dependency.artifactId+".dll";

                GenerateDependency(fileName);

                string sourceFile = tempDir + "/" + dependency.artifactId + ".dll";

                bool waiting = true;

                while (waiting)
                {
                    if (File.Exists(sourceFile))
                    {
                        waiting = false;
                    }
                }

                if (!File.Exists(destinationFile))
                {
                    System.Threading.Thread.Sleep(4500);
                    
                    File.Copy(sourceFile, destinationFile);
                }
            }
            catch (Exception exe)
            {
                Console.WriteLine("[ERROR] The Reference is not located.\n"+exe.Message);
            }

        }



        public void SystemInstall(NPanday.Model.Pom.Dependency dependency)
        {
            try
            {
                if (dependency.systemPath == null || dependency.systemPath == string.Empty)
                {
                    return;
                }
                else
                {
                    string sourceFile = dependency.systemPath;

                    sourceFile = sourceFile.Replace("\\", "/");

                    string directory = localRepository.Replace("\\", "/");


                    //Creating of Directories

                    directory = directory + "/" + dependency.groupId;


                    if (!Directory.Exists(directory))
                    {
                        Directory.CreateDirectory(directory);

                    }

                    directory = directory + "/" + dependency.artifactId;


                    if (!Directory.Exists(directory))
                    {
                        Directory.CreateDirectory(directory);
                    }

                    directory = directory + "/" + dependency.version;


                    if (!Directory.Exists(directory))
                    {
                        Directory.CreateDirectory(directory);
                    }


                    string newDll = dependency.artifactId + "-" + dependency.version + "-" + dependency.classifier + "." + dependency.type;

                    string destinationFile = localRepository + "\\" + dependency.groupId + "\\" + dependency.artifactId + "\\" + dependency.version + "\\" + newDll;

                    destinationFile = destinationFile.Replace("\\", "/");

                    if (!File.Exists(destinationFile))
                    {
                        File.Copy(sourceFile, destinationFile);
                    }

                }
            }
            catch (Exception exe)
            {
                Console.WriteLine(exe.Message);
            }
        }


        public override void Execute()
        {

            try
            {
                if(mavenProject.dependencies==null)
				{
					return;
				}

                foreach (NPanday.Model.Pom.Dependency dependency in mavenProject.dependencies)
                {
                    if (dependency.type.Equals("com_reference"))
                    {
                        //GenerateDependency(GetFileName(dependency));
                        LocalInstall(dependency, GetFileName(dependency));
                        Console.WriteLine("Successfully Installed : " + dependency.artifactId);
                        
                    }
                    if (dependency.type.Contains("gac") || dependency.type.Equals("library"))
                    {
                        if (dependency.systemPath != null || dependency.systemPath != string.Empty)
                        {
                            SystemInstall(dependency);
                            Console.WriteLine("Successfully Installed : " + dependency.artifactId);
                        }
                    }
                }
            }
            catch (Exception exe)
            {
                Console.WriteLine(exe.Message);
            }
        }
    }
}
