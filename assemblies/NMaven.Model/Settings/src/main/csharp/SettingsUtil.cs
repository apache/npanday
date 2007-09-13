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
using System.Text;
using System.Xml;
using System.Xml.Serialization;
using NMaven.Model;
#endregion

namespace NMaven.Model.Setting
{
    #region SettingsUtil

    public static class SettingsUtil
    {
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

            XmlReader reader = null;
            try
            {
                reader = XmlReader.Create(fileInfo.FullName);
                XmlSerializer serializer = new XmlSerializer(typeof(Settings));

                return (serializer.CanDeserialize(reader))
                    ? (Settings)serializer.Deserialize(reader)
                    : null;
            }
            finally
            {
                if(reader != null)
                {
                    reader.Close();
                }
            }
        }
        #endregion
    }

    #endregion
}

