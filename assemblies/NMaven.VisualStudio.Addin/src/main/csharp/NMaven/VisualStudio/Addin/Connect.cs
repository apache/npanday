using Extensibility;
using EnvDTE;
using EnvDTE80;

using System;
using System.IO;
using System.Resources;
using System.Reflection;
using System.Globalization;
using System.Drawing;

using Microsoft.VisualStudio.CommandBars;

using NMaven.Artifact;
using NMaven.IDE;
using NMaven.IDE.Controls;
using NMaven.IDE.Commands;
using NMaven.IDE.Impl;
using NMaven.IDE.View;
using NMaven.Logging;
using NMaven.VisualStudio.Logging;
using System.Diagnostics;

namespace NMaven.VisualStudio.Addin
{
    /// <summary>The object for implementing an Add-in.</summary>
    /// <seealso class='IDTExtensibility2' />
    public class Connect : IDTExtensibility2, IDTCommandTarget
    {
        private Window _windowToolWindow;

        private OutputWindowPane outputWindowPane;

        private NMaven.Logging.Logger logger;

        /// <summary>Implements the constructor for the Add-in object. Place your initialization code within this method.</summary>
        public Connect()
        {

        }

        /// <summary>Implements the OnConnection method of the IDTExtensibility2 interface. Receives notification that the Add-in is being loaded.</summary>
        /// <param term='application'>Root object of the host application.</param>
        /// <param term='connectMode'>Describes how the Add-in is being loaded.</param>
        /// <param term='addInInst'>Object representing this Add-in.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnConnection(object application, ext_ConnectMode connectMode, object addInInst, ref Array custom)
        {
            _applicationObject = (DTE2)application;
            _addInInstance = (AddIn)addInInst;
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
                    Command command = commands.AddNamedCommand2(_addInInstance, "NMavenAddin", "NMavenAddin", "Executes the command for NMavenAddin", true, 59, ref contextGUIDS, (int)vsCommandStatus.vsCommandStatusSupported + (int)vsCommandStatus.vsCommandStatusEnabled, (int)vsCommandStyle.vsCommandStylePictAndText, vsCommandControlType.vsCommandControlTypeButton);

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

                String localRepository = Environment.GetEnvironmentVariable("HOMEDRIVE")
                    + Environment.GetEnvironmentVariable("HOMEPATH") + @"\.m2\repository\";

                ArtifactContext artifactContext = new ArtifactContext();
                NMaven.Artifact.Artifact artifactWar = artifactContext.CreateArtifact("org.apache.maven.dotnet", "dotnet-service-embedder", "0.14-SNAPSHOT", "war");
                FileInfo warFileInfo = new FileInfo(localRepository + "/" + new JavaRepositoryLayout().pathOf(artifactWar) + "war");
  
			    ProcessStartInfo processStartInfo =
                    new ProcessStartInfo("mvn", @"org.apache.maven.dotnet.plugins:maven-embedder-plugin:start -Dport=8080 -DwarFile=""" + warFileInfo.FullName + @"""");
			    processStartInfo.UseShellExecute = true;
                System.Diagnostics.Process.Start(processStartInfo);        
            }
            else if (connectMode == ext_ConnectMode.ext_cm_AfterStartup)
            {
                Window win = _applicationObject.Windows.Item(EnvDTE.Constants.vsWindowKindOutput);
                OutputWindow outputWindow = (OutputWindow)win.Object;
                outputWindowPane = outputWindow.OutputWindowPanes.Add("NMaven Addin Test");

                OutputWindowPaneHandler handler = new OutputWindowPaneHandler();
                handler.SetOutputWindowPaneHandler(outputWindowPane);

                logger = NMaven.Logging.Logger.GetLogger("UC");
                logger.AddHandler(handler);
                logger.Log(Level.INFO, "NMaven Addin Test");
                //logger.Log(Level.INFO, );
                String localRepository = Environment.GetEnvironmentVariable("HOMEDRIVE")
                    + Environment.GetEnvironmentVariable("HOMEPATH") + @"\.m2\repository\";
                ArtifactContext artifactContext = new ArtifactContext();
                NMaven.Artifact.Artifact artifactWar = artifactContext.CreateArtifact("org.apache.maven.dotnet", "dotnet-service-embedder", "0.14-SNAPSHOT", "war");
                FileInfo warFileInfo = new FileInfo(localRepository + "/" + new JavaRepositoryLayout().pathOf(artifactWar) + "war");
                logger.Log(Level.INFO, @"org.apache.maven.dotnet.plugins:maven-embedder-plugin:start -Dport=8080 -DwarFile=""" + warFileInfo.FullName + @"""");
                MavenBuildControl mavenBuildControl = new MavenBuildControl();
                object programmableObject = null;

                String guidstr = "{858C3FCD-8B39-4540-A592-F31C1520B174}";
                EnvDTE80.Windows2 windows2 = (EnvDTE80.Windows2)_applicationObject.Windows;
                System.Reflection.Assembly asm = System.Reflection.Assembly.GetAssembly(mavenBuildControl.GetType());
                _windowToolWindow = windows2.CreateToolWindow2(_addInInstance, asm.Location, "NMaven.IDE.Controls.MavenBuildControl", "Maven Build Tool", guidstr, ref programmableObject);

                _windowToolWindow.Visible = true;
                _applicationObject = (DTE2)application;
                FileInfo fileInfo = new FileInfo(_applicationObject.Solution.FullName);               
                mavenBuildControl = (MavenBuildControl)_windowToolWindow.Object;
                mavenBuildControl.Init(fileInfo.Directory, logger, 9099, new Size(400, 400));
                mavenBuildControl.ClearOutputWindow += new EventHandler(ClearOutputWindowPane);
                mavenBuildControl.FocusOutputWindow += new EventHandler(ActivateOutputWindowPane);
            }
        }

        private void ClearOutputWindowPane(object sender, EventArgs args)
        {
            outputWindowPane.Clear();
        }

        private void ActivateOutputWindowPane(object sender, EventArgs args)
        {
            outputWindowPane.Activate();
        }

        /// <summary>Implements the OnDisconnection method of the IDTExtensibility2 interface. Receives notification that the Add-in is being unloaded.</summary>
        /// <param term='disconnectMode'>Describes how the Add-in is being unloaded.</param>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnDisconnection(ext_DisconnectMode disconnectMode, ref Array custom)
        {
        }

        /// <summary>Implements the OnAddInsUpdate method of the IDTExtensibility2 interface. Receives notification when the collection of Add-ins has changed.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnAddInsUpdate(ref Array custom)
        {
        }

        /// <summary>Implements the OnStartupComplete method of the IDTExtensibility2 interface. Receives notification that the host application has completed loading.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnStartupComplete(ref Array custom)
        {
        }

        /// <summary>Implements the OnBeginShutdown method of the IDTExtensibility2 interface. Receives notification that the host application is being unloaded.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnBeginShutdown(ref Array custom)
        {
        }

        /// <summary>Implements the QueryStatus method of the IDTCommandTarget interface. This is called when the command's availability is updated</summary>
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

        /// <summary>Implements the Exec method of the IDTCommandTarget interface. This is called when the command is invoked.</summary>
        /// <param term='commandName'>The name of the command to execute.</param>
        /// <param term='executeOption'>Describes how the command should be run.</param>
        /// <param term='varIn'>Parameters passed from the caller to the command handler.</param>
        /// <param term='varOut'>Parameters passed from the command handler to the caller.</param>
        /// <param term='handled'>Informs the caller if the command was handled or not.</param>
        /// <seealso class='Exec' />
        public void Exec(string commandName, vsCommandExecOption executeOption, ref object varIn, ref object varOut, ref bool handled)
        {
            handled = false;
            if (executeOption == vsCommandExecOption.vsCommandExecOptionDoDefault)
            {
                ////		if(commandName == "IDEAddin.Connect.IDEAddin")
                //{
                handled = true;

            }
            _windowToolWindow.Visible = true;
        }
        private DTE2 _applicationObject;
        private AddIn _addInInstance;
    }
}