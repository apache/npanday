using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Windows.Forms;

using NMaven.Artifact;
using NMaven.IDE;
using NMaven.IDE.Impl;
using NMaven.Service;
using NMaven.IDE.Commands;
using NMaven.Logging;

using EnvDTE80;
using EnvDTE;

namespace NMaven.IDE.Controls
{
	/// <summary>
	/// Description of MavenBuildControl.
	/// </summary>
	public class MavenBuildControl : UserControl
	{
		private IIdeContext ideContext;

		private int loggerPort;

		private ContextMenu contextmenu = new ContextMenu();

        private TreeView treeView;

        private Logger logger;

        private ToolStrip toolStrip1;
        private ToolStripDropDownButton toolStripDropDownButton1;
        private ToolStripMenuItem serverToolStripMenuItem;
        private ToolStripMenuItem startToolStripMenuItem;
        private ToolStripMenuItem stopToolStripMenuItem;
        private ToolStripMenuItem refreshSolutionToolStripMenuItem;

        private DTE2 applicationObject;

        public event EventHandler ClearOutputWindow;

        public event EventHandler FocusOutputWindow;

		public MavenBuildControl()
		{
		}

		public void Init(Logger logger, int loggerPort, Size treeSize,
            DTE2 applicationObject)
		{
			this.loggerPort = loggerPort;
            this.logger = logger;
            this.applicationObject = applicationObject;

			ideContext = new IdeContextImpl();
			IIdeConfiguration configuration = Factory.CreateIdeConfiguration();
			configuration.Logger = logger;
			configuration.SocketLoggerPort = loggerPort;
			ideContext.Init(configuration);
            try
            {
                InitializeComponent();
            }
            catch (Exception e)
            {
                logger.Log(Level.INFO, "Failed to initialize NMaven Build Control: Message = " + e.Message);
            }
		}

		private TreeNode CreateTreeNodeFor(MavenProject mavenProject)
		{
			TreeNode rootNode = new TreeNode();
            rootNode.Text = mavenProject.artifactId;

            if(mavenProject.mavenProjects != null)
            {
	            foreach(MavenProject childMavenProject in mavenProject.mavenProjects)
	            {
	            	TreeNode childNode = CreateTreeNodeFor(childMavenProject);
	            	rootNode.Nodes.Add(childNode);
	            }
            }
            rootNode.Tag = mavenProject;
            return rootNode;
		}

		private MenuItem CreateMenuItemFor(String text, String goal, String pomFile)
		{
                MenuItem menuItem = new MenuItem();
                menuItem.Text = text;
                BuildCommand buildCommand = new BuildCommand();
                buildCommand.Init(ideContext);
                buildCommand.Goal = goal;
                buildCommand.PomFile = pomFile;
                buildCommand.LoggerPort = loggerPort;
                menuItem.Click += new EventHandler(OnFocusOutputWindow);
                menuItem.Click += new EventHandler(OnClearOutputWindow);
				menuItem.Click += new EventHandler(buildCommand.Execute);
				return menuItem;
		}

		private void OnClearOutputWindow(object sender, EventArgs args)
		{
			if(ClearOutputWindow != null)
			{
				ClearOutputWindow(this, args);
			}
		}

		private void OnFocusOutputWindow(object sender, EventArgs args)
		{
			if(FocusOutputWindow != null)
			{
				FocusOutputWindow(this, args);
			}
		}

        private void treeView_MouseUp(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Right)
            {
                Point point = new Point(e.X, e.Y);
                TreeNode node = treeView.GetNodeAt(point);
                if (node == null)
                {
                    logger.Log(Level.INFO, "Unable to obtain reference to project - build options disabled: Coordinates: X = "
                        + e.X + ", Y = " + e.Y);
                    return;
                }

                if (node.Tag == null)
                {
                    logger.Log(Level.INFO, "Please open and load a solution file project.");
                    return;
                }

                MavenProject mavenProject = (MavenProject) node.Tag;

                contextmenu.MenuItems.Clear();
                contextmenu.MenuItems.Add(CreateMenuItemFor("Compile Project", "compile", mavenProject.pomPath));
                contextmenu.MenuItems.Add(CreateMenuItemFor("Clean Project", "clean", mavenProject.pomPath));
                contextmenu.MenuItems.Add(CreateMenuItemFor("Test Project", "test", mavenProject.pomPath));
                contextmenu.MenuItems.Add(CreateMenuItemFor("Install Project", "install", mavenProject.pomPath));

                contextmenu.Show(this, PointToClient(treeView.PointToScreen(point)));
            }
        }

