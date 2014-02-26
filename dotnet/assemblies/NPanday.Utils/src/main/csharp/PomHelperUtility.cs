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
using System.Xml;
using System.Xml.Serialization;
using log4net;
using NPanday.Model.Pom;

namespace NPanday.Utils
{
    public class PomHelperUtility
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(PomHelperUtility));

        private FileInfo pom;
        public bool isWebRefEmpty = false;

        RspUtility rspUtil = new RspUtility();

        public PomHelperUtility(FileInfo solutionFile, FileInfo projectFile)
        {
            FileInfo pomFile = PomHelperUtility.FindPomFileUntil(
                projectFile.Directory,
                solutionFile.Directory);

            this.pom = pomFile;
        }

        public PomHelperUtility(string pom)
        {
            this.pom = new FileInfo(pom);
        }

        public PomHelperUtility(FileInfo pom)
        {
            this.pom = pom;
        }

        public DirectoryInfo SourceDirectory
        {
            get
            {
                string src = GetPomXPathExprValue(@"//pom:project/pom:build/pom:sourceDirectory");
                return GetBuildSrcDir(src, false);
            }
        }

        public string ArtifactId
        {
            get
            {
                return GetPomXPathExprValue(@"//pom:project/pom:artifactId");

            }
        }

        public string GroupId
        {
            get
            {
                return GetPomXPathExprValue(@"//pom:project/pom:groupId");

            }
        }

        public string Version
        {
            get
            {
                return GetPomXPathExprValue(@"//pom:project/pom:version");

            }
        }



        public string Packaging
        {
            get
            {
                return GetPomXPathExprValue(@"//pom:project/pom:packaging");

            }
        }

        public bool HasPlugin(string pluginGroupId, string pluginArtifactId)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();
            return FindPlugin(model, pluginGroupId, pluginArtifactId) != null;
        }

        public static Plugin FindPlugin(NPanday.Model.Pom.Model model, string groupId, string artifactId)
        {
            if (model.build.plugins == null)
            {
                return null;
            }

            foreach (Plugin plugin in model.build.plugins)
            {
                if (groupId.ToLower().Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase) && artifactId.ToLower().Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    return plugin;
                }
            }

            return null;

        }

        public void AddPlugin(string groupId, string artifactId, string version, bool extensions, PluginConfiguration pluginConf, string pluginGoal)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

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

            if (pluginConf != null)
            {
                plugin.configuration = pluginConf;
            }

            if (null != pluginGoal || !String.Empty.Equals(pluginGoal))
            {
                addPluginExecution(plugin, pluginGoal, null);
            }

            plugins.Add(plugin);


            model.build.plugins = plugins.ToArray();

            WriteModelToPom(model);

        }

        public void AddPlugin(string groupId, string artifactId, string version, bool extensions, PluginConfiguration pluginConf)
        {
            AddPlugin(groupId, artifactId, version, extensions, pluginConf, null);
        }

        public DirectoryInfo TestSourceDirectory
        {
            get
            {
                string src = GetPomXPathExprValue(@"//pom:project/pom:build/pom:testSourceDirectory");
                return GetBuildSrcDir(src, true);
            }
        }

        private DirectoryInfo GetBuildSrcDir(string src, bool testSource)
        {
            string[] defTestSources = { "src/test/csharp", "src/test/vb" };
            string[] defSrcSources = { "src/main/csharp", "src/main/vb" };
            string[] defaultDirs = testSource ? defTestSources : defSrcSources;

            if (string.IsNullOrEmpty(src) && "csharp".Equals(NPandayCompilerPluginLanguage, StringComparison.OrdinalIgnoreCase))
            {
                src = defaultDirs[0];
            }


            if (string.IsNullOrEmpty(src) && "vb".Equals(NPandayCompilerPluginLanguage, StringComparison.OrdinalIgnoreCase))
            {
                src = defaultDirs[1];
            }

            // check for default folders
            if (string.IsNullOrEmpty(src))
            {
                foreach (string defDir in defaultDirs)
                {
                    DirectoryInfo d = new DirectoryInfo(pom.Directory.FullName + @"\" + defDir);
                    if (d.Exists)
                    {
                        src = defDir;
                        break;
                    }
                }
            }


            // set the default: where language is c#, 
            // if incase language is not found
            if (string.IsNullOrEmpty(src))
            {
                src = defaultDirs[0];
            }




            // if relative path, concatinate the folder of the pom
            if (IsRelativePath(src))
            {
                src = pom.Directory.FullName + @"\" + src;
            }
            return new DirectoryInfo(new DirectoryInfo(src).FullName);
        }

        public string NPandayCompilerPluginLanguage
        {
            get
            {

                string lang = GetNPandayCompilerPluginConfigurationValue("language");
                if (string.IsNullOrEmpty(lang))
                {
                    return null;
                }
                else if ("vb".Equals(lang, StringComparison.OrdinalIgnoreCase))
                {
                    return "vb";
                }
                else if ("csharp".Equals(lang, StringComparison.OrdinalIgnoreCase)
                    || "cs".Equals(lang, StringComparison.OrdinalIgnoreCase)
                    || "c#".Equals(lang, StringComparison.OrdinalIgnoreCase))
                {
                    return "csharp";
                }

                return lang;
            }

        }

        public string CompilerPluginConfigurationKeyfile
        {
            get
            {
                string key = GetNPandayCompilerPluginConfigurationValue("keyfile");
                if (IsRelativePath(key))
                {
                    return NormalizeFileToWindowsStyle(new FileInfo(pom.Directory.FullName + @"\" + key).FullName);
                }

                return key;
            }
            set
            {
                if (string.IsNullOrEmpty(value))
                {
                    SetNPandayCompilerPluginConfigurationValue("keyfile", null);
                    return;
                }

                string str = "";
                if (IsRelativePath(value))
                {
                    FileInfo f = new FileInfo(pom.Directory + @"\" + value);
                    str = GetRelativePathFromPom(f);

                }
                else
                {
                    FileInfo f = new FileInfo(value);
                    str = GetRelativePathFromPom(f);
                }

                SetNPandayCompilerPluginConfigurationValue("keyfile", str);
            }
        }

        public string GetNPandayCompilerPluginConfigurationValue(string config)
        {
            return GetPomXPathExprValue(@"//pom:project/pom:build/pom:plugins[./pom:plugin/pom:groupId = 'org.apache.npanday.plugins' and  ./pom:plugin/pom:artifactId = 'maven-compile-plugin'][1]/pom:plugin/pom:configuration/pom:" + config);
        }

        private string PomNamespaceURI
        {
            get
            {
                XmlDocument xmlDocument = new XmlDocument();
                xmlDocument.Load(pom.FullName);
                return xmlDocument.DocumentElement.NamespaceURI;
            }
        }

        public NPanday.Model.Pom.Model ReadPomAsModel()
        {
            return PomHelperUtility.ReadPomAsModel(this.pom);
        }

        public static NPanday.Model.Pom.Model ReadPomAsModel(FileInfo pomfile)
        {
            if (!pomfile.Exists)
            {
                throw new Exception("Pom file not found: " + pomfile.FullName);
            }

            XmlDocument xmlDocument = new XmlDocument();
            xmlDocument.Load(pomfile.FullName);
            String namespaceUri = xmlDocument.DocumentElement.NamespaceURI;

            if (string.IsNullOrEmpty(namespaceUri))
            {
                xmlDocument.DocumentElement.SetAttribute("xmlns", "http://maven.apache.org/POM/4.0.0");
                xmlDocument.Save(pomfile.FullName);
            }

            XmlTextReader reader = null;
            NPanday.Model.Pom.Model model = null;
            try
            {
                reader = new XmlTextReader(pomfile.FullName);
                reader.WhitespaceHandling = WhitespaceHandling.Significant;
                reader.Normalization = true;
                reader.XmlResolver = null;

                XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Pom.Model));

                if (!serializer.CanDeserialize(reader))
                {
                    throw new Exception(string.Format("Pom File ({0}) Reading Error, Pom File might contain invalid or malformed data", pomfile.FullName));
                }

                model = (NPanday.Model.Pom.Model)serializer.Deserialize(reader);
            }
            catch
            {
                throw;
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.Close();
                        ((IDisposable)reader).Dispose();
                    }
                }
                catch
                {
                    log.Warn("Failed to close stream reader after accessing pom.xml.");
                }

            }
            return model;
        }


        public void WriteModelToPom(NPanday.Model.Pom.Model model)
        {
            PomHelperUtility.WriteModelToPom(this.pom, model);
        }

        public bool IsWebReferenceEmpty()
        {
            string[] directories;
            if (Directory.Exists(pom.DirectoryName + "/Web References"))
                directories = Directory.GetDirectories(pom.DirectoryName + "/Web References");
            else
                directories = Directory.GetDirectories(pom.DirectoryName + "/App_WebReferences");

            bool isEmpty = false;
            if (directories.Length == 0)
            {
                isEmpty = true;
            }
            return isEmpty;
        }

        public void DeletePlugin(Plugin plugin)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();
            List<NPanday.Model.Pom.Plugin> plugins = new List<NPanday.Model.Pom.Plugin>();
            if (model.build.plugins != null)
            {
                foreach (Plugin item in model.build.plugins)
                {
                    if (!item.artifactId.Equals(plugin.artifactId))
                    {
                        plugins.Add(item);
                    }
                }
            }

            model.build.plugins = plugins.ToArray();

            WriteModelToPom(model);

        }

        public static void WriteModelToPom(FileInfo pomFile, NPanday.Model.Pom.Model model)
        {
            if (!pomFile.Directory.Exists)
            {
                pomFile.Directory.Create();
            }
            TextWriter writer = null;
            XmlSerializer serializer = null;
            try
            {
                writer = new StreamWriter(pomFile.FullName);
                serializer = new XmlSerializer(typeof(NPanday.Model.Pom.Model));
                serializer.Serialize(writer, model);
            }
            catch
            {
                throw;
            }
            finally
            {
                try
                {
                    if (writer != null)
                    {
                        writer.Close();
                        writer.Dispose();
                    }
                }
                catch
                {
                    log.Warn("Failed to close stream writer after writing to pom.xml.");
                }

            }
        }

        public void SetNPandayCompilerPluginConfigurationValue(string config, string value)
        {

            NPanday.Model.Pom.Model model = ReadPomAsModel();


            if (model.build == null)
            {
                model.build = new NPanday.Model.Pom.Build();
            }

            NPanday.Model.Pom.Plugin compilePlugin = null;
            List<NPanday.Model.Pom.Plugin> plugins = new List<NPanday.Model.Pom.Plugin>();

            if (model.build.plugins != null)
            {
                foreach (NPanday.Model.Pom.Plugin plugin in model.build.plugins)
                {
                    if ("org.apache.npanday.plugins".Equals(plugin.groupId)
                        && "maven-compile-plugin".Equals(plugin.artifactId))
                    {
                        compilePlugin = plugin;
                    }

                    plugins.Add(plugin);
                }
            }


            if (compilePlugin == null)
            {
                compilePlugin = new NPanday.Model.Pom.Plugin();
                compilePlugin.groupId = "org.apache.npanday.plugins";
                compilePlugin.artifactId = "maven-compile-plugin";
                compilePlugin.extensions = true;
                plugins.Add(compilePlugin);
            }

            XmlElement configElement = null;
            List<XmlElement> elems = new List<XmlElement>();

            if (compilePlugin.configuration != null && compilePlugin.configuration.Any != null)
            {
                foreach (XmlElement el in compilePlugin.configuration.Any)
                {
                    if (config.Equals(el.Name))
                    {
                        configElement = el;
                    }
                    elems.Add(el);
                }
            }
            else
            {
                compilePlugin.configuration = new NPanday.Model.Pom.PluginConfiguration();
            }


            if (configElement == null)
            {
                XmlDocument xmlDocument = new XmlDocument();
                //configElement = xmlDocument.CreateElement(config, @"http://maven.apache.org/POM/4.0.0");
                configElement = xmlDocument.CreateElement(config, PomNamespaceURI);
                configElement.RemoveAll();

                elems.Add(configElement);

            }
            configElement.InnerText = value;

            if (string.IsNullOrEmpty(value))
            {
                elems.Remove(configElement);
            }

            compilePlugin.configuration.Any = elems.ToArray();

            model.build.plugins = plugins.ToArray();

            WriteModelToPom(model);
        }

        private bool IsRelativePath(string path)
        {
            if (string.IsNullOrEmpty(path))
            {
                return false;
            }
            // in windows full path contains : eg. c:\ or d:\
            return !path.Contains(":");
        }





        // @"//pom:project/pom:build/pom:plugins[./pom:plugin/pom:groupId = 'org.apache.npanday.plugins' and  ./pom:plugin/pom:artifactId = 'maven-compile-plugin'][1]/pom:plugin/pom:configuration/pom:keyfile"

        public string GetPomXPathExprValue(string xpath_expr)
        {
            try
            {

                if (!pom.Exists)
                {
                    throw new Exception("Pom file not found: File = " + pom.FullName);
                }


                String pomFileName = pom.FullName;

                System.Xml.XmlDocument xmldoc = new System.Xml.XmlDocument();
                xmldoc.Load(pomFileName);

                System.Xml.XmlNamespaceManager xmlnsManager = new System.Xml.XmlNamespaceManager(xmldoc.NameTable);
                //xmlnsManager.AddNamespace("pom", "http://maven.apache.org/POM/4.0.0");
                xmlnsManager.AddNamespace("pom", PomNamespaceURI);


                System.Xml.XmlNodeList valueList = xmldoc.SelectNodes(xpath_expr, xmlnsManager);

                foreach (System.Xml.XmlNode val in valueList)
                {
                    return (val.InnerText);
                }

            }
            catch (Exception)
            {
                return null;
            }

            // try without the namespace if value is not found
            if (!string.IsNullOrEmpty(xpath_expr) && xpath_expr.Contains("pom:"))
            {
                return GetPomXPathExprValue(xpath_expr.Replace("pom:", ""));
            }

            return null;
        }

        public string GetRelativePathFromPom(FileInfo file)
        {
            return PomHelperUtility.GetRelativePath(pom.Directory, file);
        }

        public string GetRelativePathFromPom(DirectoryInfo dir)
        {
            return PomHelperUtility.GetRelativePath(pom.Directory, dir);
        }

        public string GetRelativePathFromPom(string filename)
        {
            return PomHelperUtility.GetRelativePath(pom.Directory, new FileInfo(filename));
        }




        public bool IsPomDependency(NPanday.Model.Pom.Dependency dependency)
        {
            return IsPomDependency(dependency.groupId, dependency.artifactId, dependency.version);
        }

        public bool IsPomDependency(string artifactId)
        {
            return IsPomDependency(null, artifactId, null);
        }

        public bool IsPomDependency(string groupId, string artifactId)
        {
            return IsPomDependency(groupId, artifactId, null);
        }

        public bool IsPomDependency(string groupId, string artifactId, string version)
        {
            // consider groupId and version if not empty
            if (!string.IsNullOrEmpty(groupId)
                && !string.IsNullOrEmpty(version))
            {
                return !string.IsNullOrEmpty
                (
                    GetPomXPathExprValue(
                        string.Format("//pom:project/pom:dependencies/pom:dependency[./pom:artifactId = '{0}' and  ./pom:groupId = '{1}' and  ./pom:version = '{2}'][1]/pom:artifactId",
                        artifactId,
                        groupId,
                        version)
                        )
                );

            }
            // consider groupId
            else if (!string.IsNullOrEmpty(groupId))
            {
                return !string.IsNullOrEmpty
                (
                    GetPomXPathExprValue(
                        string.Format("//pom:project/pom:dependencies/pom:dependency[./pom:artifactId = '{0}' and  ./pom:groupId = '{1}'][1]/pom:artifactId",
                        artifactId,
                        groupId)
                        )
                );

            }
            // consider version
            else if (!string.IsNullOrEmpty(version))
            {
                return !string.IsNullOrEmpty
                (
                    GetPomXPathExprValue(
                        string.Format("//pom:project/pom:dependencies/pom:dependency[./pom:artifactId = '{0}' and  ./pom:version = '{1}'][1]/pom:artifactId",
                        artifactId,
                        version)
                        )
                );

            }
            // just the artifact
            else if (!string.IsNullOrEmpty(artifactId))
            {
                return !string.IsNullOrEmpty
                (
                    GetPomXPathExprValue(
                        string.Format("//pom:project/pom:dependencies/pom:dependency[translate(./pom:artifactId, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '{0}'][1]/pom:artifactId",
                        artifactId.ToLower())
                        )
                );

            }


            return false;
        }




        public void RemovePomDependency(NPanday.Model.Pom.Dependency dependency)
        {
            RemovePomDependency(dependency.groupId, dependency.artifactId, dependency.version);
        }

        public void RemovePomDependency(string artifactId)
        {
            RemovePomDependency(null, artifactId, null);
        }

        public void RemovePomDependency(string groupId, string artifactId)
        {
            RemovePomDependency(groupId, artifactId, null);
        }

        public void RemovePomDependency(string groupId, string artifactId, string version)
        {

            if (string.IsNullOrEmpty(artifactId))
            {
                throw new Exception("Pom Dependency Removal Error, artifactId paramter must not be null");
            }

            if (!IsPomDependency(groupId, artifactId, version))
            {
                throw new Exception(string.Format("Pom Doesnot have a dependency {0}:{1}:{2}", groupId, artifactId, version));
            }


            NPanday.Model.Pom.Model model = ReadPomAsModel();

            List<Dependency> newDependencies = new List<Dependency>();
            if (model.dependencies != null)
            {
                foreach (Dependency dependency in model.dependencies)
                {
                    if (artifactId.ToLower().Equals(dependency.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                    {
                        // consider groupId and version if not empty
                        if (!string.IsNullOrEmpty(groupId)
                            && !string.IsNullOrEmpty(version))
                        {
                            if (!(groupId.Equals(groupId) && version.Equals(version)))
                            {
                                newDependencies.Add(dependency);
                            }
                        }
                        // consider groupId
                        else if (!string.IsNullOrEmpty(groupId))
                        {
                            if (!groupId.Equals(groupId))
                            {
                                newDependencies.Add(dependency);
                            }
                        }
                        // consider version
                        else if (!string.IsNullOrEmpty(version))
                        {
                            if (!groupId.Equals(groupId))
                            {
                                newDependencies.Add(dependency);
                            }
                        }

                    }
                    else
                    {
                        newDependencies.Add(dependency);
                    }
                }
                model.dependencies = newDependencies.ToArray();

                WriteModelToPom(model);
            }
        }

        public void AddPomDependency(string groupId, string artifactId)
        {
            AddPomDependency(groupId, artifactId, null, "dotnet-library", null, null);
        }

        public void AddPomDependency(string groupId, string artifactId, string version)
        {
            AddPomDependency(groupId, artifactId, version, "dotnet-library", null, null);
        }

        public void AddPomDependency(string groupId, string artifactId, string version, string type, string scope, string systemPath)
        {
            NPanday.Model.Pom.Dependency dependency = new NPanday.Model.Pom.Dependency(); ;

            dependency.artifactId = artifactId;
            dependency.groupId = groupId;
            dependency.version = version;
            dependency.type = type;
            dependency.scope = scope;
            if (!string.IsNullOrEmpty(systemPath))
            {
                dependency.systemPath = new FileInfo(systemPath).FullName;
            }
            AddPomDependency(dependency);

        }

        public void AddPomDependency(NPanday.Model.Pom.Dependency dependency)
        {

            if ("vb".Equals(NPandayCompilerPluginLanguage))
            {
                if (rspUtil.IsVbcRspIncluded(dependency.artifactId))
                    return;
            }
            else
            {
                if (rspUtil.IsCscRspIncluded((dependency.artifactId)))
                    return;
            }
            if (IsPomDependency(dependency))
            {
                throw new Exception(string.Format(
                    "Error in Adding Artifact Dependency [groupId: {0}, artifactId:{1}, version:{2}], \nDependency already exists in the Pom dependencies",
                    dependency.groupId,
                    dependency.artifactId,
                    dependency.version));
            }
            NPanday.Model.Pom.Model model = ReadPomAsModel();
            List<Dependency> dependencies = new List<Dependency>();
            if (model.dependencies != null)
            {
                dependencies.AddRange(model.dependencies);
            }
            dependencies.Add(dependency);
            model.dependencies = dependencies.ToArray();
            WriteModelToPom(model);

        }



        public static FileInfo FindPomFileUntil(DirectoryInfo start, DirectoryInfo until)
        {
            if (start == null || until == null)
            {
                throw new NullReferenceException("Null Reference Exception: start and until parameter must not be null");
            }

            DirectoryInfo currentDir = start;
            FileInfo pomFile = new FileInfo(currentDir + @"\pom.xml");

            // check if both parent and current directory is in the same folder
            if (!until.Root.FullName.Equals(start.Root.FullName, StringComparison.OrdinalIgnoreCase) || !until.Exists || !start.Exists)
            {
                // they are not in the same root directory or, until or start directory does not exists
                throw new Exception(string.Format("Folders are not on the same drive: {0} is not in the same disk drive with {1}", until.FullName, start.FullName));

            }

            if (!start.FullName.StartsWith(until.FullName))
            {
                // start is not a sub Directory of until
                throw new Exception(string.Format("Folder is not a subdirectory: {0} is not a subdirectory of {1}", start.FullName, until.FullName));
            }


            while (!until.FullName.Equals(currentDir.FullName, StringComparison.OrdinalIgnoreCase))
            {

                pomFile = new FileInfo(currentDir.FullName + @"\pom.xml");

                if (pomFile.Exists)
                {
                    return pomFile;
                }
                currentDir = currentDir.Parent;
            }


            pomFile = new FileInfo(currentDir.FullName + @"\pom.xml");
            if (pomFile.Exists)
            {
                return pomFile;
            }

            throw new Exception(string.Format("Pom file not found: pom.xml not found from folder: {0} down to folder: {1}", start.FullName, until.FullName));
        }

        public static string GetRelativePath(DirectoryInfo baseDir, DirectoryInfo dir)
        {
            if (dir == null)
            {
                return null;
            }

            // if the file is not on the same root with the baseDir
            // then return the file, its point less to get its relative path
            // since they are not in the same root

            if (baseDir == null || !baseDir.Root.FullName.Equals(dir.Root.FullName, StringComparison.OrdinalIgnoreCase))
            {
                return dir.ToString();
            }


            DirectoryInfo commonDir = GetCommonDirectory(baseDir, dir);
            string relative = "";

            string[] baseArr = RemoveCommonDirAndTokenize(commonDir, baseDir);
            foreach (string b in baseArr)
            {
                relative += @"..\";
            }

            string[] fileArr = RemoveCommonDirAndTokenize(commonDir, dir);
            foreach (string f in fileArr)
            {
                relative += f + @"\";
            }

            return NormalizeFileToWindowsStyle(relative);
        }


        public static string GetRelativePath(DirectoryInfo baseDir, FileInfo file)
        {
            return NormalizeFileToWindowsStyle(GetRelativePath(baseDir, file.Directory) + @"\" + file.Name);
        }



        public static DirectoryInfo GetCommonDirectory(DirectoryInfo dir1, DirectoryInfo dir2)
        {
            string strDir = "";
            string[] arrDir1 = NormalizeFileToWindowsStyle(dir1).FullName.Split('\\');
            string[] arrDir2 = NormalizeFileToWindowsStyle(dir2).FullName.Split('\\');

            int len = (arrDir1.Length < arrDir2.Length ? arrDir1.Length : arrDir2.Length);

            if (len == 0)
            {
                return dir1.Root;
            }

            for (int i = 0; i < len; i++)
            {
                if (!arrDir1[i].Equals(arrDir2[i], StringComparison.OrdinalIgnoreCase))
                {
                    break;
                }


                strDir += arrDir1[i] + @"\";
            }

            return new DirectoryInfo(strDir);

        }

        private static string[] RemoveCommonDirAndTokenize(DirectoryInfo commonDir, FileInfo file)
        {

            string[] arrcommonDir = NormalizeFileToWindowsStyle(commonDir).FullName.Split('\\');
            string[] arrdir = NormalizeFileToWindowsStyle(file).FullName.Split('\\');

            string[] str = new string[arrdir.Length - arrcommonDir.Length];

            for (int i = arrcommonDir.Length, j = 0; i < arrdir.Length; i++, j++)
            {
                str[j] = arrdir[i];
            }

            return str;

        }

        private static string[] RemoveCommonDirAndTokenize(DirectoryInfo commonDir, DirectoryInfo dir)
        {
            return RemoveCommonDirAndTokenize(commonDir, new FileInfo(dir.FullName));
        }

        public static string NormalizeFileToWindowsStyle(string fileName)
        {
            if (string.IsNullOrEmpty(fileName))
            {
                return fileName;
            }

            fileName = fileName.Replace('/', '\\');

            // remove all instance of double slashes
            while (fileName.Contains("\\\\"))
            {
                fileName = fileName.Replace("\\\\", "\\");
            }

            if (!fileName.EndsWith("..\\") && fileName.EndsWith("\\"))
            {
                fileName = fileName.Substring(0, fileName.Length - 1);
            }


            // remove slash \ if its on the first char
            if (fileName.StartsWith("\\"))
            {
                fileName = fileName.Substring(1);
            }

            return fileName;
        }


        public static FileInfo NormalizeFileToWindowsStyle(FileInfo file)
        {
            return new FileInfo(NormalizeFileToWindowsStyle(file.ToString()));
        }

        public static DirectoryInfo NormalizeFileToWindowsStyle(DirectoryInfo dir)
        {
            return new DirectoryInfo(NormalizeFileToWindowsStyle(dir.ToString()));
        }

        #region AddWebReference
        public void AddWebReference(string name, string path, string output)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            if (!isWebRefExistingInPom(path, model))
            {
                bool hasWSDLPlugin = false;
                if (model != null && model.build != null && model.build.plugins != null)
                {
                    foreach (Plugin plugin in model.build.plugins)
                    {
                        if (isWsdlPlugin(plugin))
                        {
                            hasWSDLPlugin = true;
                            addPluginExecution(plugin, "wsdl", null);

                            addWebConfiguration(plugin, "webreferences", name, path, output);
                        }
                    }
                    if (!hasWSDLPlugin)
                    {
                        Plugin webReferencePlugin = addPlugin(model,
                            "org.apache.npanday.plugins",
                            "maven-wsdl-plugin",
                            null,
                            false
                            );
                        addPluginExecution(webReferencePlugin, "wsdl", null);

                        addWebConfiguration(webReferencePlugin, "webreferences", name, path, output);
                    }
                }

                foreach (Plugin plugin in model.build.plugins)
                {
                    if ("org.apache.npanday.plugins".Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                        && "maven-compile-plugin".Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                    {
                        if (plugin.configuration == null || plugin.configuration.Any == null)
                        {
                            break;
                        }
                        XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                        for (int count = elems.Length; count-- > 0; )
                        {
                            if ("includeSources".Equals(elems[count].Name))
                            {

                                XmlDocument xmlDocument = new XmlDocument();
                                XmlElement elem = xmlDocument.CreateElement("includeSources", @"http://maven.apache.org/POM/4.0.0");

                                //LOOP THROUGH EXISTING AND ADD
                                //GET .CS FILE AND ADD
                                foreach (XmlNode n in elems[count].ChildNodes)
                                {
                                    if ("includeSource".Equals(n.Name))
                                    {
                                        XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");

                                        node.InnerText = n.InnerText.Replace("\\","/");
                                        if ((!elem.InnerXml.Contains(node.InnerText)) && (!node.InnerText.Contains(".disco")))
                                        {
                                            elem.AppendChild(node);
                                        }
                                    }
                                }
                                DirectoryInfo fullPath = new FileInfo(Path.Combine(pom.Directory.FullName, path.Trim('\r', ' ', '\n'))).Directory;
                                foreach (FileInfo file in fullPath.GetFiles("*.cs"))
                                {
                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, "includeSource", @"http://maven.apache.org/POM/4.0.0");

                                    node.InnerText = GetRelativePath(pom.Directory, file);
                                    node.InnerText = node.InnerText.Replace("\\", "/");
                                    if (!elem.InnerText.Contains(node.InnerText))
                                    {
                                        elem.AppendChild(node);
                                    }
                                }
                                foreach (FileInfo file in fullPath.GetFiles("*.vb"))
                                {
                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, "includeSource", @"http://maven.apache.org/POM/4.0.0");

                                    node.InnerText = GetRelativePath(pom.Directory, file);
                                    node.InnerText = node.InnerText.Replace("\\", "/");
                                    if (!elem.InnerText.Contains(node.InnerText))
                                    {
                                        elem.AppendChild(node);
                                    }
                                }
                                elems[count] = elem;

                                break;
                            }
                        }
                    }
                }
                WriteModelToPom(model);
            }
        }

        Plugin addPlugin(NPanday.Model.Pom.Model model, string groupId, string artifactId, string version, bool extensions)
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

        public void AddMavenCompilePluginConfiguration(string pluginGroupId, string pluginArtifactId, string confPropCollection, string confProp, string confPropVal)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            bool configExists = false;

            foreach (Plugin plugin in model.build.plugins)
            {
                XmlDocument xmlDocument = new XmlDocument();

                if (pluginGroupId.Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                    && pluginArtifactId.Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    if (plugin.configuration == null && plugin.configuration.Any == null)
                    {
                        return;
                    }
                    XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                    for (int count = elems.Length; count-- > 0; )
                    {
                        if (confPropCollection.Equals(elems[count].Name))
                        {
                            XmlElement elem = xmlDocument.CreateElement(confPropCollection, @"http://maven.apache.org/POM/4.0.0");

                            //Loop throught existing item and
                            //append everything including the newly added item
                            foreach (XmlNode n in elems[count].ChildNodes)
                            {
                                if (confProp.Equals(n.Name))
                                {
                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");
                                    node.InnerText = n.InnerText.Replace("\\", "/");
                                    elem.AppendChild(node);
                                    if (n.InnerText.Equals(confPropVal))
                                    {
                                        configExists = true;
                                    }
                                }
                            }

                            if (!configExists)
                            {
                                XmlNode nodeAdded = xmlDocument.CreateNode(XmlNodeType.Element, confProp, @"http://maven.apache.org/POM/4.0.0");

                                nodeAdded.InnerText = confPropVal.Replace("\\", "/");
                                if (!elems[count].InnerXml.Contains(nodeAdded.InnerText))
                                {
                                    elem.AppendChild(nodeAdded);
                                }
                                elems[count] = elem;
                            }

                            break;
                        }
                    }
                }
            }

            WriteModelToPom(model);

        }

        public void RenameMavenCompilePluginConfiguration(string pluginGroupId, string pluginArtifactId, string confPropCollection, string confProp, string confPropVal, string newPropVal)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            foreach (Plugin plugin in model.build.plugins)
            {
                XmlDocument xmlDocument = new XmlDocument();

                if (pluginGroupId.Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                    && pluginArtifactId.Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    if (plugin.configuration == null && plugin.configuration.Any == null)
                    {
                        return;
                    }
                    XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                    for (int count = elems.Length; count-- > 0; )
                    {
                        if (confPropCollection.Equals(elems[count].Name))
                        {
                            XmlElement elem = xmlDocument.CreateElement(confPropCollection, @"http://maven.apache.org/POM/4.0.0");

                            //Loop throught existing item and
                            //append everything except for the item to change
                            //check for the item and change it
                            foreach (XmlNode n in elems[count].ChildNodes)
                            {
                                if (confProp.Equals(n.Name))
                                {
                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");
                                    if (n.InnerText.Equals(confPropVal))
                                    {
                                        node.InnerText = newPropVal;
                                    }
                                    else
                                    {
                                        node.InnerText = n.InnerText;
                                    }
                                    elem.AppendChild(node);
                                }
                            }

                            elems[count] = elem;

                            break;
                        }
                    }
                }
            }
            WriteModelToPom(model);
        }


        public void RemoveMavenCompilePluginConfiguration(string pluginGroupId, string pluginArtifactId, string confPropCollection, string confProp, string confPropVal)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            foreach (Plugin plugin in model.build.plugins)
            {
                XmlDocument xmlDocument = new XmlDocument();

                if (pluginGroupId.Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                    && pluginArtifactId.Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    if (plugin.configuration == null && plugin.configuration.Any == null)
                    {
                        return;
                    }
                    XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                    for (int count = elems.Length; count-- > 0; )
                    {
                        if (confPropCollection.Equals(elems[count].Name))
                        {
                            XmlElement elem = xmlDocument.CreateElement(confPropCollection, @"http://maven.apache.org/POM/4.0.0");

                            //Loop throught existing item and
                            //append everything except for the item to change
                            //check for the item and change it
                            foreach (XmlNode n in elems[count].ChildNodes)
                            {
                                if (confProp.Equals(n.Name))
                                {
                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");
                                    if (!n.InnerText.Equals(confPropVal))
                                    {
                                        node.InnerText = n.InnerText;
                                        elem.AppendChild(node);
                                    }
                                }
                            }

                            elems[count] = elem;

                            break;
                        }
                    }
                }
            }
            WriteModelToPom(model);
        }




        public void AddMavenResxPluginConfiguration(string pluginGroupId, string pluginArtifactId, string confPropCollection, string confProp, string sourceFile, string resxName)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            foreach (Plugin plugin in model.build.plugins)
            {
                XmlDocument xmlDocument = new XmlDocument();

                if (pluginGroupId.Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                    && pluginArtifactId.Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    if (plugin.configuration == null && plugin.configuration.Any == null)
                    {
                        return;
                    }
                    XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                    for (int count = elems.Length; count-- > 0; )//(XmlElement elem in ((XmlElement[])plugin.configuration.Any))
                    {

                        if (confPropCollection.Equals(elems[count].Name))
                        {
                            XmlElement elem = xmlDocument.CreateElement(confPropCollection, @"http://maven.apache.org/POM/4.0.0");

                            //Loop throught existing item and
                            //append everything including the newly added item



                            foreach (XmlNode n in elems[count].ChildNodes)
                            {
                                if (confProp.Equals(n.Name))
                                {

                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");
                                    XmlNodeList nList = n.ChildNodes;
                                    foreach (XmlNode nChild in nList)
                                    {
                                        XmlNode innerChild = xmlDocument.CreateNode(XmlNodeType.Element, nChild.Name, @"http://maven.apache.org/POM/4.0.0");
                                        innerChild.InnerText = nChild.InnerText;
                                        node.AppendChild(innerChild);
                                    }


                                    elem.AppendChild(node);

                                }
                            }

                            XmlNode confPropNode = xmlDocument.CreateNode(XmlNodeType.Element, confProp, @"http://maven.apache.org/POM/4.0.0");
                            XmlNode nodeSourceFile = xmlDocument.CreateNode(XmlNodeType.Element, "sourceFile", @"http://maven.apache.org/POM/4.0.0");
                            XmlNode nodeResxName = xmlDocument.CreateNode(XmlNodeType.Element, "name", @"http://maven.apache.org/POM/4.0.0");
                            nodeSourceFile.InnerText = sourceFile;
                            nodeResxName.InnerText = resxName;

                            confPropNode.AppendChild(nodeSourceFile);
                            confPropNode.AppendChild(nodeResxName);

                            elem.AppendChild(confPropNode);

                            elems[count] = elem;

                            break;
                        }
                    }
                }
            }

            WriteModelToPom(model);

        }

        public void RenameMavenResxPluginConfiguration(string pluginGroupId, string pluginArtifactId, string confPropCollection, string confProp, string sourceFile, string resxName, string newSourceFile, string newResxName)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            foreach (Plugin plugin in model.build.plugins)
            {
                XmlDocument xmlDocument = new XmlDocument();

                if (pluginGroupId.Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                    && pluginArtifactId.Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    if (plugin.configuration == null && plugin.configuration.Any == null)
                    {
                        return;
                    }
                    XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                    for (int count = elems.Length; count-- > 0; )//(XmlElement elem in ((XmlElement[])plugin.configuration.Any))
                    {

                        if (confPropCollection.Equals(elems[count].Name))
                        {
                            XmlElement elem = xmlDocument.CreateElement(confPropCollection, @"http://maven.apache.org/POM/4.0.0");

                            //Loop throught existing item and
                            //append everything including the newly added item

                            foreach (XmlNode n in elems[count].ChildNodes)
                            {
                                if (confProp.Equals(n.Name))
                                {

                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");
                                    XmlNodeList nList = n.ChildNodes;
                                    foreach (XmlNode nChild in nList)
                                    {
                                        XmlNode innerChild = xmlDocument.CreateNode(XmlNodeType.Element, nChild.Name, @"http://maven.apache.org/POM/4.0.0");
                                        if (nChild.InnerText.Equals(sourceFile) && nChild.Name.Equals("sourceFile"))
                                        {
                                            innerChild.InnerText = newSourceFile;
                                        }
                                        else if (nChild.InnerText.Equals(resxName) && nChild.Name.Equals("name"))
                                        {
                                            innerChild.InnerText = newResxName;
                                        }
                                        else
                                        {
                                            innerChild.InnerText = nChild.InnerText;

                                        }
                                        node.AppendChild(innerChild);

                                    }
                                    elem.AppendChild(node);

                                }
                            }
                            elems[count] = elem;

                            break;
                        }
                    }
                }
            }
            WriteModelToPom(model);
        }



        public void RemoveMavenResxPluginConfiguration(string pluginGroupId, string pluginArtifactId, string confPropCollection, string confProp, string sourceFile, string resxName)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();

            foreach (Plugin plugin in model.build.plugins)
            {
                XmlDocument xmlDocument = new XmlDocument();

                if (pluginGroupId.Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                    && pluginArtifactId.Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                {
                    if (plugin.configuration == null && plugin.configuration.Any == null)
                    {
                        return;
                    }
                    XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                    for (int count = elems.Length; count-- > 0; )//(XmlElement elem in ((XmlElement[])plugin.configuration.Any))
                    {

                        if (confPropCollection.Equals(elems[count].Name))
                        {
                            XmlElement elem = xmlDocument.CreateElement(confPropCollection, @"http://maven.apache.org/POM/4.0.0");

                            //Loop throught existing item and
                            //append everything including the newly added item

                            foreach (XmlNode n in elems[count].ChildNodes)
                            {
                                if (confProp.Equals(n.Name))
                                {

                                    XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");
                                    XmlNodeList nList = n.ChildNodes;
                                    foreach (XmlNode nChild in nList)
                                    {
                                        XmlNode innerChild = xmlDocument.CreateNode(XmlNodeType.Element, nChild.Name, @"http://maven.apache.org/POM/4.0.0");
                                        if (nChild.Name.Equals("sourceFile"))
                                        {
                                            if (!nChild.InnerText.Equals(sourceFile))
                                            {
                                                innerChild.InnerText = nChild.InnerText;
                                                node.AppendChild(innerChild);
                                            }
                                        }

                                        if (nChild.Name.Equals("name"))
                                        {
                                            if (!nChild.InnerText.Equals(resxName))
                                            {
                                                innerChild.InnerText = nChild.InnerText;
                                                node.AppendChild(innerChild);
                                            }
                                        }
                                    }
                                    if (node.HasChildNodes)
                                    {
                                        elem.AppendChild(node);
                                    }

                                }
                            }
                            elems[count] = elem;

                            break;
                        }
                    }
                }
            }
            WriteModelToPom(model);
        }


        void addPluginExecution(Plugin plugin, string goal, string phase)
        {
            if (string.IsNullOrEmpty(goal))
            {
                // there is nothing to write
                return;
            }
            if (!isExistingPluginExecution(plugin, goal))
            {
                List<PluginExecution> list = new List<PluginExecution>();
                if (plugin.executions == null)
                {
                    plugin.executions = new List<PluginExecution>().ToArray();
                    list.AddRange(plugin.executions);

                }
                else
                {
                    list.AddRange(plugin.executions);
                }
                PluginExecution exe = new PluginExecution();


                exe.goals = new string[] { goal };


                list.Add(exe);


                plugin.executions = list.ToArray();
            }
        }

        bool isExistingPluginExecution(Plugin plugin, string goal)
        {
            if (plugin.executions == null)
                return false;

            foreach (PluginExecution execution in plugin.executions)
            {
                foreach (string existingGoal in execution.goals)
                {
                    if (existingGoal.Equals(goal))
                        return true;
                }
            }
            return false;
        }

        bool isWsdlPlugin(Plugin plugin)
        {
            return (!string.IsNullOrEmpty(plugin.groupId) &&
                                !string.IsNullOrEmpty(plugin.artifactId) &&
                                    plugin.groupId.Equals("org.apache.npanday.plugins") &&
                                        plugin.artifactId.Equals("maven-wsdl-plugin"));
        }

        void addWebConfiguration(Plugin plugin, string parentTag, string name, string path, string output)
        {
            if (string.IsNullOrEmpty(parentTag))
            {
                // there is nothing to write
                return;
            }

            if (string.IsNullOrEmpty(name))
            {
                // there is nothing to write
                return;
            }

            if (string.IsNullOrEmpty(path))
            {
                // there is nothing to write
                return;
            }
            if (string.IsNullOrEmpty(output))
            {
                int endIndex = path.LastIndexOf('\\');
                if (endIndex < 0)
                {
                    output = path;
                }
                else
                {
                    output = path.Substring(0, endIndex);
                }
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
            XmlElement elem = getWebReferencesElement(elems.ToArray(), "webreferences");

            XmlDocument xmlDocument = new XmlDocument();





            XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, "webreference", @"http://maven.apache.org/POM/4.0.0");
            XmlNode nodeName = xmlDocument.CreateNode(XmlNodeType.Element, "namespace", @"http://maven.apache.org/POM/4.0.0");
            XmlNode nodePath = xmlDocument.CreateNode(XmlNodeType.Element, "path", @"http://maven.apache.org/POM/4.0.0");
            XmlNode nodeOutput = xmlDocument.CreateNode(XmlNodeType.Element, "output", @"http://maven.apache.org/POM/4.0.0");
            nodeName.InnerText = name;
            nodePath.InnerText = path != null ? path.Replace("\\", "/") : path;
            nodeOutput.InnerText = output != null ? output.Replace("\\", "/") : output;

            node.AppendChild(nodeName);
            node.AppendChild(nodePath);
            node.AppendChild(nodeOutput);
            if (elem == null)
            {
                elem = xmlDocument.CreateElement(parentTag, @"http://maven.apache.org/POM/4.0.0");
                elem.AppendChild(node);
                elems.Add(elem);
            }
            else
            {
                elem.AppendChild(elem.OwnerDocument.ImportNode(node, true));
            }



            plugin.configuration.Any = elems.ToArray();
        }

        XmlElement getWebReferencesElement(XmlElement[] elems, string elementName)
        {
            foreach (XmlElement elem in elems)
            {
                if (!string.IsNullOrEmpty(elem.Name) && elem.Name.ToLower().Equals(elementName.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                    return elem;
            }
            return null;
        }

        #endregion
        #region RemoveWebReference
        public void RemoveWebReference(string path, string name)
        {
            NPanday.Model.Pom.Model model = ReadPomAsModel();
            if (model != null && model.build != null && model.build.plugins != null)
            {
                foreach (Plugin plugin in model.build.plugins)
                {
                    if (isWsdlPlugin(plugin))
                    {
                        removeWebConfiguration(plugin, name);

                        if (!isWebRefEmpty)
                        {
                            WriteModelToPom(model);
                        }
                    }

                    if ("org.apache.npanday.plugins".Equals(plugin.groupId.ToLower(), StringComparison.InvariantCultureIgnoreCase)
                        && "maven-compile-plugin".Equals(plugin.artifactId.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                    {
                        XmlElement[] elems = ((XmlElement[])plugin.configuration.Any);
                        for (int count = elems.Length; count-- > 0; )
                        {

                            if ("includeSources".Equals(elems[count].Name))
                            {

                                XmlDocument xmlDocument = new XmlDocument();
                                XmlElement elem = xmlDocument.CreateElement("includeSources", @"http://maven.apache.org/POM/4.0.0");

                                //LOOP THROUGH EXISTING AND ADD IF ITS NOT A WEBREFERENCE
                                string compareStr = GetRelativePath(pom.Directory, new DirectoryInfo(path));
                                foreach (XmlNode n in elems[count].ChildNodes)
                                {
                                    if ("includeSource".Equals(n.Name))
                                    {
                                        if (n.InnerText != null && !n.InnerText.Trim().StartsWith(compareStr.Replace("\\", "/")))
                                        {
                                            if (!n.InnerText.Contains(name))
                                            {
                                                XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, n.Name, @"http://maven.apache.org/POM/4.0.0");

                                                node.InnerText = n.InnerText;
                                                elem.AppendChild(node);
                                            }
                                        }
                                    }
                                }

                                elems[count] = elem;
                                WriteModelToPom(model);
                                break;
                            }
                        }
                    }
                }
            }

        }

        void removeWebConfiguration(Plugin plugin, string name)
        {

            if (string.IsNullOrEmpty(name))
            {
                //nothing to remove
                return;
            }

            XmlElement[] elems;
            if (plugin.configuration.Any != null)
            {
                elems = plugin.configuration.Any;


                foreach (XmlElement elem in elems)
                {
                    XmlNode removeMe = null;
                    if (!string.IsNullOrEmpty(elem.Name) &&
                        elem.Name.ToLower().Equals("webreferences", StringComparison.InvariantCultureIgnoreCase))
                    {
                        foreach (XmlNode node in elem.ChildNodes)
                        {
                            if (node.Name.ToLower().Equals("webreference", StringComparison.InvariantCultureIgnoreCase))
                            {
                                foreach (XmlNode node1 in node.ChildNodes)
                                {
                                    if (node1.Name.ToLower().Equals("namespace", StringComparison.InvariantCultureIgnoreCase) &&
                                        node1.InnerText.ToLower().Equals(name.ToLower(), StringComparison.InvariantCultureIgnoreCase))
                                    {
                                        removeMe = node;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (removeMe != null)
                    {
                        elem.RemoveChild(removeMe);
                    }
                    if (elem.InnerText.Equals(string.Empty))
                    {
                        isWebRefEmpty = true;
                    }

                }

                if (isWebRefEmpty)
                {
                    DeletePlugin(plugin);
                }

            }

        }
        #endregion
        #region RenameWebReference
        public void RenameWebReference(string fullpath, string oldName, string newName, string path, string output)
        {
            string compareStr = Path.Combine(fullpath.Substring(0, fullpath.LastIndexOf("\\")), oldName);
            RemoveWebReference(compareStr, oldName);
            AddWebReference(newName, path, output);
        }
        #endregion


        //check if web reference is existing
        public bool isWebRefExisting(string name)
        {
            bool exists = false;
            StreamReader sr = new StreamReader(pom.FullName);
            String temp = sr.ReadLine();

            try
            {
                while (temp != null)
                {
                    if (temp.Contains(name + ".wsdl"))
                    {
                        exists = true;
                        break;
                    }
                    temp = sr.ReadLine();
                }
            }
            catch
            {
                throw;
            }
            finally
            {
                try
                {
                    sr.Close();
                    sr.Dispose();
                }
                catch
                { }
            }
            return exists;

        }

        public bool isWebRefExistingInPom(string path, NPanday.Model.Pom.Model model)
        {
            bool exists = false;
            List<NPanday.Model.Pom.Plugin> plugins = new List<NPanday.Model.Pom.Plugin>();
            if (model.build.plugins != null)
            {
                foreach (Plugin item in model.build.plugins)
                {
                    if (item.artifactId.Equals("maven-wsdl-plugin") && item.configuration != null)
                    {
                        List<XmlElement> elems = new List<XmlElement>();
                        elems.AddRange(item.configuration.Any);
                        XmlElement elem = getWebReferencesElement(elems.ToArray(), "webreferences");
                        if (elem.InnerXml.Contains(path.Replace("\\", "/")))
                        {
                            exists = true;
                        }
                    }
                }
            }
            return exists;
        }
    }
}

