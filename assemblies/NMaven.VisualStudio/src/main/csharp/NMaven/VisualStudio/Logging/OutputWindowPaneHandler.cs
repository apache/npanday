using System;
using System.Runtime.CompilerServices;
using EnvDTE;
using NMaven.Logging;

namespace NMaven.VisualStudio.Logging
{
	/// <summary>
	/// Description of OutputWindowPaneHandler.
	/// </summary>
	public class OutputWindowPaneHandler : IHandler
	{
		private Level level;
		
		private OutputWindowPane outputWindowPane;
				
		public OutputWindowPaneHandler()
		{
			this.level = Level.INFO;
		}
		
		public void SetOutputWindowPaneHandler(OutputWindowPane outputWindowPane)
		{
			this.outputWindowPane = outputWindowPane;	
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void publish(LogRecord record)
		{
			if(record.GetLevel().GetValue() >= level.GetValue())
			{
				outputWindowPane.OutputString(record.GetMessage().Trim() + Environment.NewLine);
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
