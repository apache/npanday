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
using System.Windows.Forms;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public abstract class AbstractPomConverter : IPomConverter
    {
        public static Dictionary<string, string> npandayTypeMap = new Dictionary<string, string>();
        static AbstractPomConverter()
        {
            // reverse of ArtifactType in dotnet-core
            npandayTypeMap.Add("library", "dotnet-library");
            npandayTypeMap.Add("module", "dotnet-module");
            npandayTypeMap.Add("exe", "dotnet-executable");
            npandayTypeMap.Add("winexe", "dotnet-executable");
        }

        protected RspUtility rspUtil;
        protected ArtifactContext artifactContext;
        protected List<Artifact.Artifact> localArtifacts;
        protected string mainPomFile;
        protected NPanday.Model.Pom.Model parent;
        protected string groupId;
        protected string version;


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

            Dependency interDependency = new Dependency();

            interDependency.artifactId = projectRef.Name;
            interDependency.groupId = !string.IsNullOrEmpty(groupId) ? groupId : projectRef.Name;
            interDependency.version = string.IsNullOrEmpty(version) ? "1.0-SNAPSHOT" : version;
            interDependency.type = "dotnet-library";

            if (projectRef.ProjectReferenceDigest != null
                && !string.IsNullOrEmpty(projectRef.ProjectReferenceDigest.OutputType))
            {
                interDependency.type = projectRef.ProjectReferenceDigest.OutputType.ToLower();
            }

            AddDependency(interDependency);

        }


        protected void AddInterProjectDependenciesToList()
        {
            foreach (ProjectReference projectRef in projectDigest.ProjectReferences)
            {
                AddInterProjectDependency(projectRef);
            }
        }

        protected Dependency GetProjectReferenceDependency(Reference reference)
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

        protected virtual void AddProjectReferenceDependency(Reference reference)
        {
            Dependency dep = GetProjectReferenceDependency(reference);
            if (dep != null)
            {
                AddDependency(dep);
            }
        }

        protected void AddProjectReferenceDependenciesToList()
        {
            foreach (Reference reference in projectDigest.References)
            {
                AddProjectReferenceDependency(reference);
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

            // resolve first from artifact
            Artifact.Artifact artifact = GetArtifact(reference);
            if (artifact != null)
            {
                Dependency dependency = new Dependency();
                dependency.artifactId = artifact.ArtifactId;
                dependency.groupId = artifact.GroupId;
                dependency.version = artifact.Version;
                dependency.type = "dotnet-library";
                return dependency;
            }

            List<string> refs = GacUtility.GetInstance().GetAssemblyInfo(reference.Name, null, projectDigest.Platform);

            // resolve from GAC
            if (refs.Count > 0)
            {
                // Assembly is found at the gac

                //exclude ProcessArchitecture when loading assembly on a non-32 bit machine
                refs = GacUtility.GetInstance().GetAssemblyInfo(reference.Name, reference.Version, null);

                System.Reflection.Assembly a = System.Reflection.Assembly.ReflectionOnlyLoad(new System.Reflection.AssemblyName(refs[0]).FullName);

                Dependency refDependency = new Dependency();
                refDependency.artifactId = reference.Name;
                refDependency.groupId = reference.Name;

                refDependency.type = GacUtility.GetNPandayGacType(a, reference.PublicKeyToken);

                refDependency.version = reference.Version ?? "1.0.0.0";

                if (reference.PublicKeyToken != null)
                {
                    refDependency.classifier = reference.PublicKeyToken;
                }
                else
                {
                    int start = a.FullName.IndexOf("PublicKeyToken=");
                    int length = (a.FullName.Length) - start;
                    refDependency.classifier = a.FullName.Substring(start, length);
                    refDependency.classifier = refDependency.classifier.Replace("PublicKeyToken=", "");
                }

                return refDependency;
            }

            bool isPathReference = false;

            // resolve using system path
            if (!string.IsNullOrEmpty(reference.HintFullPath) && new FileInfo(reference.HintFullPath).Exists)
            {

                // silent for re-import
                //commented out 
                //if (projectDigest.ExistingPom != null)
                //{
                //    return null;
                //}
                //else
                {
                    string prjRefPath = Path.Combine(projectDigest.FullDirectoryName, ".references");
                    //verbose for new-import
                    if (!reference.HintFullPath.ToLower().StartsWith(prjRefPath.ToLower()) && !reference.Name.Contains("Interop"))
                    {
                        MessageBox.Show(
                         string.Format("Warning: Build may not be portable if local references are used, Reference is not in Maven Repository or in GAC."
                                     + "\nReference: {0}"
                                     + "\nDeploying the reference to a Repository, will make the code portable to other machines",
                             reference.HintFullPath
                         ), "Add Reference", MessageBoxButtons.OK, MessageBoxIcon.Warning);

                        isPathReference = true;
                    }

                }


                // uncomment this if systemPath is supported

                Dependency refDependency = new Dependency();

                if (!isPathReference)
                {
                    refDependency.artifactId = reference.Name;
                   
                    //get version from the name above the last path
                    string[] pathTokens = reference.HintFullPath.Split("\\\\".ToCharArray());
                    refDependency.groupId = pathTokens[pathTokens.Length - 3];
                    refDependency.version = pathTokens[pathTokens.Length-2].Replace(reference.Name+"-","") ?? "1.0.0.0";
                    //refDependency.version = reference.Version ?? "1.0.0.0";
                    
                    refDependency.type = "dotnet-library";
                    //refDependency.scope = "system";
                    //refDependency.systemPath = reference.HintFullPath;

                }
                else
                {
                    refDependency.artifactId = reference.Name;
                    refDependency.groupId = reference.Name;
                    refDependency.version = reference.Version ?? "1.0.0.0";
                    refDependency.type = "dotnet-library";
                    refDependency.scope = "system";
                    refDependency.systemPath = reference.HintFullPath;
                }
                
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



        protected string GetDefineConfigurationValue()
        {
            List<string> defines = new List<string>();

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
