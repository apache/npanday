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
	/// Description of JavaField.
	/// </summary>
	[Serializable]
	public class JavaField
	{
		private string fieldName;
		
		private string fieldValue;
		
		private string access;
		
		private string returnType;
		
		private List<String> comments;

		private string annotation;
				
		public string Annotation
		{
			get 
			{
				return annotation;
			}
			
			set
			{
				this.annotation = value;
			}
		}
		
		public List<String> Comments
		{
			get 
			{
				return comments;
			}
			
			set
			{
				this.comments = value;
			}
		}
		
		public string FieldName
		{
			get 
			{
				return fieldName;
			}
			
			set
			{
				this.fieldName = value;
			}
		}
		
		public string FieldValue
		{
			get 
			{
				return fieldValue;
			}
			
			set
			{
				this.fieldValue = value;
			}
		}	
		
		public string Access
		{
			get 
			{
				return access;
			}
			
			set
			{
				this.access = value;
			}
		}	
		
		public string ReturnType
		{
			get 
			{
				return returnType;
			}
			
			set
			{
				this.returnType = value;
			}
		}			
	}
}
