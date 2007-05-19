using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Windows.Forms;
using NMaven.IDE;
using NMaven.IDE.Impl;
using NMaven.Service;
using NMaven.IDE.Commands;
using NMaven.Logging;

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

        private TreeView treeView = new TreeView();
        
        public event EventHandler ClearOutputWindow;
        
        public event EventHandler FocusOutputWindow;        
             
		public MavenBuildControl()
		{		
		}

		public void Init(DirectoryInfo buildDirectory, Logger logger, int loggerPort, Size treeSize)
		{
			this.loggerPort = loggerPort;
			
	        treeView.ClientSize = treeSize;	        
			ideContext = new IdeContextImpl();
			IIdeConfiguration configuration = Factory.CreateIdeConfiguration();
			configuration.Logger = logger;
			configuration.SocketLoggerPort = loggerPort;
			ideContext.Init(configuration);
			List<MavenProject> mavenProjects = ideContext.GetMavenProjectsFrom(buildDirectory);
			foreach(MavenProject mavenProject in mavenProjects)
			{
				treeView.Nodes.Add(CreateTreeNodeFor(mavenProject));
			}
					            
            treeView.MouseClick += new MouseEventHandler(this.treeView_MouseUp);
            Controls.Add(treeView);		
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
                if (node == null) return;
                MavenProject mavenProject = (MavenProject) node.Tag;
                contextmenu.MenuItems.Clear();              
                contextmenu.MenuItems.Add(CreateMenuItemFor("Compile Project", "compile", mavenProject.pomPath));
                contextmenu.MenuItems.Add(CreateMenuItemFor("Clean Project", "clean", mavenProject.pomPath));
                contextmenu.MenuItems.Add(CreateMenuItemFor("Test Project", "test", mavenProject.pomPath));
                contextmenu.MenuItems.Add(CreateMenuItemFor("Install Project", "install", mavenProject.pomPath));
                
                contextmenu.Show(this, PointToClient(treeView.PointToScreen(point)));
            }
        }  		
	}
}
