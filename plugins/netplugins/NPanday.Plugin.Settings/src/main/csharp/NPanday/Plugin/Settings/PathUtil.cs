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
using System.IO;

namespace NPanday.Plugin.Settings
{
    // TODO: Move this to common PathUtil, when NPANDAY-422 is fixed
    public static class PathUtil
    {
        public static string GetHomeM2Folder()
        {
            return Environment.GetEnvironmentVariable("USERPROFILE") + "/.m2";
        }

        public static string BuildSettingsFilePath( string settingsPathOrFile )
        {
            if (settingsPathOrFile.EndsWith( "xml" ))
            {
                return settingsPathOrFile;
            }

            return Path.Combine(settingsPathOrFile, "npanday-settings.xml");
        }

        public static string FirstExisting(params string[] probingPaths)
        {
            foreach (string dir in probingPaths)
            {
                if (Directory.Exists(dir))
                    return dir;
            }

            return null;
        }

        public static string FirstContainingFile(string fileToBeContained, params string[] probingPaths)
        {
            foreach (string dir in probingPaths)
            {
                if (File.Exists(Path.Combine(dir, fileToBeContained)))
                    return dir;
            }

            return null;
        }
    }
}
