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

#region Using

using System;
using System.Collections.Generic;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using NPanday.Model.Settings;

#endregion

namespace NPanday.Utils
{
    #region SettingsUtil

    public static class SettingsUtil
    {
        public static string MavenSettingsNamespace = "http://maven.apache.org/SETTINGS/1.0.0";
        public static string defaultProfileID = "NPanday.id";

        #region GetDefaultSettingsPath()
        /// <summary>
        /// Gets the default settings path.
        /// </summary>
        /// <returns>
        /// The default settings path.
        /// </returns>
        public static string GetDefaultSettingsPath()
        {
            string m2Dir = Environment.GetEnvironmentVariable("M2_HOME");

            if ((!string.IsNullOrEmpty(m2Dir)) && Directory.Exists(m2Dir))
            {
                string confDir = m2Dir + @"\conf";

                if (Directory.Exists(confDir))
                {
                    string settingsPath = confDir + @"\settings.xml";

                    if (File.Exists(settingsPath))
                    {
                        return settingsPath;
                    }
                }
            }

            return null;
        }
        #endregion

        #region GetUserSettingsPath()
        /// <summary>
        /// Gets the user settings path.
        /// </summary>
        /// <returns>
        /// The user settings path.
        /// </returns>
        public static string GetUserSettingsPath()
        {
            string profileDir = Environment.GetEnvironmentVariable("USERPROFILE");

            if ((!string.IsNullOrEmpty(profileDir)) && Directory.Exists(profileDir))
            {
                string m2Dir = profileDir + @"\.m2";

                if (Directory.Exists(m2Dir))
                {
                    string settingsPath = m2Dir + @"\settings.xml";

                    if (File.Exists(settingsPath))
                    {
                        return settingsPath;
                    }
                }
            }

            return null;
        }
        #endregion

        #region GetLocalRepositoryPath()
        /// <summary>
        /// Gets the local repository path.
        /// </summary>
        /// <returns>
        /// The local repository path
        /// </returns>
        public static string GetLocalRepositoryPath()
        {
            string path = null;
            string userSettingsPath = GetUserSettingsPath();

            if (!string.IsNullOrEmpty(userSettingsPath))
            {
                try
                {
                    Settings settings = ReadSettings(userSettingsPath);

                    if (settings != null)
                    {
                        path = settings.localRepository;
                    }
                }
                catch
                {
                }

                if (!string.IsNullOrEmpty(path))
                {
                    return path;
                }
            }

            string defaultSettingsPath = GetDefaultSettingsPath();

            if (!string.IsNullOrEmpty(defaultSettingsPath))
            {
                try
                {
                    Settings settings = ReadSettings(defaultSettingsPath);

                    if (settings != null)
                    {
                        path = settings.localRepository;
                    }
                }
                catch
                {
                }

                if (!string.IsNullOrEmpty(path))
                {
                    return path;
                }
            }

            string profileDir = Environment.GetEnvironmentVariable("USERPROFILE");

            return profileDir + @"\.m2\repository";
        }
        #endregion

        public static Settings ReadSettings(string path)
        {
            if (path == null)
            {
                throw new ArgumentNullException("path");
            }

            path = path.Trim();

            if (path.Length == 0)
            {
                throw new ArgumentException("Value is empty", "path");
            }

            return ReadSettings(new FileInfo(path));
        }

        #region ReadSettings(FileInfo)
        /// <summary>
        /// Reads the settings.
        /// </summary>
        /// <param name="fileInfo">The file info.</param>
        /// <returns>
        /// The settings read from the file
        /// </returns>
        public static Settings ReadSettings(FileInfo fileInfo)
        {
            if (fileInfo == null)
            {
                throw new ArgumentNullException("fileInfo");
            }

            TextReader reader = null;
            try
            {
                reader = new System.IO.StreamReader(fileInfo.FullName);
                XmlSerializer serializer = new XmlSerializer(typeof(Settings));

                return (Settings)serializer.Deserialize(new ConstantNamespaceXmlTextReader(reader, "", MavenSettingsNamespace));
            }
            finally
            {
                if (reader != null)
                {
                    reader.Close();
                }
            }
        }
        #endregion

