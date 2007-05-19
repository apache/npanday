using System;
using NMaven.Logging;

namespace NMaven.IDE
{
	/// <summary>
	/// Description of Factory.
	/// </summary>
	public static class Factory
	{
		
		public static IIdeConfiguration CreateIdeConfiguration()
		{
			return new IdeConfigurationImpl();
		}
		
		private class IdeConfigurationImpl : IIdeConfiguration
		{
			private Logger logger;
			
			private int socketLoggerPort;
			
			public Logger Logger
			{
				get
				{
					return logger;	
				}
				
				set
				{
					logger = value;	
				}
			}
			
			public int SocketLoggerPort
			{
				get
				{
					return socketLoggerPort;	
				}
				
				set
				{
					socketLoggerPort = value;	
				}
			}				
		}
	}
}
