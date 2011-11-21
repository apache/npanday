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