package npanday.vendor.impl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import npanday.PlatformUnsupportedException;
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
        if (getInstallRoot() != null)
        {
            configuredPaths.add( getInstallRoot() );
        }

        // add .NET-SDK install root as path
        if ( getSdkInstallRoot() != null )
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
        final String sdkInstallRoot = configuredFramework.getSdkInstallRoot();
        if ( Strings.isNullOrEmpty( sdkInstallRoot ))
            return null;
        return new File( sdkInstallRoot );
    }

    public File getInstallRoot()
    {
        final String installRoot = configuredFramework.getInstallRoot();
        if ( Strings.isNullOrEmpty( installRoot ))
            return null;
        return new File( installRoot );
    }

    // TODO: Move this to a composed type: GacFinderStrategy
    public File getGlobalAssemblyCacheDirectoryFor( String artifactType )
        throws PlatformUnsupportedException
    {
        if (!ArtifactTypeHelper.isDotnetAnyGac( artifactType ))
            return null;

        if ( ArtifactTypeHelper.isDotnetGenericGac( artifactType ))
        {
            if ( vendor.equals( Vendor.MICROSOFT ) && getFrameworkVersion().equals( "1.1.4322" ) )
            {
                return new File( System.getenv("SystemRoot"), "\\assembly\\GAC\\" );
            }
            else if ( vendor.equals( Vendor.MICROSOFT ) )
            {
                // Layout changed since 2.0
                // http://discuss.joelonsoftware.com/default.asp?dotnet.12.383883.5
                return new File( System.getenv("SystemRoot"), "\\assembly\\GAC_MSIL\\" );
            }
            // TODO: fully implement finder for generic gac-type - better configure it somehow!
            else if ( vendor.equals( Vendor.MONO ) )
            {
                return getGacRootForMono();
            }
        }
        else if ( artifactType.equals( ArtifactType.GAC.getPackagingType() ) )
        {
            return new File( System.getenv("SystemRoot"), "\\assembly\\GAC\\" );
        }
        else if ( artifactType.equals( ArtifactType.GAC_32.getPackagingType() ) )
        {
            return new File(System.getenv("SystemRoot"), "\\assembly\\GAC_32\\" );
        }
        else if ( artifactType.equals( ArtifactType.GAC_32_4.getPackagingType() ) )
        {
            return new File(System.getenv("SystemRoot"), "\\Microsoft.NET\\assembly\\GAC_32\\" );
        }
        else if ( artifactType.equals( ArtifactType.GAC_64.getPackagingType() ) )
        {
            return new File(System.getenv("SystemRoot"), "\\assembly\\GAC_64\\" );
        }
        else if ( artifactType.equals( ArtifactType.GAC_64_4.getPackagingType() ) )
        {
            return new File(System.getenv("SystemRoot"), "\\Microsoft.NET\\assembly\\GAC_64\\" );
        }
        else if ( artifactType.equals( ArtifactType.GAC_MSIL.getPackagingType() ) )
        {
            return new File( System.getenv("SystemRoot"), "\\assembly\\GAC_MSIL\\" );
        }
        else if ( artifactType.equals( ArtifactType.GAC_MSIL4.getPackagingType() ) )
        {
            return new File( System.getenv("SystemRoot"), "\\Microsoft.NET\\assembly\\GAC_MSIL\\" );
        }
        throw new PlatformUnsupportedException("NPANDAY-113-006: Could not locate a valid GAC");
    }

    private File getGacRootForMono()
        throws PlatformUnsupportedException
    {
       // TODO: Multiple implemenations for finding mono gac...
        /*
        Found this somewhere...

            File sdkInstallRoot = getSdkInstallRoot();
            File gacRoot = new File( getSdkInstallRoot().getParentFile().getAbsolutePath() + "/lib/mono/gac" );
            if ( !gacRoot.exists() )
            {
                throw new PlatformUnsupportedException(
                    "NPANDAY-113-005: The Mono GAC path does not exist: Path = " +
                        gacRoot.getAbsolutePath() );

            }
            return gacRoot;

         */

        String path = System.getenv( "PATH" );
        if ( path != null )
        {
            String[] tokens = path.split( System.getProperty( "path.separator" ) );
            for ( String token : tokens )
            {
                File gacRoot = new File( new File( token ).getParentFile(), "lib/mono/gac/" );
                if ( gacRoot.exists() )
                {
                    return gacRoot;
                }
            }
        }
        //check settings file

        String monoRoot = System.getenv( "MONO_ROOT" );
        if ( monoRoot != null && !new File( monoRoot ).exists() )
        {
            // getLogger().warn( "MONO_ROOT has been incorrectly set. Trying /usr : MONO_ROOT = " + monoRoot );
        }
        else if ( monoRoot != null )
        {
            return new File(( !monoRoot.endsWith( File.separator ) ) ? monoRoot + File.separator : monoRoot);
        }

        if ( new File( "/usr/lib/mono/gac/" ).exists() )
        {
            // Linux default location
            return new File( "/usr/lib/mono/gac/" );
        }
        else if ( new File( "/Library/Frameworks/Mono.framework/Home/lib/mono/gac/" ).exists() )
        {
            // Mac OS X default location
            return new File( "/Library/Frameworks/Mono.framework/Home/lib/mono/gac/" );
        }
        else
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-061-008: Could not locate Global Assembly Cache for Mono. Try setting the MONO_ROOT environmental variable." );
        }
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

