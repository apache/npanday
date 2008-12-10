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
using System.Runtime.CompilerServices;
using EnvDTE;
using NPanday.Logging;

namespace NPanday.VisualStudio.Logging
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
