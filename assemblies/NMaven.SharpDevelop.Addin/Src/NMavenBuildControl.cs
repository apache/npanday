using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Drawing;
using System.Net;
using System.Net.Sockets;

using NMaven.IDE.View;
using NMaven.IDE;
using NMaven.IDE.Commands;
using NMaven.IDE.Controls;
using NMaven.IDE.Impl;
using NMaven.Service;
using NMaven.Logging;

using ICSharpCode.SharpDevelop.Gui;
using ICSharpCode.SharpDevelop.Gui.XmlForms;
using ICSharpCode.SharpDevelop;

namespace NMaven.SharpDevelop.Addin
{
	public class NMavenBuildControl : BaseSharpDevelopUserControl
	{
       
		private CompilerMessageView view;
		
		private MessageViewCategory category;
		
		public NMavenBuildControl()
		{
			category = new MessageViewCategory("NMaven Build");
			view = CompilerMessageView.Instance;
			view.AddCategory(category);
    		
			SetupFromXmlStream(this.GetType().Assembly.GetManifestResourceStream("NMaven.SharpDevelop.Addin.Resources.NMavenBuildControl.xfrm"));
			
			CompilerMessageViewHandler handler = new CompilerMessageViewHandler();
			handler.SetCompilerMessageView(view);
			Logger logger = Logger.GetLogger("NMaven Build");
			logger.AddHandler(handler);
			logger.Log(Level.INFO, "NMaven Build\r\n");
			MavenBuildControl buildControl = new MavenBuildControl();            
			buildControl.Init(logger, findOpenPort(), new Size(400, 400) );
			buildControl.Size = new Size(400, 400);
			buildControl.ClearOutputWindow += new EventHandler(ClearOutputWindow);
			this.Controls.Add(buildControl);
		}
		
		void ClearOutputWindow(object sender, EventArgs e)
		{
			category.ClearText();
		}
		
		private int findOpenPort()
		{
			for(int i = 1; i < 10; i++)
			{
				int port = (new Random()).Next(1025, 65536);
				try {
		            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
		            socket.Bind(new IPEndPoint(IPAddress.Any, port));
		            socket.Close();
		            return port;
				}
				catch (SocketException e)
				{					
				}								
			}
			return -1;
		}
	}		
}
