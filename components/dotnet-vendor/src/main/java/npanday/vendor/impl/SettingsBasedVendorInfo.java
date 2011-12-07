package npanday.vendor.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import npanday.model.settings.Framework;
import npanday.vendor.Vendor;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link VendorInfo} backed by configurations from {@link npanday.model.settings.Vendor}
 * and {@link npanday.model.settings.Framework}
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class SettingsBasedVendorInfo
    implements VendorInfo
{
    private npanday.model.settings.Vendor configuredVendor;

    private Framework configuredFramework;

    private boolean isDefault;

    private Vendor vendor;

    private List<File> executablePaths;

    public SettingsBasedVendorInfo( npanday.model.settings.Vendor configuredVendor, Framework configuredFramework )
    {

        this.configuredVendor = configuredVendor;
        this.configuredFramework = configuredFramework;

        this.isDefault =
            configuredVendor.getIsDefault() != null && configuredVendor.getIsDefault().toLowerCase().trim().equals(
                "true" );
        this.vendor = VendorFactory.createVendorFromName( configuredVendor.getVendorName() );

        collectExistingPaths();
    }

    private void collectExistingPaths()
    {
        List<File> configuredPaths = new ArrayList<File>();

        // add .NET install root as path
        configuredPaths.add( getInstallRoot() );

        // add .NET-SDK install root as path
        if ( configuredFramework.getSdkInstallRoot() != null )
        {
            configuredPaths.add( getSdkInstallRoot() );
        }

        // copy configured additional execution paths
        if ( configuredFramework.getExecutablePaths() != null )
        {
            for ( Object path : configuredFramework.getExecutablePaths() )
            {
                configuredPaths.add( new File( (String) path ) );
            }
        }

        executablePaths = ImmutableList.copyOf( Iterables.filter( configuredPaths, new Predicate<File>() {
            public boolean apply( @Nullable File file )
            {
                return file.exists();
            }
        }));
    }

    public File getSdkInstallRoot()
    {
        return new File(configuredFramework.getSdkInstallRoot());
    }

    public File getInstallRoot()
    {
        return new File(configuredFramework.getInstallRoot());
    }


    public Vendor getVendor()
    {
        return vendor;
    }

    public String getVendorVersion()
    {
        return configuredVendor.getVendorVersion();
    }

    public String getFrameworkVersion()
    {
        return configuredFramework.getFrameworkVersion();
    }

    // TODO: Iterable should be enough here!
    public List<File> getExecutablePaths()
    {
        return executablePaths;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public String toString()
    {
        return "[Configured Vendor Info for " + vendor + " " + getVendorVersion() + ", Framework Version = "
            + getFrameworkVersion() + "]";
    }
}