        private void InitializeComponent()
        {
            System.Windows.Forms.TreeNode treeNode1 = new System.Windows.Forms.TreeNode("No Solution Loaded");
            this.toolStrip1 = new System.Windows.Forms.ToolStrip();
            this.toolStripDropDownButton1 = new System.Windows.Forms.ToolStripDropDownButton();
            this.serverToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.startToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.stopToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.refreshSolutionToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.treeView = new System.Windows.Forms.TreeView();
            this.toolStrip1.SuspendLayout();
            this.SuspendLayout();
            //
            // toolStrip1
            //
            this.toolStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripDropDownButton1});
            this.toolStrip1.Location = new System.Drawing.Point(0, 0);
            this.toolStrip1.Name = "toolStrip1";
            this.toolStrip1.Size = new System.Drawing.Size(380, 25);
            this.toolStrip1.TabIndex = 1;
            this.toolStrip1.Text = "toolStrip1";
            //
            // toolStripDropDownButton1
            //
            this.toolStripDropDownButton1.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Text;
            this.toolStripDropDownButton1.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.serverToolStripMenuItem,
            this.refreshSolutionToolStripMenuItem});
            this.toolStripDropDownButton1.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStripDropDownButton1.Name = "toolStripDropDownButton1";
            this.toolStripDropDownButton1.Size = new System.Drawing.Size(70, 22);
            this.toolStripDropDownButton1.Text = "Options";
            //
            // serverToolStripMenuItem
            //
            this.serverToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.startToolStripMenuItem,
            this.stopToolStripMenuItem});
            this.serverToolStripMenuItem.Name = "serverToolStripMenuItem";
            this.serverToolStripMenuItem.Size = new System.Drawing.Size(195, 22);
            this.serverToolStripMenuItem.Text = "Server";
            //
            // startToolStripMenuItem
            //
            this.startToolStripMenuItem.Name = "startToolStripMenuItem";
            this.startToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.startToolStripMenuItem.Text = "Start";
            this.startToolStripMenuItem.Click += new System.EventHandler(this.startToolStripMenuItem_Click);
            //
            // stopToolStripMenuItem
            //
            this.stopToolStripMenuItem.Name = "stopToolStripMenuItem";
            this.stopToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.stopToolStripMenuItem.Text = "Stop";
            this.stopToolStripMenuItem.Click += new System.EventHandler(this.stopToolStripMenuItem_Click);
            //
            // refreshSolutionToolStripMenuItem
            //
            this.refreshSolutionToolStripMenuItem.Name = "refreshSolutionToolStripMenuItem";
            this.refreshSolutionToolStripMenuItem.Size = new System.Drawing.Size(195, 22);
            this.refreshSolutionToolStripMenuItem.Text = "Load Solution";
            this.refreshSolutionToolStripMenuItem.Click += new System.EventHandler(this.refreshSolutionToolStripMenuItem_Click);
            //
            // treeView
            //
            this.treeView.Location = new System.Drawing.Point(4, 29);
            this.treeView.Name = "treeView";
            treeNode1.Name = "";
            treeNode1.Text = "No Solution Loaded";
            this.treeView.Nodes.AddRange(new System.Windows.Forms.TreeNode[] {
            treeNode1});
            this.treeView.Size = new System.Drawing.Size(373, 169);
            this.treeView.TabIndex = 2;
            this.treeView.MouseClick += new System.Windows.Forms.MouseEventHandler(this.treeView_MouseUp);
            //
            // MavenBuildControl
            //
            this.Controls.Add(this.treeView);
            this.Controls.Add(this.toolStrip1);
            this.Name = "MavenBuildControl";
            this.Size = new System.Drawing.Size(380, 201);
            this.toolStrip1.ResumeLayout(false);
            this.toolStrip1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        private void toolStripButton1_Click(object sender, EventArgs e)
        {
            if (Controls.Contains(treeView))
            {
                treeView.Nodes.Clear();
            }
            logger.Log(Level.INFO, "Solution " + applicationObject.Solution.FullName);

            FileInfo fileInfo = new FileInfo(applicationObject.Solution.FullName);
            List<MavenProject> mavenProjects = ideContext.GetMavenProjectsFrom(fileInfo.Directory);
            foreach (MavenProject mavenProject in mavenProjects)
            {
                logger.Log(Level.INFO, mavenProject.artifactId);
                treeView.Nodes.Add(CreateTreeNodeFor(mavenProject));
            }
        }

        private void stopToolStripMenuItem_Click(object sender, EventArgs e)
        {
            WebClient webClient = new WebClient();
            try
            {
                webClient.DownloadData("http://localhost:8080?shutdown=true");
            }
            catch (WebException ex)
            {

            }
            logger.Log(Level.INFO, "Shutdown Maven embedder");
        }

        private void startToolStripMenuItem_Click(object sender, EventArgs e)
        {
            WebClient webClient = new WebClient();
            byte[] data = null;
            try
            {
                data = webClient.DownloadData("http://localhost:8080/dotnet-service-embedder");
            }
            catch (WebException ex)
            {
                logger.Log(Level.INFO, "Unable to contact maven embedder. Starting new instance: Message = " + ex.Message);
            }
            if (data != null && data.Length > 0)
            {
                logger.Log(Level.INFO, "Maven embedder already Started.");
                return;
            }
            String localRepository = Environment.GetEnvironmentVariable("HOMEDRIVE")
              + Environment.GetEnvironmentVariable("HOMEPATH") + @"\.m2\repository\";
            ArtifactContext artifactContext = new ArtifactContext();
            NMaven.Artifact.Artifact artifactWar = artifactContext.CreateArtifact("org.apache.maven.dotnet", "dotnet-service-embedder", "0.14-SNAPSHOT", "war");
            FileInfo warFileInfo = new FileInfo(localRepository + "/" + new JavaRepositoryLayout().pathOf(artifactWar) + "war");
            logger.Log(Level.INFO, "Executing external command plugin: Command = " + @"mvn org.apache.maven.dotnet.plugins:maven-embedder-plugin:start -Dport=8080 -DwarFile=""" + warFileInfo.FullName + @"""");

            ProcessStartInfo processStartInfo =
                new ProcessStartInfo("mvn", @"org.apache.maven.dotnet.plugins:maven-embedder-plugin:start -Dport=8080 -DwarFile=""" + warFileInfo.FullName + @"""");
            processStartInfo.UseShellExecute = true;
            processStartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            System.Diagnostics.Process.Start(processStartInfo);
        }

        private void refreshSolutionToolStripMenuItem_Click(object sender, EventArgs e)
        {
            logger.Log(Level.INFO, "Solution = " + applicationObject.Solution);
            if (applicationObject.Solution == null)
            {
                logger.Log(Level.INFO, "Please open a solution file project before loading.");
                return;
            }

            if (Controls.Contains(treeView))
            {
                treeView.Nodes.Clear();
            }

            logger.Log(Level.INFO, "Loading Solution: Name = " + applicationObject.Solution.FullName);
            FileInfo fileInfo = null;
            try 
            {
                fileInfo = new FileInfo(applicationObject.Solution.FullName);
            }
            catch(ArgumentException ex)
            {
                logger.Log(Level.INFO, "Invalid Solution");
                return;
            }
            
            if (!fileInfo.Exists)
            {
                logger.Log(Level.INFO, "Solution not found: Name = " + applicationObject.Solution.FullName);
                return;
            }

            List<MavenProject> mavenProjects = null;
            try
            {
                mavenProjects = ideContext.GetMavenProjectsFrom(fileInfo.Directory);
            }
            catch (IOException ex)
            {
                logger.Log(Level.INFO, "Unable to load solution. Try starting the server: Message = "
                    + ex.Message);
                return;
            }

            foreach (MavenProject mavenProject in mavenProjects)
            {
                treeView.Nodes.Add(CreateTreeNodeFor(mavenProject));
            }
        }
	}
}
