using System;
using NMaven.Service;
using NMaven.IDE;

namespace NMaven.IDE.Commands
{
	/// <summary>
	/// Description of BuildCommand
	/// </summary>
	public class BuildCommand
	{
		private string goal;
		
		private string pomFile;
		
		private int loggerPort;
		
		private IIdeContext ideContext;
		
		public BuildCommand()
		{
		}
				
		public void Init(IIdeContext ideContext)
		{
			this.ideContext = ideContext;
		}
		
        public void Execute(object sender, EventArgs args)
        {
        	MavenExecutionRequest request = new MavenExecutionRequest();
        	request.goal = this.Goal;
        	request.pomFile = this.PomFile;
        	request.loggerPort = loggerPort;
        	request.loggerPortSpecified = true;
        	ideContext.Build(request);
        }	
        
			public int LoggerPort
			{
				get
				{
					return loggerPort;	
				}
				
				set
				{
					loggerPort = value;	
				}
			}	
			
			public String Goal
			{
				get
				{
					return goal;	
				}
				
				set
				{
					goal = value;	
				}
			}		
			
		
			public String PomFile
			{
				get
				{
					return pomFile;	
				}
				
				set
				{
					pomFile = value;	
				}
			}				
	}
}