        #region MergeSettings(Settings,string)
        /// <summary>
        /// Merges the settings in <paramref name="settings"/> with the current ones saved to <paramref name="path"/>.
        /// </summary>
        public static void MergeSettings(Settings settings, string path)
        {
            // read original settings.xml
            XmlDocument settingsXmlDoc = new XmlDocument();
            settingsXmlDoc.Load(path);

            Profile profile = GetProfile(settings, defaultProfileID, false);

            // TODO: current code does not support removal of the npanday profile!
            if (profile != null)
            {
                //convert NPanday Profile to XmlNode
                XmlDocument newProfileDocument = new XmlDocument();
                newProfileDocument.LoadXml(SerializeProfileToXml(profile));
                XmlElement newProfileNode = newProfileDocument.DocumentElement;
                XmlElement importedNewProfileNode = importWithDocumentNamespace(newProfileNode, settingsXmlDoc);


                // search for npanday profile in settings.xml
                XmlNode oldProfileNode =
                    settingsXmlDoc.SelectSingleNode("(//*[local-name()='profiles']/*[local-name()='profile'])[*[local-name()='id']='" + defaultProfileID + "']");
                if (oldProfileNode != null)
                {
                    oldProfileNode.RemoveAll();

                    while (importedNewProfileNode.HasChildNodes)
                    {
                        oldProfileNode.AppendChild(importedNewProfileNode.FirstChild);
                    }
                }
                else
                {
                    XmlNode profilesNode = settingsXmlDoc.SelectSingleNode("//*[local-name='profiles']");
                    if (profilesNode == null)
                    {
                        // create profiles
                        profilesNode = createElementWithDocumentNamespace("profiles", settingsXmlDoc);
                        settingsXmlDoc.DocumentElement.AppendChild(profilesNode);
                    }

                    profilesNode.AppendChild(importedNewProfileNode);
                }

                bool createActiveProfile = false;
                XmlNode activeProfileNode;

                // search for activeProfiles in settings.xml
                XmlNode activeProfilesNode = settingsXmlDoc.SelectSingleNode("//*[local-name() = 'activeProfiles']");
                if (activeProfilesNode == null)
                {
                    activeProfilesNode = createElementWithDocumentNamespace("activeProfiles", settingsXmlDoc);
                    settingsXmlDoc.DocumentElement.AppendChild(activeProfilesNode);
                    createActiveProfile = true;
                }
                else
                {
                    activeProfileNode =
                        settingsXmlDoc.SelectSingleNode("(//*[local-name() = 'activeProfiles']/*[local-name() = 'activeProfile'])[text()='" + defaultProfileID + "']");
                    if (activeProfileNode == null)
                    {
                        createActiveProfile = true;
                    }
                }

                // add NPanday.id to <activeProfiles>
                if (createActiveProfile)
                {
                    activeProfileNode = createElementWithDocumentNamespace("activeProfile", settingsXmlDoc);
                    activeProfileNode.InnerText = defaultProfileID;
                    activeProfilesNode.AppendChild(activeProfileNode);
                }
            }

            settingsXmlDoc.Save(path);
        }

        private static XmlElement importWithDocumentNamespace(XmlElement newProfileNode, XmlDocument settingsXmlDoc)
        {
            Console.Write(newProfileNode.OuterXml);


            if (settingsXmlDoc.DocumentElement.NamespaceURI != newProfileNode.NamespaceURI)
            {
                return (XmlElement)settingsXmlDoc.ReadNode(new ConstantNamespaceXmlTextReader(new StringReader(newProfileNode.OuterXml), newProfileNode.NamespaceURI, settingsXmlDoc.DocumentElement.NamespaceURI));
            }

            return (XmlElement)settingsXmlDoc.ImportNode(newProfileNode, true);
        }

        private static XmlElement createElementWithDocumentNamespace(string localName, XmlDocument settingsXmlDoc)
        {
            return settingsXmlDoc.CreateElement("", localName, settingsXmlDoc.DocumentElement.NamespaceURI);
        }

        #endregion


