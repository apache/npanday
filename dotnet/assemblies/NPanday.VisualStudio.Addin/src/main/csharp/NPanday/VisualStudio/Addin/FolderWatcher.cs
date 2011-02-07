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
using System.Xml.XPath;
using System.Windows.Forms;

namespace NPanday.VisualStudio.Addin
{
    public class WebServicesReferenceUtils
    {
        public static string GetReferenceFile(string referenceDirectory)
        {
            string fname = "";
            foreach (string f in Directory.GetFiles(referenceDirectory))
            {
                string fext = Path.GetExtension(f).ToLower();
                if (fext.Equals(".map", StringComparison.InvariantCultureIgnoreCase) || fext.Equals(".discomap", StringComparison.InvariantCultureIgnoreCase)
                    || fext.Equals(".svcmap", StringComparison.InvariantCultureIgnoreCase))
                {
                    fname = f;
                    break;
                }

            }
            return fname;
        }

        public static string GetWsdlFile(string referenceDirectory)
        {
            string fname = "";
            foreach (string f in Directory.GetFiles(referenceDirectory))
            {
                string fext = Path.GetExtension(f).ToLower();
                if (fext.Equals(".wsdl", StringComparison.InvariantCultureIgnoreCase))
                {
                    fname = f;
                    break;
                }

            }
            return fname;
        }


        public static string GetWsdlUrl(string referencePath)
        {
            XPathDocument xDoc = new XPathDocument(referencePath);
            XPathNavigator xNav = xDoc.CreateNavigator();
            string xpathExpression;
            string url = "";

            if (referencePath.Contains(Messages.MSG_D_SERV_REF))
            {
                xpathExpression = @"ReferenceGroup/Metadata/MetadataFile[MetadataType='Wsdl']/@SourceUrl";
            }
            else
            {
                xpathExpression = @"DiscoveryClientResultsFile/Results/DiscoveryClientResult[@referenceType='System.Web.Services.Discovery.ContractReference']/@url";
            }

            System.Xml.XPath.XPathNodeIterator xIter = xNav.Select(xpathExpression);
            if (xIter.MoveNext())
            {
                url = xIter.Current.TypedValue.ToString();
            }
            return url;
        }
    }

    public class WebsiteAssemblyReferenceWatcher
    { 
        //public event RenamedEventHandler Renamed;
        public event FileSystemEventHandler Created;
        public event FileSystemEventHandler Deleted;

        FileSystemWatcher watcher;
        string folderPath;

        public WebsiteAssemblyReferenceWatcher(string folderPath)
        {
            this.folderPath = folderPath;
            this.init();
        }

        public void Start()
        {
            watcher.EnableRaisingEvents = true;
        }

        public void Stop()
        {
            watcher.EnableRaisingEvents = false;
            watcher.Dispose();
        }

        void init()
        {
            watcher = new FileSystemWatcher(folderPath);
            watcher.NotifyFilter = NotifyFilters.FileName;
            watcher.Deleted += new FileSystemEventHandler(watcher_Deleted);
            watcher.Created += new FileSystemEventHandler(watcher_Created);
            watcher.IncludeSubdirectories = false;
            
        }

       

        void watcher_Created(object sender, FileSystemEventArgs e)
        {
            if (Created != null)
            {
                Created(this, e);
            }
        }

        void watcher_Deleted(object sender, FileSystemEventArgs e)
        {
            if (Deleted != null)
                Deleted(this, e);
        }

    }

    public class WebServicesReferenceWatcher
    {
        public event EventHandler<WebReferenceEventArgs> Renamed;
        public event EventHandler<WebReferenceEventArgs> Created;
        public event EventHandler<WebReferenceEventArgs> Deleted;

        FileSystemWatcher watcher;
        string folderPath;

        public WebServicesReferenceWatcher(string folderPath)
        {
            this.folderPath = folderPath;
            this.init();
        }

        public void Start()
        {
            watcher.EnableRaisingEvents = true;
        }

        public void Stop()
        {
            watcher.EnableRaisingEvents = false;
            watcher.Dispose();
        }

        void init()
        {
            
			watcher = new FileSystemWatcher(folderPath);
            watcher.NotifyFilter = NotifyFilters.DirectoryName;
            watcher.Renamed += new RenamedEventHandler(watcher_Renamed);
            watcher.Deleted += new FileSystemEventHandler(watcher_Deleted);
            watcher.Created += new FileSystemEventHandler(watcher_Created);
            watcher.Error += new ErrorEventHandler(watcher_Error);
            watcher.Changed += new FileSystemEventHandler(watcher_Changed);
            watcher.IncludeSubdirectories = false;
            
        }

        void watcher_Changed(object sender, FileSystemEventArgs e)
        {
            Console.WriteLine(e.FullPath);
        }

        void watcher_Error(object sender, ErrorEventArgs e)
        {
            this.Stop();
        }

        void watcher_Created(object sender, FileSystemEventArgs e)
        {
            WebReferenceEventArgs a = new WebReferenceEventArgs(e.ChangeType, e.FullPath, e.Name);

            onCreated(a);
        }

        void watcher_Deleted(object sender, FileSystemEventArgs e)
        {
            WebReferenceEventArgs a = new WebReferenceEventArgs(e.ChangeType, e.FullPath, e.Name);
            onDeleted(a);
        }

        void watcher_Renamed(object sender, RenamedEventArgs e)
        {
            WebReferenceEventArgs a = new WebReferenceEventArgs(e.ChangeType, e.FullPath, e.Name);
            a.OldNamespace = e.OldName;
            
            onRenamed(a);            
        }

        void onRenamed(WebReferenceEventArgs e)
        {
            if (Renamed != null)
            {
                Renamed(this, e);
            }
        }

        void onDeleted(WebReferenceEventArgs e)
        {
            if (Deleted != null)
            {
                Deleted(this, e);
            }
        }

        void onCreated(WebReferenceEventArgs e)
        {
            if (Created != null)
            {
                Created(this, e);
            }
        }



    }

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
