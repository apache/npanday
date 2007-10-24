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
