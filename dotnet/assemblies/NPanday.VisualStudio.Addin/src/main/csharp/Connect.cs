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

#region Using
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Net;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
//using System.Web.Services.Protocols;
using System.Windows.Forms;
using System.Xml;
using EnvDTE;
using EnvDTE80;
using Extensibility;
using log4net;
using log4net.Config;
using log4net.Core;
using log4net.Repository.Hierarchy;
using Microsoft.VisualStudio.CommandBars;
using NPanday.Artifact;
using NPanday.Model.Pom;
using NPanday.ProjectImporter.Parser.VisualStudioProjectTypes;
using NPanday.Utils;
using NPanday.VisualStudio.Addin.Commands;
using NPanday.VisualStudio.Addin.Helper;
using VSLangProj;
using VSLangProj80;
using NPanday.ProjectImporter.Utils;

#endregion

namespace NPanday.VisualStudio.Addin
{

    #region Connect
    /// <summary>The object for implementing an Add-in.</summary>
    /// <seealso class='IDTExtensibility2' />
    public class Connect : IDTExtensibility2, IDTCommandTarget, IWebServicesRefUtils
    {
        #region Connect()
        /// <summary>
        /// Implements the constructor for the Add-in object.
        /// Place your initialization code within this method.
        /// </summary>
        public Connect()
        {
        }
        #endregion
        #region Clearing OutputWindow upon closing solution
        //to hold eventhandler for solution
        private static EnvDTE.SolutionEvents globalSolutionEvents;

        //to hold eventhandler for projects
        private static EnvDTE.ProjectItemsEvents projectItemsEvents;

        void SolutionEvents_BeforeClosing()
        {
            mavenRunner.ClearOutputWindow();
            outputWindowPane.Clear();
        }

        void SolutionEvents_Opened()
        {
            if (_applicationObject != null && _applicationObject.Solution != null)
            {
                attachReferenceEvent();
            }
        }
        
        void SolutionEvents_ProjectAdded(EnvDTE.Project project)
        {
            if (_applicationObject != null && _applicationObject.Solution != null)
            {
                attachReferenceEvent();
            }
        }

        //to hold eventhandler for projectItemsEvents
        void ProjectItemEvents_ItemAdded(ProjectItem projectItem)
        {
            if (_applicationObject != null && projectItem != null)
            {
                PomHelperUtility pomUtil = createPomUtility(projectItem.ContainingProject);
                if (pomUtil == null)
                    return;

                // added configuration when including web or service reference

                if (!pomUtil.isWebRefExisting(projectItem.Name))
                {
                    string refType = IsWebReference(projectItem) ? Messages.MSG_D_WEB_REF : Messages.MSG_D_SERV_REF;
                    String reference = GetReference(projectItem, refType);
                    if (reference != null)
                    {
                        addWebReference(pomUtil, projectItem.Name, refType + "\\" + projectItem.Name + "\\" + reference, string.Empty);
                    }
                }

                //determine which plugin the projectItem belongs to

                if (projectItem.Name.Contains(".cs") || projectItem.Name.Contains(".vb") || projectItem.Name.Contains(".disco"))
                {
                    //change addpluginConfiguration to accept xmlElement instead
                    pomUtil.AddMavenCompilePluginConfiguration("org.apache.npanday.plugins", "maven-compile-plugin", "includeSources", "includeSource", GetRelativePathToProject(projectItem, projectItem.Name));
                }

                if (projectItem.Name.Contains(".resx"))
                {
                    string resxName = projectItem.ContainingProject.Name + "." + projectItem.Name.Replace(".resx", "");

                    log.DebugFormat("Adding resource {0}", resxName);

                    //check if resx plugin already exists
                    if (!pomUtil.HasPlugin("org.apache.npanday.plugins", "maven-resgen-plugin"))
                    {
                        try
                        {
                            XmlDocument xmlDocument = new XmlDocument();

                            PluginConfiguration pluginConf = new PluginConfiguration();

                            XmlElement nodeCollection = xmlDocument.CreateElement("embeddedResources", @"http://maven.apache.org/POM/4.0.0");
                            //XmlNode nodeCollection = xmlDocument.CreateNode(XmlNodeType.Element, "embeddededResources", @"http://maven.apache.org/POM/4.0.0");
                            XmlNode node = xmlDocument.CreateNode(XmlNodeType.Element, "embeddededResource", @"http://maven.apache.org/POM/4.0.0");
                            XmlNode nodeSrc = xmlDocument.CreateNode(XmlNodeType.Element, "sourceFile", @"http://maven.apache.org/POM/4.0.0");
                            XmlNode nodeName = xmlDocument.CreateNode(XmlNodeType.Element, "name", @"http://maven.apache.org/POM/4.0.0");

                            nodeSrc.InnerText = projectItem.Name;
                            nodeName.InnerText = resxName;
                            node.AppendChild(nodeSrc);
                            node.AppendChild(nodeName);
                            nodeCollection.AppendChild(node);
                            List<XmlElement> anyHolder = new List<XmlElement>();
                            anyHolder.Add(nodeCollection);
                            pluginConf.Any = anyHolder.ToArray();

                            pomUtil.AddPlugin("org.apache.npanday.plugins", "maven-resgen-plugin", null, true, pluginConf);
                        }
                        catch (Exception e)
                        {
                            //Suppressed Exception for malfored pom
                        }

                    }
                    //add plugin conifguration
                    else
                    {
                        pomUtil.AddMavenResxPluginConfiguration("org.apache.npanday.plugins", "maven-resgen-plugin", "embeddedResources", "embeddedResource", projectItem.Name, resxName);
                    }

                    string culture = MSBuildUtils.DetermineResourceCulture(projectItem.Name);
                    if (!string.IsNullOrEmpty(culture))
                    {
                        log.DebugFormat("Resource has culture {0}, adding MSBuild plugin to POM", culture);
                        if (!pomUtil.HasPlugin("org.apache.npanday.plugins", "NPanday.Plugin.Msbuild.JavaBinding"))
                        {
                            pomUtil.AddPlugin("org.apache.npanday.plugins", "NPanday.Plugin.Msbuild.JavaBinding", null, false, null, "compile");
                        }
                    }
                }
            }
        }

        private PomHelperUtility createPomUtility(Project project)
        {
            PomHelperUtility util = null;
            if (_applicationObject != null && _applicationObject.Solution != null)
            {
                try
                {
                    if (_applicationObject.Solution.FullName != null && _applicationObject.Solution.FullName.Length > 0)
                    {
                        util = new PomHelperUtility(new FileInfo(_applicationObject.Solution.FullName), new FileInfo(project.FullName));
                    }
                    else
                    {
                        log.Debug("POM utility was requested before solution was created!");
                        util = new PomHelperUtility(new FileInfo(project.FullName), new FileInfo(project.FullName));
                    }
                }
                catch (Exception e)
                {
                    log.Debug("Not updating POM: " + e.Message, e);
                }
            }
            return util;
        }

        void ProjectItemEvents_ItemRemoved(ProjectItem projectItem)
        {
            if (_applicationObject != null && projectItem != null)
            {
                if (_applicationObject != null && projectItem != null)
                {
                    PomHelperUtility pomUtil = createPomUtility(projectItem.ContainingProject);
                    if (pomUtil == null)
                        return;

                    // remove web reference configuration in pom.xml when using "Exclude in Project"

                    string fullPath = projectItem.get_FileNames(1);
                    string refType = Messages.MSG_D_WEB_REF;

                    if (fullPath.StartsWith(Path.GetDirectoryName(projectItem.ContainingProject.FullName) + "\\" + Messages.MSG_D_SERV_REF))
                    {
                        refType = Messages.MSG_D_SERV_REF;
                    }

                    string reference = GetReference(projectItem, refType);
                    if (reference != null)
                    {
                        string path = GetReferencePath(projectItem, refType);
                        pomUtil.RemoveWebReference(path, projectItem.Name);
                    }

                    if (projectItem.Name.Contains(".cs") || projectItem.Name.Contains(".vb"))
                    {
                        //change addpluginConfiguration to accept xmlElement instead
                        pomUtil.RemoveMavenCompilePluginConfiguration("org.apache.npanday.plugins", "maven-compile-plugin", "includeSources", "includeSource", GetRelativePathToProject(projectItem, null));
                    }

                    if (projectItem.Name.Contains(".resx"))
                    {
                        string resxName = projectItem.ContainingProject.Name + "." + projectItem.Name.Replace(".resx", "");
                        pomUtil.RemoveMavenResxPluginConfiguration("org.apache.npanday.plugins", "maven-resgen-plugin", "embeddedResources", "embeddedResource", projectItem.Name, resxName);
                    }
                }
            }

        }

        void ProjectItemEvents_ItemRenamed(ProjectItem projectItem, string oldName)
        {
            if (_applicationObject != null && projectItem != null)
            {
                PomHelperUtility pomUtil = createPomUtility(projectItem.ContainingProject);
                if (pomUtil != null)
                {
                    if (projectItem.Name.Contains(".cs") || projectItem.Name.Contains(".vb"))
                    {
                        //change addpluginConfiguration to accept xmlElement instead
                        pomUtil.RenameMavenCompilePluginConfiguration("org.apache.npanday.plugins", "maven-compile-plugin", "includeSources", "includeSource", GetRelativePathToProject(projectItem, oldName), GetRelativePathToProject(projectItem, null));
                    }

                    if (projectItem.Name.Contains(".resx"))
                    {
                        string resxName = projectItem.ContainingProject.Name + "." + projectItem.Name.Replace(".resx", "");
                        string oldResxName = projectItem.ContainingProject.Name + "." + oldName.Replace(".resx", "");
                        pomUtil.RenameMavenResxPluginConfiguration("org.apache.npanday.plugins", "maven-resgen-plugin", "embeddedResources", "embeddedResource", oldName, oldResxName, projectItem.Name, resxName);
                    }
                }
            }

        }

