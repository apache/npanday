#region licence
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
#endregion

using System;
using System.Xml;
using NPanday.Utils;
using NUnit.Framework;
using NPanday.Model.Settings;
using System.IO;

namespace NPanday.Utils_Test
{
    [TestFixture]
    public class SettingsUtilTest
    {
        private string _repoUrl1 = "http://repo1.maven.org/maven2";
        private string _repoUrl2 = "http://repo.exist.com/maven2";
        private Settings _settings;

        private NameTable _nameTable;
        private XmlNamespaceManager _namespaceManager;

        public SettingsUtilTest()
        {
            _nameTable = new NameTable();
            _namespaceManager = new XmlNamespaceManager(_nameTable);

        }

        [Test]
        public void AddRepository_NoProfileTag__AddsRepoToModel()
        {
            _settings = ReadTestSettings("test-settings.xml");

            Profile profile = SettingsUtil.GetDefaultProfile(_settings, true);
            Assert.IsNotNull(profile);
            SettingsUtil.SetProfileRepository(profile, _repoUrl1, true, false);

            Assert.AreEqual(1, _settings.profiles.Length, "Settings does not contain a profile");

            Repository repository = SettingsUtil.GetRepositoryFromProfile(profile, _repoUrl1);

            Assert.IsNotNull(repository, "Repository '" + _repoUrl1 + "' was not added to profile");

            Assert.AreEqual("npanday.repo", repository.id);
            Assert.AreEqual(_repoUrl1, repository.url);
        }

        [Test]
        public void AddRepository_WithProfileTag_AddsRepoToModel()
        {
            _settings = ReadTestSettings("test-settings-with-profile.xml");

            Profile profile = SettingsUtil.GetDefaultProfile(_settings, false);
            Assert.IsNotNull(profile);

            SettingsUtil.SetProfileRepository(profile, _repoUrl2, true, false);

            Assert.AreEqual(1, _settings.profiles.Length, "Settings does not contain a profile");
            Assert.AreEqual(1, _settings.profiles[0].repositories.Length);

            Repository repository = SettingsUtil.GetRepositoryFromProfile(profile, _repoUrl1);
            Assert.IsNull(repository, "Repository '" + _repoUrl1 + "' was in the profile");

            repository = SettingsUtil.GetRepositoryFromProfile(profile, _repoUrl2);
            Assert.IsNotNull(repository, "Repository '" + _repoUrl2 + "' was not added to profile");
            Assert.AreEqual("npanday.repo", repository.id);
            Assert.AreEqual(_repoUrl2, repository.url);
        }

        [Test]
        public void AddRepository_WithProfileAndWithoutNs__AddsRepoToModel()
        {
            _settings = ReadTestSettings("test-settings-with-profile.xml");

            Profile profile = SettingsUtil.GetDefaultProfile(_settings, false);
            Assert.IsNotNull(profile);
        }

        [Test]
        public void AddRepository_WithProfileAndNs__AddsRepoToModel()
        {
            string fullPath;
            _settings = ReadTestSettings("test-settings-with-profile-and-namespace.xml", out fullPath);

            Profile profile = SettingsUtil.GetDefaultProfile(_settings, false);
            Assert.IsNotNull(profile);
        }

        [Test]
        public void ReadWrite_WithoutNSAndProfileTag_DoesNotChangeContent()
        {
            ReadWrite_LeavesContentUnchanged("test-settings.xml");
        }

        [Test]
        public void ReadWrite_WithNsWithoutProfileTag_DoesNotChangeContent()
        {
            ReadWrite_LeavesContentUnchanged("test-settings-with-namespace.xml");
        }

        [Test]
        public void ReadWrite_WithoutNsWithProfileTag_DoesNotChangeContent()
        {
            ReadWrite_LeavesContentUnchanged("test-settings-with-profile.xml");
        }

        [Test]
        public void ReadWrite_WithNsAndProfileTag_DoesNotChangeContent()
        {
            ReadWrite_LeavesContentUnchanged("test-settings-with-profile-and-namespace.xml");
        }

