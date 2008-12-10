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
using System.IO;
using System.Reflection;
using System.Xml;
using System.Xml.Serialization;

using NPanday.Plugin;

namespace NPanday.Plugin.Injector.Impl
{
	/// <summary>
	/// Provides methods for injecting maven project models values into fields.
	/// </summary>
	[FieldInjectorAttribute("NPanday.Model.Pom.Model")]
	public sealed class MavenProjectInjector : IFieldInjector 
	{
		public MavenProjectInjector()
		{
		}
		
		/// 
		/// <see cref ="NPanday.Plugin.Injector.IFieldInjector.Inject(object, FieldInfo, object)"> 
		/// 
		public void Inject(object targetObject, FieldInfo fieldInfo, object fieldObject)
		{
			fieldInfo.SetValue(targetObject, this.CreatePomModelFor( ((string) fieldObject) ));
		}
		
		/// 
		/// <see cref ="NPanday.Plugin.Injector.IFieldInjector.GetJavaClassName()"> 
		/// 		
		public string GetJavaClassName() 
		{
			return "org.apache.maven.project.MavenProject";				
		}  		
		
		/// <summary>
		/// Creates a model from the specified project file.
		/// </summary>
		/// <param name="fileName">The fully qualified file name of the project file</param>
		/// <returns>A model from the specified project file</returns>
   		private NPanday.Model.Pom.Model CreatePomModelFor(string fileName)
		{
			TextReader reader = new StreamReader(fileName);
		    XmlSerializer serializer = new XmlSerializer(typeof(NPanday.Model.Pom.Model));
			return (NPanday.Model.Pom.Model) serializer.Deserialize(reader);	
		}  
	}
}