        private static string SerializeProfileToXml(Profile profile)
        {
            if (profile == null) throw new ArgumentNullException("profile");

            XmlRootAttribute profileRoot = new XmlRootAttribute("profile");
            profileRoot.Namespace = MavenSettingsNamespace;

            XmlAttributes attributes = new XmlAttributes();
            attributes.XmlRoot = profileRoot;

            XmlAttributeOverrides overrides = new XmlAttributeOverrides();
            overrides.Add(profile.GetType(), attributes);

            XmlSerializerNamespaces xmlnsEmpty = new XmlSerializerNamespaces();
            xmlnsEmpty.Add("", MavenSettingsNamespace);

            XmlSerializer xs = new XmlSerializer(profile.GetType(), overrides);
            StringWriter xout = new StringWriter();
            xs.Serialize(xout, profile, xmlnsEmpty);
            String SerializedProfile = xout.ToString();
            xout.Close();

            return SerializedProfile;
        }

        #region AddActiveProfile(Settings, string)
        /// <summary>
        /// Add a profile as active
        /// </summary>
        /// <param name="settings">The settings</param>
        /// <param name="profileId">The profile id</param>
        public static void AddActiveProfile(Settings settings, string profileId)
        {
            bool isActive = false;
            if (settings.activeProfiles == null)
            {
                settings.activeProfiles = new string[] { profileId };
                isActive = true;
            }
            else
            {
                foreach (string id in settings.activeProfiles)
                {
                    if (id == profileId)
                    {
                        isActive = true;
                        break;
                    }
                }
            }

            if (!isActive)
            {
                string[] newActiveProfiles = new string[settings.activeProfiles.Length + 1];
                settings.activeProfiles.CopyTo(newActiveProfiles, 0);
                newActiveProfiles[settings.activeProfiles.Length] = profileId;
                settings.activeProfiles = newActiveProfiles;
            }
        }
        #endregion

        #region GetProfile(Settings, string)

        /// <summary>
        /// Get profile
        /// </summary>
        /// <param name="settings">The settings info</param>
        /// <param name="profileId">The profile id</param>
        /// <param name="create"></param>
        /// <returns>The profile from the settings</returns>
        public static Profile GetProfile(Settings settings, string profileId, bool create)
        {
            if (settings.profiles != null)
            {
                foreach (Profile profile in settings.profiles)
                {
                    if (profileId.Equals(profile.id))
                    {
                        return profile;
                    }
                }
            }

            if (create)
            {
                Profile profile = new Profile();
                profile.id = profileId;
                if (settings.profiles == null)
                {
                    settings.profiles = new Profile[] { profile };
                }
                else
                {
                    List<Profile> profiles = new List<Profile>();
                    profiles.AddRange(settings.profiles);
                    profiles.Add(profile);
                    settings.profiles = profiles.ToArray();
                }
                return profile;
            }

            return null;
        }
        #endregion

        #region GetAllRepositories()
        /// <summary>
        /// Get all repositories from the settings.xml
        /// </summary>
        /// <param name="settings">The settings info</param>
        /// <returns>
        /// All repositories from the settings
        /// </returns>
        public static List<Repository> GetAllRepositories(Settings settings)
        {
            List<Repository> repos = new List<Repository>();

            foreach (Profile profile in settings.profiles)
            {
                if (profile.repositories != null)
                {
                    repos.AddRange(profile.repositories);
                }
            }
            return repos;
        }
        #endregion

        #region GetRepositoryByUrl(Settings, string)
        /// <summary>
        /// Get repository from settings by url
        /// </summary>
        /// <param name="settings">The settings</param>
        /// <param name="url">The repository url</param>
        /// <returns>The repository from the settings.xml</returns>
        public static Repository GetRepositoryByUrl(Settings settings, string url)
        {
            if (settings.profiles != null)
            {
                foreach (Profile profile in settings.profiles)
                {
                    if (profile.repositories != null)
                    {
                        foreach (Repository repo in profile.repositories)
                        {
                            if (url.Equals(repo.url))
                            {
                                return repo;
                            }
                        }
                    }
                }
            }
            return null;
        }
        #endregion

