using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;

namespace NMaven.Logging
{
	public class Logger 
	{
		private List<IHandler> handlers;
		
		private String name;
		
	    private static LogManager logManager = new LogManager();
	    
		private Logger(String name)
		{
			handlers = new List<IHandler>();
			this.name = name;
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public static Logger GetLogger(String name)
		{
			Logger logger = logManager.GetLogger(name);
			if(logger == null)
			{
				logger = new Logger(name);
				logManager.addLogger(logger);
			}
			return logger;
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void Log(Level level, String msg)
		{
			LogRecord logRecord = new LogRecord(level, msg);
			if(handlers.Count == 0)
			{
				handlers.Add(new ConsoleHandler());
			}
			foreach(IHandler handler in handlers)
			{				
				handler.publish(logRecord);
			}
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void AddHandler(IHandler handler) 
		{
			handlers.Add(handler);
		}
		
		public String getName()
		{
			return name;			
		}
	}
}
