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
 
#endregion
  
  namespace NMaven.VisualStudio.Addin
  {
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
              throw new Exception("The method or operation is not implemented.");
          }

          public object EndInvoke(IAsyncResult result)
          {
              throw new Exception("The method or operation is not implemented.");
          }

          public object Invoke(Delegate method, object[] args)
          {
              throw new Exception("The method or operation is not implemented.");
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
     public class Connect : IDTExtensibility2, IDTCommandTarget
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

         static bool projectRefEventLoaded;

        #region OnConnection(object,ext_ConnectMode,object,Array)
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
                        "NMaven Build System", "Executes the command for NMavenAddin", true, 480, ref contextGUIDS,
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

         void attachReferenceEvent()
         {
             //References
             referenceEvents = new List<ReferencesEvents>();
             foreach (Project project in _applicationObject.Solution.Projects)
             {
                 projectRefEventLoaded = true;
                 VSProject vsProject = null;
                 try
                 {
                     vsProject = (VSProject)project.Object;
                 }
                 catch
                 {
                     //  not a csproj / vbproj file. Could be a solution folder. skip it.
                     continue;
                 }
                 referenceEvents.Add(vsProject.Events.ReferencesEvents);
                 vsProject.Events.ReferencesEvents.ReferenceRemoved
                     += new _dispReferencesEvents_ReferenceRemovedEventHandler(ReferencesEvents_ReferenceRemoved);

             }
         }

        

         private void launchNMavenBuildSystem()
         {
                // just to be safe, check if nmaven is already launched
                if(_nmavenLaunched)
                {
                  outputWindowPane.OutputString("\nNMaven Addin Has Already Started...");
                  return;
                }
                

                
                Window win = _applicationObject.Windows.Item(EnvDTE.Constants.vsWindowKindOutput);
                OutputWindow outputWindow = (OutputWindow)win.Object;
                outputWindowPane = outputWindow.OutputWindowPanes.Add("NMaven Build System");

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
                        if (control.Caption.Equals("Add &Reference..."))
                        {
                            CommandBarButton ctl = (CommandBarButton)
                                commandBar.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, control.Index, true);
                            ctl.Click += new _CommandBarButtonEvents_ClickEventHandler(cbShowAddArtifactsForm_Click);
                            ctl.Caption = "Add Maven Artifact...";
                            ctl.Visible = true;
                            addReferenceControls.Add(ctl);
              							
              				CommandBarButton ctl1 = (CommandBarButton)
                                commandBar.Controls.Add(MsoControlType.msoControlButton, 
                                System.Type.Missing, System.Type.Missing, control.Index, true);
                            ctl1.Click += 
                                new _CommandBarButtonEvents_ClickEventHandler(cbShowConfigureRepositoryForm_Click);
                            ctl1.Caption = "Configure Maven Repository...";
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
                            ctlSettingsXml.Caption = "Change Maven settings.xml...";
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
                            ctlSignAssembly.Caption = "Set NMaven Compile Sign Assembly Key...";
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
                            ctlProjectImport.Caption = "NMaven: Import Project";
                            ctlProjectImport.Visible = true;
                            addReferenceControls.Add(ctlProjectImport);

                            
                            
                        }
                        // included build web site to support web site projects
                        else if ((control.Caption.Equals("Clea&n")) || (control.Caption.Equals("Publis&h Selection")) || (control.Caption.Equals("Publis&h Web Site")))
                        {
                            // Add the stop maven build button here

                            CommandBarButton stopButton = (CommandBarButton)commandBar.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, control.Index + 1, true);
                            stopButton.Caption = "Stop Maven Build...";
                            stopButton.Visible = true;
                            stopButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbStopMavenBuild_Click);
                            buildControls.Add(stopButton);




                            CommandBarPopup ctl = (CommandBarPopup)
                                commandBar.Controls.Add(MsoControlType.msoControlPopup,
                                System.Type.Missing, System.Type.Missing, control.Index + 1, true);
                            ctl.Caption = "Maven Phase";
                            ctl.Visible = true;
                            buildControls.Add(ctl);



                            CommandBarButton cleanAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            cleanAllButton.Caption = "All Project: Clean";
                            cleanAllButton.Visible = true;
                            cleanAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbCleanAll_Click);


                            CommandBarButton testAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            testAllButton.Caption = "All Project: Test";
                            testAllButton.Visible = true;
                            testAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbTestAll_Click);

                            CommandBarButton installAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            installAllButton.Caption = "All Project: Install";
                            installAllButton.Visible = true;
                            installAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbInstallAll_Click);

                            CommandBarButton buildAllButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            buildAllButton.Caption = "All Project: Build [compile]";
                            buildAllButton.Visible = true;
                            buildAllButton.FaceId = 645;
                            buildAllButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbBuildAll_Click);

                            

                            CommandBarButton cleanButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            cleanButton.Caption = "Current Project: Clean";
                            cleanButton.Visible = true;
                            cleanButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbClean_Click);



                            CommandBarButton testButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            testButton.Caption = "Current Project: Test";
                            testButton.Visible = true;
                            testButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbTest_Click);

                            CommandBarButton installButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            installButton.Caption = "Current Project: Install";
                            installButton.Visible = true;
                            installButton.Click += new _CommandBarButtonEvents_ClickEventHandler(cbInstall_Click);

                            CommandBarButton buildButton = (CommandBarButton)ctl.Controls.Add(MsoControlType.msoControlButton,
                                System.Type.Missing, System.Type.Missing, 1, true);
                            buildButton.Caption = "Current Project: Build [compile]";
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
                outputWindowPane.OutputString("\nNMaven Addin Successful Started...");
        }
        #endregion

         void ReferencesEvents_ReferenceRemoved(Reference pReference)
         {
             try
             {
                 NMavenPomHelperUtility pomUtil = new NMavenPomHelperUtility(_applicationObject.Solution, pReference.ContainingProject);
                 pomUtil.RemovePomDependency(pReference.Name);
             }
             catch (Exception e)
             {
                 MessageBox.Show(e.Message, "NMaven Remove Dependency Error:");
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
                                    nunitControl.Caption = "Run Unit Test";
                                    nunitControl.Visible = true;
                                    CommandBarButton nunitCompileAndRunControl = (CommandBarButton)
                                        commandBar.Controls.Add(MsoControlType.msoControlButton,
                                        System.Type.Missing, System.Type.Missing, control.Index, true);
                                    nunitCompileAndRunControl.Click
                                        += new _CommandBarButtonEvents_ClickEventHandler(cbCompileAndRunUnitTest_Click);
                                    nunitCompileAndRunControl.Caption = "Compile and Run Unit Test";
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
            //remove maven menus
            DTE2 dte2 = _applicationObject;
            
            addReferenceControls = new List<CommandBarButton>();
            buildControls = new List<CommandBarControl>();
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

                vsProject.Events.ReferencesEvents.ReferenceRemoved
                        -= new _dispReferencesEvents_ReferenceRemovedEventHandler(ReferencesEvents_ReferenceRemoved);
                    
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

        private void NMavenBuildSelectedProject(String goal)
        {
            FileInfo pomFile = CurrentSelectedProjectPom;
            Project project = CurrentSelectedProject;
            NMavenPomHelperUtility pomUtility = new NMavenPomHelperUtility(pomFile);

            string errStr = null;

            if ("pom".Equals(pomUtility.Packaging, StringComparison.OrdinalIgnoreCase))
            {
                errStr = string.Format("Not A Project Pom Error: {0} is not a project Pom, the pom is a parent pom type.", pomFile);
            }
            else if(!pomUtility.ArtifactId.Equals(project.Name, StringComparison.OrdinalIgnoreCase))
            {
                errStr = string.Format("The Pom may not be the project's Pom: Project Name: {0} is not equal to Pom artifactId: {1}", project.Name, pomUtility.ArtifactId);
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
            executeBuildCommand(pomFile, goal);
        }



        private void NMavenBuildAllProjects(String goal)
        {
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
                    DialogResult res = MessageBox.Show("A Maven Build is currently running, Do you want to stop the build and proceed to a new Build Execution?", "Stop NMaven Build:", MessageBoxButtons.YesNo, MessageBoxIcon.Question);

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

                MessageBox.Show("Maven Execution Error: " + err.Message, 
                    "Execution Error:",
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
                     else if(pomFile.Exists || parentPomFile.Exists)
                     {
                         if(!"pom".Equals(pomUtil.Packaging) 
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
                         throw new Exception("Parent pom.xml Not Found");
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

                 FileInfo pomFile = NMavenPomHelperUtility.FindPomFileUntil(
                     new FileInfo(CurrentSelectedProject.FullName).Directory, 
                     new FileInfo( _applicationObject.Solution.FileName).Directory);

                 if (pomFile != null)
                 {
                     return pomFile;
                 }

                 return null;
             
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
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show("Maven Execution Error: " + e.Message,"Error",MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show("Maven Execution Error: " + e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                 DialogResult res = MessageBox.Show("Do you want to stop the Maven Build?", "Stop NMaven Build:", MessageBoxButtons.YesNo, MessageBoxIcon.Question);

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
                AddArtifactsForm form = new AddArtifactsForm(project, container, logger, CurrentSelectedProjectPom);
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
            outputWindowPane.OutputString("\nShutting Down NMaven Visual Studio Addin...");
            mavenRunner.Quit();
            outputWindowPane.OutputString("\nNMaven Successfully Shutdown...");
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
        //private DirectoryInfo baseDirectoryInfo; 
        private MavenRunner mavenRunner;
        private bool _nmavenLaunched = false;
        #endregion
    }
    #endregion
}