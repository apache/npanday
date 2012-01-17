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

namespace NPanday.ProjectImporter.Digest.Model
{
    public class SilverlightApplicationReference
    {
        private string guid;
        public string Guid
        {
            get { return guid; }
            set { guid = value; }
        }

        private string relativePath;
        public string RelativePath
        {
            get { return relativePath; }
            set { relativePath = value; }
        }

        private string targetDirectory;
        public string TargetDirectory
        {
            get { return targetDirectory; }
            set { targetDirectory = value; }
        }

        // what is this for?
        private bool value4;

        // internal property to set based on the guid
        private ProjectDigest project;
        public ProjectDigest Project
        {
            get { return project; }
            set { project = value; }
        }

        private static SilverlightApplicationReference parseProjectString(string s)
        {
            SilverlightApplicationReference reference = new SilverlightApplicationReference();

            string[] split = s.Split('|');
            reference.guid = split[0];
            reference.relativePath = split[1];
            reference.targetDirectory = split[2];
            if (split.Length > 3)
            {
                reference.value4 = bool.Parse(split[3]);
            }
            return reference;
        }

        internal static List<SilverlightApplicationReference> parseApplicationList(string silverlightApplicationList)
        {
            List<SilverlightApplicationReference> appList = new List<SilverlightApplicationReference>();
            string[] apps = silverlightApplicationList.Split(',');
            foreach (string app in apps)
            {
                appList.Add(parseProjectString(app));
            }
            return appList;
        }

        public override string ToString()
        {
            return "{guid=" + guid + "; relativePath=" + relativePath + "; targetDirectory=" + targetDirectory + "}";
        }
    }
}
