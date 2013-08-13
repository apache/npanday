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
using System.Text.RegularExpressions;
using System.Xml;
using log4net;
using NPanday.Model.Pom;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class AzurePomConverter : AbstractPomConverter
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(AzurePomConverter));

        public AzurePomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            // relies on this being set everywhere - if it were set per project, this would need to be evaluated on the project references
            if (!projectDigest.UseMsDeploy)
            {
                throw new Exception("You must use Web Deploy 2.0 to package web applications when using Azure projects");
            }

            GenerateHeader("azure-cloud-service");

            //Add SCM Tag
            if (scmTag != null && scmTag != string.Empty && Model.parent==null)
            {
                Scm scmHolder = new Scm();
                scmHolder.connection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.developerConnection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.url = scmTag;

                Model.scm = scmHolder;
            }

            // Add Com Reference Dependencies
            if (projectDigest.ComReferenceList.Length > 0)
            {
                AddComReferenceDependency();
            }
            
			//Add Project WebReferences
            AddWebReferences();

            //Add EmbeddedResources maven-resgen-plugin
            AddEmbeddedResources();
            
            // Add Project Inter-dependencies
            foreach (ProjectReference projectRef in projectDigest.ProjectReferences)
            {
                AddProjectReference(projectRef);
            }

            // Add Project Reference Dependencies
            // override the one from the parent to add new types for Azure
            AddProjectReferenceDependenciesToList(true);

            Plugin plugin = AddPlugin("org.apache.npanday.plugins", "azure-maven-plugin");
            if (!string.IsNullOrEmpty(projectDigest.TargetFramework))
                AddPluginConfiguration(plugin, "frameworkVersion", projectDigest.TargetFramework);
            else
            {
                // TODO: crude hack until the plugin doesn't require this and picks the right minimum default
                AddPluginConfiguration(plugin, "frameworkVersion", "4.0");
            }

            if (!string.IsNullOrEmpty(projectDigest.ProductVersion) && projectDigest.ProductVersion != "1.6")
                AddPluginConfiguration(plugin, "executableVersion", projectDigest.ProductVersion);

            if (!string.IsNullOrEmpty(projectDigest.CloudConfig))
            {
                AddPluginConfiguration(plugin, "serviceConfigurationFile", projectDigest.CloudConfig);
            }

            Dictionary<string, string> extraRoleContent = new Dictionary<string,string>();
            foreach (Content content in projectDigest.Contents)
            {
                Regex r = new Regex(@"(\w+)Content\\(.+)");
                Match m = r.Match(content.IncludePath);
                if (m.Success)
                {
                    string role = m.Groups[1].Value;
                    string include = m.Groups[2].Value;

                    if (extraRoleContent.ContainsKey(role))
                    {
                        extraRoleContent[role] = extraRoleContent[role] + "," + include;
                    }
                    else
                    {
                        extraRoleContent.Add(role, include);
                    }
                }
                else
                {
                    log.WarnFormat("Not copying content declared in project from an unknown path: {0}", content.IncludePath);
                }
            }

            if (extraRoleContent.Count > 0)
            {
                Plugin antPlugin = AddPlugin("org.apache.maven.plugins", "maven-antrun-plugin", null, false);

                Dictionary<string, string> configuration = new Dictionary<string, string>();

                AddPluginExecution(antPlugin, "copy-files", new string[] { "run" }, "prepare-package");

                XmlDocument xmlDocument = new XmlDocument();
                string xmlns = @"http://maven.apache.org/POM/4.0.0";
                XmlElement tasks = xmlDocument.CreateElement("tasks", xmlns);
                foreach (string role in extraRoleContent.Keys)
                {
                    XmlElement copyTask = xmlDocument.CreateElement("copy", xmlns);
                    copyTask.SetAttribute("todir", @"${project.build.directory}/packages/" + projectDigest.ProjectName + "/" + role);
                    XmlElement fileset = xmlDocument.CreateElement("fileset", xmlns);
                    fileset.SetAttribute("dir", role + "Content");
                    fileset.SetAttribute("includes", extraRoleContent[role]);
                    copyTask.AppendChild(fileset);
                    tasks.AppendChild(copyTask);
                }

                PluginExecutionConfiguration config = new PluginExecutionConfiguration();
                config.Any = new XmlElement[] { tasks };
                antPlugin.executions[0].configuration = config;
            }

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml")), Model);
            }
        }

        private void AddProjectReference(ProjectReference projectRef)
        {
            Dependency dependency = new Dependency();

            dependency.artifactId = projectRef.Name;
            dependency.groupId = !string.IsNullOrEmpty(groupId) ? groupId : projectRef.Name;
            dependency.version = string.IsNullOrEmpty(version) ? "1.0-SNAPSHOT" : version;

            dependency.type = "dotnet-library";
            if (projectRef.ProjectReferenceDigest != null
                && !string.IsNullOrEmpty(projectRef.ProjectReferenceDigest.OutputType))
            {
                string type = projectRef.ProjectReferenceDigest.OutputType.ToLower();
                if (npandayTypeMap.ContainsKey(type))
                    type = npandayTypeMap[type];
                dependency.type = type;
            }
            if (projectRef.RoleType != null)
            {
                string targetFramework = projectDigest.TargetFramework;
                // TODO: same hack as above - the Azure project doesn't need to target a framework, and instead we should support different ones (See also azure-maven-plugin roleproperties generation)
                if (string.IsNullOrEmpty(targetFramework))
                    targetFramework = "4.0";

                if (!projectRef.ProjectReferenceDigest.TargetFramework.Equals(targetFramework))
                {
                    log.WarnFormat("Project reference '{0}' targets a different framework version ({1}) to the Azure project ({2}), and may not succeed when uploaded to Azure.", 
                        projectRef.Name, projectRef.ProjectReferenceDigest.TargetFramework, targetFramework);
                }

                switch (projectRef.RoleType)
                {
                    case "Web":
                        dependency.type = "msdeploy-package";
                        break;
                    case "Worker":
                        dependency.type = "dotnet-application";
                        break;
                    default:
                        log.Warn("Unknown role type '" + projectRef.RoleType + "' - treating as a dotnet-library");
                        break;
                }
            }

            AddDependency(dependency);
        }
    }
}
