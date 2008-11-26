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
using Extensibility;
using EnvDTE;
using EnvDTE80;

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Resources;
using System.Reflection;
using System.Globalization;
using System.Drawing;
using System.Threading;
//using System.Web.Services.Protocols;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Serialization;
using System.Xml.XPath;

using Microsoft.VisualStudio.CommandBars;

using VSLangProj;

using NMaven.Artifact;
using NMaven.Logging;
using NMaven.VisualStudio.Logging;

using NMaven.Model.Setting;
using NMaven.Model.Pom;

using NMaven.Utils;
using System.Runtime.CompilerServices;
using VSLangProj80;
using System.Text;


#endregion

namespace NMaven.VisualStudio.Addin
{
    /// <summary>
    /// MSG_E_* = ERROR MSGS
    /// MSG_EF_* = ERROR MSGS W/ STRING.FORMAT
    /// MSG_L_* = LOG MSGS
    /// MSG_Q_* = QUESTIONS STATEMENT
    /// MSG_D_* = DISPLAY TEXT(Button, CommnadBar)
    /// MSG_C_* = Caption
    /// </summary>
    public static class Messages
    {
        public const string MSG_E_NOTIMPLEMENTED = "The method or operation is not implemented.";
        public const string MSG_L_NMAVEN_ALREADY_STARTED = "\nNMaven Addin Has Already Started.";
        public const string MSG_L_NMAVEN_ADDIN_STARTED = "\nNMaven Addin Successfully Started.";
        public const string MSG_E_NMAVEN_REMOVE_DEPENDENCY_ERROR = "NMaven Remove Dependency Error:";
        public const string MSG_Q_STOP_MAVEN_BUILD = "Do you want to stop the Maven Build?";
        public const string MSG_EF_NOT_A_PROJECT_POM = "Not A Project Pom Error: {0} is not a project Pom, the pom is a parent pom type.";
        public const string MSG_EF_NOT_THE_PROJECT_POM = "The Pom may not be the project's Pom: Project Name: {0} is not equal to Pom artifactId: {1}";
        public const string MSG_E_PARENTPOM_NOTFOUND = "parent-pom.xml Not Found";//from Parent pom.xml to paren-pom.xml
        public const string MSG_E_EXEC_ERROR = "Maven Execution Error: ";
        public const string MSG_L_SHUTTING_DOWN_NMAVEN = "\nShutting Down NMaven Visual Studio Addin.";
        public const string MSG_L_SUCCESFULLY_SHUTDOWN = "\nNMaven Successfully Stopped.";//from ShutDown to Stopped
        public const string MSG_D_NMAVEN_BUILD_SYSTEM = "NMaven Build System";
        public const string MSG_T_NMAVEN_BUILDSYSTEM = "Executes the command for NMaven Addin";
        public const string MSG_C_ADD_REFERENCE = "Add &Reference...";
        public const string MSG_C_ADD_WEB_REFERENCE = "Add W&eb Reference...";
        public const string MSG_C_UPDATE_POM_WEB_REFERENCES = "Update POM Web References...";
        public const string MSG_C_CONFIGURE_MAVEN_REPO = "Configure Maven Repository...";
        public const string MSG_C_ADD_MAVEN_ARTIFACT = "Add Maven Artifact...";
        public const string MSG_C_CHANGE_MAVEN_SETTING_XML = "Change Maven settings.xml...";
        public const string MSG_C_SET_COMPILE_SIGN_ASSEMBLY_KEY = "Set NMaven Compile Sign Assembly Key...";
        public const string MSG_C_IMPORT_PROJECT = "NMaven: Import Project";
        public const string MSG_C_STOP_MAVEN_BUILD = "Stop Maven Build";
        public const string MSG_C_MAVEN_PHASE = "Maven Phase";
        public const string MSG_C_CLEAN_ALLPROJECT = "All Projects: Clean";
        public const string MSG_C_TEST_ALLPROJECT = "All Projects: Test";
        public const string MSG_C_INSTALL_ALLPROJECT = "All Projects: Install";
        public const string MSG_C_BUILD_CURRENTPROJECT = "Current Project: Build [compile]";
        public const string MSG_C_INSTALL_CURRENTPROJECT = "Current Project: Install";
        public const string MSG_C_BUILD_ALLPROJECT = "All Projects: Build [compile]";
        public const string MSG_C_CLEAN_CURRENTPROJECT = "Current Project: Clean";
        public const string MSG_C_TEST_CURRENTPROJECT = "Current Project: Test";
        public const string MSG_C_RUNUNITTEST = "Run Unit Test/s";
        public const string MSG_C_COMPILEANDRUNTEST = "Compile and Run Unit Test/s";
        public const string MSG_Q_STOPCURRENTBUILD = "A Maven Build is currently running, Do you want to stop the build and proceed to a new Build Execution?";
        public const string MSG_C_STOPNMAVENBUILD = "Stop NMaven Build";
        public const string MSG_C_EXEC_ERROR = "Execution Error:";
        public const string MSG_C_ERROR = "Error";
    }
    public class NMavenBuildSystemProperties : System.ComponentModel.ISynchronizeInvoke
    {

