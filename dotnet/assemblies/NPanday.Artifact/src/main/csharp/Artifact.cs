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
using System.IO;

namespace NPanday.Artifact
{
    public sealed class Artifact
    {
        private string artifactId;

        private string groupId;

        private string version;

        private FileInfo fileInfo;

        private int artifactScope;

        private string extension;

        private String remotePath;
		
		private string systemPath;

        public String RemotePath
        {
            get { return remotePath; }
            set { this.remotePath = value; }
        }

        public string Extension
        {
            get { return extension; }
            set { extension = value; }
        }

        public string ArtifactId
        {
            get { return artifactId; }
            set { artifactId = value; }
        }

        public string GroupId
        {
            get { return groupId; }
            set { groupId = value; }
        }

        public string Version
        {
            get { return version; }
            set { version = value; }
        }

        public FileInfo FileInfo
        {
            get { return fileInfo; }
            set { fileInfo = value; }
        }

        public int ArtifactScope
        {
            get { return artifactScope; }
            set { artifactScope = value; }
        }
		
		public string SystemPath
		{
			get { return systemPath; }
			set { systemPath = value; }
		}
		
    }     
}
