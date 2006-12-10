using System;
using System.IO;
using System.Collections;
using System.Xml.Serialization;
using Microsoft.Win32;
using NMaven.Utility;

namespace NMaven.Utility.Settings
{
    class SettingsGeneratorDelegator : SettingsGenerator
    {
        internal SettingsGeneratorDelegator() : base() { }

        internal new nmavenSettingsVendor GetVendorForGnu(String libPath)
        {
            return base.GetVendorForGnu(libPath);
        }

        internal new nmavenSettingsDefaultSetup GetDefaultSetup(string defaultMonoCLR,  string installRoot)
        {
            return base.GetDefaultSetup(defaultMonoCLR, installRoot);
        }
    }
}
