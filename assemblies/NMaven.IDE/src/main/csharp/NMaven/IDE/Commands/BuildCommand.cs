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
using System.Web.Services.Protocols;

using NMaven.IDE;
using NMaven.Logging;
using NMaven.Service;

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
            try
            {
                ideContext.Build(request);
            }
            catch (SoapException e)
            {
                ideContext.GetLogger().Log(Level.INFO, "NMaven: Error in build: " + e.Code + ", " + e.SubCode
                    + "," + e.StackTrace);
            }
            catch (Exception e)
            {
                ideContext.GetLogger().Log(Level.INFO, "NMaven: Error in build: " + e.Message);
            }
        	
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
