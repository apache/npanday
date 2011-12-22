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

namespace NPanday.VisualStudio.Addin
{
    public class WebReferenceEventArgs : FileSystemEventArgs
    {

        public WebReferenceEventArgs(WatcherChangeTypes changeType, string directory, string name )
            :base(changeType, directory,name)
        {
            this.referenceDirectory = directory;    
        }

        private string referenceDirectory;

        public string ReferenceDirectory
        {
            get { return referenceDirectory; }
            set { referenceDirectory = value; }
        }


        private string oldNamespace;

        public string OldNamespace
        {
            get { return oldNamespace; }
            set { oldNamespace = value; }
        }


        private string _namespace;

        public string Namespace
        {
            get { return _namespace; }
            set { _namespace = value; }
        }

        private string wsdlUrl;

        public string WsdlUrl
        {
            get { return wsdlUrl; }
            set { wsdlUrl = value; }
        }

        private string wsdlFile;

        public string WsdlFile
        {
            get { return wsdlFile; }
            set { wsdlFile = value; }
        }


        public void Init(string wsPath)
        {
            if (!string.IsNullOrEmpty(wsPath))
            {
                if (!this.referenceDirectory.Equals(Path.Combine(wsPath, this.Name), StringComparison.InvariantCultureIgnoreCase))
                {
                    this.referenceDirectory = Path.Combine(wsPath, this.Name);
                }
                this.Namespace = this.Name;
                if (this.ChangeType != WatcherChangeTypes.Deleted)
                {
                    string projectPath = Path.GetDirectoryName(Path.GetDirectoryName(this.referenceDirectory));
                    
                    this.wsdlUrl = WebServicesReferenceUtils.GetWsdlUrl(WebServicesReferenceUtils.GetReferenceFile(this.referenceDirectory));
                    this.wsdlFile = WebServicesReferenceUtils.GetWsdlFile(this.referenceDirectory);
                    this.wsdlFile = this.wsdlFile.Substring(projectPath.Length+1);

                }
            }
        }

	
    }
}