        private object application;

        public object Application
        {
            get { return application; }
            set { application = value; }
        }

        #region ISynchronizeInvoke Members

        public IAsyncResult BeginInvoke(Delegate method, object[] args)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public object EndInvoke(IAsyncResult result)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public object Invoke(Delegate method, object[] args)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public bool InvokeRequired
        {
            get { return false; }
        }

        #endregion
    }

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

        void SolutionEvents_BeforeClosing()
        {
            mavenRunner.ClearOutputWindow();
            outputWindowPane.Clear();
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

            _applicationObject = (DTE2)application;
            mavenRunner = new MavenRunner(_applicationObject);
            _addInInstance = (AddIn)addInInst;
            Command command = null;
            mavenConnected = true;

            //next two lines add a eventhandler to handle beforeclosing a solution
            globalSolutionEvents = (EnvDTE.SolutionEvents)((Events2)_applicationObject.Events).SolutionEvents;
            globalSolutionEvents.BeforeClosing += new _dispSolutionEvents_BeforeClosingEventHandler(SolutionEvents_BeforeClosing);


            if (connectMode == ext_ConnectMode.ext_cm_UISetup)
            {

                object[] contextGUIDS = new object[] { };
                Commands2 commands = (Commands2)_applicationObject.Commands;
                string toolsMenuName;

                try
                {
                    //If you would like to move the command to a different menu, change the word "Tools" to the
                    //  English version of the menu. This code will take the culture, append on the name of the menu
                    //  then add the command to that menu. You can find a list of all the top-level menus in the file
                    //  CommandBar.resx.
                    ResourceManager resourceManager = new ResourceManager("IDEAddin.CommandBar", Assembly.GetExecutingAssembly());
                    CultureInfo cultureInfo = new System.Globalization.CultureInfo(_applicationObject.LocaleID);
                    string resourceName = String.Concat(cultureInfo.TwoLetterISOLanguageName, "Tools");
                    toolsMenuName = resourceManager.GetString(resourceName);
                }
                catch
                {
                    //We tried to find a localized version of the word Tools, but one was not found.
                    //  Default to the en-US word, which may work for the current culture.
                    toolsMenuName = "Tools";
                }

                //Place the command on the tools menu.
                //Find the MenuBar command bar, which is the top-level command bar holding all the main menu items:
                Microsoft.VisualStudio.CommandBars.CommandBar menuBarCommandBar = ((Microsoft.VisualStudio.CommandBars.CommandBars)_applicationObject.CommandBars)["MenuBar"];

                //Find the Tools command bar on the MenuBar command bar:
                CommandBarControl toolsControl = menuBarCommandBar.Controls[toolsMenuName];
                CommandBarPopup toolsPopup = (CommandBarPopup)toolsControl;

                //This try/catch block can be duplicated if you wish to add multiple commands to be handled by your Add-in,
                //  just make sure you also update the QueryStatus/Exec method to include the new command names.
                try
                {
                    //Add a command to the Commands collection:
                    command = commands.AddNamedCommand2(_addInInstance, "NMavenAddin",
                        Messages.MSG_D_NMAVEN_BUILD_SYSTEM, Messages.MSG_T_NMAVEN_BUILDSYSTEM, true, 480, ref contextGUIDS,
                        (int)vsCommandStatus.vsCommandStatusSupported + (int)vsCommandStatus.vsCommandStatusEnabled,
                        (int)vsCommandStyle.vsCommandStylePictAndText,
                        vsCommandControlType.vsCommandControlTypeButton);

                    //Add a control for the command to the tools menu:
                    if ((command != null) && (toolsPopup != null))
                    {
                        command.AddControl(toolsPopup.CommandBar, 1);
                    }
                }
                catch (System.ArgumentException)
                {
                    //If we are here, then the exception is probably because a command with that name
                    //  already exists. If so there is no need to recreate the command and we can
                    //  safely ignore the exception.
                }

            }
            else if (connectMode == ext_ConnectMode.ext_cm_AfterStartup)
            {
                launchNMavenBuildSystem();
            }
        }

