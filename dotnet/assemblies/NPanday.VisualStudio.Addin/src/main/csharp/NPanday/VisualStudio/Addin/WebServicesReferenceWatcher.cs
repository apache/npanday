using System;
using System.IO;

namespace NPanday.VisualStudio.Addin
{
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
}