        /// <summary>
        /// Returns either a relative path to project (if a project item is assciated with a file - like *.cs) or just the name of project item
        /// </summary>
        /// <param name="projectItem"></param>
        /// <returns></returns>
        private static string GetRelativePathToProject(ProjectItem projectItem, string fileName)
        {
            if (projectItem.FileCount == 1)
            {
                Uri fullPathUri = fileName == null ? new Uri(projectItem.get_FileNames(0)) : new Uri(Path.Combine(Path.GetDirectoryName(projectItem.get_FileNames(0)), fileName));
                Uri projectUri = new Uri(Path.GetDirectoryName(projectItem.ContainingProject.FullName) + Path.DirectorySeparatorChar);
                return projectUri.MakeRelativeUri(fullPathUri).ToString().Replace("%20", " ");
            }
            return projectItem.Name;
        }

        private static string GetReferencePath(ProjectItem projectItem, string refType)
        {
            return Path.GetDirectoryName(projectItem.ContainingProject.FullName) + "\\" + refType + "\\" + projectItem.Name;
        }

        private static string GetReference(ProjectItem projectItem, string refType)
        {
            string path = GetReferencePath(projectItem, refType);
            if (Directory.Exists(path))
            {
                string[] files = Directory.GetFiles(path, "*.wsdl");

                if (files.Length > 0)
                {
                    return Path.GetFileName(files[0]);
                }
            }

            return null;
        }

