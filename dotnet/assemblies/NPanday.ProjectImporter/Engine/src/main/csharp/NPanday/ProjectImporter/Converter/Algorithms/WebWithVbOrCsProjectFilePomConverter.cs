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
using System.Text;
using System.Xml;
using System.IO;

using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;
using NPanday.Model.Pom;

using NPanday.Artifact;

using System.Reflection;

using NPanday.ProjectImporter.Converter;

using NPanday.ProjectImporter.Validator;


/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class WebWithVbOrCsProjectFilePomConverter : NormalPomConverter
    {
        public WebWithVbOrCsProjectFilePomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            // just call the base, but dont write it we still need some minor adjustments for it
            base.ConvertProjectToPomModel(false,scmTag);
            Model.packaging = "asp";

            // Write SCMTag
            if (scmTag != null && scmTag != string.Empty && Model.parent == null)
            {
                Scm scmHolder = new Scm();
                scmHolder.connection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.developerConnection = string.Format("scm:svn:{0}", scmTag);
                scmHolder.url = scmTag;

                Model.scm = scmHolder;
            }


            Model.build.sourceDirectory = ".";

            // change the outputDirectory of the plugin
            Plugin compilePlugin = FindPlugin("org.apache.npanday.plugins", "maven-compile-plugin");
            AddPluginConfiguration(compilePlugin, "outputDirectory", "bin");

            // Add NPanday compile plugin 
            Plugin aspxPlugin = AddPlugin("org.apache.npanday.plugins", "maven-aspx-plugin");
            if (!string.IsNullOrEmpty(projectDigest.TargetFramework))
                AddPluginConfiguration(aspxPlugin, "frameworkVersion", projectDigest.TargetFramework);
                
            // add msbuild plugin config in pom if there's a maven-resgen-plugin but no msbuild config 
            // generates resources in target/bin folder
            if (( FindPlugin("npanday.plugin", "maven-resgen-plugin") != null  && FindPlugin("npanday.plugin", "NPanday.Plugin.Msbuild.JavaBinding") == null) || ( FindPlugin("org.apache.npanday.plugins", "maven-resgen-plugin") != null  && FindPlugin("org.apache.npanday.plugins", "NPanday.Plugin.Msbuild.JavaBinding") == null)) 
            {
                Plugin msBuildPlugin = AddPlugin("org.apache.npanday.plugins", "NPanday.Plugin.Msbuild.JavaBinding");
                AddPluginExecution(msBuildPlugin, "compile", "validate");
                AddPluginConfiguration(msBuildPlugin, "frameworkVersion", ProjectDigest.TargetFramework);
            }    

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }


        }


    }
}
