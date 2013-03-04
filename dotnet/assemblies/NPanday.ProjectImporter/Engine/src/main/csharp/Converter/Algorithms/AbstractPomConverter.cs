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
using System.Windows.Forms;
using System.Xml;
using log4net;
using NPanday.Artifact;
using NPanday.Model.Pom;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;
using Microsoft.Win32;
using System.Text.RegularExpressions;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public abstract class AbstractPomConverter : IPomConverter
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(AbstractPomConverter));

        public static Dictionary<string, string> npandayTypeMap = new Dictionary<string, string>();
        static AbstractPomConverter()
        {
            // reverse of ArtifactType in dotnet-core
            npandayTypeMap.Add("library", "dotnet-library");
            npandayTypeMap.Add("module", "dotnet-module");
            npandayTypeMap.Add("exe", "dotnet-executable");
            npandayTypeMap.Add("winexe", "dotnet-windows-executable");
        }

        protected RspUtility rspUtil;
        protected ArtifactContext artifactContext;
        protected List<Artifact.Artifact> localArtifacts;
        protected string mainPomFile;
        protected NPanday.Model.Pom.Model parent;
        protected string groupId;
        protected string version;

        private List<string> nonPortableReferences;

        public List<string> GetNonPortableReferences()
        {
            return nonPortableReferences;
        }

        protected ProjectDigest projectDigest;



        public ProjectDigest ProjectDigest
        {
            get { return projectDigest; }
        }

        private List<Reference> missingReferences;

        public List<Reference> GetMissingReferences()
        {
            return missingReferences;
        }
        
        protected NPanday.Model.Pom.Model model;
        private static List<Artifact.Artifact> testingArtifacts;

        // TODO: this might be more effective if it was cached across instances, 
        // but would need to be mapped by TargetFrameworkIdentifier and TargetFrameworkVersion
        protected Dictionary<string, string> TargetFrameworkDirectories;

        public NPanday.Model.Pom.Model Model
        {
            get { return model; }
        }

        public AbstractPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId)
        {
            artifactContext = new ArtifactContext();
            this.projectDigest = projectDigest;
            this.mainPomFile = mainPomFile;
            this.parent = parent;
            this.groupId = FilterID(groupId);
            this.version = parent != null ? parent.version : null;

            this.rspUtil = new RspUtility();
            this.model = new NPanday.Model.Pom.Model();
            
            // Add build Tag
            this.model.build = new NPanday.Model.Pom.Build();

            this.missingReferences = new List<Reference>();
            this.nonPortableReferences = new List<string>();

            // TODO: this is a hack because of bad design. The things that talk to the local repository should be pulled out of here, and able to be stubbed/mocked instead
            if (testingArtifacts != null)
            {
                this.localArtifacts = testingArtifacts;
            }
        }

        #region AddEmbeddedResources


        private bool isResgenSupported(string rsrc)
        {
            bool isSupported=false;
            if (rsrc.Contains(".txt") || rsrc.Contains(".resx") || rsrc.Contains(".resource"))
            {
                isSupported = true ;
            }
            return isSupported;
        }

        /// <summary>
        /// Auto Generate the Resources unsupported by Resgen.exe
        /// </summary>
        /// <param name="rsrcList"></param>
        protected void AddResources(List<string> rsrcList)
        {
            List<NPanday.Model.Pom.Resource> resources = new List<NPanday.Model.Pom.Resource>();
            
            if (model.build.resources != null)
            {
                resources.AddRange(model.build.resources);
            }

            // Add other resource file
            
            Resource r = new Resource();
            r.directory = "./";
            r.includes = rsrcList.ToArray();
            resources.Add(r);

            model.build.resources = resources.ToArray();



        }
        
        protected void AddEmbeddedResources()
        {
            if (projectDigest != null && projectDigest.EmbeddedResources != null && projectDigest.EmbeddedResources.Length > 0)
            {
                Plugin embeddedResourcePlugin = AddPlugin(
                    "org.apache.npanday.plugins",
                    "maven-resgen-plugin",
                    null,
                    false
                );

                if (!string.IsNullOrEmpty(projectDigest.TargetFramework))
                    AddPluginConfiguration(embeddedResourcePlugin, "frameworkVersion", projectDigest.TargetFramework);

                List<Dictionary<string, string>> embeddedResourceList = new List<Dictionary<string, string>>();
                List<string> resourceList = new List<string>();   
                foreach (EmbeddedResource embeddedResource in projectDigest.EmbeddedResources)
                {
                    if (isResgenSupported(embeddedResource.IncludePath))
                    {
                        Dictionary<string, string> value = new Dictionary<string, string>();
                        string sourceFile = embeddedResource.IncludePath;
                        if (sourceFile == null)
                            continue;

                        value.Add("sourceFile", sourceFile);
                        value.Add("name", parseEmbeddedName(projectDigest.RootNamespace, sourceFile));

                        embeddedResourceList.Add(value);
                    }
                    else
                    {
                        resourceList.Add(embeddedResource.IncludePath);
                    }
                }
                if (embeddedResourceList.Count > 0)
                {
                    AddPluginConfiguration(embeddedResourcePlugin, "embeddedResources", embeddedResourceList);
                }
                if (resourceList.Count > 0)
                {
                    AddResources(resourceList);
                }
            }
        }

        string parseEmbeddedName(string nameSpace, string sourceFilePath)
        {
            string returnVal = string.Empty;
            FileInfo sourceFileInfo = new FileInfo(sourceFilePath);
            if ("vb".Equals(projectDigest.Language))
            {
                returnVal = sourceFileInfo.Name.Replace(".resx", string.Empty);
            }
            else if ("csharp".Equals(projectDigest.Language))
            {
                foreach (string name in sourceFilePath.Split("\\".ToCharArray(), StringSplitOptions.RemoveEmptyEntries))
                {
                    if (name.IndexOf(".") < 0)
                    {
                        returnVal += name.Trim().Replace(' ', '_') + ".";
                    }
                    else
                    {
                        returnVal += name.Substring(0, name.LastIndexOf("."));
                    }

                }
            }
            return string.Format("{0}.{1}", nameSpace, returnVal);
        }

        #endregion
        #region AddWebReferences
        /// <summary>
        /// Adds WebReference as Plugin
        /// Author: Shein Melicer G. Ernacio
        /// </summary>
        protected void AddWebReferences()
        {
            if (projectDigest != null && projectDigest.WebReferenceUrls != null && projectDigest.WebReferenceUrls.Length > 0)
            {
                Plugin webReferencePlugin = AddPlugin(
                    "org.apache.npanday.plugins",
                    "maven-wsdl-plugin",
                    null,
                    false
                );

                AddPluginExecution(webReferencePlugin, "wsdl", null);

                List<Dictionary<string, string>> webReferenceUrlList = new List<Dictionary<string, string>>();

                foreach (WebReferenceUrl webReferenceUrl in projectDigest.WebReferenceUrls)
                {
                    Dictionary<string, string> value = new Dictionary<string, string>();
                    string name = webReferenceUrl.RelPath;

                    if (name != null)
                    {
                        int startIndex = name.IndexOf("\\");
                        if (startIndex >= 0)
                        {
                            name = name.Substring(startIndex + 1);
                        }
                        name = getNameSpace(name);
                        value.Add("namespace", name);
                        value.Add("output", webReferenceUrl.RelPath);
                        DirectoryInfo dirInfo = new DirectoryInfo(Path.Combine(projectDigest.FullDirectoryName, webReferenceUrl.RelPath));
                        FileInfo[] fileInfo = dirInfo.GetFiles("*.wsdl");
                        if (fileInfo.Length > 0)
                        {
                            value.Add("path", Path.Combine(webReferenceUrl.RelPath, fileInfo[0].Name));
                        }
                        else
                        {
                            value.Add("path", webReferenceUrl.RelPath + name + ".wsdl");
                        }

                        webReferenceUrlList.Add(value);
                    }
                }

                AddPluginConfiguration(webReferencePlugin, "webreferences", webReferenceUrlList);

            }
        }

        string getNameSpace(string relPath)
        {
            if (string.IsNullOrEmpty(relPath))
                return string.Empty;
            if (relPath.EndsWith("\\"))
                relPath = relPath.Substring(0, relPath.Length - 1);
            return relPath.Replace("\\", ".");
        }
        #endregion
        public abstract void ConvertProjectToPomModel(bool writePom, string scmTag);

        public void ConvertProjectToPomModel(string scmTag)
        {
            ConvertProjectToPomModel(true,scmTag);
        }

        private string FilterID(string partial)
        {
            string filtered = string.Empty;
            if (partial.EndsWith("."))
            {
                partial = partial.Substring(0, partial.Length - 1);
            }
            char before = '*';
            foreach (char item in partial)
            {

                if ((Char.IsNumber(item) || Char.IsLetter(item)) || ((item == '.' && before != '.') || (item == '-' && before != '-')))
                {
                    filtered += item;
                }
                before = item;
            }

            return filtered;
        }

        bool HasSpecialCharacters(string partial)
        {
            bool isSpecial = false;
            foreach (char item in partial)
            {

                if ((!Char.IsNumber(item) && !Char.IsLetter(item)) && item != '.' && item != '-')
                {
                    isSpecial = true;
                }
            }

            return isSpecial;
        }


        protected void GenerateHeader(string packaging)
        {
            // Add Parent Header
            if (parent != null)
            {
                model.parent = new NPanday.Model.Pom.Parent();
                model.parent.artifactId = FilterID(parent.artifactId);
                model.parent.groupId = FilterID(parent.groupId);
                model.parent.version = parent.version;


                if (!string.IsNullOrEmpty(mainPomFile))
                {
                    DirectoryInfo dir = new DirectoryInfo(Path.GetDirectoryName(projectDigest.FullFileName));
                    FileInfo file = new FileInfo(mainPomFile);
                    model.parent.relativePath = PomHelperUtility.GetRelativePath(dir, file);
                }
            }
            else
            {
                model.groupId = !string.IsNullOrEmpty(groupId) ? FilterID(groupId) : FilterID(projectDigest.ProjectName);
                model.version = string.IsNullOrEmpty(version) ? "1.0-SNAPSHOT" : version;
            }

            string projectName = projectDigest.ProjectName;
            if (HasSpecialCharacters(projectDigest.ProjectName))
            {
                FileInfo f = new FileInfo(ProjectDigest.FullFileName);
                projectName = f.Name.Substring(0, f.Name.Length - f.Extension.Length);
            }

            model.modelVersion = "4.0.0";
            model.artifactId = FilterID(projectName);
            model.name = string.Format("{0} : {1}", !string.IsNullOrEmpty(groupId) ? groupId : FilterID(projectDigest.ProjectName), FilterID(projectDigest.ProjectName));
            model.packaging = packaging;
        }


        protected void AddComReferenceDependency()
        {
            foreach (ComReference comReference in projectDigest.ComReferenceList)
            {
                Dependency comDependency = new Dependency();
                comDependency.groupId = comReference.Include;
                comDependency.artifactId = comReference.Include;
                comDependency.version = comReference.VersionMajor + "." + comReference.VersionMinor + ".0" + ".0";
                comDependency.type = "com_reference";
                comDependency.classifier = string.Format("{0}\\{1}.{2}\\{3}", comReference.Guid, convertDecToHex(comReference.VersionMajor), convertDecToHex(comReference.VersionMinor), convertDecToHex(comReference.Lcid));
                comDependency.classifier = comDependency.classifier.Replace("\\", "-");
                AddDependency(comDependency);
            }
        }

        string convertDecToHex(string decimalNum)
        {
            int decNum;
            if (int.TryParse(decimalNum, out decNum))
            {
                return decNum.ToString("X");
            }
            return decimalNum;
        }


        protected bool IsModelHasDependency(Dependency dependency)
        {
            if (model.dependencies == null)
            {
                return false;
            }

            foreach (Dependency var in model.dependencies)
            {
                if (var.artifactId.Equals(dependency.artifactId) && var.groupId.Equals(dependency.groupId))
                {
                    return true;
                }
            }

            return false;

        }



        protected void AddDependency(Dependency dependency)
        {
            // add dependency if it doesnot exists
            if (!IsModelHasDependency(dependency))
            {
                List<Dependency> dependencies = new List<Dependency>();
                if (model.dependencies != null)
                {
                    dependencies.AddRange(model.dependencies);
                }
                dependencies.Add(dependency);
                model.dependencies = dependencies.ToArray();
                return;
            }
        }



        protected void AddInterProjectDependency(ProjectReference projectRef)
        {
            AddDependency(CreateInterProjectDependency(projectRef.Name, projectRef.ProjectReferenceDigest));
        }

        protected Dependency CreateInterProjectDependency(string name, ProjectDigest digest)
        {
            Dependency interDependency = new Dependency();

            interDependency.artifactId = name;
            interDependency.groupId = !string.IsNullOrEmpty(groupId) ? groupId : name;
            interDependency.version = string.IsNullOrEmpty(version) ? "1.0-SNAPSHOT" : version;
            interDependency.type = "dotnet-library";

            if (digest != null
                && !string.IsNullOrEmpty(digest.OutputType))
            {
                interDependency.type = digest.OutputType.ToLower();
            }
            return interDependency;
        }


        protected void AddInterProjectDependenciesToList()
        {
            foreach (ProjectReference projectRef in projectDigest.ProjectReferences)
            {
                AddInterProjectDependency(projectRef);
            }
        }

        protected virtual Dependency GetProjectReferenceDependency(Reference reference)
        {
            Dependency refDependency = ResolveDependency(reference);
            if (refDependency == null)
            {
                missingReferences.Add(reference);

                // TODO: check if reference.Version is always set - ResolveDependency does some filename parsing that we should factor out so it's not done multiple times
                refDependency = new Dependency();
                refDependency.groupId = reference.Name;
                refDependency.artifactId = reference.Name;
                refDependency.version = reference.Version;
                refDependency.type = "dotnet-library";
            }

            if (!("library".Equals(refDependency.type, StringComparison.OrdinalIgnoreCase)
                  || "dotnet-library".Equals(refDependency.type, StringComparison.OrdinalIgnoreCase)))
            {
                // ignore gac if already in the RSP 
                if (rspUtil.IsRspIncluded(refDependency.artifactId, projectDigest.Language))
                {
                    return null;
                }
            }

            return refDependency;
        }

        protected void AddProjectReferenceDependenciesToList()
        {
            foreach (Reference reference in projectDigest.References)
            {
                Dependency dep = GetProjectReferenceDependency(reference);
                if (dep != null)
                {
                    AddDependency(dep);
                }
            }
        }

        protected Plugin AddPlugin(string groupId, string artifactId)
        {
            return AddPlugin(groupId, artifactId, null, true);
        }

        

        protected Plugin AddPlugin(string groupId, string artifactId, string version, bool extensions)
        {
            List<NPanday.Model.Pom.Plugin> plugins = new List<NPanday.Model.Pom.Plugin>();
            if (model.build.plugins != null)
            {
                plugins.AddRange(model.build.plugins);
            }


            // Add NPanday compile plugin 
            NPanday.Model.Pom.Plugin plugin = new NPanday.Model.Pom.Plugin();
            plugin.groupId = groupId;
            plugin.artifactId = artifactId;
            plugin.version = version;
            plugin.extensions = extensions;
            
            plugins.Add(plugin);
            

            model.build.plugins = plugins.ToArray();

            return plugin;

        }

        /// <summary>
        /// Adds PluginExecution
        /// </summary>
        /// <param name="plugin"></param>
        /// <param name="goal"></param>
        /// <param name="phase"></param>
        protected void AddPluginExecution(Plugin plugin, string goal, string phase)
        {
            AddPluginExecution(plugin, goal, new string[] { goal }, phase);
        }

        protected void AddPluginExecution(Plugin plugin, string id, string[] goals, string phase)
        {
            AddPluginExecution(plugin, id, goals, phase, null);
        }

        protected void AddPluginExecution(Plugin plugin, string id, string[] goals, string phase, Dictionary<string,string> configuration)
        {
            if (goals.Length == 0)
                throw new Exception("Plugin execution must contain goals");

            List<PluginExecution> list = new List<PluginExecution>();
            if (plugin.executions == null)
            {
                plugin.executions = new List<PluginExecution>().ToArray();
                list.AddRange(plugin.executions);
            }

            PluginExecution exe = new PluginExecution();

            exe.id = id;
            exe.goals = goals;
            exe.phase = phase;

            if (configuration != null)
            {
                PluginExecutionConfiguration config = new PluginExecutionConfiguration();

                List<XmlElement> elems = new List<XmlElement>();

                XmlDocument xmlDocument = new XmlDocument();

                foreach (string key in configuration.Keys)
                {
                    XmlElement elem = xmlDocument.CreateElement(key, @"http://maven.apache.org/POM/4.0.0");
                    elem.InnerText = configuration[key];
                    elems.Add(elem);
                }

                config.Any = elems.ToArray();
                exe.configuration = config;
            }

            list.Add(exe);

            plugin.executions = list.ToArray();
        }

        protected void AddPluginConfiguration(Plugin plugin, string tag, string value)
        {
            if (string.IsNullOrEmpty(tag) || string.IsNullOrEmpty(value))
            {
                // there is nothing to write
                return;
            }

            if (plugin.configuration == null)
            {
                plugin.configuration = new PluginConfiguration();
            }


            List<XmlElement> elems = new List<XmlElement>();
            if (plugin.configuration.Any != null)
            {
                elems.AddRange(plugin.configuration.Any);
            }



            XmlDocument xmlDocument = new XmlDocument();
            XmlElement elem = xmlDocument.CreateElement(tag, @"http://maven.apache.org/POM/4.0.0");
            elem.InnerText = value;



            elems.Add(elem);
            plugin.configuration.Any = elems.ToArray();

        }

        /// <summary>
        /// Design for WebReferenceUrl
        /// </summary>
        /// <param name="plugin">the plugin</param>
        /// <param name="parentTag">myReference</param>
        /// <param name="properties">name-value-property</param>
        protected void AddPluginConfiguration(Plugin plugin, string parentTag, List<Dictionary<string, string>> properties)
        {
            if (string.IsNullOrEmpty(parentTag) || properties == null)
            {
                // there is nothing to write
                return;
            }

            if (properties.Count <= 0)
            {
                // there is nothing to write
                return;
            }

            if (plugin.configuration == null)
            {
                plugin.configuration = new PluginConfiguration();
            }

            List<XmlElement> elems = new List<XmlElement>();
            if (plugin.configuration.Any != null)
            {
                elems.AddRange(plugin.configuration.Any);
            }

            XmlDocument xmlDocument = new XmlDocument();
            XmlElement elem = xmlDocument.CreateElement(parentTag, @"http://maven.apache.org/POM/4.0.0");


            string childElement = parentTag.Substring(0, parentTag.Length - 1);
            foreach (Dictionary<string, string> property in properties)
            {

                XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, childElement, @"http://maven.apache.org/POM/4.0.0");
                if ("embeddedResources".Equals(parentTag))
                {
                    XmlNode nodeSourceFile = xmlDocument.CreateNode(XmlNodeType.Element, "sourceFile", @"http://maven.apache.org/POM/4.0.0");
                    XmlNode nodeName = xmlDocument.CreateNode(XmlNodeType.Element, "name", @"http://maven.apache.org/POM/4.0.0");
                    nodeName.InnerText = property["name"];
                    nodeSourceFile.InnerText = property["sourceFile"];
                    node.AppendChild(nodeSourceFile);
                    node.AppendChild(nodeName);
                }
                else
                {
                    XmlNode nodeName = xmlDocument.CreateNode(XmlNodeType.Element, "namespace", @"http://maven.apache.org/POM/4.0.0");
                    XmlNode nodePath = xmlDocument.CreateNode(XmlNodeType.Element, "path", @"http://maven.apache.org/POM/4.0.0");
                    XmlNode nodeOutput = xmlDocument.CreateNode(XmlNodeType.Element, "output", @"http://maven.apache.org/POM/4.0.0");
                    nodeName.InnerText = property["namespace"];
                    nodePath.InnerText = property["path"] != null ? property["path"].Replace("\\", "/") : property["path"];
                    nodeOutput.InnerText = property["output"] != null ? property["output"].Replace("\\", "/") : property["output"];
                    node.AppendChild(nodeName);
                    node.AppendChild(nodePath);
                    node.AppendChild(nodeOutput);
                }
                elem.AppendChild(node);
            }

            elems.Add(elem);
            plugin.configuration.Any = elems.ToArray();
        }

        protected void AddPluginConfiguration(Plugin plugin, string parentTag, string childTags, string[] values)
        {
            if (string.IsNullOrEmpty(parentTag) || string.IsNullOrEmpty(childTags) || values == null)
            {
                // there is nothing to write
                return;
            }

            if (values.Length <= 0)
            {
                // there is nothing to write
                return;
            }



            if (plugin.configuration == null)
            {
                plugin.configuration = new PluginConfiguration();
            }


            List<XmlElement> elems = new List<XmlElement>();
            if (plugin.configuration.Any != null)
            {
                elems.AddRange(plugin.configuration.Any);
            }


            XmlDocument xmlDocument = new XmlDocument();
            XmlElement elem = xmlDocument.CreateElement(parentTag, @"http://maven.apache.org/POM/4.0.0");

            foreach (string value in values)
            {
                XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, childTags, @"http://maven.apache.org/POM/4.0.0");
                node.InnerText = value.Trim();
                elem.AppendChild(node);
            }

            elems.Add(elem);
            plugin.configuration.Any = elems.ToArray();

        }


        protected Dependency ResolveDependency(Reference reference)
        {
            // For MSbuild, the typical order is as follows (from Microsoft.Common.targets):
            // (1) Files from current project - indicated by {CandidateAssemblyFiles}
            // (2) $(ReferencePath) - the reference path property, which comes from the .USER file.
            // (3) The hintpath from the referenced item itself, indicated by {HintPathFromItem}.
            // (4) The directory of MSBuild's "target" runtime from GetReferenceAssemblyPaths (if applicable) and GetFrameworkPath (<= 3.5).
            //     The "target" runtime folder is the folder of the runtime that MSBuild is a part of.
            // (5) Registered assembly folders, indicated by {Registry:*,*,*}
            // (6) Legacy registered assembly folders, indicated by {AssemblyFolders}
            // (7) Resolve to the GAC.
            // (8) Treat the reference's Include as if it were a real file name.
            // (9) Look in the application's output folder (like bin\debug)

            // NPanday uses the following order:
            //  - Files from artifact repository
            //  - The hintpath (step 3 above)
            //  - The target framework directories (step 4 above)
            //  - The registered assembly directories (step 5 above)
            //  - The GAC (step 7 above)

            Dependency refDependency;

            // resolve first from artifact
            refDependency = ResolveDependencyFromLocalRepository(reference);

            // resolve using hint path
            if (refDependency == null)
                refDependency = ResolveDependencyFromHintPath(reference);

            // resolve from target framework directories
            if (refDependency == null && projectDigest.DependencySearchConfig.SearchFramework)
                refDependency = ResolveDependencyFromDirectories(reference, GetTargetFrameworkDirectories(), "target framework");

            // resolve from registered assembly directories
            if (refDependency == null && projectDigest.DependencySearchConfig.SearchAssemblyFoldersEx)
                refDependency = ResolveDependencyFromDirectories(reference, GetTargetFrameworkAssemblyFoldersEx(), "extra assembly folder");

            // resolve from GAC
            if (refDependency == null && projectDigest.DependencySearchConfig.SearchGac)
                refDependency = ResolveDependencyFromGAC(reference);

            if (refDependency == null)
                log.DebugFormat("Unable to resolve {0}", reference.Name);

            return refDependency;
        }

        private Dictionary<string,string> GetTargetFrameworkAssemblyFoldersEx()
        {
            Dictionary<string,string> directories = new Dictionary<string,string>();

            RegistryKey root = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\" + projectDigest.TargetFrameworkIdentifier);

            if (projectDigest.TargetFrameworkVersion == "v4.0")
            {
                GetTargetFrameworkDirectoriesAssemblyFoldersEx(directories, root.OpenSubKey("v4.0.30319\\AssemblyFoldersEx"));
            }
            if (projectDigest.TargetFrameworkVersion == "v3.5")
            {
                GetTargetFrameworkDirectoriesAssemblyFoldersEx(directories, root.OpenSubKey("v3.5\\AssemblyFoldersEx"));
            }
            if (projectDigest.TargetFrameworkVersion == "v3.5" || projectDigest.TargetFrameworkVersion == "v3.0")
            {
                GetTargetFrameworkDirectoriesAssemblyFoldersEx(directories, root.OpenSubKey("v3.0\\AssemblyFoldersEx"));
            }
            if (projectDigest.TargetFrameworkVersion == "v3.5" || projectDigest.TargetFrameworkVersion == "v3.0" || projectDigest.TargetFrameworkVersion == "v2.0")
            {
                GetTargetFrameworkDirectoriesAssemblyFoldersEx(directories, root.OpenSubKey("v2.0.50727\\AssemblyFoldersEx"));
            }

            if (directories.Count == 0)
                log.WarnFormat("No AssemblyFoldersEx registry key found for {0} {1}", projectDigest.TargetFrameworkIdentifier, projectDigest.TargetFrameworkVersion);

            return directories;
        }

        protected static void GetTargetFrameworkDirectoriesAssemblyFoldersEx(Dictionary<string, string> targetFrameworkDirectories, RegistryKey assemblyFolderEx)
        {
            if (assemblyFolderEx != null)
            {
                foreach (string key in assemblyFolderEx.GetSubKeyNames())
                {
                    string v = (string)assemblyFolderEx.OpenSubKey(key).GetValue(null);
                    if (v != null)
                    {
                        // strip non-alphanumeric characters to make a property
                        targetFrameworkDirectories.Add(new Regex("[^A-Za-z0-9]").Replace(key, ""), v);
                    }
                }
            }
        }

        private Dependency ResolveDependencyFromDirectories(Reference reference, Dictionary<string, string> directories, string label)
        {
            foreach (KeyValuePair<string, string> entry in directories)
            {
                string directory = entry.Value;
                string path = Path.Combine(directory, reference.Name + ".dll");
                if (File.Exists(path))
                {
                    // Note that a "provided" scope may be more appropriate here, if NPanday were to support it
                    // This could likewise replace the GAC types as all of that lookup should occur at build time

                    string var = "npanday." + entry.Key;
                    AddProperty(var, directory);
                    Dependency refDependency = CreateDependencyFromSystemPath(reference, "${" + var + "}/" + reference.Name + ".dll");

                    // We do not list these as non-portable, for two reasons:
                    //  - they should not be copied to the local repository, because there can be multiple conflicting versions in different SDKs
                    //    (any copying would require a rigorous use of classifiers)
                    //  - they should not be included in packages (e.g. for MSDeploy), which the system packaging currently avoids

                    log.DebugFormat("Resolved {0} from {1} directories: {2}:{3}:{4}",
                        reference.Name, label, refDependency.groupId, refDependency.artifactId, refDependency.version);
                    return refDependency;
                }
            }
            return null;
        }

        private void AddProperty(string var, string value)
        {
            if (model.properties == null)
                model.properties = new ModelProperties();

            List<XmlElement> elems = new List<XmlElement>();
            if (model.properties.Any != null)
            {
                foreach (XmlElement e in model.properties.Any)
                {
                    Console.WriteLine(e.ToString());
                    if (e.Name == var)
                    {
                        if (e.InnerText != value)
                            throw new Exception("Inconsistent property: " + var + " replacing " + e.Value + " with " + value);
                        else
                            return;
                    }
                    else
                    {
                        elems.Add(e);
                    }
                }
            }

            XmlDocument xmlDocument = new XmlDocument();

            XmlElement elem = xmlDocument.CreateElement(var, @"http://maven.apache.org/POM/4.0.0");
            elem.InnerText = value;
            elems.Add(elem);

            model.properties.Any = elems.ToArray();
        }

        protected virtual Dictionary<string, string> GetTargetFrameworkDirectories()
        {
            if (TargetFrameworkDirectories == null)
            {
                // TODO: add support for WinFX, which adds: $(CLR_REF_PATH) and $(WinFXAssemblyDirectory) (constructed by an MSBuild task)
                // TODO: add support for CompactFramework, which overwrites with the output of the GetDeviceFrameworkPath MSBuild task
                // TODO: may need to accommodate design-time facades which have special treatment in MSBuild

                Dictionary<string, string> targetFrameworkDirectories = new Dictionary<string, string>();

                if (projectDigest.TargetFrameworkVersion == "v4.0")
                {
                    // v4.0 overrides the path to just include the reference assemblies
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFrameworkReferenceAssemblies", "Version40", "FrameworkRef40");
                }
                if (projectDigest.TargetFrameworkVersion == "v3.5")
                {
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFrameworkReferenceAssemblies", "Version35", "FrameworkRef35");
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFramework", "Version35", "Framework35");
                }
                if (projectDigest.TargetFrameworkVersion == "v3.5" || projectDigest.TargetFrameworkVersion == "v3.0")
                {
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFrameworkReferenceAssemblies", "Version30", "FrameworkRef30");
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFramework", "Version30", "Framework30");
                }
                if (projectDigest.TargetFrameworkVersion == "v3.5" || projectDigest.TargetFrameworkVersion == "v3.0" || projectDigest.TargetFrameworkVersion == "v2.0")
                {
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFramework", "Version20", "Framework20");
                }
                if (projectDigest.TargetFrameworkVersion == "v1.1")
                {
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFramework", "Version11", "Framework11");
                }

                if (targetFrameworkDirectories.Count == 0)
                {
                    log.WarnFormat("Unsupported framework version for determining target framework directories: {0}", projectDigest.TargetFrameworkVersion);
                }

                // Add SDK directory
                if (projectDigest.TargetFrameworkVersion == "v4.0")
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFrameworkSdk", "Version40", "FrameworkSdk40");
                else if (projectDigest.TargetFrameworkVersion == "v3.5")
                    AddTargetFrameworkDirectory(targetFrameworkDirectories, "GetPathToDotNetFrameworkSdk", "Version35", "FrameworkSdk35");
                // Version30 is unsupported for this call
                // no value for SDK 2.0

                log.InfoFormat("Target framework directories: {0}", string.Join(",", new List<string>(targetFrameworkDirectories.Values).ToArray()));
                TargetFrameworkDirectories = targetFrameworkDirectories;
            }
            return TargetFrameworkDirectories;
        }

        private void AddTargetFrameworkDirectory(Dictionary<string, string> directories, string method, string version, string key)
        {
            // If the VS requirement moves up to a newer .NET requirement, we can just do:
            //  ToolLocationHelper.GetPathToDotNetFramework(TargetDotNetFrameworkVersion.Version40);
            // However, when targetting an earlier version we end up with Microsoft.Build.Utilities and Microsoft.Build.Utilities.v4.0
            // in the list of assemblies, and there's no guarantee the types will be loaded from the right one

            // Iterate over loaded assemblies to find ToolLocationHelper
            bool found = false;
            foreach (System.Reflection.Assembly a in AppDomain.CurrentDomain.GetAssemblies())
            {
                if (a.GetName().Name.StartsWith("Microsoft.Build.Utilities"))
                {
                    Type helperType = null;
                    Type versionType = null;
                    foreach (Type t in a.GetExportedTypes())
                    {
                        if (t.Name == "ToolLocationHelper")
                        {
                            helperType = t;
                        }
                        else if (t.Name == "TargetDotNetFrameworkVersion")
                        {
                            versionType = t;
                        }
                    }
                    if (helperType == null)
                    {
                        log.Error("Unable to find ToolLocationHelper type");
                    }
                    else if (versionType == null)
                    {
                        log.Error("Unable to find TargetDotNetFrameworkVersion type");
                    }
                    else
                    {
                        log.DebugFormat("Using ToolLocationHelper from {0}; TargetDotNetFrameworkVersion from {1}",
                            helperType.Assembly.GetName(), versionType.Assembly.GetName());

                        string value = (string)helperType.InvokeMember(method,
                            System.Reflection.BindingFlags.InvokeMethod, System.Type.DefaultBinder, "",
                            new object[] { Enum.Parse(versionType, version) });

                        log.DebugFormat("Adding target directory {0} = {1}", key, value);
                        if (!string.IsNullOrEmpty(value))
                        {
                            directories.Add(key, value);
                            found = true;
                        }
                    }
                }
            }
            if (!found)
                log.WarnFormat("Unable to find framework location for {0} {1}", method, version);
        }

        private Dependency ResolveDependencyFromLocalRepository(Reference reference)
        {
            Artifact.Artifact artifact = GetArtifact(reference);
            if (artifact != null)
            {
                log.DebugFormat("Resolved {0} from local repository: {1}:{2}:{3}", 
                    reference.Name, artifact.GroupId, artifact.ArtifactId, artifact.Version);

                Dependency dependency = new Dependency();
                dependency.artifactId = artifact.ArtifactId;
                dependency.groupId = artifact.GroupId;
                dependency.version = artifact.Version;
                dependency.type = "dotnet-library";
                return dependency;
            }
            return null;
        }

        private void WarnNonPortableReference(string path, Dependency refDependency)
        {
            if (projectDigest.DependencySearchConfig.CopyToMaven)
            {
                log.InfoFormat("Copying to Maven local repository: {0} as {1}:{2}:{3}", path, refDependency.groupId, refDependency.artifactId, refDependency.version);
                RepositoryUtility.InstallAssembly(path, refDependency.groupId, refDependency.artifactId, refDependency.version);

                // reset the dependency
                refDependency.scope = null;
                refDependency.systemPath = null;
            }
            else
            {
                // if it is in the project, we still consider it non-portable because packaging plugins will exclude system dependencies
                // however, we can adjust the path to be a bit more portable across different checkouts
                // first, check if the library is somewhere inside the solution (mainPomFile is top-most POM)
                string projectRoot = new DirectoryInfo(mainPomFile).Parent.FullName;
                if (PathUtility.IsSubdirectoryOf(projectRoot, path))
                {
                    // if so, adjust path to be relative to this project's POM file
                    path = "${basedir}\\" + PathUtility.MakeRelative(projectDigest.FullDirectoryName + "\\", path);
                    refDependency.systemPath = path;
                }
                log.WarnFormat("Adding non-portable reference to POM: {0}", path);
            }

            // add to list regardless so we can get a message at the end in a user presentable way
            nonPortableReferences.Add(path);
        }

        private Dependency ResolveDependencyFromHintPath(Reference reference)
        {
            if (!string.IsNullOrEmpty(reference.HintFullPath) && new FileInfo(reference.HintFullPath).Exists)
            {
                string prjRefPath = Path.Combine(projectDigest.FullDirectoryName, ".references");
                //verbose for new-import
                if (!reference.HintFullPath.ToLower().StartsWith(prjRefPath.ToLower()) && !reference.Name.Contains("Interop"))
                {
                    Dependency refDependency = CreateDependencyFromSystemPath(reference, reference.HintFullPath);

                    WarnNonPortableReference(reference.HintFullPath, refDependency);

                    log.DebugFormat("Resolved {0} from hint path: {1}:{2}:{3}", reference.Name, refDependency.groupId, refDependency.artifactId, refDependency.version);

                    return refDependency;
                }
                else
                {
                    // The dependency is in .references, or an Interop. 
                    // TODO: not a very good parsing of .references - if we have .references, don't we already know it from local repo?

                    Dependency refDependency = new Dependency();
                    refDependency.artifactId = reference.Name;
                   
                    //get version from the name above the last path
                    string[] pathTokens = reference.HintFullPath.Split("\\\\".ToCharArray());
                    if (pathTokens.Length < 3)
                    {
                        // should only hit this if it is in .references, and it was incorrectly constructed
                        throw new Exception("Invalid hint path: " + reference.HintFullPath);
                    }
                    refDependency.groupId = pathTokens[pathTokens.Length - 3];
                    refDependency.version = pathTokens[pathTokens.Length-2].Replace(reference.Name+"-","") ?? "1.0.0.0";                    
                    refDependency.type = "dotnet-library";

                    log.DebugFormat("Resolved {0} from previously resolved references: {1}:{2}:{3}", 
                        reference.Name, refDependency.groupId, refDependency.artifactId, refDependency.version);
                    
                    return refDependency;
                }
            }
            return null;
        }

        private static Dependency CreateDependencyFromSystemPath(Reference reference, string path)
        {
            Dependency refDependency = new Dependency();
            refDependency.artifactId = reference.Name;
            refDependency.groupId = reference.Name;
            refDependency.version = reference.Version ?? "1.0.0.0";
            refDependency.type = "dotnet-library";
            refDependency.scope = "system";
            refDependency.systemPath = path;
            return refDependency;
        }

        private Dependency ResolveDependencyFromGAC(Reference reference)
        {
            List<string> refs = GacUtility.GetInstance().GetAssemblyInfo(reference.Name, reference.Version, projectDigest.Platform);
            if (refs.Count > 0)
            {
                log.DebugFormat("GAC references for {0} version {1} platform {2}: {3}", reference.Name,
                                reference.Version, projectDigest.Platform, string.Join(", ", refs.ToArray()));

                // Assembly is found at the gac
                if (refs.Count > 1)
                {
                    log.Warn("Found more than one reference for a single version, using the first only");
                }

                System.Reflection.AssemblyName name = new System.Reflection.AssemblyName(refs[0]);
                System.Reflection.Assembly a = System.Reflection.Assembly.ReflectionOnlyLoad(name.FullName);

                Dependency refDependency = new Dependency();
                refDependency.artifactId = reference.Name;
                refDependency.groupId = reference.Name;

                refDependency.type = GacUtility.GetNPandayGacType(a.ImageRuntimeVersion, name.ProcessorArchitecture, reference.PublicKeyToken);

                refDependency.version = reference.Version ?? "1.0.0.0";

                if (reference.PublicKeyToken != null)
                {
                    refDependency.classifier = reference.PublicKeyToken;
                }
                else
                {
                    int start = a.FullName.IndexOf("PublicKeyToken=");
                    if (start < 0)
                    {
                        int length = (a.FullName.Length) - start;
                        refDependency.classifier = a.FullName.Substring(start, length);
                        refDependency.classifier = refDependency.classifier.Replace("PublicKeyToken=", "");
                    }
                    else
                    {
                        log.Warn("No public key token found for GAC dependency, excluding classifier: " + a.FullName);
                    }
                }

                log.DebugFormat("Resolved {0} from GAC: {1}:{2}:{3}:{4}",
                    reference.Name, refDependency.groupId, refDependency.artifactId, refDependency.version, refDependency.classifier);

                return refDependency;
            }

            return null;
        }

        Artifact.Artifact GetArtifactFromRepoUsingEmbeddedAssemblyVersionNumber(Reference reference)
        {
            if (localArtifacts == null)
            {
                // TODO: this is a horribly slow operation on the local repository. We should consider alternatives (e.g. maven repository index, cache + querying remote repos)
                localArtifacts = artifactContext.GetArtifactRepository().GetArtifacts();
            }

            foreach (Artifact.Artifact artifact in localArtifacts)
            {
                if (artifact.ArtifactId.Equals(reference.Name, StringComparison.OrdinalIgnoreCase)
                    && artifact.Version.Equals(reference.Version, StringComparison.OrdinalIgnoreCase))
                {
                    return artifact;
                }
            }

            return null;
        }


        Artifact.Artifact GetArtifactFromRepoUsingHintPathVersionNumber(Reference reference)
        {
            if (string.IsNullOrEmpty(reference.HintFullPath) || !(new FileInfo(reference.HintFullPath).Exists))
            {
                return null;
            }

            return artifactContext.GetArtifactRepository().GetArtifact(new FileInfo(reference.HintFullPath));

        }



        protected Artifact.Artifact GetArtifact(Reference reference)
        {
            //if (string.IsNullOrEmpty(reference.Name) || string.IsNullOrEmpty(reference.Version))
            //{
            //    return null;
            //}

            //if (localArtifacts == null)
            //{
            //    return null;
            //}

            try
            {

                Artifact.Artifact artifact = GetArtifactFromRepoUsingEmbeddedAssemblyVersionNumber(reference);

                if (artifact == null)
                {
                    artifact = GetArtifactFromRepoUsingHintPathVersionNumber(reference);
                }
                if (artifact == null)
                {
                    artifact = artifactContext.GetArtifactRepository().GetArtifact(new FileInfo(reference.HintPath));
                }
                return artifact;
            }
            catch
            {

                return null;
            }


        }





        protected string GetSourceDir()
        {
            if (projectDigest.Compiles.Length == 0)
            {
                return "./";
            }

            DirectoryInfo common = new DirectoryInfo(Path.GetDirectoryName(projectDigest.Compiles[0].IncludeFullPath));
            foreach (Compile compile in projectDigest.Compiles)
            {
                DirectoryInfo srcInclude = new DirectoryInfo(Path.GetDirectoryName(compile.IncludeFullPath));

                common = PomHelperUtility.GetCommonDirectory(common, srcInclude);

            }



            DirectoryInfo prjDir = new DirectoryInfo(Path.GetDirectoryName(projectDigest.FullFileName));
            string srcDir = PomHelperUtility.GetRelativePath(prjDir, common);

            if (string.IsNullOrEmpty(srcDir))
            {
                return "./";
            }

            if (srcDir.Contains(".."))
            {
                return "./";
            }

            return srcDir;


        }

        protected string GetVBDefineConfigurationValue()
        {
            List<string> defines = new List<string>();

            if (!string.IsNullOrEmpty(projectDigest.DefineConstants))
            {
                defines.Add(projectDigest.DefineConstants);
            }

            if (!string.IsNullOrEmpty(projectDigest.MyType))
            {
                defines.Add(string.Format("_MyType=\"{0}\"", projectDigest.MyType));
            }


            if (!string.IsNullOrEmpty(projectDigest.Platform))
            {
                defines.Add(string.Format("PLATFORM=\"{0}\"", projectDigest.Platform));
            }

            return string.Join(",", defines.ToArray());
        }

        // useful for unit testing rather than grabbing them from the local repository
        public static void UseTestingArtifacts(List<Artifact.Artifact> artifacts)
        {
            testingArtifacts = artifacts;
        }
    }
}
