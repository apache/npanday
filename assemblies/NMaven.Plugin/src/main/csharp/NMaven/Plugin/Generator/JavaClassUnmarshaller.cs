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
using System.IO;
using System.Collections;
using System.Reflection;

using NMaven.Plugin;

namespace NMaven.Plugin.Generator
{
	/// <summary>
	/// Description of JavaClassUnmarshaller.
	/// </summary>
	public class JavaClassUnmarshaller : MarshalByRefObject 
	{
		public JavaClassUnmarshaller()
		{
		}
			
		public List<JavaClass> GetMojosFor(string assemblyName, string groupId)
		{
			List<JavaClass> javaClasses = new List<JavaClass>();
			Assembly[] assemblies = AppDomain.CurrentDomain.GetAssemblies();
			foreach(Assembly assembly in assemblies)
			{
				Console.WriteLine("Assembly :" + assembly.GetName().Name);
				if(assembly.GetName().Name.Equals(assemblyName))
				{
					Type[] types = assembly.GetTypes();
					foreach(Type type in types)
					{
						String baseName = type.BaseType.Name;
						if(baseName.Equals("AbstractMojo"))
						{
							JavaClass javaClass = convert(type, groupId);
							javaClasses.Add(javaClass);
						}
					}			
				}
			}
			return javaClasses;
		}
		
