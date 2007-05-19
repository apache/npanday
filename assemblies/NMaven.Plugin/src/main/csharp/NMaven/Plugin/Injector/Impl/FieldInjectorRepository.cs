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
using System.Collections.Generic;
using System.Collections;

namespace NMaven.Plugin.Injector.Impl
{
	/// <summary>
	/// Provides methods for getting field injectors.
	/// </summary>
	internal sealed class FieldInjectorRepository
	{		
		internal FieldInjectorRepository()
		{
		}
		
		internal IFieldInjector getFieldInjectorFor(FieldInfo fieldInfo)
		{
			Type[] types = this.GetType().Assembly.GetTypes();
			foreach(Type type in types)
			{
				if(type.GetInterface("NMaven.Plugin.Injector.IFieldInjector") == null) 
				{
					continue;
				}
				
			    foreach (Attribute attribute in type.GetCustomAttributes(false))
		        {	 
			    	if(attribute is FieldInjectorAttribute)
			    	{
			    		FieldInjectorAttribute fieldInjectorAttribute = (FieldInjectorAttribute) attribute;	
			    		Console.WriteLine(fieldInfo.FieldType.FullName + ":" + fieldInjectorAttribute.TargetClassName);
			    		if(fieldInfo.FieldType.FullName.Equals(fieldInjectorAttribute.TargetClassName))
			    		{
			    			return (IFieldInjector) type.GetConstructor(System.Type.EmptyTypes).Invoke(null);
			    		}
			    	}
		        }  
			}
			return null;
		}
		

       internal String GetFieldTypeFor(FieldInfo fieldInfo)
       {
	    	foreach (Attribute attribute in fieldInfo.GetCustomAttributes(true))
	        {	            	
				FieldAttribute fieldAttribute = (FieldAttribute) attribute;
				return fieldAttribute.Type;
	        }  
	    	return null;
       }       
	}
}
