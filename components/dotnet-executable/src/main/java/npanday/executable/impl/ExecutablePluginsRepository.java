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
package npanday.executable.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import npanday.executable.CommandCapability;
import npanday.executable.ExecutableCapability;
import npanday.executable.MutableExecutableCapability;
import npanday.model.executable.plugins.Platform;
import npanday.model.executable.plugins.CommandFilter;
import npanday.model.executable.plugins.ExecutablePlugin;
import npanday.model.executable.plugins.ExecutablePluginsModel;
import npanday.model.executable.plugins.io.xpp3.ExecutablePluginXpp3Reader;
import npanday.registry.ModelInterpolator;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.impl.AbstractMultisourceRepository;
import npanday.vendor.VendorInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Provides services for accessing the executable information within the executable-plugins.xml file.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.executable.impl.ExecutablePluginsRepository"
 */
public final class ExecutablePluginsRepository
    extends AbstractMultisourceRepository<ExecutablePluginsModel>
    implements Repository
{

    /**
     * A list of executable capabilities as specified within the executable-plugins.xml file
     */
    private List<ExecutablePlugin> executablePlugins = new ArrayList<ExecutablePlugin>();

    @Override
    protected ExecutablePluginsModel loadFromReader( Reader reader, Hashtable properties ) throws
        IOException,
        XmlPullParserException
    {
        ExecutablePluginXpp3Reader xpp3Reader = new ExecutablePluginXpp3Reader();
        return xpp3Reader.read( reader );
    }

    @Override
    protected void mergeLoadedModel( ExecutablePluginsModel model ) throws NPandayRepositoryException
    {
        executablePlugins.addAll( model.getExecutablePlugins() );
    }

    /**
     * Remove all stored values in preparation for a reload.
     */
    @Override
    protected void clear()
    {
        executablePlugins.clear();
    }

    /**
     * Returns a list of executable capabilities as specified within the executable-plugins.xml file.
     *
     * @param vendorInfo
     * @return a list of executable capabilities as specified within the executable-plugins.xml file
     */
    List<ExecutableCapability> getCapabilities( final VendorInfo vendorInfo )
    {
        List<ExecutableCapability> platformCapabilities = new ArrayList<ExecutableCapability>();
        for ( ExecutablePlugin plugin : executablePlugins )
        {
            String pluginClassName = plugin.getPluginClass();
            String executable = plugin.getExecutable();
            String vendor = plugin.getVendor();
            String vendorVersion = plugin.getVendorVersion();
            String identifier = plugin.getIdentifier();
            String profile = plugin.getProfile();
            List<String> frameworkVersions = plugin.getFrameworkVersions();

            if ( vendor != null && !vendorInfo.getVendor().getVendorName().toLowerCase().equals( vendor.toLowerCase() ) )
            {
                continue;
            }

            // TODO: check with version range!
            if ( vendorVersion != null && !vendorInfo.getVendorVersion().equals( vendorVersion ) )
            {
                continue;
            }

            if ( frameworkVersions != null && frameworkVersions.size() > 0 )
            {
                if ( !Iterables.any(
                    frameworkVersions, new Predicate<String>()
                {
                    public boolean apply( @Nullable String frameworkVersion )
                    {
                        return vendorInfo.getFrameworkVersion().equals( frameworkVersion );
                    }
                }
                ) )
                {
                    continue;
                }
            }

            // TODO: since the platform is fix here, we could already strip it down to those actually available
            List platforms = plugin.getPlatforms();
            for ( Iterator j = platforms.iterator(); j.hasNext(); )
            {
                MutableExecutableCapability platformCapability = new MutableExecutableCapability();

                platformCapability.setVendorInfo( vendorInfo );

                // TODO: strip empty ones
                platformCapability.setProbingPaths( plugin.getProbingPaths() );

                Platform platform = (Platform) j.next();
                String os = platform.getOperatingSystem();

                platformCapability.setOperatingSystem( os );
                platformCapability.setPluginClassName( pluginClassName );
                platformCapability.setExecutableName( executable );
                platformCapability.setIdentifier( identifier );
                platformCapability.setFrameworkVersions( frameworkVersions );
                platformCapability.setProfile( profile );
                String arch = platform.getArchitecture();
                CommandFilter filter = plugin.getCommandFilter();

                List<String> includes = ( filter != null ) ? filter.getIncludes() : new ArrayList<String>();
                List<String> excludes = ( filter != null ) ? filter.getExcludes() : new ArrayList<String>();
                platformCapability.setCommandCapability(
                    CommandCapability.Factory.createDefaultCommandCapability( includes, excludes )
                );

                if ( arch != null )
                {
                    platformCapability.setArchitecture( arch );
                }

                platformCapabilities.add( platformCapability );
            }
        }
        return platformCapabilities;
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