        private void ReadWrite_LeavesContentUnchanged(string testSettingsXml)
        {
            string fullPath;
            _settings = ReadTestSettings(testSettingsXml, out fullPath);

            Console.WriteLine("file:///" + new Uri(fullPath).AbsolutePath);

            Assert.IsNotNull(_settings);

            XmlDocument beforeXd = new XmlDocument();
            beforeXd.Load(fullPath);
            string before = beforeXd.OuterXml;

            SettingsUtil.MergeSettings(_settings, fullPath);

            XmlDocument afterXd = new XmlDocument();
            afterXd.Load(fullPath);
            string after = afterXd.OuterXml;

            Assert.AreEqual(before, after);
        }

        [Test]
        public void CreateDefaultProfile_WithoutNsAndProfileTag_AddsProfileTagsWithoutNamespace()
        {
            string fullPath;
            _settings = ReadTestSettings("test-settings.xml", out fullPath);

            Assert.IsNotNull(_settings);

            SettingsUtil.GetDefaultProfile(_settings, true);

            SettingsUtil.MergeSettings(_settings, fullPath);

            XmlDocument afterXd = new XmlDocument();
            afterXd.Load(fullPath);

            assertNodeExists(afterXd, "/settings/profiles");
            assertNodeExists(afterXd, "/settings/profiles/profile[id='" + SettingsUtil.defaultProfileID + "']");
            assertNodeExists(afterXd, "/settings/activeProfiles");
            assertNodeExists(afterXd, "/settings/activeProfiles/activeProfile[text()='" + SettingsUtil.defaultProfileID + "']");
        }

        [Test]
        public void CreateDefaultProfile_WithNsWithoutProfileTag_AddsProfileTagsWithNamespace()
        {
            string fullPath;
            _settings = ReadTestSettings("test-settings-with-namespace.xml", out fullPath);

            Assert.IsNotNull(_settings);

            SettingsUtil.GetDefaultProfile(_settings, true);

            SettingsUtil.MergeSettings(_settings, fullPath);


            _namespaceManager.AddNamespace("settings", SettingsUtil.MavenSettingsNamespace);

            XmlDocument afterXd = new XmlDocument(_nameTable);
            afterXd.Load(fullPath);

            assertNodeExists(afterXd, "/settings:settings");
            assertNodeExists(afterXd, "/settings:settings/settings:profiles");
            assertNodeExists(afterXd, "/settings:settings/settings:profiles/settings:profile[settings:id='" + SettingsUtil.defaultProfileID + "']");
            assertNodeExists(afterXd, "/settings:settings/settings:activeProfiles");
            assertNodeExists(afterXd, "/settings:settings/settings:activeProfiles/settings:activeProfile[text()='" + SettingsUtil.defaultProfileID + "']");
        }

        private void assertNodeExists(XmlDocument xml, string xpath)
        {
            assertNodesExists(xml, xpath, 1);
        }

        private void assertNodesExists(XmlDocument xml, string xpath, int count)
        {
            XmlNodeList nodes = xml.SelectNodes(xpath, _namespaceManager);
            if (nodes.Count == 0)
                Assert.Fail("Could not find a node matching " + xpath + " in " + xml.BaseURI);
            if (nodes.Count < count)
                Assert.Fail("Found less than " + count + " nodes matching " + xpath + " in " + xml.BaseURI);
            if (nodes.Count > count)
                Assert.Fail("Found more than " + count + " node(s) matching " + xpath + " in " + xml.BaseURI);
        }

        private Settings ReadTestSettings(string settingsXml)
        {
            string fullPath;
            return ReadTestSettings(settingsXml, out fullPath);
        }

        private Settings ReadTestSettings(string settingsXml, out string fullPath)
        {
            DirectoryInfo projectRoot = PathUtility.FindProjectRoot(new DirectoryInfo(Directory.GetCurrentDirectory()));

            FileInfo originalPath = getSettingsPath(projectRoot, settingsXml);
            string dir = Path.Combine(PathUtility.GetBuildDirectory(projectRoot).FullName, "test-workbench");
            Directory.CreateDirectory(dir);
            fullPath = Path.Combine(dir, settingsXml);

            originalPath.CopyTo(fullPath, true);
            return SettingsUtil.ReadSettings(originalPath);
        }

        private FileInfo getSettingsPath(DirectoryInfo projectRoot, string settingsXml)
        {
            string path = Path.Combine(projectRoot.FullName, "src");
            path = Path.Combine(path, "test");
            path = Path.Combine(path, "resource");
            path = Path.Combine(path, "m2");
            return new FileInfo(Path.Combine(path, settingsXml));
        }
    }
}
