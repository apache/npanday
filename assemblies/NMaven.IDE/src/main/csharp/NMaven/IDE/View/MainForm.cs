
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using NMaven.IDE;
using NMaven.IDE.Impl;
using NMaven.Service;
using NMaven.IDE.Commands;
using NMaven.IDE.Controls;
using NMaven.Logging;

namespace NMaven.IDE.View {
	/// <summary>
	/// Description of MainForm.
	/// </summary>
	public partial class MainForm
	{
			
		[STAThread]
		public static void Main(string[] args)
		{
			Application.EnableVisualStyles();
			Application.SetCompatibleTextRenderingDefault(false);
			MainForm mainForm = new MainForm();
			Size size = new Size(400, 400);
			
			mainForm.Init(Logger.GetLogger("IDE"), size);
			Application.Run(mainForm);
		}
				
		public MainForm()
		{
		}
		
		public void Init(Logger logger, Size size)
		{
			InitializeComponent();
			MavenBuildControl mavenBuildControl = new MavenBuildControl();
			mavenBuildControl.Size = size;
			mavenBuildControl.Init(null, logger, 9099, size);
			this.Controls.Add(mavenBuildControl);	
			//MavenDependencyUserControl mpuc = new MavenDependencyUserControl();
			//this.Controls.Add(mpuc);
		}
	}
}
