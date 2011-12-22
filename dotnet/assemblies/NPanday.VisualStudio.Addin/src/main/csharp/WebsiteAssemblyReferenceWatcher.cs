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
using System.IO;

namespace NPanday.VisualStudio.Addin
{
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
}