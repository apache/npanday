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
using System.Reflection;

namespace NPanday.Plugin.Injector
{
	/// <summary>
	/// Provides services for injecting information into fields.
	/// </summary>
	public interface IFieldInjector
	{
		/// <summary>
		/// Injects the specified field object into the field info of the specified target object.
		/// </summary>
		/// <param name="targetObject">the target object containing the fields that are to be injected</param>
		/// <param name="fieldInfo">the field to inject</param>
		/// <param name="fieldObject">the value to set on the field</param>
		void Inject(object targetObject, FieldInfo fieldInfo, object fieldObject);
		
		/// <summary>
		/// Returns the java class name of field within the Java Mojo equivalent.
		/// </summary>
		/// <returns>the java class name of field within the Java Mojo equivalent</returns>
		string GetJavaClassName();
	}
}
