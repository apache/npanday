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
