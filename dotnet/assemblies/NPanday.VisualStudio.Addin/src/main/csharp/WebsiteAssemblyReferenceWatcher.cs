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