        private static bool IsWebReference(ProjectItem item)
        {
            if (item.ContainingProject.Object is VSProject)
            {
                ProjectItem webrefs = ((VSProject)item.ContainingProject.Object).WebReferencesFolder;
                if (webrefs != null && webrefs.ProjectItems != null)
                {
                    foreach (ProjectItem webref in webrefs.ProjectItems)
                    {
                        if (webref == item)
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        #endregion
        static bool projectRefEventLoaded;

        #region OnConnection(object,ext_ConnectMode,object,Array)
        /// <summary>
        /// Used for SaveAllDocuments
        /// </summary>
        private CommandBarControl saveAllControl;



        /// <summary>
        /// Implements the OnConnection method of the IDTExtensibility2 interface.
        /// Receives notification that the Add-in is being loaded.
        /// </summary>
        /// <param term='application'>Root object of the host application.</param>
        /// <param term='connectMode'>Describes how the Add-in is being loaded.</param>
        /// <param term='addInInst'>Object representing this Add-in.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnConnection(object application, ext_ConnectMode connectMode, object addInInst, ref Array custom)
        {
            if (_applicationObject == null)
            {
                // this should only be done once!

                _applicationObject = (DTE2)application;
                mavenRunner = new MavenRunner(_applicationObject);
                mavenRunner.RunnerStopped += new EventHandler(mavenRunner_RunnerStopped);
                _addInInstance = (AddIn)addInInst;
                mavenConnected = true;

                string paneName = "NPanday Build System";

                outputWindowPane = getOrCreateOutputPane(paneName);

                try
                {
                    // Currently, MSBuild uses the namespace NPanday.VisualStudio, while NPanday uses the artifactID NPanday.VisualStudio.AddIn for embedded resources. Workaround...
                    Stream fileStream = Assembly.GetExecutingAssembly().GetManifestResourceStream("NPanday.VisualStudio.log4net.xml");
                    if (fileStream == null)
                    {
                        fileStream = Assembly.GetExecutingAssembly().GetManifestResourceStream("NPanday.VisualStudio.Addin.log4net.xml");
                    }
                    if (fileStream != null)
                    {
                        XmlConfigurator.Configure(fileStream);
                    }
                    else
                    {
                        outputWindowPane.OutputString("Failed to configure logging subsystem: No log4net.xml configuration found\n");
                    }
                }
                catch (Exception e)
                {
                    outputWindowPane.OutputString("Failed to configure logging subsystem: " + e.Message + "\n");
                }

                Level level =  Level.Info;
                // TODO: should be a better way to enable this
                if (_addInInstance.Name.Contains("SNAPSHOT"))
                {
                    level = Level.Debug;
                }

                Hierarchy h = (Hierarchy)LogManager.GetRepository();
                Logger rootLogger = h.Root;
                OutputWindowPaneAppender appender = new OutputWindowPaneAppender(outputWindowPane, level);
                rootLogger.AddAppender(appender);

                log.Debug("Intialized panes");

                _buttonCommandRegistry = new ButtonCommandRegistry(_applicationObject, buildCommandContext);

                _finder = new VisualStudioControlsFinder(_applicationObject);
                _finder.IndexCommands();

                globalSolutionEvents = (EnvDTE.SolutionEvents)((Events2)_applicationObject.Events).SolutionEvents;
                globalSolutionEvents.BeforeClosing += new _dispSolutionEvents_BeforeClosingEventHandler(SolutionEvents_BeforeClosing);
                globalSolutionEvents.Opened += new _dispSolutionEvents_OpenedEventHandler(SolutionEvents_Opened);
                globalSolutionEvents.ProjectAdded += new _dispSolutionEvents_ProjectAddedEventHandler(SolutionEvents_ProjectAdded);

                projectItemsEvents = (EnvDTE.ProjectItemsEvents)((Events2)_applicationObject.Events).ProjectItemsEvents;
                projectItemsEvents.ItemAdded += new _dispProjectItemsEvents_ItemAddedEventHandler(ProjectItemEvents_ItemAdded);
                projectItemsEvents.ItemRemoved += new _dispProjectItemsEvents_ItemRemovedEventHandler(ProjectItemEvents_ItemRemoved);
                projectItemsEvents.ItemRenamed += new _dispProjectItemsEvents_ItemRenamedEventHandler(ProjectItemEvents_ItemRenamed);
            }

            log.Debug("OnConnection() called with connect mode " + connectMode);

            // this is only called once per installation
            if (connectMode == ext_ConnectMode.ext_cm_UISetup)
            {
                log.Debug("Registering Startup-Command in Tools-Menu");

                Command toolsMenuCommand = null;
                object[] contextGUIDS = new object[] { };
                Commands2 commands = (Commands2)_applicationObject.Commands;

                string toolsMenuName = VSCommandCaptions.Tools;

                //Place the command on the tools menu.
                //Find the MenuBar command bar, which is the top-level command bar holding all the main menu items:
                CommandBar menuBarCommandBar = ((CommandBars)_applicationObject.CommandBars)["MenuBar"];

                //Find the Tools command bar on the MenuBar command bar:
                CommandBarControl toolsControl = menuBarCommandBar.Controls[toolsMenuName];
                CommandBarPopup toolsPopup = (CommandBarPopup)toolsControl;

                if (toolsPopup == null)
                {
                    string message = "Will skip adding control, as the tools popup could not be found with name '" + toolsMenuName + "'";
                    log.Warn(message);
                    MessageBox.Show(message);
                }

                //This try/catch block can be duplicated if you wish to add multiple commands to be handled by your Add-in,
                //  just make sure you also update the QueryStatus/Exec method to include the new command names.
                try
                {
                    //Add a command to the Commands collection:
                    toolsMenuCommand = commands.AddNamedCommand2(_addInInstance, "NPandayAddin",
                        Messages.MSG_D_NPANDAY_BUILD_SYSTEM, Messages.MSG_T_NPANDAY_BUILDSYSTEM, true, 480, ref contextGUIDS,
                        (int)vsCommandStatus.vsCommandStatusSupported + (int)vsCommandStatus.vsCommandStatusEnabled,
                        (int)vsCommandStyle.vsCommandStylePictAndText,
                        vsCommandControlType.vsCommandControlTypeButton);

                    //Add a control for the command to the tools menu:
                    if ((toolsMenuCommand != null) && (toolsPopup != null))
                    {
                        toolsMenuCommand.AddControl(toolsPopup.CommandBar, 1);
                    }
                    else
                    {
                        string message = "Skipped adding control as the NPanday start command could not be found.";
                        log.Warn(message);
                        MessageBox.Show(message);
                    }
                }
                catch (System.ArgumentException ex)
                {
                    //If we are here, then the exception is probably because a command with that name
                    //  already exists. If so there is no need to recreate the command and we can
                    //  safely ignore the exception.
                    log.Warn("Exception occured when adding NPanday to the Tools menu: " + ex.Message, ex);
                }

            }


            if (connectMode == ext_ConnectMode.ext_cm_AfterStartup)
            {
                launchNPandayBuildSystem();
            }
        }

        private OutputWindowPane getOrCreateOutputPane(string paneName)
        {
            Window win = _applicationObject.Windows.Item(EnvDTE.Constants.vsWindowKindOutput);
            OutputWindow outputWindow = (OutputWindow)win.Object;
            OutputWindowPanes panes = outputWindow.OutputWindowPanes;

            foreach (OutputWindowPane outputPane in panes)
            {
                if (outputPane.Name == paneName)
                {
                    return outputPane;
                }
            }
            return outputWindow.OutputWindowPanes.Add(paneName);
        }

        private IButtonCommandContext buildCommandContext()
        {
            return new ButtonCommandContext(this, _applicationObject);
        }

        private class ButtonCommandContext : IButtonCommandContext
        {
            private Connect _connect;
            private readonly DTE2 _application;

            public ButtonCommandContext(Connect connect, DTE2 application)
            {
                _connect = connect;
                _application = application;
            }

            public FileInfo CurrentSelectedProjectPom
            {
                get { return _connect.CurrentSelectedProjectPom; }
            }

            public ArtifactContext ArtifactContext
            {
                get { return _connect.container; }
            }

            public void ExecuteCommand(string visualStudioCommandName)
            {
                _application.ExecuteCommand(visualStudioCommandName, "");
            }

            public void ExecuteCommand<TCommand>() where TCommand : ButtonCommand, new()
            {
                _connect._buttonCommandRegistry.Excecute<TCommand>(this);
            }
        }

        void mavenRunner_RunnerStopped(object sender, EventArgs e)
        {
            //stopButton.Enabled = false;
        }

        public static bool IsWebSite(Project project)
        {
            return project.Kind.Equals(VisualStudioProjectType.GetVisualStudioProjectTypeGuid(VisualStudioProjectTypeEnum.Web_Site), StringComparison.OrdinalIgnoreCase);
        }

        public static bool IsWebProject(Project project)
        {
            // quick check for the right extender, in lieu of full GUID availability (see below)
            try
            {
                if (project.get_Extender("WebApplication") != null)
                {
                    return true;
                }
            }
            catch
            {
                // ignore
            }

            // project Kind is only the main one, so the below is not often correct. A more definitive query would be to get all the subtype GUIDs for a project (see http://www.mztools.com/articles/2007/mz2007016.aspx)
            return project.Kind.Equals(VisualStudioProjectType.GetVisualStudioProjectTypeGuid(VisualStudioProjectTypeEnum.Web_Site), StringComparison.OrdinalIgnoreCase) ||
                project.Kind.Equals(VisualStudioProjectType.GetVisualStudioProjectTypeGuid(VisualStudioProjectTypeEnum.Web_Application), StringComparison.OrdinalIgnoreCase);
        }

        public static bool IsCloudProject(Project project)
        {
            return project.Kind.Equals(VisualStudioProjectType.GetVisualStudioProjectTypeGuid(VisualStudioProjectTypeEnum.WindowsAzure_CloudService), StringComparison.OrdinalIgnoreCase);
        }

        private const string FOLDER_KIND_GUID = "{66A26720-8FB5-11D2-AA7E-00C04F688DDE}";

        public static bool IsFolder(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // compare the project kind to the folder guid
            return (String.Compare(project.Kind, FOLDER_KIND_GUID, true) == 0);
        }

        public static IEnumerable<Project> GetAllProjects(Projects solutionProjects)
        {
            List<Project> projects = new List<Project>();
            foreach (Project project in solutionProjects)
            {
                addProject(projects, project);
            }
            return projects;
        }

        private static void addProject(List<Project> projects, Project project)
        {
            log.DebugFormat("Adding solution project: {0}, type: {1}", project.Name, project.Kind);
            if (IsFolder(project))
            {
                foreach (ProjectItem item in project.ProjectItems)
                {
                    if (item.SubProject != null)
                    {
                        addProject(projects, item.SubProject);
                    }
                }
            }
            else
            {
                projects.Add(project);
            }
        }

        void InsertKeyTag(string filePath, string key)
        {
            try
            {
                StreamReader reader = new StreamReader(filePath);
                List<String> contents = new List<string>();

                string temp = reader.ReadLine();
                contents.Add(temp);

                while (temp != null)
                {
                    temp = reader.ReadLine();
                    if (temp != null)
                    {
                        contents.Add(temp);
                        string includeSources = "</includeSources>";
                        if (temp.Contains(includeSources))
                        {
                            string keyNode = temp.Substring(0, temp.IndexOf(includeSources));
                            keyNode += string.Format("<keyfile>{0}</keyfile>", key);
                            contents.Add(keyNode);
                        }
                    }
                }
                reader.Close();

                TextWriter writer = new StreamWriter(filePath);

                foreach (string item in contents)
                {
                    writer.WriteLine(item);
                }

                writer.Close();
            }
            catch (Exception e)
            {
                log.Error("Unable to insert key tag in POM: " + e.Message);
            }


        }

        void SigningEvents_SignatureAdded()
        {
            // TODO: Currently it seems this code is called unnecessarily sometimes, and should not iterate all projects (which may not require it - e.g. parent and ccproj)
            // TODO: should also use PomHelperUtility instead of DOM manipulation

            Solution2 solution = (Solution2)_applicationObject.Solution;
            string pomFilePath = string.Empty;
            foreach (Project project in GetAllProjects(solution.Projects))
            {
                string name = null;
                try
                {
                    name = project.FullName;
                }
                catch
                {
                    // ignore - project is not yet set up
                }
                if (string.IsNullOrEmpty(name))
                    continue;

                String key = string.Empty;
                XmlDocument doc = new XmlDocument();
                bool isSigned = false;
                try
                {
                    //Reading the project file
                    doc.Load(project.FullName);

                    //construct the path for the pom file and check for file existance.
                    //return if file does not exist.
                    pomFilePath = project.FullName.Substring(0, project.FullName.LastIndexOf("\\"));
                    pomFilePath += "\\pom.xml";
                    if (!File.Exists(pomFilePath))
                    {
                        return;
                    }
                    XmlNodeList keyFileList = doc.GetElementsByTagName("AssemblyOriginatorKeyFile");
                    XmlNodeList signBoolList = doc.GetElementsByTagName("SignAssembly");

                    foreach (XmlNode item in signBoolList)
                    {
                        if (item.InnerText.Equals("true"))
                        {
                            isSigned = true;
                        }
                    }

                    if (isSigned)
                    {
                        foreach (XmlNode item in keyFileList)
                        {
                            key = item.InnerText;
                        }
                    }
                }
                catch (Exception e)
                {
                    log.Error("Unable to find signing tags in POM: " + e.Message, e);
                }
                try
                {
                    //read pom
                    doc.Load(pomFilePath);

                    XmlNodeList pluginList = doc.GetElementsByTagName("plugin");
                    XmlNode configurationNode = null;

                    foreach (XmlNode item in pluginList)
                    {
                        if (item.InnerText.Contains("maven-compile-plugin"))
                        {
                            configurationNode = item.LastChild;
                        }
                    }

                    if (configurationNode == null)
                    {
                        // TODO: perhaps should add it here and proceed instead?
                        return;
                    }

                    //isSigned adding keyfile tag
                    if (!configurationNode.InnerText.Contains(".snk") && key != string.Empty)
                    {
                        //add keyfile tag
                        InsertKeyTag(pomFilePath, key);
                    }

                    //!isSigned removing keyfile tag
                    if (configurationNode.InnerText.Contains(".snk") && !isSigned)
                    {
                        //delete keyfile tag
                        configurationNode.RemoveChild(configurationNode.LastChild);

                        XmlTextWriter txtwriter = new XmlTextWriter(pomFilePath, null);

                        txtwriter.Formatting = Formatting.Indented;
                        txtwriter.Indentation = 2;
                        doc.Save(txtwriter);
                        txtwriter.Close();
                    }
                }
                catch (Exception e)
                {
                    log.Error("Unable to add signing configuration in POM: " + e.Message, e);
                }
            }
        }

        void attachReferenceEvent()
        {
            SigningEvents_SignatureAdded();

            //References
            referenceEvents = new List<ReferencesEvents>();
            Solution2 solution = (Solution2)_applicationObject.Solution;

            this.wsRefWatcher = new List<WebServicesReferenceWatcher>();
            this.svRefWatcher = new List<WebServicesReferenceWatcher>();

            foreach (Project project in GetAllProjects(solution.Projects))
            {
                projectRefEventLoaded = true;

                string referenceFolder = string.Empty;
                string serviceRefFolder = string.Empty;

                if (IsWebSite(project))
                {
                    VsWebSite.VSWebSite website = (VsWebSite.VSWebSite)project.Object;
                    referenceFolder = Path.Combine(website.Project.FullName, "App_WebReferences");
                    if (!Directory.Exists(referenceFolder))
                    {
                        Directory.CreateDirectory(referenceFolder);
                    }
                }
                else
                {
                    try
                    {
                        VSProject2 classProject = (VSProject2)project.Object;
                        referenceEvents.Add(classProject.Events2.ReferencesEvents);
                        classProject.Events2.ReferencesEvents.ReferenceRemoved
                            += new _dispReferencesEvents_ReferenceRemovedEventHandler(ReferencesEvents_ReferenceRemoved);
                        classProject.Events2.ReferencesEvents.ReferenceAdded += new _dispReferencesEvents_ReferenceAddedEventHandler(ReferencesEvents_ReferenceAdded);

                        ProjectItem webReferenceFolder = classProject.WebReferencesFolder;
                        if (webReferenceFolder == null)
                        {
                            webReferenceFolder = classProject.CreateWebReferencesFolder();
                        }
                        referenceFolder = Path.Combine(Path.GetDirectoryName(project.FullName), webReferenceFolder.Name);
                        serviceRefFolder = Path.Combine(Path.GetDirectoryName(project.FullName), Messages.MSG_D_SERV_REF);

                        if (!Directory.Exists(serviceRefFolder))
                        {
                            Directory.CreateDirectory(serviceRefFolder);
                        }
                    }
                    catch
                    {
                        //  not a csproj / vbproj file. Could be a solution folder. skip it.
                        continue;
                    }
                }

                //attach web references watcher
                try
                {
                    if (!string.IsNullOrEmpty(referenceFolder))
                    {
                        string wsPath = referenceFolder;
                        WebServicesReferenceWatcher wsw = new WebServicesReferenceWatcher(wsPath);
                        wsw.Created += new EventHandler<WebReferenceEventArgs>(wsw_Created);
                        wsw.Deleted += new EventHandler<WebReferenceEventArgs>(wsw_Deleted);
                        wsw.Renamed += new EventHandler<WebReferenceEventArgs>(wsw_Renamed);
                        wsw.Start();
                        this.wsRefWatcher.Add(wsw);
                    }

                    if (!string.IsNullOrEmpty(serviceRefFolder))
                    {
                        string svPath = serviceRefFolder;
                        WebServicesReferenceWatcher srw = new WebServicesReferenceWatcher(svPath);
                        srw.Created += new EventHandler<WebReferenceEventArgs>(srw_Created);
                        srw.Deleted += new EventHandler<WebReferenceEventArgs>(srw_Deleted);
                        srw.Renamed += new EventHandler<WebReferenceEventArgs>(srw_Renamed);
                        srw.Start();
                        this.svRefWatcher.Add(srw);
                    }
                }
                catch (Exception ex)
                {

                    throw ex;
                }

            }
        }

        void ReferencesEvents_ReferenceAdded(Reference pReference)
        {
            bool isSystemPath = false;
            try
            {
                if (!mavenConnected)
                    return;

                ArtifactContext artifactContext = new ArtifactContext();
                Artifact.Artifact artifact = new NPanday.Artifact.Artifact();

                //check if reference is in maven repository
                bool iNPandayRepo = false;
                try
                {
                    artifact = artifactContext.GetArtifactRepository().GetArtifact(new FileInfo(pReference.Path));
                    if (artifact != null)
                    {
                        iNPandayRepo = true;
                    }
                }
                catch
                {
                    // TODO: should this really be ignored?
                }

                //check if reference is already in pom
                PomHelperUtility pomUtil = createPomUtility(pReference.ContainingProject);
                if (pomUtil == null || pomUtil.IsPomDependency(pReference.Name))
                {
                    return;
                }

                //setup default dependency values
                string refType;
                string refName = pReference.Name;
                string refGroupId = pReference.Name;
                string refToken = string.Empty;
                string refVersion = pReference.Version;
                string systemPath = string.Empty;
                string scope = string.Empty;

                //check  if reference is activex
                if (pReference.Type == prjReferenceType.prjReferenceTypeActiveX)
                {
                    refType = "com_reference";
                    if (refName.ToLower().StartsWith("interop.", true, CultureInfo.InvariantCulture))
                        refName = refName.Substring(8);
                    refToken = pReference.Identity.Substring(0, pReference.Identity.LastIndexOf(@"\")).Replace("\\", "-");
                    refGroupId = refName;
                }
                else if (pReference.Type == prjReferenceType.prjReferenceTypeAssembly)
                {
                    //if reference is assembly
                    if (pReference.SourceProject != null && pReference.ContainingProject.DTE.Solution.FullName == pReference.SourceProject.DTE.Solution.FullName)
                    {
                        // if intra-project reference, let's mimic Add Maven Artifact

                        // TODO: below will force VS build the referenced project, let's disable this for awhile
                        //pReference.SourceProject.DTE.ExecuteCommand("ClassViewContextMenus.ClassViewProject.Build", string.Empty);

                        NPanday.Model.Pom.Model solutionPOM = NPanday.Utils.PomHelperUtility.ReadPomAsModel(CurrentSolutionPom);
                        NPanday.Model.Pom.Model projectPOM = NPanday.Utils.PomHelperUtility.ReadPomAsModel(CurrentSelectedProjectPom);

                        refGroupId = solutionPOM.groupId;

                        if (projectPOM.version != null && projectPOM.version != "0.0.0.0")
                        {
                            refVersion = projectPOM.version;
                        }
                        else if ((solutionPOM.version != null && solutionPOM.version != "0.0.0.0"))
                        {
                            refVersion = solutionPOM.version;
                        }
                        else
                        {
                            refVersion = "1.0-SNAPSHOT";
                        }
                        refType = "dotnet-library";
                    }
                    else
                    {
                        // TODO: ideally this could reuse more logic from the project importer, while making use of the passed in information for version & path that can avoid having to scan the framework directories
                        if (pReference.Path != null)
                        {
                            scope = "system";
                            systemPath = pReference.Path;
                            refType = "dotnet-library";
                            /* This warning is only applicable for those added by browse, not for those from the framework. Can't currently differentiate, so avoid the warning to reduce misleading noise.
                            if (!iNPandayRepo)
                            {
                                MessageBox.Show(string.Format("Warning: Build may not be portable if local references are used, Reference is not in Maven Repository."
                                         + "\nReference: {0}"
                                         + "\nDeploying the reference to a Repository, will make the code portable to other machines",
                                 pReference.Name
                             ), "Add Reference", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                            }
                             */
                        }
                        else
                        {
                            // TODO: can we get the process architecture from the project properties, so that it is more accurate if targeted to a different arch than we are generating on?
                            List<string> refs = GacUtility.GetInstance().GetAssemblyInfo(pReference.Name, pReference.Version, null);

                            Assembly a = null;
                            AssemblyName name = null;

                            if (refs.Count > 0)
                            {
                                name = new System.Reflection.AssemblyName(refs[0]);
                                a = Assembly.ReflectionOnlyLoad(name.FullName);
                            }

                            refToken = pReference.PublicKeyToken.ToLower();
                            refType = GacUtility.GetNPandayGacType(a.ImageRuntimeVersion, name.ProcessorArchitecture, refToken);
                        }
                    }
                }
                else
                {
                    throw new Exception("Unrecognized reference type: " + pReference.Type);
                }

                Dependency dep = new Dependency();
                dep.artifactId = refName;
                dep.groupId = refGroupId;
                dep.version = refVersion;
                dep.type = refType;


                if (!string.IsNullOrEmpty(refToken))
                    dep.classifier = refToken;
                if (!string.IsNullOrEmpty(systemPath))
                    dep.systemPath = systemPath;
                if (!string.IsNullOrEmpty(scope))
                    dep.scope = scope;


                pomUtil.AddPomDependency(dep);
            }
            catch (Exception e)
            {
                string msg = "Error converting reference to artifact, not added to POM: " + e.Message;
                log.Debug(msg, e);
                MessageBox.Show(msg, "Add Reference", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }
        /* Commented out earlier
        void webw_Deleted(object sender, FileSystemEventArgs e)
        {
            try
            {
                PomHelperUtility pomUtil = createPomUtility();
                if (Path.GetExtension(e.Name).ToLower() == ".dll" || Path.GetExtension(e.Name).ToLower() == ".exe")
                {
                    pomUtil.RemovePomDependency(Path.GetFileNameWithoutExtension(e.Name));
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, Messages.MSG_E_NPANDAY_REMOVE_DEPENDENCY_ERROR);
            }

        }
        */
        void wsw_Renamed(object sender, WebReferenceEventArgs e)
        {
            try
            {
                //wait for the files to be created
                WebReferencesClasses wrc = new WebReferencesClasses(e.ReferenceDirectory);
                wrc.WaitForClasses(e.Namespace);

                e.Init(projectReferenceFolder(CurrentSelectedProject));
                PomHelperUtility pomUtil = createPomUtility(CurrentSelectedProject);
                lock (typeof(PomHelperUtility))
                {
                    if (pomUtil != null)
                    {
                        pomUtil.RenameWebReference(e.ReferenceDirectory, e.OldNamespace, e.Namespace, e.WsdlFile, string.Empty);
                    }
                }
            }
            catch (Exception ex)
            {
                log.Error("Error on webservice rename: " + ex.Message);
            }
        }

        void wsw_Deleted(object sender, WebReferenceEventArgs e)
        {
            try
            {
                e.Init(projectReferenceFolder(CurrentSelectedProject));
                PomHelperUtility pomUtil = createPomUtility(CurrentSelectedProject);
                lock (typeof(PomHelperUtility))
                {
                    if (pomUtil != null)
                    {
                        pomUtil.RemoveWebReference(e.ReferenceDirectory, e.Namespace);
                    }
                }
            }
            catch (Exception ex)
            {
                log.Error("Error on webservice delete: " + ex.Message);
            }
        }

        void wsw_Created(object sender, WebReferenceEventArgs e)
        {
            try
            {
                Solution2 solution = (Solution2)_applicationObject.Solution;

                //wait for the files to be created
                WebReferencesClasses wrc = new WebReferencesClasses(e.ReferenceDirectory);
                wrc.WaitForClasses(e.Namespace);

                e.Init(projectReferenceFolder(CurrentSelectedProject));

                PomHelperUtility pomUtil = createPomUtility(CurrentSelectedProject);
                lock (typeof(PomHelperUtility))
                {
                    if (pomUtil != null)
                    {
                        pomUtil.AddWebReference(e.Namespace, e.WsdlFile, string.Empty);
                    }
                }


            }
            catch (Exception ex)
            {
                log.Error("Error creating web service: " + ex.Message);
            }
        }

        void srw_Renamed(object sender, WebReferenceEventArgs e)
        {
            try
            {
                System.Threading.Thread.Sleep(1500);
                e.Init(Path.Combine(Path.GetDirectoryName(CurrentSelectedProject.FullName), Messages.MSG_D_SERV_REF));
                PomHelperUtility pomUtil = createPomUtility(CurrentSelectedProject);
                if (pomUtil != null)
                    pomUtil.RenameWebReference(e.ReferenceDirectory, e.OldNamespace, e.Namespace, e.WsdlFile, string.Empty);
            }
            catch (Exception ex)
            {
                log.Error("Error renaming web service: " + ex.Message);
            }
        }

        void srw_Deleted(object sender, WebReferenceEventArgs e)
        {
            try
            {
                e.Init(Path.Combine(Path.GetDirectoryName(CurrentSelectedProject.FullName), Messages.MSG_D_SERV_REF));
                PomHelperUtility pomUtil = createPomUtility(CurrentSelectedProject);
                if (pomUtil != null)
                    pomUtil.RemoveWebReference(e.ReferenceDirectory, e.Namespace);
            }
            catch (Exception ex)
            {
                log.Error("Error deleting web service: " + ex.Message);
            }
        }

        void srw_Created(object sender, WebReferenceEventArgs e)
        {
            try
            {
                System.Threading.Thread.Sleep(2500);
                Solution2 solution = (Solution2)_applicationObject.Solution;

                string path = Path.Combine(Path.GetDirectoryName(CurrentSelectedProject.FullName), Messages.MSG_D_SERV_REF);
                e.Init(path);
                PomHelperUtility pomUtil = createPomUtility(CurrentSelectedProject);
                if (pomUtil != null)
                    pomUtil.AddWebReference(e.Namespace, e.WsdlFile, string.Empty);
            }
            catch (Exception ex)
            {
                log.Error("Error creating web service: " + ex.Message);
            }
        }

        void addWebReference(PomHelperUtility pomUtil, string name, string path, string output)
        {
            lock (typeof(PomHelperUtility))
            {
                pomUtil.AddWebReference(name, path, output);
            }
        }

        string projectReferenceFolder(Project project)
        {
            string wsPath = null;
            if (IsWebSite(project))
            {
                VsWebSite.VSWebSite website = (VsWebSite.VSWebSite)project.Object;
                wsPath = Path.Combine(website.Project.FullName, "App_WebReferences");
                if (!Directory.Exists(wsPath))
                {
                    Directory.CreateDirectory(wsPath);
                }
            }
            else
            {
                VSProject2 vsProject = (VSProject2)project.Object;

                ProjectItem webReferenceFolder = vsProject.WebReferencesFolder;
                if (webReferenceFolder == null)
                {
                    webReferenceFolder = vsProject.CreateWebReferencesFolder();
                }

                wsPath = Path.Combine(Path.GetDirectoryName(project.FullName), webReferenceFolder.Name);
            }
            return wsPath;
        }

        private void launchNPandayBuildSystem()
        {
            log.Debug("launchNPandayBuildSystem() called, _npandayLaunched is " + _npandayLaunched);

            try
            {
                // just to be safe, check if NPanday is already launched
                if (_npandayLaunched)
                {
                    log.Error(Messages.MSG_L_NPANDAY_ALREADY_STARTED);
                    return;
                }

                Stopwatch swStartingBuildSystem = new Stopwatch();
                swStartingBuildSystem.Start();

                container = new ArtifactContext();

                EnvDTE80.Windows2 windows2 = (EnvDTE80.Windows2)_applicationObject.Windows;

                DTE2 dte2 = _applicationObject;

                addReferenceControls = new List<CommandBarButton>();
                buildControls = new List<CommandBarControl>();

                bool placedAddStopBuildMenu = false;
                bool placedNPandayMenus = false;
                bool placedAllProjectMenu = false;

                CommandBarControl[] barControls;

                if (_finder.TryFindCommands(VSCommandCaptions.AddReference, out barControls))
                {
                    foreach (CommandBarControl barControl in barControls)
                    {
                        _buttonCommandRegistry.AddBefore<AddArtifactsCommand>(barControl);
                    }
                }
                else
                {
                    log.Error(Messages.MSG_L_UNABLE_TO_REGISTER_ADD_ARTIFACT_MENU);
                }

                foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
                {
                    foreach (CommandBarControl control in commandBar.Controls)
                    {
                        if (control.Caption.Equals("C&onfiguration Manager..."))
                        {
                            //add solution menu
                            createStopBuildMenu(commandBar, control);
                            placedAddStopBuildMenu = true;

                            createNPandayMenus(commandBar, control);
                            placedNPandayMenus = true;

                            createAllProjectMenu(commandBar, control);
                            placedAllProjectMenu = true;

                        }
                        // included build web site to support web site projects
                        else if (
                            _finder.IsThisCommand(control, VSCommandCaptions.Clean)
                            || _finder.IsThisCommand(control, VSCommandCaptions.PublishSelection)
                            || _finder.IsThisCommand(control, VSCommandCaptions.PublishWebSite))
                        {
                            // Add the stop maven build button here

                            createStopBuildMenu(commandBar, control);
                            placedAddStopBuildMenu = true;
                            createNPandayMenus(commandBar, control);
                            placedNPandayMenus = true;

                            CommandBarPopup ctl = (CommandBarPopup)
                                                  commandBar.Controls.Add(MsoControlType.msoControlPopup,
                                                                          System.Type.Missing, System.Type.Missing,
                                                                          control.Index + 1, true);
                            ctl.Caption = Messages.MSG_C_CUR_PROJECT;
                            ctl.Visible = true;

                            buildControls.Add(ctl);

                            createAllProjectMenu(commandBar, control);
                            placedAllProjectMenu = true;

                            createCurrentProjectMenu(ctl);
                        }
                    }
                }
                nunitControls = new List<CommandBarButton>();
                Window solutionExplorerWindow = dte2.Windows.Item(Constants.vsWindowKindSolutionExplorer);
                _selectionEvents = dte2.Events.SelectionEvents;
                _selectionEvents.OnChange += new _dispSelectionEvents_OnChangeEventHandler(this.OnChange);
                _npandayLaunched = true;
                // outputWindowPane.Clear();

                if (!placedAddStopBuildMenu)
                    log.Error(Messages.MSG_L_UNABLE_TO_REGISTER_STOP_BUILD_MENU);
                if (!placedNPandayMenus)
                    log.Error(Messages.MSG_L_UNABLE_TO_REGISTER_NPANDAY_MENUS);
                if (!placedAllProjectMenu)
                    log.Error(Messages.MSG_L_UNABLE_TO_REGISTER_ALL_PROJECTS_MENU);

                swStartingBuildSystem.Stop();

                string[] nameParts = _addInInstance.Name.Split(' ');
                // Version should be the second "word" in the name.
                string NPandayVersion = (nameParts.Length > 1) ? nameParts[1] : "UNKNOWN";
                log.InfoFormat(Messages.MSG_L_NPANDAY_ADDIN_STARTED, NPandayVersion,
                                                            swStartingBuildSystem.Elapsed.TotalSeconds);

            }
            catch (Exception e)
            {
                log.Fatal("NPanday Build System failed to start up: " + e.Message, e);

                MessageBox.Show("Error thrown: " + e.Message + Environment.NewLine + Environment.NewLine + "Consulte the log for details.", "NPanday Build System failed to start up!");
            }

            if (_applicationObject.Solution != null)
                attachReferenceEvent();

            log.Debug("launchNPandayBuildSystem() exited, _npandayLaunched is " + _npandayLaunched);
        }

        CommandBarButton cleanButton;
        CommandBarButton testButton;
        CommandBarButton installButton;
        CommandBarButton deployButton;
        CommandBarButton buildButton;
        CommandBarButton resyncProjectReferencesButton;
        CommandBarButton resyncProjectReferencesFromLocalRepositoryButton;

        private void createCurrentProjectMenu(CommandBarPopup ctl)
        {
            resyncProjectReferencesButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            resyncProjectReferencesButton.Visible = true;
            resyncProjectReferencesButton.Caption = "Resync References";
            resyncProjectReferencesButton.Click += new _CommandBarButtonEvents_ClickEventHandler(OnResyncProjectReferencesButtonClicked);

            resyncProjectReferencesFromLocalRepositoryButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            resyncProjectReferencesFromLocalRepositoryButton.Visible = true;
            resyncProjectReferencesFromLocalRepositoryButton.Caption = "Resync References From Local Repository";
            resyncProjectReferencesFromLocalRepositoryButton.Click += new _CommandBarButtonEvents_ClickEventHandler(OnResyncProjectReferencesFromLocalRepositoryButtonClicked);

            cleanButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            cleanButton.Caption = Messages.MSG_C_CLEAN;
            cleanButton.Visible = true;
            cleanButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbClean_Click);



            testButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            testButton.Caption = Messages.MSG_C_TEST;
            testButton.Visible = true;
            testButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbTest_Click);

            installButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            installButton.Caption = Messages.MSG_C_INSTALL;
            installButton.Visible = true;
            installButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbInstall_Click);

            deployButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            deployButton.Caption = Messages.MSG_C_DEPLOY;
            deployButton.Visible = true;
            deployButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbDeploy_Click);
            buildButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            buildButton.Caption = Messages.MSG_C_BUILD;
            buildButton.Visible = true;
            buildButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbBuild_Click);


            buildControls.Add(buildButton);
            buildControls.Add(installButton);
            buildControls.Add(deployButton);
            buildControls.Add(cleanButton);
            buildControls.Add(testButton);
            buildControls.Add(resyncProjectReferencesButton);
            buildControls.Add(resyncProjectReferencesFromLocalRepositoryButton);
        }

        void OnResyncProjectReferencesButtonClicked(CommandBarButton button, ref bool cancelDefault)
        {
            bool resyncFromRemoteRepository = true;
            ResyncCurrentProjectArtifacts(resyncFromRemoteRepository);
        }

        void OnResyncProjectReferencesFromLocalRepositoryButtonClicked(CommandBarButton button, ref bool cancelDefault)
        {
            bool resyncFromRemoteRepository = false;
            ResyncCurrentProjectArtifacts(resyncFromRemoteRepository);
        }

        private void ResyncCurrentProjectArtifacts(bool fromRemoteRepository)
        {
            refManagerHasError = false;
            log.InfoFormat("Re-syncing artifacts in {0} project... ", CurrentSelectedProject.Name);
            try
            {
                IReferenceManager refmanager = new ReferenceManager();
                refmanager.OnError += new EventHandler<ReferenceErrorEventArgs>(refmanager_OnError);
                refmanager.Initialize((VSProject2)CurrentSelectedProject.Object);

                if (fromRemoteRepository)
                {
                    refmanager.ResyncArtifacts();
                }
                else
                {
                    refmanager.ResyncArtifactsFromLocalRepository();
                }

                if (!refManagerHasError)
                {
                    log.InfoFormat("done [{0}]", DateTime.Now.ToString("hh:mm tt"));
                }
            }
            catch (Exception ex)
            {
                if (refManagerHasError)
                {
                    log.Warn(ex.Message);
                }
                else
                {
                    log.ErrorFormat("failed: {0}", ex.Message);
                }

                if (!ex.Message.Contains("no valid pom file"))
                {
                    log.Debug(ex.Message, ex);
                }
            }
        }

        bool refManagerHasError = false;
        void refmanager_OnError(object sender, ReferenceErrorEventArgs e)
        {
            refManagerHasError = true;
            log.Warn(e.Message);
        }

        private void createStopBuildMenu(CommandBar commandBar, CommandBarControl control)
        {
            stopButton = (CommandBarButton)commandBar.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, control.Index + 1, true);
            stopButton.Caption = Messages.MSG_C_STOPNPANDAYBUILD;
            //stopButton.Enabled = false;
            stopButton.Visible = true;
            stopButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbStopMavenBuild_Click);
            buildControls.Add(stopButton);
        }

        CommandBarButton ctlSettingsXml;
        CommandBarButton ctlSignAssembly;

        private void createNPandayMenus(CommandBar commandBar, CommandBarControl control)
        {
            ctlSettingsXml = (CommandBarButton)
            commandBar.Controls.Add(MsoControlType.msoControlButton,
                                      System.Type.Missing,
                                      System.Type.Missing,
                                      control.Index + 1,
                                      true);
            ctlSettingsXml.Click +=
                new _CommandBarButtonEvents_ClickEventHandler(cbChangeSettingsXmlForm_Click);
            ctlSettingsXml.Caption = Messages.MSG_C_CHANGE_MAVEN_SETTING_XML;
            ctlSettingsXml.Visible = true;

            buildControls.Add(ctlSettingsXml);

            ctlSignAssembly = (CommandBarButton)
            commandBar.Controls.Add(MsoControlType.msoControlButton,
                                      System.Type.Missing,
                                      System.Type.Missing,
                                      control.Index + 1,
                                      true);
            ctlSignAssembly.Click +=
                new _CommandBarButtonEvents_ClickEventHandler(cbSetSignAssemblyForm_Click);
            ctlSignAssembly.Caption = Messages.MSG_C_SET_COMPILE_SIGN_ASSEMBLY_KEY;
            ctlSignAssembly.Visible = true;
            buildControls.Add(ctlSignAssembly);

            _buttonCommandRegistry.Add<ImportSelectedProjectCommand>(commandBar, control.Index + 1);
        }

        CommandBarPopup ctlAll;
        CommandBarButton cleanAllButton;
        CommandBarButton testAllButton;
        CommandBarButton installAllButton;
        CommandBarButton buildAllButton;
        CommandBarButton resyncSolutionReferencesButton;
        CommandBarButton resyncSolutionReferencesFromLocalRepositoryButton;

        private void createAllProjectMenu(CommandBar commandBar, CommandBarControl control)
        {

            ctlAll = (CommandBarPopup)commandBar.Controls.Add(MsoControlType.msoControlPopup,
                System.Type.Missing, System.Type.Missing, control.Index + 1, true);
            ctlAll.Caption = Messages.MSG_C_ALL_PROJECTS;
            ctlAll.Visible = true;
            ctlAll.BeginGroup = true;
            buildControls.Add(ctlAll);

            resyncSolutionReferencesButton = (CommandBarButton)ctlAll.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            resyncSolutionReferencesButton.Visible = true;
            resyncSolutionReferencesButton.Caption = "Resync References";
            resyncSolutionReferencesButton.Click += new _CommandBarButtonEvents_ClickEventHandler(OnResyncSolutionReferencesButtonClicked);

            resyncSolutionReferencesFromLocalRepositoryButton = (CommandBarButton)ctlAll.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            resyncSolutionReferencesFromLocalRepositoryButton.Visible = true;
            resyncSolutionReferencesFromLocalRepositoryButton.Caption = "Resync References From Local Repository";
            resyncSolutionReferencesFromLocalRepositoryButton.Click += new _CommandBarButtonEvents_ClickEventHandler(OnResyncSolutionReferencesFromLocalRepositoryButtonClicked);

            cleanAllButton = (CommandBarButton)ctlAll.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            cleanAllButton.Caption = Messages.MSG_C_CLEAN;
            cleanAllButton.Visible = true;
            cleanAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbCleanAll_Click);


            testAllButton = (CommandBarButton)ctlAll.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            testAllButton.Caption = Messages.MSG_C_TEST;
            testAllButton.Visible = true;
            testAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbTestAll_Click);

