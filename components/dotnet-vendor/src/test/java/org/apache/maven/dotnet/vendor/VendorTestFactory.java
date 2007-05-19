package org.apache.maven.dotnet.vendor;

import org.apache.maven.dotnet.model.settings.DefaultSetup;

import java.io.File;
import java.util.List;

public class VendorTestFactory
{
    public static VendorInfo getVendorInfo( Vendor vendor, String vendorVersion, String frameworkVersion )
    {
        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        vendorInfo.setVendor( vendor );
        vendorInfo.setFrameworkVersion( frameworkVersion );
        vendorInfo.setVendorVersion( vendorVersion );
        return vendorInfo;
    }

    public static VendorInfo getVendorInfo( Vendor vendor, String vendorVersion, String frameworkVersion,
                                            List<File> executablePaths )
    {
        VendorInfo vendorInfo = getVendorInfo(vendor, vendorVersion, frameworkVersion);
        vendorInfo.setExecutablePaths( executablePaths );

        return vendorInfo;
    }

    public static DefaultSetup getDefaultSetup( String vendorName, String vendorVersion, String frameworkVersion )
    {
        DefaultSetup defaultSetup = new DefaultSetup();
        defaultSetup.setVendorName( vendorName );
        defaultSetup.setVendorVersion( vendorVersion );
        defaultSetup.setFrameworkVersion( frameworkVersion );
        return defaultSetup;
    }
}

