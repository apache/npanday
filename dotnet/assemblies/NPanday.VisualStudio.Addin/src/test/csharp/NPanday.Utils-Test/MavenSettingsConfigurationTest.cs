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
using System.IO;
using System.Windows.Forms;
using NUnit.Framework;
using NPanday.VisualStudio.Addin;
using NPanday.Utils;
using NPanday.Model.Setting;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    class MavenSettingsConfigurationTest
    {
        private Settings settings;
        private string settingsPathOriginal;
        private string settingsPath;

        private AddArtifactsForm addArtifactsFrm;

        public MavenSettingsConfigurationTest()
        {

            settingsPathOriginal = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\m2\\test-settings.xml")).FullName;
            settingsPath = settingsPathOriginal.Replace("test-settings.xml", "test-settings2.xml");

            File.Copy(settingsPathOriginal, settingsPath);

            addArtifactsFrm = new AddArtifactsForm();
            addArtifactsFrm.addProfilesTag(settingsPath);

            settings = SettingsUtil.ReadSettings(settingsPath);
        }

        [Test]
        public void CheckIfSettingsXMLIsValidTest()
        {
            Assert.IsNotNull(settings.profiles);
            File.Delete(settingsPath);
        }
    }
}