            installAllButton = (CommandBarButton)ctlAll.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            installAllButton.Caption = Messages.MSG_C_INSTALL;
            installAllButton.Visible = true;
            installAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbInstallAll_Click);

            buildAllButton = (CommandBarButton)ctlAll.Controls.Add(MsoControlType.msoControlButton,
                System.Type.Missing, System.Type.Missing, 1, true);
            buildAllButton.Caption = Messages.MSG_C_BUILD;
            buildAllButton.Visible = true;
            buildAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbBuildAll_Click);

            buildControls.Add(buildAllButton);
            buildControls.Add(installAllButton);
            buildControls.Add(cleanAllButton);
            buildControls.Add(testAllButton);
            buildControls.Add(resyncSolutionReferencesButton);
            buildControls.Add(resyncSolutionReferencesFromLocalRepositoryButton);
        }

        void OnResyncSolutionReferencesButtonClicked(CommandBarButton button, ref bool cancelDefault)
        {
            bool resyncFromRemoteRepository = true;
            ResyncSolutionArtifacts(resyncFromRemoteRepository);
        }

        void OnResyncSolutionReferencesFromLocalRepositoryButtonClicked(CommandBarButton button, ref bool cancelDefault)
        {
            bool resyncFromRemoteRepository = false;
            ResyncSolutionArtifacts(resyncFromRemoteRepository);
        }

        private void ResyncSolutionArtifacts(bool fromRemoteRepository)
        {
            refManagerHasError = false;
            log.Info("Re-syncing artifacts in all projects... ");
            try
            {
                if (_applicationObject.Solution != null)
                {                    
                    Solution2 solution = (Solution2)_applicationObject.Solution;
                    foreach (Project project in GetAllProjects(solution.Projects))
                    {
                        if (!IsWebSite(project) && !IsFolder(project) && !IsCloudProject(project) && project.Object != null)
                        {
                            IReferenceManager mgr = new ReferenceManager();
                            mgr.OnError += new EventHandler<ReferenceErrorEventArgs>(refmanager_OnError);
                            mgr.Initialize((VSProject2)project.Object);
                            if (fromRemoteRepository)
                            {
                                mgr.ResyncArtifacts();
                            }
                            else
                            {
                                mgr.ResyncArtifactsFromLocalRepository();
                            }
                            mgr = null;
                        }
                    }
                }
                if (!refManagerHasError)
                {
                    log.InfoFormat("done [{0}]", DateTime.Now.ToString("hh:mm tt"));
                }
            }
            catch (Exception ex)
            {
                if (refManagerHasError)
                {
                    log.Warn(ex.Message);
                }
                else
                {
                    log.ErrorFormat("failed: {0}", ex.Message);
                }

                if (!ex.Message.Contains("no valid pom file"))
                {
                    log.Debug(ex.Message, ex);
                }
            }
        }

        void buildAllButton_Click(CommandBarButton Ctrl, ref bool CancelDefault)
        {
            cbBuildAll_Click(Ctrl, ref CancelDefault);
        }

        void awfButton_Click(CommandBarButton Ctrl, ref bool CancelDefault)
        {
            log.Debug("Add web reference click.");
        }
        #endregion

        void ReferencesEvents_ReferenceRemoved(Reference pReference)
        {
            try
            {
                if (!mavenConnected)
                    return;

                PomHelperUtility pomUtil = createPomUtility(pReference.ContainingProject);
                if (pomUtil != null)
                {
                    string refName = pReference.Name;
                    if (pReference.Type == prjReferenceType.prjReferenceTypeActiveX && refName.ToLower().StartsWith("interop.", true, CultureInfo.InvariantCulture))
                        refName = refName.Substring(8);

                    pomUtil.RemovePomDependency(refName);
                }
            }
            catch //(Exception e)
            {
                //MessageBox.Show(e.Message, Messages.MSG_E_NPANDAY_REMOVE_DEPENDENCY_ERROR);
            }
        }

        #region OnChange()
        public void OnChange()
        {
            //Since the Solution  event is not so stable, attaching Reference Event is added here instead.
            if (!projectRefEventLoaded)
            {
                attachReferenceEvent();
            }

            foreach (SelectedItem item in _applicationObject.SelectedItems)
            {
                if (item.Name.EndsWith("Test.cs"))
                {
                    if (nunitControls.Count == 0)
                    {
                        DTE2 dte2 = _applicationObject;
                        foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
                        {
                            foreach (CommandBarControl control in commandBar.Controls)
                            {
                                if (control.Caption.Equals("View &Code"))
                                {
                                    CommandBarButton nunitControl = (CommandBarButton)
                                        commandBar.Controls.Add(MsoControlType.msoControlButton,
                                        System.Type.Missing, System.Type.Missing, control.Index, true);
                                    nunitControl.Click += new _CommandBarButtonEvents_ClickEventHandler(cbRunUnitTest_Click);
                                    nunitControl.Caption = Messages.MSG_C_RUNUNITTEST;
                                    nunitControl.Visible = true;
                                    CommandBarButton nunitCompileAndRunControl = (CommandBarButton)
                                        commandBar.Controls.Add(MsoControlType.msoControlButton,
                                        System.Type.Missing, System.Type.Missing, control.Index, true);
                                    nunitCompileAndRunControl.Click
                                        += new _CommandBarButtonEvents_ClickEventHandler(cbCompileAndRunUnitTest_Click);
                                    nunitCompileAndRunControl.Caption = Messages.MSG_C_COMPILEANDRUNTEST;
                                    nunitCompileAndRunControl.Visible = true;
                                    nunitControls.Add(nunitControl);
                                    nunitControls.Add(nunitCompileAndRunControl);
                                }
                            }
                        }
                    }
                    else
                    {
                        foreach (CommandBarButton button in nunitControls)
                        {
                            button.Visible = true;
                        }
                    }
                }
                else
                {
                    foreach (CommandBarButton button in nunitControls)
                    {
                        button.Visible = false;
                    }
                }
            }
        }
        #endregion

        static bool mavenConnected;

        #region ClearOutputWindowPane(object,EventArgs)
        private void ClearOutputWindowPane(object sender, EventArgs args)
        {
            outputWindowPane.Clear();
        }
        #endregion

        #region ActivateOutputWindowPane(object,EventArgs)
        private void ActivateOutputWindowPane(object sender, EventArgs args)
        {
            outputWindowPane.Activate();
        }
        #endregion

        #region OnDisconnection(ext_DisconnectMode,Array)
        /// <summary>
        /// Implements the OnDisconnection method of the IDTExtensibility2 interface. 
        /// Receives notification that the Add-in is being unloaded.
        /// </summary>
        /// <param term='disconnectMode'>Describes how the Add-in is being unloaded.</param>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnDisconnection(ext_DisconnectMode disconnectMode, ref Array custom)
        {
            //check if NPanday is already closed
            if (_applicationObject == null)
            {
                return;
            }
            mavenConnected = false;
            //remove maven menus
            DTE2 dte2 = _applicationObject;

            addReferenceControls = new List<CommandBarButton>();
            buildControls = new List<CommandBarControl>();

            _buttonCommandRegistry.UnregisterAll();

            outputWindowPane.Clear();
            mavenRunner.ClearOutputWindow();

            List<CommandBarControl> npandayCmdBarCtrl = new List<CommandBarControl>();

            foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
            {
                foreach (CommandBarControl control in commandBar.Controls)
                {
                    if (control.Caption.ToLower().Contains("maven") || control.Caption.ToLower().Contains("npanday") || control.Caption.ToLower().Contains("pom"))
                    {
                        //getting the npanday controls instead of deleting
                        //to prevent index out of bounds exception being thrown 
                        //when deleting from within the List of CommandBarControls
                        npandayCmdBarCtrl.Add(control);
                    }

                }
            }

            //Delete the npandayCtrls
            foreach (CommandBarControl delCtrl in npandayCmdBarCtrl)
            {
                //false works a temporary control
                delCtrl.Delete(false);
            }

            //unregister maven event listener
            foreach (Project project in dte2.Solution.Projects)
            {
                VSProject vsProject = null;
                try
                {
                    vsProject = (VSProject)project.Object;
                }
                catch
                {
                    continue;
                }

                vsProject.Events.ReferencesEvents.ReferenceRemoved
                        -= new _dispReferencesEvents_ReferenceRemovedEventHandler(ReferencesEvents_ReferenceRemoved);

            }

            foreach (WebServicesReferenceWatcher w in wsRefWatcher)
            {
                w.Stop();
            }

            foreach (WebServicesReferenceWatcher s in svRefWatcher)
            {
                s.Stop();
            }

            if (disconnectMode != Extensibility.ext_DisconnectMode.ext_dm_HostShutdown)
            {
                this.OnBeginShutdown(ref custom);
            }
            _applicationObject = null;
        }

        //used for Unit Testing on Disconnect
        protected bool IsApplicationObjectNull()
        {
            if (_applicationObject == null)
            {
                return true;
            }
            return false;
        }
        #endregion

        #region OnAddInsUpdate(Array)
        /// <summary>
        /// Implements the OnAddInsUpdate method of the IDTExtensibility2 interface. 
        /// Receives notification when the collection of Add-ins has changed.
        /// </summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnAddInsUpdate(ref Array custom)
        {
        }
        #endregion

        #region OnStartupComplete(Array)
        /// <summary>
        /// Implements the OnStartupComplete method of the IDTExtensibility2 interface. 
        /// Receives notification that the host application has completed loading.
        /// </summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnStartupComplete(ref Array custom)
        {
            launchNPandayBuildSystem();
        }
        #endregion

        #region NPandayBuild

        /// <summary>
        /// Checks the currently selected pom file if there is a vb project in it.
        /// </summary>
        private bool IsVbProject(Project project)
        {
            if (project.UniqueName.Contains("vbproj"))
                return true;
            return false;
        }

        private CommandBarControl GetSaveAllControl()
        {
            CommandBarControl saveCtrl = null;
            EnvDTE80.Windows2 windows2 = (EnvDTE80.Windows2)_applicationObject.Windows;

            DTE2 dte2 = _applicationObject;


            foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
            {
                foreach (CommandBarControl control in commandBar.Controls)
                {
                    if (control.Caption == "Save A&ll")
                    {
                        saveCtrl = control;
                        break;
                    }

                }
            }
            return saveCtrl;
        }

        private void AutoImport()
        {
            string warningMsg = string.Empty;
            Solution2 solution = (Solution2)_applicationObject.Solution;
            ProjectImporter.NPandayImporter.ReImportProject(solution.FullName, ref warningMsg);
        }

        [Obsolete]
        private void SaveAllDocuments()
        {
            // TODO: Hook up for non-English
            SigningEvents_SignatureAdded();

            if (saveAllControl == null)
            {
                saveAllControl = GetSaveAllControl();
            }
            if (saveAllControl != null)
            {
                saveAllControl.Execute();
                //AutoImport();
            }
        }

        private void NPandayBuildSelectedProject(String goal)
        {
            SaveAllDocuments();
            FileInfo pomFile = CurrentSelectedProjectPom;
            Project project = CurrentSelectedProject;
            PomHelperUtility pomUtility = new PomHelperUtility(pomFile);

            string errStr = null;

            if (pomFile == null)
            {
                errStr = string.Format("Pom File {0} not found!", project.FullName.Substring(0, project.FullName.LastIndexOf('\\')) + "\\pom.xml");
                throw new Exception(errStr);
            }



            if ("pom".Equals(pomUtility.Packaging, StringComparison.OrdinalIgnoreCase))
            {
                errStr = string.Format(Messages.MSG_EF_NOT_A_PROJECT_POM, pomFile);
            }

            if (!string.IsNullOrEmpty(errStr))
            {
                //DialogResult res = MessageBox.Show(errStr + "\nWould you like to continue building?", "Pom Error:", MessageBoxButtons.YesNo, MessageBoxIcon.Error);

                //if (res != DialogResult.Yes)
                //{
                //    throw new Exception(errStr);
                //}
                throw new Exception(errStr);
            }

            try
            {
                //check if project has webreference
                if (ProjectHasWebReferences(project))
                {
                    if (MessageBox.Show("Do you want to update webservice references?", "NPanday Build", MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
                    {
                        UpdateWebReferences(project);
                    }
                }

            }
            catch
            {
                //never update
            }

            executeBuildCommand(pomFile, goal);
        }

        private void NPandayBuildAllProjects(String goal)
        {
            SaveAllDocuments();
            FileInfo pomFile = CurrentSolutionPom;
            PomHelperUtility pomUtility = new PomHelperUtility(pomFile);

            try
            {
                Solution2 solution = (Solution2)_applicationObject.Solution;
                bool asked = false;
                foreach (Project project in GetAllProjects(solution.Projects))
                {
                    if (!IsWebSite(project) && ProjectHasWebReferences(project))
                    {
                        if (!asked && MessageBox.Show("Do you want to update webservice references?", "NPanday Build", MessageBoxButtons.YesNo, MessageBoxIcon.Question) != DialogResult.Yes)
                        {
                            break;
                        }
                        else
                            asked = true;

                        UpdateWebReferences(project);
                    }

                }

            }
            catch
            {
            }

            executeBuildCommand(pomFile, goal);

        }

        #endregion

        #region executeBuildCommand(FileInfo,string)
        [MethodImpl(MethodImplOptions.Synchronized)]
        private void executeBuildCommand(FileInfo pomFile, String goal)
        {

            try
            {
                if (mavenRunner.IsRunning)
                {
                    DialogResult res = MessageBox.Show(Messages.MSG_Q_STOPCURRENTBUILD, Messages.MSG_C_STOPNPANDAYBUILD, MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                    // re-check if it is still running, before calling stop
                    if (mavenRunner.IsRunning && res == DialogResult.Yes)
                    {
                        mavenRunner.stop();
                    }
                    else
                    {
                        // do not execute the request
                        return;
                    }

                }

                //stopButton.Enabled = true;


                if (string.IsNullOrEmpty(ChangeMavenSettingsXmlForm.SettingsXmlFile))
                {
                    mavenRunner.execute(pomFile.FullName, goal);
                }
                else
                {
                    string[] args = new string[1];
                    args[0] = string.Format("-s\"{0}\"", ChangeMavenSettingsXmlForm.SettingsXmlFile);
                    mavenRunner.execute(pomFile.FullName, goal, args);
                }
            }
            catch (Exception err)
            {

                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + err.Message,
                    Messages.MSG_C_EXEC_ERROR,
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
            //stopButton.Enabled = false;


        }
        #endregion

        public FileInfo CurrentSolutionPom
        {
            get
            {
                try
                {
                    FileInfo parentPomFile = new FileInfo(Path.GetDirectoryName(_applicationObject.Solution.FileName) + @"\parent-pom.xml");
                    FileInfo pomFile = new FileInfo(Path.GetDirectoryName(_applicationObject.Solution.FileName) + @"\pom.xml");

                    PomHelperUtility parentPomUtil = new PomHelperUtility(parentPomFile);
                    PomHelperUtility pomUtil = new PomHelperUtility(pomFile);

                    if (!parentPomFile.Exists)
                    {
                        return pomFile;
                    }
                    else if (pomFile.Exists || parentPomFile.Exists)
                    {
                        if (!"pom".Equals(pomUtil.Packaging)
                            && parentPomFile.Exists
                            && "pom".Equals(parentPomUtil.Packaging))
                        {
                            return parentPomFile;
                        }
                        return pomFile;
                    }
                    else
                    {
                        //MessageBox.Show("Parent pom.xml Not Found! ",
                        //"File Not Found:",
                        //MessageBoxButtons.OK,
                        //MessageBoxIcon.Error);
                        throw new Exception(Messages.MSG_E_PARENTPOM_NOTFOUND);
                    }
                }
                catch (Exception)
                {
                    //MessageBox.Show("Locating Parent pom.xml Error: " + e.Message, 
                    //    "Locating Parent pom.xml Error:",
                    //    MessageBoxButtons.OK,
                    //    MessageBoxIcon.Error);
                    throw;
                }
            }
        }

        public FileInfo CurrentSelectedProjectPom
        {
            get
            {

                try
                {
                    FileInfo pomFile = PomHelperUtility.FindPomFileUntil(
                new FileInfo(CurrentSelectedProject.FullName).Directory,
                new FileInfo(_applicationObject.Solution.FileName).Directory);

                    if (pomFile != null)
                    {
                        return pomFile;
                    }

                    return null;

                }
                catch
                {
                    return null;
                }
            }

        }

        public Project CurrentSelectedProject
        {
            get
            {
                foreach (Project project in (Array)_applicationObject.ActiveSolutionProjects)
                {
                    return project;
                }

                return null;

            }
        }


        #region cbRunUnitTest_Click(CommandBarButton,bool)
        private void cbRunUnitTest_Click(CommandBarButton btn, ref bool Cancel)
        {
            executeBuildCommand(CurrentSelectedProjectPom, "org.apache.npanday.plugins:maven-test-plugin:test");
        }
        #endregion

        #region cbCompileAndRunUnitTest_Click(CommandBarButton,bool)
        private void cbCompileAndRunUnitTest_Click(CommandBarButton btn, ref bool Cancel)
        {
            executeBuildCommand(CurrentSelectedProjectPom, "test");
        }
        #endregion

        #region cbInstallAll_Click(CommandBarButton,bool)
        private void cbInstallAll_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildAllProjects("install");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbCleanAll_Click(CommandBarButton,bool)
        private void cbCleanAll_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildAllProjects("clean");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbBuildAll_Click(CommandBarButton,bool)
        private void cbBuildAll_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildAllProjects("compile");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbTestAll_Click(CommandBarButton,bool)
        private void cbTestAll_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildAllProjects("test");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbInstall_Click(CommandBarButton,bool)
        private void cbInstall_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildSelectedProject("install");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbInstall_Click(CommandBarButton,bool)

        private void cbDeploy_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildSelectedProject("deploy");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbClean_Click(CommandBarButton,bool)
        private void cbClean_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildSelectedProject("clean");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbBuild_Click(CommandBarButton,bool)
        private void cbBuild_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildSelectedProject("compile");
            }
            catch (Exception e)
            {
                MessageBox.Show(Messages.MSG_E_EXEC_ERROR + e.Message, Messages.MSG_C_ERROR, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        #region cbTest_Click(CommandBarButton,bool)
        private void cbTest_Click(CommandBarButton btn, ref bool Cancel)
        {
            try
            {
                NPandayBuildSelectedProject("test");
            }
            catch (Exception e)
            {
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        #endregion

        private void cbShowConfigureRepositoryForm_Click(CommandBarButton btn, ref bool Cancel)
        {
            new ConfigureMavenRepositoryForm().Show();
        }

        #region cbStopMavenBuild_Click(CommandBarButton,bool)
        private void cbStopMavenBuild_Click(CommandBarButton btn, ref bool Cancel)
        {
            if (mavenRunner != null && mavenRunner.IsRunning)
            {
                DialogResult res = MessageBox.Show(Messages.MSG_Q_STOP_MAVEN_BUILD, Messages.MSG_C_STOPNPANDAYBUILD, MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                // re-check if it is still running, before calling stop
                if (mavenRunner.IsRunning && res == DialogResult.Yes)
                {
                    mavenRunner.stop();
                }
            }
        }
        #endregion

        // by jan ancajas
        #region cbChangeSettingsXmlForm_Click(CommandBarButton, bool)
        private void cbChangeSettingsXmlForm_Click(CommandBarButton btn, ref bool Cancel)
        {
            ChangeMavenSettingsXmlForm frm = new ChangeMavenSettingsXmlForm();
            frm.ShowDialog();
        }
        #endregion

        #region cbSetSignAssemblyForm_Click(CommandBarButton, bool)

        private void cbSetSignAssemblyForm_Click(CommandBarButton btn, ref bool Cancel)
        {


            //First selected project
            foreach (Project project in (Array)_applicationObject.ActiveSolutionProjects)
            {
                NPandaySignAssembly frm = new NPandaySignAssembly(project, container, CurrentSelectedProjectPom);
                frm.ShowDialog();
                break;
            }


        }

        #endregion

        #region OnBeginShutdown(Array)
        /// <summary>Implements the OnBeginShutdown method of the IDTExtensibility2 interface. Receives notification that the host application is being unloaded.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnBeginShutdown(ref Array custom)
        {
            log.Info(Messages.MSG_L_SHUTTING_DOWN_NPANDAY);
            mavenRunner.Quit();
            log.Info(Messages.MSG_L_SUCCESFULLY_SHUTDOWN);
        }
        #endregion

        #region QueryStatus(string,vsCommandStatusTextWanted,vsCommandStatus,commandText)
        /// <summary>
        /// Implements the QueryStatus method of the IDTCommandTarget interface. 
        /// This is called when the command's availability is updated.
        /// </summary>
        /// <param term='commandName'>The name of the command to determine state for.</param>
        /// <param term='neededText'>Text that is needed for the command.</param>
        /// <param term='status'>The state of the command in the user interface.</param>
        /// <param term='commandText'>Text requested by the neededText parameter.</param>
        /// <seealso class='Exec' />
        public void QueryStatus(string commandName, vsCommandStatusTextWanted neededText, ref vsCommandStatus status, ref object commandText)
        {

            if (neededText == vsCommandStatusTextWanted.vsCommandStatusTextWantedNone)
            {
                if (commandName == "IDEAddin.Connect.IDEAddin")
                {
                    attachReferenceEvent();
                    status = (vsCommandStatus)vsCommandStatus.vsCommandStatusSupported | vsCommandStatus.vsCommandStatusEnabled;
                    return;
                }
            }
        }
        #endregion

        #region Exec(string,vsCommandExecOption,object,object,bool)
        /// <summary>
        /// Implements the Exec method of the IDTCommandTarget interface. 
        /// This is called when the command is invoked.
        /// </summary>
        /// <param term='commandName'>The name of the command to execute.</param>
        /// <param term='executeOption'>Describes how the command should be run.</param>
        /// <param term='varIn'>Parameters passed from the caller to the command handler.</param>
        /// <param term='varOut'>Parameters passed from the command handler to the caller.</param>
        /// <param term='handled'>Informs the caller if the command was handled or not.</param>
        /// <seealso class='Exec' />
        public void Exec(string commandName, vsCommandExecOption executeOption, ref object varIn, ref object varOut, ref bool handled)
        {
            //outputWindowPane.OutputString(commandName);
            handled = false;
            if (executeOption == vsCommandExecOption.vsCommandExecOptionDoDefault)
            {
                handled = true;
            }
        }
        #endregion

        #region Fields
        private DTE2 _applicationObject;
        private AddIn _addInInstance;
        private OutputWindowPane outputWindowPane;
        private List<CommandBarButton> addReferenceControls;
        private List<CommandBarButton> nunitControls;
        private List<CommandBarControl> buildControls;
        private EnvDTE.SelectionEvents _selectionEvents;
        private ArtifactContext container;
        private List<ReferencesEvents> referenceEvents;
        private List<VSLangProj80.VSLangProjWebReferencesEvents> webRefEvents;
        //private DirectoryInfo baseDirectoryInfo; 
        private MavenRunner mavenRunner;
        private bool _npandayLaunched = false;
        private CommandBarButton stopButton;

        private ButtonCommandRegistry _buttonCommandRegistry;
        private VisualStudioControlsFinder _finder;

        private static readonly ILog log = LogManager.GetLogger(typeof(Connect));

        List<WebServicesReferenceWatcher> wsRefWatcher = new List<WebServicesReferenceWatcher>();
        List<WebServicesReferenceWatcher> svRefWatcher = new List<WebServicesReferenceWatcher>();

        #endregion

        #region IWebServicesRefUtils Members

        public bool ProjectHasWebReferences(Project project)
        {
            try
            {
                if (project.Object == null)
                    return false;

                VSProject2 p = (VSProject2)project.Object;
                if (p.WebReferencesFolder == null)
                    return false;
                if (p.WebReferencesFolder.ProjectItems.Count > 0)
                    return true;
                else
                    return false;

            }
            catch
            {
                return false;
            }
        }

        //public bool RemovePomWebReferenceInfo(string webRefNamespace)
        //{
        //    PomHelperUtility pomUtil = createPomUtility();
        //    pomUtil.RemoveWebReference(webRefNamespace);
        //    return true;

        //}

        public bool AddPomWebReferenceInfo(IWebServiceRefInfo webref)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public void UpdateWebReferences(Project project)
        {
            foreach (IWebServiceRefInfo wsInfo in GetWebReferences(project))
            {
                try
                {
                    UpdateWSDLFile(wsInfo);

                }
                catch (Exception ex)
                {
                    log.ErrorFormat("Error updating {0}. [{1}]", wsInfo.Name, ex.Message);
                }
            }

        }

        public List<IWebServiceRefInfo> GetWebReferences(Project project)
        {
            VSProject2 p = (VSProject2)project.Object;
            List<IWebServiceRefInfo> list = new List<IWebServiceRefInfo>();

            foreach (ProjectItem item in p.WebReferencesFolder.ProjectItems)
            {
                string refFolder = Path.Combine(Path.Combine(Path.GetDirectoryName(project.FullName), p.WebReferencesFolder.Name), item.Name);
                string fname = WebServicesReferenceUtils.GetReferenceFile(refFolder);
                if (!string.IsNullOrEmpty(fname))
                {
                    WebServiceRefInfo wr = new WebServiceRefInfo(item.Name, WebServicesReferenceUtils.GetWsdlUrl(fname));
                    wr.WsdlFile = WebServicesReferenceUtils.GetWsdlFile(refFolder);
                    list.Add(wr);
                }
            }

            return list;
        }

        public bool UpdateWSDLFile(IWebServiceRefInfo webRef)
        {
            byte[] page = null;
            string wsdlUrl = webRef.WSDLUrl;

            WebClient webClient = new WebClient();
            try
            {
                page = webClient.DownloadData(wsdlUrl);

                string wsdlContent = Encoding.UTF8.GetString(page);
                TextWriter wr = new StreamWriter(webRef.WsdlFile, false);
                wr.Write(wsdlContent);
                wr.Flush();
                wr.Close();

            }
            catch (Exception ex)
            {
                MessageBox.Show("Cannot read url : " + wsdlUrl + Environment.NewLine + ex.Message + Environment.NewLine + ex.StackTrace);
                return false;
            }
            return true;
        }

        public bool GenerateProxies(IWebServiceRefInfo webRef)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        #endregion

    }
    #endregion
}
