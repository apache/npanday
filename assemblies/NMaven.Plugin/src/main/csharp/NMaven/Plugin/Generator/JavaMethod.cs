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

namespace NMaven.Plugin.Generator
{
	/// <summary>
	/// Description of JavaMethod.
	/// </summary>
	[Serializable]
	public class JavaMethod
	{
		private string methodName;
		
		private string access;
		
		private string returnType;
		
		private Code code;
		
		private List<String> comments;

		public Code Code
		{
			get 
			{
				return code;
			}
			
			set
			{
				this.code = value;				
			}
		}
		
		public List<String> Comments
		{
			get {
				return comments;
			}
			
			set
			{
				this.comments = value;
			}
		}
		
		public string MethodName
		{
			get {
				return methodName;
			}
			
			set
			{
				this.methodName = value;
			}
		}
		
		public string Access
		{
			get {
				return access;
			}
			
			set
			{
				this.access = value;
			}
		}	
		
		public string ReturnType
		{
			get {
				return returnType;
			}
			
			set
			{
				this.returnType = value;
			}
		}					
		
	}
}