		public JavaClass convert(Type abstractMojoType, string groupId)
		{		
	        JavaClass javaClass = new JavaClass();
		 	javaClass.PackageName = abstractMojoType.Namespace;
		 	javaClass.ClassName = abstractMojoType.Name;
		 	javaClass.ExtendsClassName = "org.apache.maven.dotnet.plugin.AbstractMojo";
	 	    
		 	ImportPackage importPackage = new ImportPackage();
		 	javaClass.ImportPackage = importPackage.AddPackage("org.apache.maven.dotnet.plugin.FieldAnnotation");
		 	
		 	List<String> classComments = new List<String>();
			System.Attribute[] attributes =
				System.Attribute.GetCustomAttributes(abstractMojoType);
			
			foreach(Attribute attribute in attributes) 
			{
				if(attribute is ClassAttribute)
				{
					ClassAttribute mojo = (ClassAttribute) attribute;			
					classComments.Add(@"@phase " + mojo.Phase);	
					classComments.Add(@"@goal " + mojo.Goal);	
					break;
				}
			}
			
			javaClass.Comments = classComments;
		 	
		 	List<JavaField> javaFields = new List<JavaField>();
        	foreach(FieldInfo field in abstractMojoType.GetFields())
	        {
	            foreach (Attribute attribute in field.GetCustomAttributes(true))
	            {	            	
					FieldAttribute mojo = (FieldAttribute) attribute;
					javaFields.Add(CreateJavaField("public", mojo.Type, mojo.Name, 
					                               CreateMojoComment(mojo.Expression),
					                               "FieldAnnotation()"));
	            }
	        }
        	
        	//mojo parameters
        	javaFields.Add(CreateJavaField("private", "org.apache.maven.project.MavenProject", "project",  
        	                               CreateMojoComment("${project}"), null));        	
        	javaFields.Add(CreateJavaField("private", "String", "localRepository",  
        	                               CreateMojoComment("${settings.localRepository}"), null));
        	javaFields.Add(CreateJavaField("private", "String", "vendor",  
        	                               CreateMojoComment("${vendor}"), null));
        	javaFields.Add(CreateJavaField("private", "String", "vendorVersion",  
        	                               CreateMojoComment("${vendorVersion}"), null));
        	javaFields.Add(CreateJavaField("private", "String", "frameworkVersion", 
        	                               CreateMojoComment("${frameworkVersion}"), null));
        	
        	//components
        	List<String> comments = new List<String>();
        	comments.Add("@component");
        	javaFields.Add(CreateJavaField("private", "org.apache.maven.dotnet.executable.NetExecutableFactory", 
        	                               "netExecutableFactory", comments, null));
        	javaFields.Add(CreateJavaField("private", "org.apache.maven.dotnet.plugin.PluginContext", 
        	                               "pluginContext", comments, null)); 
        	
        	//methods
        	List<JavaMethod> javaMethods = new List<JavaMethod>();
        		
        	javaMethods.Add(CreateJavaMethod("public", "String", "getMojoArtifactId", 
        	                                 new Code().AddLine(@"return """
        	                                                    + abstractMojoType.Namespace + @""";")));
        	                       	
        	javaMethods.Add(CreateJavaMethod("public", "String", "getMojoGroupId", 
        	                                 new Code().AddLine(@"return """ 
        	                                                    + groupId + @""";")));

        	javaMethods.Add(CreateJavaMethod("public", "String", "getClassName", 
        	                                 new Code().AddLine(@"return """ + abstractMojoType.Namespace 
        	                                                    + "." + abstractMojoType.Name + @""";")));
        	                               
        	javaMethods.Add(CreateJavaMethod("public", "org.apache.maven.dotnet.plugin.PluginContext", 
        	                                 "getNetPluginContext",
        	                                 CreateCodeWithSimpleReturnType("pluginContext")));
        	
        	javaMethods.Add(CreateJavaMethod("public", "org.apache.maven.dotnet.executable.NetExecutableFactory", 
        	                                 "getNetExecutableFactory",
        	                                 CreateCodeWithSimpleReturnType("netExecutableFactory"))); 
        	                                
        	javaMethods.Add(CreateJavaMethod("public", "org.apache.maven.project.MavenProject", "getMavenProject", 
        	                                 CreateCodeWithSimpleReturnType("project"))); 
        	                                
        	javaMethods.Add(CreateJavaMethod("public", "String", "getLocalRepository", 
        	                                 CreateCodeWithSimpleReturnType("localRepository")));  
        	
        	javaMethods.Add(CreateJavaMethod("public", "String", "getVendorVersion", 
        	                                 CreateCodeWithSimpleReturnType("vendorVersion")));          	

        	javaMethods.Add(CreateJavaMethod("public", "String", "getVendor", 
        	                                 CreateCodeWithSimpleReturnType("vendor"))); 
        	
        	javaMethods.Add(CreateJavaMethod("public", "String", "getFrameworkVersion", 
        	                                 CreateCodeWithSimpleReturnType("frameworkVersion")));            	
        	javaClass.JavaMethods = javaMethods;
		 	javaClass.JavaFields = javaFields;
		 	return javaClass;
		}	
		
		private List<String> CreateMojoComment(string expression)
		{
		    List<String> comments = new List<String>();
		    comments.Add(@"@parameter expression = """ + expression + @"""");	
		    return comments;
		}
		
		private JavaField CreateJavaField(string access, string fieldType, string fieldName, 
		                                  List<String> comments, string annotation)
		{
		    JavaField javaField = new JavaField();
			javaField.Access = access;
			javaField.FieldName = fieldName;
			javaField.ReturnType = fieldType;
			javaField.Comments = comments;
			javaField.Annotation = annotation;
			return javaField;
		}
		
		private Code CreateCodeWithSimpleReturnType(String type)
		{
			Code code = new Code();
			return code.AddLine("return " + type + ";");
		}
		
		private JavaMethod CreateJavaMethod(string access, string returnType, string methodName, Code code)
		{
        	JavaMethod javaMethod = new JavaMethod();
        	javaMethod.MethodName = methodName;
        	javaMethod.ReturnType = returnType;
        	javaMethod.Access = access;
        	javaMethod.Code = code;
        	return javaMethod;
		}
			
		public void unmarshall(JavaClass javaClass, FileInfo fileInfo)
		{
			StreamWriter streamWriter = fileInfo.CreateText();
			streamWriter.WriteLine("package " + javaClass.PackageName + ";");
			streamWriter.WriteLine("");
			
			foreach(String importPackageName in javaClass.ImportPackage.Packages)
			{
				streamWriter.WriteLine("import " + importPackageName + ";");
				streamWriter.WriteLine("");
			}
			
			if(javaClass.Comments != null)
			{
				streamWriter.WriteLine("/**");
				foreach(String comment in javaClass.Comments)
				{
					streamWriter.WriteLine(" * " + comment);		
				}
				streamWriter.WriteLine(" */");				
			}
						   
			streamWriter.WriteLine("public class " + javaClass.ClassName);
			if(javaClass.ExtendsClassName != null)
			{
				streamWriter.WriteLine("    extends " + javaClass.ExtendsClassName);
			}
			streamWriter.WriteLine("{");
			
			if(javaClass.JavaFields != null)
			{
				foreach(JavaField javaField in javaClass.JavaFields)
				{
					List<String> comments = javaField.Comments;
					if(comments != null && comments.Count > 0)
					{
						streamWriter.WriteLine("       /**");
						foreach(String comment in comments)
						{
							streamWriter.WriteLine("        * " + comment);
						}	
						streamWriter.WriteLine("        */");						
					}
					if(javaField.Annotation != null)
					{
						streamWriter.WriteLine("        @" + javaField.Annotation);
					}
					
					streamWriter.WriteLine("        " + javaField.Access + " " + 
					                      javaField.ReturnType + " " + javaField.FieldName + ";");
					streamWriter.WriteLine("");
				}
			}
			
			if(javaClass.JavaMethods != null)
			{
				foreach(JavaMethod javaMethod in javaClass.JavaMethods)
				{
					List<String> comments = javaMethod.Comments;
					if(comments != null && comments.Count > 0)
					{
						streamWriter.WriteLine("       /**");
						foreach(String comment in comments)
						{
							streamWriter.WriteLine("      * " + comment);
						}	
						streamWriter.WriteLine("        */");						
					}
					streamWriter.WriteLine("        " + javaMethod.Access + " " + javaMethod.ReturnType 
					                       + " " + javaMethod.MethodName + "()");
					streamWriter.WriteLine("        {");
					foreach(String codeLine in javaMethod.Code.CodeLines)
					{
						streamWriter.WriteLine("            " + codeLine);
					}
					streamWriter.WriteLine("        }");
					streamWriter.WriteLine("");
					 
				}
			}
			
			streamWriter.WriteLine("}");			
			streamWriter.AutoFlush = true;
			streamWriter.Close();
			Console.WriteLine("File Exists = " + fileInfo.Exists);
		}

	
        private FieldInfo GetFieldInfoFor(Type type, String name)
        {
        	foreach(FieldInfo field in type.GetFields())
	        {
	            foreach (Attribute attribute in field.GetCustomAttributes(true))
	            {	            	
					FieldAttribute mojo = (FieldAttribute) attribute;
					if(mojo.Name.Equals(name))
						return field;
	            }
	        }
	        return null;
        }	
	}
}
