using System;
using System.Runtime.CompilerServices;
using System.Windows.Forms;
using System.Drawing;
using System.Threading;
using NMaven.Logging;
using ICSharpCode.Core;
using ICSharpCode.SharpDevelop.Gui;
using ICSharpCode.SharpDevelop;

namespace NMaven.SharpDevelop.Addin
{
	/// <summary>
	/// Description of CompilerMessageViewLogger.
	/// </summary>
	public class CompilerMessageViewHandler : IHandler
	{
		private Level level;
		
		private CompilerMessageView compilerMessageView;
				
		public CompilerMessageViewHandler()
		{
			this.level = Level.INFO;
		}
		
		public void SetCompilerMessageView(CompilerMessageView compilerMessageView)
		{
			this.compilerMessageView = compilerMessageView;	
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void publish(LogRecord record)
		{
			if(record.GetLevel().GetValue() >= level.GetValue())
			{
				compilerMessageView.GetCategory("NMaven Build")
					.AppendText(record.GetMessage());
			}
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void SetLevel(Level level)
		{
			this.level = level;
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public Level GetLevel()
		{
			return level;
		}		
	}
}
