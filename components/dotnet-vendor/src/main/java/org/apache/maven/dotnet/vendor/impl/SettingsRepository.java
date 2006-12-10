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

import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.dotnet.model.settings.NMavenSettings;
import org.apache.maven.dotnet.model.settings.Vendor;
import org.apache.maven.dotnet.model.settings.DefaultSetup;
import org.apache.maven.dotnet.model.settings.Framework;
import org.apache.maven.dotnet.model.settings.io.xpp3.NMavenSettingsXpp3Reader;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorUnsupportedException;

/**
 *  Provides methods for loading and reading the nmaven-settings config file.
 *
 * @author Shane Isbell
 */
public class SettingsRepository
    implements Repository
{

    /**
     * List of all vendors from the nmaven-settings file. The <code>Vendor</code> is the raw model type.
     */
    private List<Vendor> vendors;

    /**
     * The default setup: framework version, vendor, vendor version. If no information is provided by the user, then
     * this information will be used to choose the environment. It may also be used for partial matches, if appropriate.
     */
    private DefaultSetup defaultSetup;

    /**
     * List of all vendors from the nmaven-settings file.
     */
    private List<VendorInfo> vendorInfos;

    /**
     * Constructor. This method is intended to be invoked by the <code>RepositoryRegistry<code>, not by the
     * application developer.
     */
    public SettingsRepository()
    {
    }

    /**
     * @see Repository#load(java.io.InputStream, java.util.Hashtable)
     */
    public void load( InputStream inputStream, Hashtable properties )
        throws IOException
    {
        NMavenSettingsXpp3Reader xpp3Reader = new NMavenSettingsXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        NMavenSettings settings;
        try
        {
            settings = xpp3Reader.read( reader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NMAVEN-104-000: Could not read executable-plugins.xml" );
        }
        vendors = settings.getVendors();
        defaultSetup = settings.getDefaultSetup();
        vendorInfos = new ArrayList<VendorInfo>();

        for ( Vendor v : vendors )
        {
            List<Framework> frameworks = v.getFrameworks();
            for ( Framework framework : frameworks )
            {
                VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
                vendorInfo.setVendorVersion( v.getVendorVersion() );
                vendorInfo.setExecutablePath( new File( framework.getInstallRoot() ) );
                vendorInfo.setFrameworkVersion( framework.getFrameworkVersion() );
                try
                {
                    vendorInfo.setVendor( VendorFactory.createVendorFromName( v.getVendorName() ) );
                }
                catch ( VendorUnsupportedException e )
                {
                    continue;
                }
                vendorInfo.setDefault(
                    v.getIsDefault() != null && v.getIsDefault().toLowerCase().trim().equals( "true" ) );
                vendorInfos.add( vendorInfo );
            }
        }
    }

    /**
     * @see Repository#setRepositoryRegistry(org.apache.maven.dotnet.registry.RepositoryRegistry)
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
    }

    /**
     * Returns all vendor infos from the nmaven-settings file.
     *
     * @return all vendor infos from the nmaven-settings file
     */
    List<VendorInfo> getVendorInfos()
    {
        return vendorInfos;
    }

    /**
     *  Returns the install root for the .NET framework for the specified parameters. None of the parameter values
     *  should be null.
     *
     * @param vendor            the vendor name
     * @param vendorVersion     the vendor version
     * @param frameworkVersion  the .NET framework version
     * @return the install root for the .NET framework
     * @throws org.apache.maven.dotnet.PlatformUnsupportedException if there is no install root found for the specified parameters
     */
    File getInstallRootFor( String vendor, String vendorVersion, String frameworkVersion )
        throws PlatformUnsupportedException
    {
        if ( vendor == null || vendorVersion == null || frameworkVersion == null )
        {
            throw new PlatformUnsupportedException( "NMAVEN-104-001: One of more of the parameters is null: Vendor = " +
                vendor + ", Vendor Version = " + vendorVersion + ", Framework Version = " + frameworkVersion );
        }
        for ( Vendor v : vendors )
        {
            if ( vendor.equals( v.getVendorName().trim() ) && vendorVersion.equals( v.getVendorVersion().trim() ) )
            {
                List<Framework> frameworks = v.getFrameworks();
                for ( Framework framework : frameworks )
                {
                    if ( frameworkVersion.equals( framework.getFrameworkVersion().trim() ) )
                    {
                        return new File( framework.getInstallRoot() );
                    }
                }
            }
        }
        throw new PlatformUnsupportedException( "NMAVEN-104-002: Unable to find install root: Vendor = " + vendor +
            ", Vendor Version = " + vendorVersion + ", Framework Version = " + frameworkVersion );
    }

    /**
     * Returns the default setup: framework version, vendor, vendor version. If no information is provided by the user, then
     * this information will be used to choose the environment. It may also be used for partial matches, if appropriate.
     *
     * @return the default setup: framework version, vendor, vendor version
     */
    DefaultSetup getDefaultSetup()
    {
        return defaultSetup;
    }

}
