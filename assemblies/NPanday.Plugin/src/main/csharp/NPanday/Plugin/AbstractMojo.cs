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

using NPanday.Plugin.Injector;
using NPanday.Plugin.Injector.Impl;

namespace NPanday.Plugin
{
	/// <summary>
	/// Base class for Mojos.
	/// </summary>
	public abstract class AbstractMojo : MarshalByRefObject 
    {
        public abstract void Execute();
        
        public abstract Type GetMojoImplementationType();
               
        /// <summary>
        /// Injects values into fields of the child class using information from the specified 
        /// configuration file. 
        /// </summary>
        /// <param name="configurationFile">The file that contains the values used to populate the fields</param>
        public void InjectFields(String configurationFile)
        {
			XmlTextReader reader = 
				new XmlTextReader(@configurationFile);
			FieldInjectorRepository fieldInjectorRepository = new FieldInjectorRepository();
			
			while(reader.Read() == true)
			{	
				FieldInfo fieldInfo = GetFieldInfoFor(this.GetMojoImplementationType(), reader.Name);				
				if(fieldInfo != null) 
				{
					IFieldInjector fieldInjector = fieldInjectorRepository.getFieldInjectorFor(fieldInfo);
					fieldInjector.Inject(this, fieldInfo, reader.ReadString() );									
				}					
			}        	
        }
        
       // public List<JavaClass> CreateJavaClassesForPlugin()
        
        private FieldInfo GetFieldInfoFor(Type type, String name)
        {
        	foreach(FieldInfo field in type.GetFields())
	        {
	            foreach (Attribute attribute in field.GetCustomAttributes(true))
	            {	            	
					FieldAttribute fieldAttribute = (FieldAttribute) attribute;
					if(fieldAttribute.Name.Equals(name))
						return field;
	            }
	        }
	        return null;
        }
    }
}
