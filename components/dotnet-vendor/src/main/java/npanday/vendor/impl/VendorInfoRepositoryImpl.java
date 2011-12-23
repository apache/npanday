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

import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import npanday.model.settings.Framework;
import npanday.vendor.*;
import npanday.registry.RepositoryRegistry;
import npanday.PlatformUnsupportedException;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * Provides an implementation of <code>VendorInfoRepository</code>.
 *
 * @author Shane Isbell
 */
public class VendorInfoRepositoryImpl
    implements VendorInfoRepository, LogEnabled
{

    /**
     * A registry component of repository (config) files
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * Cache
     */
    private List<VendorInfo> cachedVendorInfos;

    /**
     * Constructor. This method is intended to be invoked by the plexus-container, not by the application developer.
     */
    public VendorInfoRepositoryImpl()
    {
    }

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /**
     * @see npanday.vendor.VendorInfoRepository#exists()
     */
    public boolean exists()
    {
        return ( repositoryRegistry.find( "npanday-settings" ) != null );
    }

    public void clearCache()
    {
        if ( cachedVendorInfos != null )
        {
            cachedVendorInfos.clear();
            cachedVendorInfos = null;
        }
    }

    private File getInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        return settingsRepository.getInstallRootFor( vendorInfo.getVendor().getVendorName(),
                                                     vendorInfo.getVendorVersion(), vendorInfo.getFrameworkVersion() );
    }

    private File getSdkInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        return settingsRepository.getSdkInstallRootFor( vendorInfo.getVendor().getVendorName(),
                                                        vendorInfo.getVendorVersion(),
                                                        vendorInfo.getFrameworkVersion() );
    }

    public VendorInfo getConfiguredVendorInfoByExample(VendorInfo vendorInfoExample)
        throws PlatformUnsupportedException
    {
        List<VendorInfo> infos = getVendorInfosFor(vendorInfoExample, false);
        if (infos.size() == 0) {
           throw new PlatformUnsupportedException( "NPANDAY-200-001: Could not find configuration for " + vendorInfoExample );
        }

        if (infos.size() > 2) {
            // reload default
            infos = getVendorInfosFor(vendorInfoExample, true);
        }

        assert infos.size() == 1;

        return infos.get(0);
    }

    private List<VendorInfo> getVendorInfos()
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );

        if ( settingsRepository.isReloaded() )
        {
            clearCache();
            settingsRepository.setReloaded(false);
        }

        try
        {
            settingsRepository.reload();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
 
        if ( cachedVendorInfos != null && cachedVendorInfos.size() > 0 &&  !settingsRepository.isReloaded() )
        {
            return Collections.unmodifiableList( cachedVendorInfos );
        }

        cachedVendorInfos = new ArrayList<VendorInfo>();

        for ( npanday.model.settings.Vendor v : settingsRepository.getVendors() )
        {
            List<Framework> frameworks = v.getFrameworks();
            for ( Framework framework : frameworks )
            {
                VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
                vendorInfo.setVendorVersion( v.getVendorVersion() );
                List<File> executablePaths = new ArrayList<File>();

                // add .NET install root as path
                executablePaths.add(new File( framework.getInstallRoot() ));

                // add .NET-SDK install root as path
                if(framework.getSdkInstallRoot() != null)
                {
                    executablePaths.add( new File(framework.getSdkInstallRoot()));
                }

                // copy configured additional execution paths
                if (framework.getExecutablePaths() != null) {
                    for(Object path: framework.getExecutablePaths()) {
                        executablePaths.add( new File((String)path) );
                    }
                }
                vendorInfo.setExecutablePaths( executablePaths );
                vendorInfo.setFrameworkVersion( framework.getFrameworkVersion() );
                try
                {
                    vendorInfo.setVendor( VendorFactory.createVendorFromName(v.getVendorName()) );
                }
                catch ( VendorUnsupportedException e )
                {
                    continue;
                }
                vendorInfo.setDefault(
                    v.getIsDefault() != null && v.getIsDefault().toLowerCase().trim().equals( "true" ) );
                cachedVendorInfos.add( vendorInfo );
            }
        }
        settingsRepository.setReloaded( false );
        return Collections.unmodifiableList( cachedVendorInfos );
    }

    /**
     * @see VendorInfoRepository#getMaxVersion(java.util.Set<String>)
     */
    public String getMaxVersion( Set<String> versions )
        throws InvalidVersionFormatException
    {
        return new VersionMatcher().getMaxVersion( versions );
    }

    /**
     * @see VendorInfoRepository#getVendorInfosFor(String, String, String, boolean)
     */
    public List<VendorInfo> getVendorInfosFor( String vendorName, String vendorVersion, String frameworkVersion,
                                               boolean defaultOnly )
    {
        List<VendorInfo> vendorInfos = new ArrayList<VendorInfo>();
        MatchPolicyFactory matchPolicyFactory = new MatchPolicyFactory();
        matchPolicyFactory.init( logger );

        List<VendorInfoMatchPolicy> matchPolicies = new ArrayList<VendorInfoMatchPolicy>();
        if ( vendorName != null )
        {
            matchPolicies.add( matchPolicyFactory.createVendorNamePolicy( vendorName ) );
        }
        if ( vendorVersion != null )
        {
            matchPolicies.add( matchPolicyFactory.createVendorVersionPolicy( vendorVersion ) );
        }
        if ( frameworkVersion != null )
        {
            matchPolicies.add( matchPolicyFactory.createFrameworkVersionPolicy( frameworkVersion ) );
        }
        if ( defaultOnly )
        {
            matchPolicies.add( matchPolicyFactory.createVendorIsDefaultPolicy() );
        }
        for ( VendorInfo vendorInfo : getVendorInfos() )
        {
            if ( matchVendorInfo( vendorInfo, matchPolicies ) )
            {
                vendorInfos.add( vendorInfo );
            }
        }
        return vendorInfos;
    }

    /**
     * @see VendorInfoRepository#getVendorInfosFor(npanday.vendor.VendorInfo, boolean)
     */
    public List<VendorInfo> getVendorInfosFor( VendorInfo vendorInfo, boolean defaultOnly )
    {
        if ( vendorInfo == null )
        {
            return getVendorInfos();
        }
        return getVendorInfosFor( ( vendorInfo.getVendor() != null ? vendorInfo.getVendor().getVendorName() : null ),
                                  vendorInfo.getVendorVersion(), vendorInfo.getFrameworkVersion(), defaultOnly );
    }

    /**
     * Returns true if the specified vendor info matches <i>all</i> of the specified match policies, otherwise returns
     * false.
     *
     * @param vendorInfo    the vendor info to match against the match policies
     * @param matchPolicies the match policies
     * @return true if the specified vendor info matches <i>all</i> of the specified match policies, otherwise returns
     *         false
     */
    private boolean matchVendorInfo( VendorInfo vendorInfo, List<VendorInfoMatchPolicy> matchPolicies )
    {
        for ( VendorInfoMatchPolicy matchPolicy : matchPolicies )
        {
            if ( !matchPolicy.match( vendorInfo ) )
            {
                return false;
            }
        }
        return true;
    }

    public File getGlobalAssemblyCacheDirectoryFor( Vendor vendor, String frameworkVersion, String artifactType )
        throws PlatformUnsupportedException
    {
        // TODO: Duplicate code with CompilerContextImpl.init

        if (ArtifactTypeHelper.isDotnetGenericGac( artifactType ))
        {
            if ( vendor.equals( Vendor.MICROSOFT ) && frameworkVersion.equals( "1.1.4322" ) )
            {
                return new File( System.getenv("SystemRoot"), "\\assembly\\GAC\\" );
            }
            else if ( vendor.equals( Vendor.MICROSOFT ) )
            {
                // Layout changed since 2.0
                // http://discuss.joelonsoftware.com/default.asp?dotnet.12.383883.5
                return new File( System.getenv("SystemRoot"), "\\assembly\\GAC_MSIL\\" );
            }
            else if ( vendor.equals( Vendor.MONO ) && exists() )
            {
                List<VendorInfo> vendorInfos =
                    getVendorInfosFor( vendor.getVendorName(), null, frameworkVersion, true );
                Set<String> vendorVersions = new HashSet<String>();
                for ( VendorInfo vendorInfo : vendorInfos )
                {
                    vendorVersions.add( vendorInfo.getVendorVersion() );
                }
                String maxVersion;
                try
                {
                    maxVersion = getMaxVersion( vendorVersions );
                }
                catch ( InvalidVersionFormatException e )
                {
                    throw new PlatformUnsupportedException( "NPANDAY-xxx-000: Invalid version format", e );
                }

                for ( VendorInfo vendorInfo : vendorInfos )
                {
                    if ( vendorInfo.getVendorVersion().equals( maxVersion ) )
                    {
                        File sdkInstallRoot = getSdkInstallRootFor( vendorInfo );
                        File gacRoot = new File( sdkInstallRoot.getParentFile().getAbsolutePath() + "/lib/mono/gac" );
                        if ( !gacRoot.exists() )
                        {
                            throw new PlatformUnsupportedException(
                                "NPANDAY-xxx-000: The Mono GAC path does not exist: Path = " +
                                    gacRoot.getAbsolutePath() );
                        }
                        return gacRoot;
                    }
                }
                
                //TODO: MONO Support for Linux (Separate file containg installs)
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
        throw new PlatformUnsupportedException("NPANDAY-200-002: Could not locate a valid GAC");
    }
}
