using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;

namespace NMaven.Logging
{
	public class LogManager
	{
		
		private List<Logger> loggers;
		
		public LogManager()
		{
			loggers = new List<Logger>();
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public Logger GetLogger(String name)
		{
			foreach(Logger logger in loggers)
			{
				if(logger.getName().Equals(name))
					return logger;
			}
			return null;
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void addLogger(Logger logger)
		{
			loggers.Add(logger);
		}
	}
}
