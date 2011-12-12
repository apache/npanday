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

import npanday.PlatformUnsupportedException;
import npanday.model.settings.DefaultSetup;
import npanday.model.settings.Framework;
import npanday.model.settings.NPandaySettings;
import npanday.model.settings.Vendor;
import npanday.model.settings.io.xpp3.NPandaySettingsXpp3Reader;
import npanday.registry.ModelInterpolator;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.impl.AbstractMultisourceRepository;
import npanday.vendor.SettingsRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Provides methods for loading and reading the npanday-settings config file.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @plexus.component
 *   role="npanday.vendor.SettingsRepository"
 */
public final class FileBasedSettingsRepository
    extends AbstractMultisourceRepository<NPandaySettings>
    implements SettingsRepository
{
    /**
     * List of all vendors from the various sources. The <code>Vendor</code> is the raw model type.
     */
    private List<Vendor> vendors = new ArrayList<Vendor>();

    /**
     * The default setup: framework version, vendor, vendor version. If no information is provided by the user, then
     * this information will be used to choose the environment. It may also be used for partial matches, if appropriate.
     */
    private DefaultSetup defaultSetup;

    /**
     * Constructor. This method is intended to be invoked by the <code>RepositoryRegistry<code>, not by the
     * application developer.
     */
    public FileBasedSettingsRepository()
    {
    }

    @Override
    protected NPandaySettings loadFromReader( Reader reader, Hashtable properties )
        throws IOException, XmlPullParserException
    {
        NPandaySettingsXpp3Reader xpp3Reader = new NPandaySettingsXpp3Reader();
        return xpp3Reader.read( reader );
    }

    @Override
    protected void mergeLoadedModel( NPandaySettings settings )
        throws NPandayRepositoryException
    {
        vendors.addAll( settings.getVendors() );

        final DefaultSetup currentDefaultSetup = settings.getDefaultSetup();

        if ( currentDefaultSetup != null && defaultSetup != null )
        {
            getLogger().warn(
                "NPANDAY-104-006: The default setup was already defined, got overridden by a subsequent source, was: "
                    + defaultSetup + ", is now " + currentDefaultSetup );
        }

        defaultSetup = currentDefaultSetup;
    }

    @Override
    protected void clear()
    {
        vendors.clear();
        defaultSetup = null;
    }

    /**
     * Gets the raw configured model.
     *
     * @return Unmodifiable list.
     */
    public List<Vendor> getVendors()
    {
        return Collections.unmodifiableList( vendors );
    }

    /**
     * Returns the install root for the .NET framework for the specified parameters. None of the parameter values
     * should be null.
     *
     * @param vendor           the vendor name
     * @param vendorVersion    the vendor version
     * @param frameworkVersion the .NET framework version
     * @return the install root for the .NET framework
     * @throws npanday.PlatformUnsupportedException
     *          if there is no install root found for the specified parameters
     */
    public File getInstallRootFor( String vendor, String vendorVersion, String frameworkVersion )
        throws PlatformUnsupportedException
    {
        if ( vendor == null || vendorVersion == null || frameworkVersion == null )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-104-001: One of more of the parameters is null: Vendor = " + vendor + ", Vendor Version = "
                    + vendorVersion + ", Framework Version = " + frameworkVersion );
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
        throw new PlatformUnsupportedException(
            "NPANDAY-104-002: Unable to find install root: Vendor = " + vendor + ", Vendor Version = " + vendorVersion
                + ", Framework Version = " + frameworkVersion );
    }

    List<File> getExecutablePathsFor( String vendor, String vendorVersion, String frameworkVersion )
        throws PlatformUnsupportedException
    {
        List<File> executablePaths = new ArrayList<File>();
        if ( vendor == null || vendorVersion == null || frameworkVersion == null )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-104-006: One of more of the parameters is null: Vendor = " + vendor + ", Vendor Version = "
                    + vendorVersion + ", Framework Version = " + frameworkVersion );
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
                        List paths = framework.getExecutablePaths();
                        for ( Object path : paths )
                        {
                            executablePaths.add( new File( (String) path ) );
                        }
                    }
                }
            }
        }
        return executablePaths;
    }

    /**
     * Returns the default setup: framework version, vendor, vendor version. If no information is provided by the user, then
     * this information will be used to choose the environment. It may also be used for partial matches, if appropriate.
     *
     * @return the default setup: framework version, vendor, vendor version
     */
    public DefaultSetup getDefaultSetup()
    {
        return defaultSetup;
    }

    public boolean isEmpty()
    {
        return getVendors().size() == 0 && defaultSetup == null;
    }

    public void initialize()
        throws InitializationException
    {

    }

    // ### COMPONENTS REQUIRED BY THE BASE CLASS

    /**
     * @plexus.requirement
     */
    private ModelInterpolator interpolator;

    @Override
    protected ModelInterpolator getInterpolator()
    {
        return interpolator;
    }
}