        #region GetRepositoryFromProfile(Profile, string)
        /// <summary>
        /// Gets the repository object from the profile
        /// </summary>
        /// <param name="profile">The profile</param>
        /// <param name="url">The repository url</param>
        /// <returns>
        /// The repository object from the profile
        /// </returns>
        public static Repository GetRepositoryFromProfile(Profile profile, string url)
        {
            if (profile.repositories != null)
            {
                foreach (Repository repo in profile.repositories)
                {
                    if (url.Equals(repo.url))
                    {
                        return repo;
                    }
                }
            }
            return null;
        }
        #endregion

        #region AddRepositoryToProfile(Profile, string, bool, bool, Settings)
        /// <summary>
        /// Add repository to profile
        /// </summary>
        /// <param name="profile">The profile</param>
        /// <param name="url">The repository url to be added</param>
        /// <param name="isReleaseEnabled">True if release is enabled</param>
        /// <param name="isSnapshotEnabled">True if snapshot is enabled</param>
        /// <param name="settings">The settings</param>
        /// <returns>The repository</returns>
        public static Repository AddRepositoryToProfile(Profile profile, string url, bool isReleaseEnabled, bool isSnapshotEnabled)
        {
            Repository repository = GetRepositoryFromProfile(profile, url);

            if (repository == null)
            {
                repository = new Repository();
                repository.url = url;
                UpdateRepository(profile, repository, isReleaseEnabled, isSnapshotEnabled);

                // add repository to profile
                if (profile.repositories == null)
                {
                    profile.repositories = new Repository[] { repository };
                }
                else
                {
                    List<Repository> repositories = new List<Repository>();
                    repositories.AddRange(profile.repositories);
                    repositories.Insert(0, repository);
                    profile.repositories = repositories.ToArray();
                }
            }
            return repository;
        }
        #endregion

        #region RemoveRepositoryFromProfile
        /// <summary>
        /// Remove repository from profile
        /// </summary>
        /// <param name="profile">The profile</param>
        /// <param name="repository">The repository to remove</param>
        public static void RemoveRepositoryFromProfile(Profile profile, Repository repository)
        {
            List<Repository> repositories = new List<Repository>();
            repositories.AddRange(profile.repositories);
            repositories.Remove(repository);
            profile.repositories = repositories.ToArray();
        }
        #endregion

        #region
        /// <summary>
        /// Update repository information
        /// </summary>
        /// <param name="profile">The profile</param>
        /// <param name="repository">The repository</param>
        /// <param name="isReleaseEnabled">True if release is enabled</param>
        /// <param name="isSnapshotEnabled">True if snapshot is enabled</param>
        public static void UpdateRepository(Profile profile, Repository repository, bool isReleaseEnabled, bool isSnapshotEnabled)
        {
            Activation activation = new Activation();
            activation.activeByDefault = true;
            profile.activation = activation;

            RepositoryPolicy releasesPolicy = new RepositoryPolicy();
            RepositoryPolicy snapshotsPolicy = new RepositoryPolicy();
            releasesPolicy.enabled = isReleaseEnabled;
            snapshotsPolicy.enabled = isSnapshotEnabled;
            repository.releases = releasesPolicy;
            repository.snapshots = snapshotsPolicy;
            if (repository.id == null)
            {
                repository.id = generateRepositoryId(profile);
            }
        }
        #endregion

        private static string generateRepositoryId(Profile profile)
        {
            int ctr = 0;
            if (profile.repositories != null)
            {
                foreach (Repository repo in profile.repositories)
                {
                    if (repo.id != null && repo.id.StartsWith("npanday.repo."))
                    {
                        int index = int.Parse(repo.id.Substring(13));

                        if (index >= ctr)
                        {
                            ctr = index + 1;
                        }
                    }
                }
            }
            return "npanday.repo." + ctr;
        }

        public static Profile GetDefaultProfile(Settings settings)
        {
            return GetDefaultProfile(settings, false);
        }

        public static Profile GetDefaultProfile(Settings settings, bool create)
        {
            return GetProfile(settings, SettingsUtil.defaultProfileID, create);
        }
    }

    #endregion
}
