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
package org.apache.maven.dotnet.vendor.impl;

import org.apache.maven.dotnet.vendor.VendorInfoRepository;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorInfoMatchPolicy;
import org.apache.maven.dotnet.vendor.InvalidVersionFormatException;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.PlatformUnsupportedException;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
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
     * @see org.apache.maven.dotnet.vendor.VendorInfoRepository#exists()
     */
    public boolean exists()
    {
        return ( repositoryRegistry.find( "nmaven-settings" ) != null );
    }

    /**
     * @see VendorInfoRepository#getInstallRootFor(org.apache.maven.dotnet.vendor.VendorInfo)
     */
    public File getInstallRootFor( VendorInfo vendorInfo )
        throws PlatformUnsupportedException
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "nmaven-settings" );
        return settingsRepository.getInstallRootFor( vendorInfo.getVendor().getVendorName(),
                                                     vendorInfo.getVendorVersion(), vendorInfo.getFrameworkVersion() );
    }

    /**
     * @see org.apache.maven.dotnet.vendor.VendorInfoRepository#getVendorInfos()
     */
    public List<VendorInfo> getVendorInfos()
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "nmaven-settings" );
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
     * @see VendorInfoRepository#getVendorInfosFor(org.apache.maven.dotnet.vendor.VendorInfo, boolean)
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
     * @param vendorInfo the vendor info to match against the match policies
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
}
