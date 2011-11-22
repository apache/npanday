namespace NPanday.Plugin.Settings
{
    /// <remarks/>
    public class npandaySettingsVendorsVendor {
        
        /// <remarks/>
        public string vendorName;
        
        /// <remarks/>
        public string vendorVersion;
        
        /// <remarks/>
        public string isDefault;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlArrayItem(ElementName="framework", IsNullable=false)]
        public npandaySettingsVendorsVendorFrameworksFramework[] frameworks;
    }
}