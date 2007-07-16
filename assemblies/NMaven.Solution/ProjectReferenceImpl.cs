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

using System;
using System.IO;

namespace NMaven.Solution.Impl
{
	/// <summary>
	/// Description of ProjectReferenceImpl.
	/// </summary>
	internal sealed class ProjectReferenceImpl : IProjectReference
	{
		
		private FileInfo csProjFile;
		
		private string projectName;
		
		private Guid projectGuid;
		
		internal ProjectReferenceImpl()
		{
		}
		
		public FileInfo CSProjectFile
		{
			get
			{
				return csProjFile;	
			}
			
			set
			{
				csProjFile = value;	
			}
		}
		
		public string ProjectName
		{
			get
			{
				return projectName;
			}
			
			set
			{
				projectName = value;
			}
		}
		
		public Guid ProjectGuid
		{
			get
			{
				return projectGuid;
			}
			
			set
			{
				projectGuid = value;
			}
		}
	}
}