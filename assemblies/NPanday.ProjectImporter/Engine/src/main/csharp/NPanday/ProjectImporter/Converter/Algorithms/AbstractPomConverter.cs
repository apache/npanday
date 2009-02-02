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
        protected GacUtility gacUtil;
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

        protected NPanday.Model.Pom.Model model;

        public NPanday.Model.Pom.Model Model
        {
            get { return model; }
        }


        public AbstractPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId)
        {

            artifactContext = new ArtifactContext();
            this.localArtifacts = artifactContext.GetArtifactRepository().GetArtifacts();
            this.projectDigest = projectDigest;
            this.mainPomFile = mainPomFile;
            this.parent = parent;
            this.groupId = FilterID(groupId);
            this.version = parent != null ? parent.version : null;



            this.gacUtil = new GacUtility();
            this.model = new NPanday.Model.Pom.Model();
            // Add build Tag
            this.model.build = new NPanday.Model.Pom.Build();

        }

        #region AddEmbeddedResources
        protected void AddEmbeddedResources()
        {
            if (projectDigest != null && projectDigest.EmbeddedResources != null && projectDigest.EmbeddedResources.Length > 0)
            {
                Plugin embeddedResourcePlugin = AddPlugin(
                    "npanday.plugin",
                    "maven-resgen-plugin",
                    null,
                    true
                );


                List<Dictionary<string, string>> embeddedResourceList = new List<Dictionary<string, string>>();

                foreach (EmbeddedResource embeddedResource in projectDigest.EmbeddedResources)
                {
                    Dictionary<string, string> value = new Dictionary<string, string>();
                    string sourceFile = embeddedResource.IncludePath;
                    if (sourceFile == null)
                        continue;


                    value.Add("sourceFile", sourceFile);
                    value.Add("name", sourceFile.TrimEnd(".resx".ToCharArray()));

                    embeddedResourceList.Add(value);
                }
                AddPluginConfiguration(embeddedResourcePlugin, "embeddedResources", embeddedResourceList);
            }
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
                    "npanday.plugin",
                    "maven-wsdl-plugin",
                    null,
                    true
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
        public abstract void ConvertProjectToPomModel(bool writePom);

        public void ConvertProjectToPomModel()
        {
            ConvertProjectToPomModel(true);
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
                model.groupId = !string.IsNullOrEmpty(groupId) ? FilterID(groupId) : FilterID(projectDigest.AssemblyName);
                model.version = string.IsNullOrEmpty(version) ? "1.0-SNAPSHOT" : version;
            }

            string projectName = projectDigest.AssemblyName;
            if (HasSpecialCharacters(projectDigest.AssemblyName))
            {
                string[] projectFullName = projectDigest.FullFileName.Split("\\".ToCharArray());
                projectName = projectFullName[projectFullName.Length - 1];

                if (projectName.Equals("."))
                {
                    projectName = projectFullName[projectFullName.Length - 2];
                }

                if (projectName.Contains(".csproj"))
                {
                    projectName = projectName.Replace(".csproj", "");
                }
                if (projectName.Contains(".vbproj"))
                {
                    projectName = projectName.Replace(".vbproj", "");
                }

            }


            model.artifactId = FilterID(projectName);




            model.modelVersion = "4.0.0";
            model.packaging = packaging;

            model.name = string.Format("{0} : {1}", !string.IsNullOrEmpty(groupId) ? groupId : FilterID(projectDigest.AssemblyName), FilterID(projectDigest.AssemblyName));
            if (!string.IsNullOrEmpty(projectDigest.OutputType))
            {
                model.packaging = projectDigest.OutputType.ToLower();
            }
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
            interDependency.type = "library";

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

        protected virtual void AddProjectReferenceDependency(Reference reference)
        {
            Dependency refDependency = ResolveDependency(reference);
            if (refDependency == null)
            {
                return;
            }


            if (!"library".Equals(refDependency.type, StringComparison.OrdinalIgnoreCase))
            {
                // ignore gac if already in the RSP 
                if (gacUtil.IsRspIncluded(refDependency.artifactId, projectDigest.Language))
                {
                    return;
                }
            }

            AddDependency(refDependency);


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

        protected Plugin FindPlugin(string groupId, string artifactId)
        {
            return FindPlugin(groupId, artifactId, null);
        }

        protected Plugin FindPlugin(string groupId, string artifactId, string version)
        {
            if (model.build.plugins == null)
            {
                return null;
            }

            foreach (Plugin plugin in model.build.plugins)
            {
                if (groupId.Equals(plugin.groupId) && artifactId.Equals(plugin.artifactId))
                {
                    if (!string.IsNullOrEmpty(version) && version.Equals(plugin.version))
                    {
                        return plugin;
                    }
                    else if (string.IsNullOrEmpty(version))
                    {
                        return plugin;
                    }
                }
            }

            return null;

        }

        /// <summary>
        /// Adds PluginExecution
        /// </summary>
        /// <param name="plugin"></param>
        /// <param name="goal"></param>
        /// <param name="phase"></param>
        protected void AddPluginExecution(Plugin plugin, string goal, string phase)
        {
            if (string.IsNullOrEmpty(goal))
            {
                // there is nothing to write
                return;
            }

            List<PluginExecution> list = new List<PluginExecution>();
            if (plugin.executions == null)
            {
                plugin.executions = new List<PluginExecution>().ToArray();
                list.AddRange(plugin.executions);
            }

            PluginExecution exe = new PluginExecution();

            exe.goals = new string[] { goal };

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
                    nodeName.InnerText = property["sourceFile"];
                    nodeSourceFile.InnerText = property["name"];
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
                dependency.type = "library";
                return dependency;
            }



            // resolve from GAC
            if (!string.IsNullOrEmpty(gacUtil.GetAssemblyInfo(reference.Name)))
            {
                // Assembly is found at the gac

                Dependency refDependency = new Dependency();
                refDependency.artifactId = reference.Name;
                refDependency.groupId = reference.Name;

                refDependency.type = "gac";
                if ("MSIL".Equals(reference.ProcessorArchitecture, StringComparison.OrdinalIgnoreCase))
                {
                    refDependency.type = "gac_msil";
                }
                else if ("x86".Equals(reference.ProcessorArchitecture, StringComparison.OrdinalIgnoreCase))
                {
                    refDependency.type = "gac_32";
                }

                refDependency.version = reference.Version ?? "1.0.0.0";
                refDependency.classifier = reference.PublicKeyToken;

                refDependency.scope = "system";
                System.Reflection.Assembly a = System.Reflection.Assembly.Load(gacUtil.GetAssemblyInfo(reference.Name));
                refDependency.systemPath = a.Location;

                return refDependency;

            }



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
                    //verbose for new-import
                    if (!reference.Name.Contains("Interop"))
                    {
                        MessageBox.Show(
                         string.Format("Warning: Build may not be portable if local references are used, Reference is not in Maven Repository or in GAC."
                                     + "\nReference: {0}"
                                     + "\nDeploying the Reference, will make the code portable to other machines",
                             reference.HintFullPath
                         ), "Add Reference", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    }

                }


                // uncomment this if systemPath is supported

                Dependency refDependency = new Dependency();
                refDependency.artifactId = reference.Name;
                refDependency.groupId = reference.Name;
                refDependency.version = reference.Version ?? "1.0.0.0";
                refDependency.type = "library";
                refDependency.scope = "system";
                refDependency.systemPath = reference.HintFullPath;

                return refDependency;
            }
            //if (string.IsNullOrEmpty(reference.HintFullPath) && !string.IsNullOrEmpty(reference.Name))
            //{
            //    MessageBox.Show(
            //            string.Format("Warning: Build may not be portable if local references are used, Reference is not in Maven Repository or in GAC."
            //                        + "\nReference: {0}"
            //                        + "\nPlease Install it in your GAC or your Maven Repository."
            //                        + "\nInstalling Reference to your Maven Repository, will make the code portable to other machines",
            //                reference.Name
            //            ));
            //}



            return null;

        }

        Artifact.Artifact GetArtifactFromRepoUsingEmbeddedAssemblyVersionNumber(Reference reference)
        {
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



    }

}
