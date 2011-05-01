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
package npanday.assembler.impl;

import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.*;

import npanday.assembler.AssemblyInfoException;
import npanday.model.assembly.plugins.AssemblyPlugin;
import npanday.model.assembly.plugins.AssemblyPluginsModel;
import npanday.model.assembly.plugins.io.xpp3.AssemblyPluginXpp3Reader;

/**
 * Provides a way for loading the assembly-plugins.xml file and accessing its content.
 *
 * @author Shane Isbell
 */
public final class AssemblyPluginsRepository
    implements Repository
{

    /**
     * List of all assembly plugins within the repository
     */
    private List<AssemblyPlugin> assemblyPlugins;

    /**
     * Constructor. This method is intended to by invoked by the <code>RepositoryRegistry<code>, not by the
     * application developer.
     */
    public AssemblyPluginsRepository()
    {
    }

    /**
     * Loads the repository.
     *
     * @param inputStream a stream of the repository file (typically from *.xml)
     * @param properties  additional user-supplied parameters used to customize the behavior of the repository
     * @throws IOException if there is a problem loading the repository
     */
    public void load( InputStream inputStream, Hashtable properties )
        throws IOException
    {
        AssemblyPluginXpp3Reader xpp3Reader = new AssemblyPluginXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        AssemblyPluginsModel plugins = null;
        try
        {
            plugins = xpp3Reader.read( reader );
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( "NPANDAY-021-000: Could not read plugins-compiler.xml" );
        }
        assemblyPlugins = plugins.getAssemblyPlugins();
        Set languages = getAssemblyPluginLanguages();
        if ( languages.size() < assemblyPlugins.size() )
        {
            throw new IOException(
                "NPANDAY-021-001: Duplicate language entries in the assembly-plugins.xml: Total Language Count = " +
                    languages.size() + ", Total Plugins = " + assemblyPlugins.size() );
        }
    }

    /**
     * @see Repository#setRepositoryRegistry(npanday.registry.RepositoryRegistry)
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
    }

    /**
     * @see Repository#setSourceUri(String)
     */
    public void setSourceUri( String fileUri )
    {
        // not supported
    }

    /**
     * @see Repository#reload()
     */
    public void reload() throws IOException
    {
        // not supported
    }

    /**
     * Returns all assembly plugins. This list is live and any modification will remain until the next time the application
     * starts.
     *
     * @return all assembly plugins.
     */
    public List<AssemblyPlugin> getAssemblyPlugins()
    {
        return assemblyPlugins;
    }

    /**
     * Returns an assembly plugin for the specified programming language.
     *
     * @param language the programming language to use for matching an assembly plugin
     * @return assembly plugin for the specified programming language. May not be null.
     * @throws AssemblyInfoException if there is no plugin for the specified language
     */
    public AssemblyPlugin getAssemblyPluginFor( String language )
        throws AssemblyInfoException
    {
        for ( AssemblyPlugin assemblyPlugin : assemblyPlugins )
        {
            if ( assemblyPlugin.getLanguage().trim().equals( language ) )
            {
                return assemblyPlugin;
            }
        }
        throw new AssemblyInfoException( "NPANDAY-022-002: Unable to locate AssemblyPlugin: Language = " + language );
    }

    /**
     * Returns a set of all supported languages.
     *
     * @return a set of all supported languages
     */
    private Set<String> getAssemblyPluginLanguages()
    {
        Set<String> set = new HashSet<String>();

        for ( AssemblyPlugin assemblyPlugin : assemblyPlugins )
        {
            set.add( assemblyPlugin.getLanguage().trim() );
        }
        return set;
    }
}
