namespace NPanday.Plugin.Settings
{
    /// <remarks/>
    public class npandaySettingsVendorsVendorFrameworksFramework {
        
        /// <remarks/>
        public string frameworkVersion;
        
        /// <remarks/>
        public string installRoot;
        
        /// <remarks/>
        public string sdkInstallRoot;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlArrayItem(ElementName="executablePath", IsNullable=false)]
        public string[] executablePaths;
    }
}