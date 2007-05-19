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

import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.executable.CommandCapability;
import org.apache.maven.dotnet.executable.ExecutableCapability;
import org.apache.maven.dotnet.executable.compiler.CompilerCapability;
import org.apache.maven.dotnet.model.compiler.plugins.io.xpp3.CompilerPluginXpp3Reader;
import org.apache.maven.dotnet.model.compiler.plugins.CompilerPluginsModel;
import org.apache.maven.dotnet.model.compiler.plugins.CompilerPlugin;
import org.apache.maven.dotnet.model.compiler.plugins.Platform;
import org.apache.maven.dotnet.model.compiler.plugins.CommandFilter;

/**
 * Repository for reading and providing access to the compiler-plugins.xml config file.
 *
 * @author Shane Isbell
 */
public final class CompilerPluginsRepository
    implements Repository
{
    /**
     * List<org.apache.maven.dotnet.model.compiler.plugins.CompilerPlugin> of compiler plugins pulled from the 
     * compiler-plugins.xml file.
     */
    private List compilerPlugins;

    /**
     * @see Repository#load(java.io.InputStream, java.util.Hashtable)
     */
    public void load( InputStream inputStream, Hashtable properties )
        throws IOException
    {
        CompilerPluginXpp3Reader xpp3Reader = new CompilerPluginXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        CompilerPluginsModel plugins;
        try
        {
            plugins = xpp3Reader.read( reader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NMAVEN-062-000: Could not read plugins-compiler.xml" );
        }
        compilerPlugins = plugins.getCompilerPlugins();
    }

    /**
     * @see Repository#setRepositoryRegistry(org.apache.maven.dotnet.registry.RepositoryRegistry)
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
    }

    /**
     * Returns config information as list of platform capabilities.
     *
     * @return config information as list of platform capabilities.
     */
    List<ExecutableCapability> getCompilerCapabilities()
    {
        List<ExecutableCapability> platformCapabilities = new ArrayList<ExecutableCapability>();
        for ( Iterator i = compilerPlugins.iterator(); i.hasNext(); )
        {
            CompilerPlugin plugin = (CompilerPlugin) i.next();
            String language = plugin.getLanguage();
            String pluginClassName = plugin.getPluginClass();
            String executable = plugin.getExecutable();
            String compilerVendor = plugin.getVendor();
            String identifier = plugin.getIdentifier();
            String profile = plugin.getProfile();
            List<String> frameworkVersions = plugin.getFrameworkVersions();
            List<String> coreAssemblies = plugin.getAssemblies();
            String defaultAssemblyPath = plugin.getDefaultAssemblyPath();

            List platforms = plugin.getPlatforms();
            for ( Iterator j = platforms.iterator(); j.hasNext(); )
            {
                CompilerCapability platformCapability =
                    (CompilerCapability) CompilerCapability.Factory.createDefaultExecutableCapability();
                Platform platform = (Platform) j.next();
                String os = platform.getOperatingSystem();

                platformCapability.setLanguage( language );
                platformCapability.setOperatingSystem( os );
                platformCapability.setPluginClassName( pluginClassName );
                platformCapability.setExecutable( executable );
                platformCapability.setIdentifier( identifier );
                platformCapability.setFrameworkVersions( frameworkVersions );
                platformCapability.setProfile( profile );
                platformCapability.setAssemblyPath( defaultAssemblyPath );
                String arch = platform.getArchitecture();
                CommandFilter filter = plugin.getCommandFilter();
                platformCapability.setCoreAssemblies( coreAssemblies );

                platformCapability.setNetDependencyId( plugin.getNetDependencyId());

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
                    System.out.println( "NMAVEN-062-001: Unknown Vendor, skipping: Name = " + compilerVendor );
                    continue;
                }
                platformCapabilities.add( platformCapability );
            }
        }
        return platformCapabilities;
    }
}