        private const string WEB_PROJECT_KIND_GUID = "{E24C65DC-7377-472B-9ABA-BC803B73C61A}";

        public static bool IsWebProject(Project project)
        {
            // make sure there's a project item
            if (project == null)
                return false;

            // compare the project kind to the web project guid
            return (String.Compare(project.Kind, WEB_PROJECT_KIND_GUID, true) == 0);
        }

        void attachReferenceEvent()
        {
            //References
            referenceEvents = new List<ReferencesEvents>();
            Solution2 solution = (Solution2)_applicationObject.Solution;

            this.wsRefWatcher = new List<WebServicesReferenceWatcher>();

            foreach (Project project in solution.Projects)
            {
                projectRefEventLoaded = true;

                string referenceFolder = string.Empty;

                if (IsWebProject(project))
                {
                    // not needed anymore

                    //VsWebSite.VSWebSite website = (VsWebSite.VSWebSite)project.Object;
                    //string binPath = Path.Combine(website.Project.FullName, "Bin");
                    //if(!Directory.Exists(binPath))
                    //{
                    //    Directory.CreateDirectory(binPath);
                    //}
                    //WebsiteAssemblyReferenceWatcher webw = new WebsiteAssemblyReferenceWatcher(binPath);
                    //webw.Deleted += new FileSystemEventHandler(webw_Deleted);
                    //webw.Start();
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

                        }
                        catch (Exception ex)
                        {

                            throw ex;
                        }
                    }
                    catch
                    {
                        //  not a csproj / vbproj file. Could be a solution folder. skip it.
                        continue;
                    }
                }

            }
        }

        void ReferencesEvents_ReferenceAdded(Reference pReference)
        {
            try
            {
                if (!mavenConnected)
                    return;

                ArtifactContext artifactContext = new ArtifactContext();
                Artifact.Artifact artifact = new NMaven.Artifact.Artifact();

                bool inMavenRepo = false;
                try
                {
                    artifact = artifactContext.GetArtifactRepository().GetArtifact(new FileInfo(pReference.Path));
                    inMavenRepo = true;
                }
                catch
                {
                }

                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, CurrentSelectedProject);
                if (pomUtil.IsPomDependency(pReference.Name))
                {
                    return;
                }

                string refType = "gac_msil";
                string refName = pReference.Name;
                string refToken = pReference.PublicKeyToken;

                if (pReference.Type == prjReferenceType.prjReferenceTypeActiveX)
                {
                    refType = "com_reference";
                    if (refName.ToLower().StartsWith("interop."))
                        refName = refName.Substring(8);
                    refToken = pReference.Identity.Substring(0, pReference.Identity.LastIndexOf(@"\"));
                }
                else
                {
                    GacUtility gac = new GacUtility();
                    string n = gac.GetAssemblyInfo(pReference.Name);
                    if (pReference.SourceProject != null)
                    {
                        refType = "library";
                    }
                    else if (!inMavenRepo && string.IsNullOrEmpty(n))
                    {
                        MessageBox.Show(string.Format("Warning: Build may not be portable if local references are used, Reference is not in Maven Repository or in GAC."
                                                             + "\nReference: {0}"
                                                             + "\nDeploying the Reference, will make the code portable to other machines",
                                                     pReference.Name
                                                 ), "Add Reference", MessageBoxButtons.OK, MessageBoxIcon.Warning); refType = "library";
                        //return;
                    }
                }

                Dependency dep = new Dependency();
                dep.artifactId = refName;
                dep.groupId = refName;
                dep.version = pReference.Version;
                dep.classifier = refToken;
                dep.type = refType;
                pomUtil.AddPomDependency(dep);
                //SaveAllDocuments();
                //AutoImport();
            }
            catch (Exception e)
            {
                outputWindowPane.OutputString(e.Message);
            }
        }

        void webw_Deleted(object sender, FileSystemEventArgs e)
        {
            try
            {
                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, CurrentSelectedProject);
                if (Path.GetExtension(e.Name).ToLower() == ".dll" || Path.GetExtension(e.Name).ToLower() == ".exe")
                {
                    pomUtil.RemovePomDependency(Path.GetFileNameWithoutExtension(e.Name));
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, Messages.MSG_E_NMAVEN_REMOVE_DEPENDENCY_ERROR);
            }

        }

        void wsw_Renamed(object sender, WebReferenceEventArgs e)
        {
            try
            {
                System.Threading.Thread.Sleep(1500);
                e.Init(projectReferenceFolder(CurrentSelectedProject));
                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, CurrentSelectedProject);
                pomUtil.RenameWebReference(e.OldNamespace, e.Namespace, e.WsdlFile, string.Empty);

            }
            catch (Exception ex)
            {
                outputWindowPane.OutputString("\nError on webservice rename: " + ex.Message);
            }
        }

        void wsw_Deleted(object sender, WebReferenceEventArgs e)
        {
            try
            {
                e.Init(projectReferenceFolder(CurrentSelectedProject));
                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, CurrentSelectedProject);
                pomUtil.RemoveWebReference(e.Namespace);

            }
            catch (Exception ex)
            {
                outputWindowPane.OutputString("\nError on webservice delete: " + ex.Message);
            }
        }

        void wsw_Created(object sender, WebReferenceEventArgs e)
        {
            try
            {
                //wait for the files to be created
                System.Threading.Thread.Sleep(1500);
                Solution2 solution = (Solution2)_applicationObject.Solution;
                e.Init(projectReferenceFolder(CurrentSelectedProject));

                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, CurrentSelectedProject);

                pomUtil.AddWebReference(e.Namespace, e.WsdlFile, string.Empty);

            }
            catch (Exception ex)
            {
                outputWindowPane.OutputString("\nError on webservice create: " + ex.Message);
            }
        }

        string projectReferenceFolder(Project project)
        {
            VSProject2 vsProject = (VSProject2)project.Object;

            ProjectItem webReferenceFolder = vsProject.WebReferencesFolder;
            if (webReferenceFolder == null)
            {
                webReferenceFolder = vsProject.CreateWebReferencesFolder();
            }

            string wsPath = Path.Combine(Path.GetDirectoryName(project.FullName), webReferenceFolder.Name);
            return wsPath;
        }

        private void launchNMavenBuildSystem()
        {
            // just to be safe, check if nmaven is already launched
            if (_nmavenLaunched)
            {
                outputWindowPane.OutputString(Messages.MSG_L_NMAVEN_ALREADY_STARTED);
                return;
            }

            Window win = _applicationObject.Windows.Item(EnvDTE.Constants.vsWindowKindOutput);
            OutputWindow outputWindow = (OutputWindow)win.Object;
            OutputWindowPane outputPane = null;
            OutputWindowPanes panes = outputWindow.OutputWindowPanes;

            // Reuse the existing pane (if it exists)
            Boolean paneExists = false;
            for (int i = 1; i <= panes.Count; i++)
            {
                outputPane = panes.Item(i);
                if (outputPane.Name == "NMaven Build System")
                {
                    paneExists = true;
                    outputWindowPane = outputPane;
                }

            }
            if (!paneExists)
            {
                outputWindowPane = outputWindow.OutputWindowPanes.Add("NMaven Build System");
            }

            //outputWindowPane = OutputWindowPanes.Add("NMaven Build System");

            OutputWindowPaneHandler handler = new OutputWindowPaneHandler();
            handler.SetOutputWindowPaneHandler(outputWindowPane);

            logger = NMaven.Logging.Logger.GetLogger("UC");
            logger.AddHandler(handler);

            container = new ArtifactContext();



            EnvDTE80.Windows2 windows2 = (EnvDTE80.Windows2)_applicationObject.Windows;

            DTE2 dte2 = _applicationObject;

            addReferenceControls = new List<CommandBarButton>();
            buildControls = new List<CommandBarControl>();
            foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
            {
                foreach (CommandBarControl control in commandBar.Controls)
                {
                    if (control.Caption.Equals(Messages.MSG_C_ADD_REFERENCE))
                    {
                        CommandBarButton ctl = (CommandBarButton)
                            commandBar.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, control.Index, true);
                        ctl.Click += new _CommandBarButtonEvents_ClickEventHandler(cbShowAddArtifactsForm_Click);
                        ctl.Caption = Messages.MSG_C_ADD_MAVEN_ARTIFACT;
                        ctl.Visible = true;
                        addReferenceControls.Add(ctl);

                        CommandBarButton ctl1 = (CommandBarButton)
                            commandBar.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, control.Index, true);
                        ctl1.Click +=
                            new _CommandBarButtonEvents_ClickEventHandler(cbShowConfigureRepositoryForm_Click);
                        ctl1.Caption = Messages.MSG_C_CONFIGURE_MAVEN_REPO;
                        ctl1.Visible = true;
                        addReferenceControls.Add(ctl1);

                        // by jan ancajas
                        CommandBarButton ctlSettingsXml = (CommandBarButton)
                        commandBar.Controls.Add(MsoControlType.msoControlButton,
                                                  System.Type.Missing,
                                                  System.Type.Missing,
                                                  control.Index,
                                                  true);
                        ctlSettingsXml.Click +=
                            new _CommandBarButtonEvents_ClickEventHandler(cbChangeSettingsXmlForm_Click);
                        ctlSettingsXml.Caption = Messages.MSG_C_CHANGE_MAVEN_SETTING_XML;
                        ctlSettingsXml.Visible = true;
                        addReferenceControls.Add(ctlSettingsXml);



                        CommandBarButton ctlSignAssembly = (CommandBarButton)
                        commandBar.Controls.Add(MsoControlType.msoControlButton,
                                                  System.Type.Missing,
                                                  System.Type.Missing,
                                                  control.Index,
                                                  true);
                        ctlSignAssembly.Click +=
                            new _CommandBarButtonEvents_ClickEventHandler(cbSetSignAssemblyForm_Click);
                        ctlSignAssembly.Caption = Messages.MSG_C_SET_COMPILE_SIGN_ASSEMBLY_KEY;
                        ctlSignAssembly.Visible = true;
                        addReferenceControls.Add(ctlSignAssembly);



                        CommandBarButton ctlProjectImport = (CommandBarButton)
                        commandBar.Controls.Add(MsoControlType.msoControlButton,
                                                  System.Type.Missing,
                                                  System.Type.Missing,
                                                  control.Index,
                                                  true);
                        ctlProjectImport.Click +=
                            new _CommandBarButtonEvents_ClickEventHandler(cbChangeProjectImportForm_Click);
                        ctlProjectImport.Caption = Messages.MSG_C_IMPORT_PROJECT;
                        ctlProjectImport.Visible = true;
                        addReferenceControls.Add(ctlProjectImport);



                    }
                    // included build web site to support web site projects
                    else if ((control.Caption.Equals("Clea&n")) || (control.Caption.Equals("Publis&h Selection")) || (control.Caption.Equals("Publis&h Web Site")))
                    {
                        // Add the stop maven build button here

                        CommandBarButton stopButton = (CommandBarButton)commandBar.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, control.Index + 1, true);
                        stopButton.Caption = Messages.MSG_C_STOP_MAVEN_BUILD;
                        stopButton.Visible = true;
                        stopButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbStopMavenBuild_Click);
                        buildControls.Add(stopButton);




                        CommandBarPopup ctl = (CommandBarPopup)
                            commandBar.Controls.Add(MsoControlType.msoControlPopup,
                            System.Type.Missing, System.Type.Missing, control.Index + 1, true);
                        ctl.Caption = Messages.MSG_C_MAVEN_PHASE;
                        ctl.Visible = true;
                        buildControls.Add(ctl);



                        CommandBarButton cleanAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        cleanAllButton.Caption = Messages.MSG_C_CLEAN_ALLPROJECT;
                        cleanAllButton.Visible = true;
                        cleanAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbCleanAll_Click);


                        CommandBarButton testAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        testAllButton.Caption = Messages.MSG_C_TEST_ALLPROJECT;
                        testAllButton.Visible = true;
                        testAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbTestAll_Click);

                        CommandBarButton installAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        installAllButton.Caption = Messages.MSG_C_INSTALL_ALLPROJECT;
                        installAllButton.Visible = true;
                        installAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbInstallAll_Click);

                        CommandBarButton buildAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        buildAllButton.Caption = Messages.MSG_C_BUILD_ALLPROJECT;
                        buildAllButton.Visible = true;
                        buildAllButton.FaceId = 645;
                        buildAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbBuildAll_Click);



                        CommandBarButton cleanButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        cleanButton.Caption = Messages.MSG_C_CLEAN_CURRENTPROJECT;
                        cleanButton.Visible = true;
                        cleanButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbClean_Click);



                        CommandBarButton testButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        testButton.Caption = Messages.MSG_C_TEST_CURRENTPROJECT;
                        testButton.Visible = true;
                        testButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbTest_Click);

                        CommandBarButton installButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        installButton.Caption = Messages.MSG_C_INSTALL_CURRENTPROJECT;
                        installButton.Visible = true;
                        installButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbInstall_Click);

                        CommandBarButton buildButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, 1, true);
                        buildButton.Caption = Messages.MSG_C_BUILD_CURRENTPROJECT;
                        buildButton.Visible = true;
                        buildButton.FaceId = 645;
                        buildButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbBuild_Click);

                        buildControls.Add(buildAllButton);
                        buildControls.Add(installAllButton);
                        buildControls.Add(cleanAllButton);
                        buildControls.Add(testAllButton);


                        buildControls.Add(buildButton);
                        buildControls.Add(installButton);
                        buildControls.Add(cleanButton);
                        buildControls.Add(testButton);


                    }
                }
            }
            nunitControls = new List<CommandBarButton>();
            Window solutionExplorerWindow = dte2.Windows.Item(Constants.vsWindowKindSolutionExplorer);
            _selectionEvents = dte2.Events.SelectionEvents;
            _selectionEvents.OnChange += new _dispSelectionEvents_OnChangeEventHandler(this.OnChange);
            _nmavenLaunched = true;
            outputWindowPane.Clear();
            outputWindowPane.OutputString(Messages.MSG_L_NMAVEN_ADDIN_STARTED);

            if (_applicationObject.Solution != null)
                attachReferenceEvent();
        }

        void awfButton_Click(CommandBarButton Ctrl, ref bool CancelDefault)
        {
            outputWindowPane.OutputString("\n Add web reference click.");
        }
        #endregion

        void ReferencesEvents_ReferenceRemoved(Reference pReference)
        {
            try
            {
                if (!mavenConnected)
                    return;

                NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, pReference.ContainingProject);
                string refName = pReference.Name;
                if (pReference.Type == prjReferenceType.prjReferenceTypeActiveX && refName.ToLower().StartsWith("interop."))
                    refName = refName.Substring(8);

                pomUtil.RemovePomDependency(refName);
            }
            catch //(Exception e)
            {
                //MessageBox.Show(e.Message, Messages.MSG_E_NMAVEN_REMOVE_DEPENDENCY_ERROR);
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
            mavenConnected = false;
            //remove maven menus
            DTE2 dte2 = _applicationObject;

            addReferenceControls = new List<CommandBarButton>();
            buildControls = new List<CommandBarControl>();

            outputWindowPane.Clear();
            mavenRunner.ClearOutputWindow();

            foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
            {
                foreach (CommandBarControl control in commandBar.Controls)
                {
                    if (control.Caption.ToLower().Contains("maven"))
                    {
                        // i dont know what 'false' means. but it works.
                        control.Delete(false);
                    }

                }
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

                //vsProject.Events.ReferencesEvents.ReferenceRemoved
                //        -= new _dispReferencesEvents_ReferenceRemovedEventHandler(ReferencesEvents_ReferenceRemoved);

            }

            foreach (WebServicesReferenceWatcher w in wsRefWatcher)
            {
                w.Stop();
            }

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
            launchNMavenBuildSystem();
        }
        #endregion

        #region NMavenBuild

        /// <summary>
        /// Checks the currently selected pom file if there is a vb project in it.
        /// </summary>
        private bool IsVbProject(Project project)
        {
            if (project.UniqueName.Contains("vbproj"))
                return true;
            return false;
        }


        /// <summary>
        /// Updates the pomfile configuration on its rootnamespace
        /// </summary>
        private void UpdateVBProjectsPoms()
        {
            FileInfo pomFile = CurrentSelectedProjectPom;
            NMavenPomHelperUtility pomUtility = new NMavenPomHelperUtility(pomFile);
            if (pomUtility.NMavenCompilerPluginLanguage == "vb" || pomUtility.NMavenCompilerPluginLanguage == "VB")
            {

                string rootNamespace = string.Empty;
                string startUp = string.Empty;

                string projectPath = pomFile.DirectoryName + "\\" + pomUtility.ArtifactId + ".vbproj";

                FileStream fs = new FileStream(projectPath, FileMode.Open, FileAccess.Read,
                                   FileShare.ReadWrite);
                XmlDocument xmldoc = new XmlDocument();
                xmldoc.Load(fs);
                XmlNodeList nodelist = xmldoc.GetElementsByTagName("PropertyGroup");

                foreach (XmlNode xmlnode in nodelist)
                {

                    XmlNodeList childNodeLlists = xmlnode.ChildNodes;
                    foreach (XmlNode child in childNodeLlists)
                    {
                        if (child.Name.Equals("StartupObject"))
                        {
                            startUp = child.InnerText;
                        }
                        if (child.Name.Equals("RootNamespace"))
                        {
                            rootNamespace = child.InnerText;
                            break;
                        }
                    }
                }
                pomUtility.SetNMavenCompilerPluginConfigurationValue("rootNamespace", rootNamespace);
                pomUtility.SetNMavenCompilerPluginConfigurationValue("main", startUp);

                fs.Close();
            }
            SaveAllDocuments();
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
            Solution2 solution = (Solution2)_applicationObject.Solution;
            ProjectImporter.NMavenImporter.ReImportProject(solution.FullName);
        }

        private void SaveAllDocuments()
        {
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

        private void NMavenBuildSelectedProject(String goal)
        {
            SaveAllDocuments();
            UpdateVBProjectsPoms();
            FileInfo pomFile = CurrentSelectedProjectPom;
            Project project = CurrentSelectedProject;
            NMavenPomHelperUtility pomUtility = new NMavenPomHelperUtility(pomFile);

            string errStr = null;

            if ("pom".Equals(pomUtility.Packaging, StringComparison.OrdinalIgnoreCase))
            {
                errStr = string.Format(Messages.MSG_EF_NOT_A_PROJECT_POM, pomFile);
            }
            else if (!IsWebProject(project) && !pomUtility.ArtifactId.Equals(NMavenImportProjectForm.FilterID(project.Name), StringComparison.OrdinalIgnoreCase))
            {
                errStr = string.Format(Messages.MSG_EF_NOT_THE_PROJECT_POM, project.Name, pomUtility.ArtifactId);
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
                    if (MessageBox.Show("Do you want to update webservice references?", "NMaven Build", MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
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

        private void NMavenBuildAllProjects(String goal)
        {
            SaveAllDocuments();
            UpdateVBProjectsPoms();
            FileInfo pomFile = CurrentSolutionPom;
            NMavenPomHelperUtility pomUtility = new NMavenPomHelperUtility(pomFile);
            if (!"pom".Equals(pomUtility.Packaging, StringComparison.OrdinalIgnoreCase))
            {
                //DialogResult res = MessageBox.Show(errStr + "\nWould you like to continue building?", "Pom Error:", MessageBoxButtons.YesNo, MessageBoxIcon.Error);

                //if (res != DialogResult.Yes)
                //{
                //    throw new Exception(errStr);
                //}
                //throw new Exception(errStr);
            }

            try
            {
                Solution2 solution = (Solution2)_applicationObject.Solution;
                bool asked = false;
                foreach (Project project in solution.Projects)
                {
                    if (!IsWebProject(project) && ProjectHasWebReferences(project))
                    {
                        if (!asked && MessageBox.Show("Do you want to update webservice references?", "NMaven Build", MessageBoxButtons.YesNo, MessageBoxIcon.Question) != DialogResult.Yes)
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
                    DialogResult res = MessageBox.Show(Messages.MSG_Q_STOPCURRENTBUILD, Messages.MSG_C_STOPNMAVENBUILD, MessageBoxButtons.YesNo, MessageBoxIcon.Question);

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

                    NMavenPomHelperUtility parentPomUtil = new NMavenPomHelperUtility(parentPomFile);
                    NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(pomFile);

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
                        //MessageBox.Show("Parent pom.xml Not Found!!! ",
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
                    FileInfo pomFile = NMavenPomHelperUtility.FindPomFileUntil(
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
            executeBuildCommand(CurrentSelectedProjectPom, "org.apache.maven.dotnet.plugins:maven-test-plugin:test");
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
                NMavenBuildAllProjects("install");
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
                NMavenBuildAllProjects("clean");
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
                NMavenBuildAllProjects("compile");
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
                NMavenBuildAllProjects("test");
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
                NMavenBuildSelectedProject("install");
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
                NMavenBuildSelectedProject("clean");
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
                NMavenBuildSelectedProject("compile");
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
                NMavenBuildSelectedProject("test");
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
                DialogResult res = MessageBox.Show(Messages.MSG_Q_STOP_MAVEN_BUILD, Messages.MSG_C_STOPNMAVENBUILD, MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                // re-check if it is still running, before calling stop
                if (mavenRunner.IsRunning && res == DialogResult.Yes)
                {
                    mavenRunner.stop();
                }
            }
        }
        #endregion

        #region cbShowAddArtifactsForm_Click(CommandBarButton,bool)
        private void cbShowAddArtifactsForm_Click(CommandBarButton btn, ref bool Cancel)
        {
            //First selected project
            foreach (Project project in (Array)_applicationObject.ActiveSolutionProjects)
            {
                FileInfo currentPom = this.CurrentSelectedProjectPom;
                if (currentPom == null)
                {
                    DialogResult result = MessageBox.Show("Pom file not found, do you want to import the projects first before adding Maven Artifact?", "Add Maven Artifact", MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question);
                    if (result == DialogResult.Cancel)
                        return;
                    else if (result == DialogResult.Yes)
                    {
                        SaveAllDocuments();
                        NMavenImportProjectForm frm = new NMavenImportProjectForm(_applicationObject);
                        frm.ShowDialog();
                        currentPom = this.CurrentSelectedProjectPom;
                    }
                }
                AddArtifactsForm form = new AddArtifactsForm(project, container, logger, currentPom);
                form.Show();
                break;
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
                NMavenSignAssembly frm = new NMavenSignAssembly(project, container, logger, CurrentSelectedProjectPom);
                frm.ShowDialog();
                break;
            }


        }

        #endregion

        #region cbChangeProjectImportForm_Click(CommandBarButton, bool)
        private void cbChangeProjectImportForm_Click(CommandBarButton btn, ref bool Cancel)
        {
            SaveAllDocuments();
            NMavenImportProjectForm frm = new NMavenImportProjectForm(_applicationObject);
            frm.ShowDialog();
        }
        #endregion

        #region OnBeginShutdown(Array)
        /// <summary>Implements the OnBeginShutdown method of the IDTExtensibility2 interface. Receives notification that the host application is being unloaded.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnBeginShutdown(ref Array custom)
        {
            outputWindowPane.OutputString(Messages.MSG_L_SHUTTING_DOWN_NMAVEN);
            mavenRunner.Quit();
            outputWindowPane.OutputString(Messages.MSG_L_SUCCESFULLY_SHUTDOWN);
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
            outputWindowPane.OutputString(commandName);
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
        private NMaven.Logging.Logger logger;
        private List<CommandBarButton> addReferenceControls;
        private List<CommandBarButton> nunitControls;
        private List<CommandBarControl> buildControls;
        private EnvDTE.SelectionEvents _selectionEvents;
        private ArtifactContext container;
        private List<ReferencesEvents> referenceEvents;
        private List<VSLangProj80.VSLangProjWebReferencesEvents> webRefEvents;
        //private DirectoryInfo baseDirectoryInfo; 
        private MavenRunner mavenRunner;
        private bool _nmavenLaunched = false;

        List<WebServicesReferenceWatcher> wsRefWatcher = new List<WebServicesReferenceWatcher>();

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

        public bool RemovePomWebReferenceInfo(string webRefNamespace)
        {
            NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, CurrentSelectedProject);
            pomUtil.RemoveWebReference(webRefNamespace);
            return true;

        }

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
                    outputWindowPane.OutputString(string.Format("\nError updating {0}. [{1}]", wsInfo.Name, ex.Message));
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

    public interface IWebServicesRefUtils
    {
        bool ProjectHasWebReferences(Project project);
        bool RemovePomWebReferenceInfo(string webRefNamespace);
        bool AddPomWebReferenceInfo(IWebServiceRefInfo webref);
        List<IWebServiceRefInfo> GetWebReferences(Project project);
        void UpdateWebReferences(Project project);
        bool UpdateWSDLFile(IWebServiceRefInfo webRef);
        bool GenerateProxies(IWebServiceRefInfo webRef);
    }

    public interface IWebServiceRefInfo
    {
        string Name { get; set;}
        string WSDLUrl { get; set;}
        string OutputFile { get; set;}
        string WsdlFile { get; set;}
    }

    public class WebServiceRefInfo : IWebServiceRefInfo
    {
        public WebServiceRefInfo() { }
        public WebServiceRefInfo(string name, string wsdlUrl)
        {
            this.name = name;
            this.wsdlUrl = wsdlUrl;
        }

        #region IWebServiceRefInfo Members
        string name;
        public string Name
        {
            get
            {
                return name;
            }
            set
            {
                name = value;
            }
        }

        string wsdlUrl;
        public string WSDLUrl
        {
            get
            {
                return wsdlUrl;
            }
            set
            {
                wsdlUrl = value;
            }
        }

        string outputFile;
        public string OutputFile
        {
            get
            {
                return outputFile;
            }
            set
            {
                outputFile = value;
            }
        }

        string wsdlFile;
        public string WsdlFile
        {
            get
            {
                return wsdlFile;
            }
            set
            {
                wsdlFile = value;
            }
        }

        #endregion


    }
}