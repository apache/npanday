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
package org.apache.maven.dotnet.executable.impl;

import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.maven.dotnet.model.compiler.plugins.io.xpp3.ExecutablePluginXpp3Reader;
import org.apache.maven.dotnet.model.compiler.plugins.*;
import org.apache.maven.dotnet.executable.ExecutableCapability;
import org.apache.maven.dotnet.executable.CommandCapability;
import org.apache.maven.dotnet.vendor.Vendor;

/**
 * @author Shane Isbell
 */
public final class ExecutablePluginsRepository
    implements Repository
{

    private List<ExecutablePlugin> executablePlugins;

    /**
     * Loads the repository
     *
     * @param inputStream a stream of the repository file (typically from *.xml)
     * @param properties  additional user-supplied parameters used to customize the behavior of the repository
     * @throws java.io.IOException if there is a problem loading the repository
     */
    public void load( InputStream inputStream, Hashtable properties )
        throws IOException
    {
        ExecutablePluginXpp3Reader xpp3Reader = new ExecutablePluginXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        ExecutablePluginsModel plugins = null;
        try
        {
            plugins = xpp3Reader.read( reader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NMAVEN-067-000: Could not read executable-plugins.xml" );
        }
        executablePlugins = plugins.getExecutablePlugins();
    }

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
    }

    List<ExecutableCapability> getCapabilities()
    {
        List<ExecutableCapability> platformCapabilities = new ArrayList<ExecutableCapability>();
        for ( ExecutablePlugin plugin : executablePlugins )
        {
            String pluginClassName = plugin.getPluginClass();
            String executable = plugin.getExecutable();
            String compilerVendor = plugin.getVendor();
            String identifier = plugin.getIdentifier();
            String profile = plugin.getProfile();
            List<String> frameworkVersions = plugin.getFrameworkVersions();

            List platforms = plugin.getPlatforms();
            for ( Iterator j = platforms.iterator(); j.hasNext(); )
            {
                ExecutableCapability platformCapability =
                    ExecutableCapability.Factory.createDefaultExecutableCapability();
                Platform platform = (Platform) j.next();
                String os = platform.getOperatingSystem();

                platformCapability.setOperatingSystem( os );
                platformCapability.setPluginClassName( pluginClassName );
                platformCapability.setExecutable( executable );
                platformCapability.setIdentifier( identifier );
                platformCapability.setFrameworkVersions( frameworkVersions );
                platformCapability.setProfile( profile );
                String arch = platform.getArchitecture();
                CommandFilter filter = plugin.getCommandFilter();

                List<String> includes = ( filter != null ) ? filter.getIncludes() : new ArrayList<String>();
                List<String> excludes = ( filter != null ) ? filter.getExcludes() : new ArrayList<String>();
                platformCapability.setCommandCapability(
                    CommandCapability.Factory.createDefaultCommandCapability( includes, excludes ) );
                if ( arch != null )
                {
                    platformCapability.setArchitecture( arch );
                }
                if ( compilerVendor.trim().equalsIgnoreCase( "microsoft" ) )
                {
                    platformCapability.setVendor( Vendor.MICROSOFT );
                }
                else if ( compilerVendor.trim().equalsIgnoreCase( "mono" ) )
                {
                    platformCapability.setVendor( Vendor.MONO );
                }
                else if ( compilerVendor.trim().equalsIgnoreCase( "dotgnu" ) )
                {
                    platformCapability.setVendor( Vendor.DOTGNU );
                }
                else
                {
                    System.out.println( "NMAVEN-067-001: Unknown Vendor, skipping: Name = " + compilerVendor );
                    continue;
                }
                platformCapabilities.add( platformCapability );
            }
        }
        return platformCapabilities;
    }
}
