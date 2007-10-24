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
			mavenBuildControl.Init(null, logger, 9099, size, null);
			this.Controls.Add(mavenBuildControl);	
			//MavenDependencyUserControl mpuc = new MavenDependencyUserControl();
			//this.Controls.Add(mpuc);
		}
	}
}
