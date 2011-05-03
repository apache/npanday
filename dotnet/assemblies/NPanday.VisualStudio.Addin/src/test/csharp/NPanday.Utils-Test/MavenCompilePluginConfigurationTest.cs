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
using NUnit.Framework;
using NPanday.Utils;
using System.IO;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class MavenCompilePluginConfigurationTest
    {
        private PomHelperUtility pomCopy;
        private String pomPath;
        private String pomCopyPath;



        public MavenCompilePluginConfigurationTest()
        {

        }

        [SetUp]
        public void TestSetUp()
        {
            pomPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml").FullName);

            pomCopyPath = pomPath.Replace("pom.xml", "pomCopy.xml");

            pomCopy = new PomHelperUtility(pomCopyPath);

            File.Copy(pomPath, pomCopyPath);
        }
        
        [Test]
        public void AddMavenCompilePluginConfigurationTest()
        {
            pomCopy.AddMavenCompilePluginConfiguration("org.apache.npanday.plugins", "maven-compile-plugin", "includeSources", "includeSource", "IISHandler1.cs");
        }

        [Test]
        public void RenameMavenCompilePluginConfigurationTest()
        {
            pomCopy.RenameMavenCompilePluginConfiguration("org.apache.npanday.plugins", "maven-compile-plugin", "includeSources", "includeSource", "IISHandler1.cs","IISHandlerRenamed.cs");
        }

        [Test]
        public void RemoveMavenCompilePluginConfigurationTest()
        {
            pomCopy.RemoveMavenCompilePluginConfiguration("org.apache.npanday.plugins", "maven-compile-plugin", "includeSources", "includeSource", "IISHandler1.cs");
        }

        [TearDown]
        public void TestCleanUp()
        {
            File.Delete(pomCopyPath);
        }

    }
}
