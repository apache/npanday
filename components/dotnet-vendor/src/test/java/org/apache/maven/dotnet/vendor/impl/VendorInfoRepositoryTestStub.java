package npanday.vendor.impl;

import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorInfo;
import npanday.vendor.InvalidVersionFormatException;
import npanday.vendor.Vendor;

import npanday.PlatformUnsupportedException;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class VendorInfoRepositoryTestStub
    implements VendorInfoRepository
{

    private List<VendorInfo> vendorInfos;

    public boolean exists()
    {
        return true;
    }

    public File getGlobalAssemblyCacheDirectoryFor( Vendor vendor, String frameworkVersion, String artifactType )
        throws PlatformUnsupportedException
    {
        return null;  
    }

    public File getInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        return null;
    }

    public File getSdkInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        return null;
    }

    public List<VendorInfo> getVendorInfos()
    {
        return new ArrayList<VendorInfo>();
    }

    public String getMaxVersion( Set<String> versions )
        throws InvalidVersionFormatException
    {
        return new VersionMatcher().getMaxVersion( versions );
    }

    public List<VendorInfo> getVendorInfosFor( String vendorName, String vendorVersion, String frameworkVersion,
                                               boolean isDefault )
    {
        return new ArrayList<VendorInfo>();
    }

    public List<VendorInfo> getVendorInfosFor( VendorInfo vendorInfo, boolean isDefault )
    {
        return vendorInfos;
    }

    void setVendorInfos( List<VendorInfo> vendorInfos )
    {
        this.vendorInfos = vendorInfos;
    }
}
