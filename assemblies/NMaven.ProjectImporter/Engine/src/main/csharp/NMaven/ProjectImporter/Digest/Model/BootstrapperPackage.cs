using System;
using System.Collections.Generic;
using System.Text;


/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Digest.Model
{
    public class BootstrapperPackage : IncludeBase
    {
        public BootstrapperPackage(string projectBasePath) 
            : base(projectBasePath)
        {  
        }

        private string visible;
        public string Visible
        {
            get { return visible; }
            set { visible = value; }
        }

        private string productName;
        public string ProductName
        {
            get { return productName; }
            set { productName = value; }
        }

        private string install;
        public string Install
        {
            get { return install; }
            set { install = value; }
        }
    }
}
