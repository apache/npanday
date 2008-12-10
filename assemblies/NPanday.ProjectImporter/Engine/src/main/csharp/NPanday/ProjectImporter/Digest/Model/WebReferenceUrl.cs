using System;
using System.Collections.Generic;
using System.Text;


using NPanday.ProjectImporter.Digest;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{

    public class WebReferenceUrl
    {
        private string urlBehavior;
        public string UrlBehavior
        {
            get { return urlBehavior; }
            set { urlBehavior = value; }
        }

        private string relPath;
        public string RelPath
        {
            get { return relPath; }
            set { relPath = value; }
        }

        private string updateFromURL;
        public string UpdateFromURL
        {
            get { return updateFromURL; }
            set { updateFromURL = value; }
        }
        
        private string serviceLocationURL;
        public string ServiceLocationURL
        {
            get { return serviceLocationURL; }
            set { serviceLocationURL = value; }
        }
        
        private string cachedDynamicPropName;
        public string CachedDynamicPropName
        {
            get { return cachedDynamicPropName; }
            set { cachedDynamicPropName = value; }
        }
        
        private string cachedAppSettingsObjectName;
        public string CachedAppSettingsObjectName
        {
            get { return cachedAppSettingsObjectName; }
            set { cachedAppSettingsObjectName = value; }
        }
        
        private string cachedSettingsPropName;
        public string CachedSettingsPropName
        {
            get { return cachedSettingsPropName; }
            set { cachedSettingsPropName = value; }
        }
    }

}
