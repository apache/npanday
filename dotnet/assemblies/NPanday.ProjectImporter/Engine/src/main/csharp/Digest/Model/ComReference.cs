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

//Author: Joe Ocaba

namespace NPanday.ProjectImporter.Digest.Model
{
    public class ComReference
    {
        private string include;
        public string Include
        {
            get { return include; }
            set { include = value; }
        }

        private string guid;
        public string Guid
        {
            get { return guid; }
            set { guid = value; }
        }

        private string versionMajor;
        public string VersionMajor
        {
            get { return versionMajor; }
            set { versionMajor = value; }
        }

        private string versionMinor;
        public string VersionMinor
        {
            get { return versionMinor; }
            set { versionMinor = value; }
        }

        private string lcid;
        public string Lcid
        {
            get { return lcid; }
            set { lcid = value; }
        }

        private string wrapperTool;
        public string WrapperTool
        {
            get { return wrapperTool; }
            set { wrapperTool = value; }
        }

        private string isolated;
        public string Isolated
        {
            get { return isolated; }
            set { isolated = value; }
        }


    }
}
