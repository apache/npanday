using System;
using NMaven.Logging;

namespace NMaven.IDE
{
	/// <summary>
	/// Description of IIdeConfiguration.
	/// </summary>
	public interface IIdeConfiguration
	{
		int SocketLoggerPort
		{
			get;
			set;
		}	
		
		Logger Logger
		{
			get;
			set;
		}			
	}
}
