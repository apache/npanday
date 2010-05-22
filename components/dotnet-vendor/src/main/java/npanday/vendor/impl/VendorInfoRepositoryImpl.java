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
package npanday.vendor.impl;

import npanday.ArtifactTypeHelper;
import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorInfoMatchPolicy;
import npanday.vendor.InvalidVersionFormatException;
import npanday.vendor.Vendor;
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
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

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

    /**
     * @see VendorInfoRepository#getInstallRootFor(npanday.vendor.VendorInfo)
     */
    public File getInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        return settingsRepository.getInstallRootFor( vendorInfo.getVendor().getVendorName(),
                                                     vendorInfo.getVendorVersion(), vendorInfo.getFrameworkVersion() );
    }

    public File getSdkInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        return settingsRepository.getSdkInstallRootFor( vendorInfo.getVendor().getVendorName(),
                                                        vendorInfo.getVendorVersion(),
                                                        vendorInfo.getFrameworkVersion() );
    }

    /**
     * @see npanday.vendor.VendorInfoRepository#getVendorInfos()
     */
    public List<VendorInfo> getVendorInfos()
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        return Collections.unmodifiableList( settingsRepository.getVendorInfos() );
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
                                               boolean isDefault )
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
        if ( isDefault )
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
    public List<VendorInfo> getVendorInfosFor( VendorInfo vendorInfo, boolean isDefault )
    {
        if ( vendorInfo == null )
        {
            return getVendorInfos();
        }
        return getVendorInfosFor( ( vendorInfo.getVendor() != null ? vendorInfo.getVendor().getVendorName() : null ),
                                  vendorInfo.getVendorVersion(), vendorInfo.getFrameworkVersion(), isDefault );
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

        if (ArtifactTypeHelper.isDotnetGac( artifactType ))
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
        else if ( artifactType.equals( "gac" ) )
        {
            return new File( System.getenv("SystemRoot"), "\\assembly\\GAC\\" );
        }
        else if ( artifactType.equals( "gac_32" ) )
        {
            return new File(System.getenv("SystemRoot"), "\\assembly\\GAC_32\\" );
        }
        else if ( artifactType.equals( "gac_msil" ) )
        {
            return new File( System.getenv("SystemRoot"), "\\assembly\\GAC_MSIL\\" );
        }
        throw new PlatformUnsupportedException("NPANDAY-xxx-000: Could not locate a valid GAC");
    }
}
