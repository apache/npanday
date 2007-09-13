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
using System.Collections.Generic;
using System.IO;
using System.Xml;
using System.Xml.Serialization;

using NMaven.Plugin;
using NMaven.Model.Pom;
using NMaven.Model;
using NMaven.Artifact;

namespace NMaven.Plugin.Addin
{
	/// <summary>
	/// Description
	/// </summary>
	[ClassAttribute(Phase = "package", Goal = "package")]
	public sealed class AutomationExtensibilityMojo : AbstractMojo
	{
		public AutomationExtensibilityMojo()
		{
		}

		[FieldAttribute("repository", Expression = "${settings.localRepository}", Type = "java.lang.String")]
		public String localRepository;

		[FieldAttribute("mavenProject", Expression = "${project}", Type = "org.apache.maven.project.MavenProject")]
		public NMaven.Model.Pom.Model mavenProject;


		public override Type GetMojoImplementationType()
		{
			return this.GetType();
		}

		public override void Execute()
        {
            ArtifactContext artifactContext = new ArtifactContext();
            FileInfo artifactFileInfo = PathUtil.GetPrivateApplicationBaseFileFor(artifactContext.GetArtifactFor(mavenProject), 
                new FileInfo(localRepository).Directory); 
            
            Console.WriteLine("Artifact Path = " + artifactFileInfo.FullName);

            object[] extensibilityItems = new object[2];
            //Host Application
            ExtensibilityHostApplication hostApplication = new ExtensibilityHostApplication();
            List<ItemsChoiceType> itemsChoiceTypes = new List<ItemsChoiceType>();
            List<String> itemsChoiceTypeValues = new List<string>();
            
            itemsChoiceTypes.Add(ItemsChoiceType.Name);
            itemsChoiceTypeValues.Add("Microsoft Visual Studio");

            itemsChoiceTypes.Add(ItemsChoiceType.Version);
            itemsChoiceTypeValues.Add("8.0");

            hostApplication.Items = itemsChoiceTypeValues.ToArray();
            hostApplication.ItemsElementName = itemsChoiceTypes.ToArray();
            extensibilityItems[0] = hostApplication;
        
            //Addin         
            ExtensibilityAddin addin = new ExtensibilityAddin();
            List<ItemsChoiceType1> itemNames = new List<ItemsChoiceType1>();
            List<string> itemValues = new List<string>();

            itemNames.Add(ItemsChoiceType1.Assembly);
            itemValues.Add(artifactFileInfo.FullName);

            itemNames.Add(ItemsChoiceType1.FullClassName);
            itemValues.Add(mavenProject.artifactId + ".Connect");

            itemNames.Add(ItemsChoiceType1.FriendlyName);
            itemValues.Add(mavenProject.name);

            itemNames.Add(ItemsChoiceType1.Description);
            itemValues.Add(mavenProject.description);

            itemNames.Add(ItemsChoiceType1.LoadBehavior);
            itemValues.Add("0");

            itemNames.Add(ItemsChoiceType1.CommandLineSafe);
            itemValues.Add("0");

            itemNames.Add(ItemsChoiceType1.CommandPreload);
            itemValues.Add("1");

            addin.Items = itemValues.ToArray();
            addin.ItemsElementName = itemNames.ToArray();
            extensibilityItems[1] = addin;

            Extensibility extensibility = new Extensibility();
            extensibility.Items = extensibilityItems;

            //write XML
            XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Extensibility));
            XmlTextWriter xmlWriter = new XmlTextWriter(Environment.GetEnvironmentVariable("TMP")
                  + @"\NMavenBuild.AddIn", System.Text.Encoding.Unicode);           
            xmlWriter.Formatting = Formatting.Indented;
            serializer.Serialize(xmlWriter, extensibility);
    	}
	}
}
