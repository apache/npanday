using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Xml.XPath;
using System.Windows.Forms;

namespace NMaven.VisualStudio.Addin
{
    public class WebServicesReferenceUtils
    {
        public static string GetReferenceFile(string referenceDirectory)
        {
            string fname = "";
            foreach (string f in Directory.GetFiles(referenceDirectory))
            {
                string fext = Path.GetExtension(f).ToLower();
                if (fext.Equals(".map") || fext.Equals(".discomap"))
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
                if (fext.Equals(".wsdl"))
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
            string xpathExpression = @"DiscoveryClientResultsFile/Results/DiscoveryClientResult[@referenceType='System.Web.Services.Discovery.ContractReference']/@url";
            System.Xml.XPath.XPathNodeIterator xIter = xNav.Select(xpathExpression);
            string url = "";
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
            if (!this.referenceDirectory.Equals(Path.Combine(wsPath, this.Name)))
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
