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

import npanday.assembler.AssemblyInfoException;
import npanday.model.assembly.plugins.AssemblyPlugin;
import npanday.model.assembly.plugins.AssemblyPluginsModel;
import npanday.model.assembly.plugins.io.xpp3.AssemblyPluginXpp3Reader;
import npanday.registry.ModelInterpolator;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.impl.AbstractMultisourceRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Provides a way for loading the assembly-plugins.xml file and accessing its content.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component
 *   role="npanday.assembler.impl.AssemblyPluginsRepository"
 */
public final class AssemblyPluginsRepository
    extends AbstractMultisourceRepository<AssemblyPluginsModel>
    implements Repository
{

    /**
     * List of all assembly plugins within the repository
     */
    private List<AssemblyPlugin> assemblyPlugins = new ArrayList<AssemblyPlugin>( );

    /**
     * Constructor. This method is intended to by invoked by the <code>RepositoryRegistry<code>, not by the
     * application developer.
     */
    public AssemblyPluginsRepository()
    {
    }

    @Override
    protected AssemblyPluginsModel loadFromReader( Reader reader, Hashtable properties )
        throws IOException, XmlPullParserException
    {
        AssemblyPluginXpp3Reader xpp3Reader = new AssemblyPluginXpp3Reader();
        return xpp3Reader.read( reader );
    }

    @Override
    protected void mergeLoadedModel( AssemblyPluginsModel model )
        throws NPandayRepositoryException
    {
        assemblyPlugins.addAll( model.getAssemblyPlugins());
        Set languages = getAssemblyPluginLanguages();
        if ( languages.size() < assemblyPlugins.size() )
        {
            throw new NPandayRepositoryException(
                "NPANDAY-021-002: Duplicate language entries in the assembly-plugins.xml: Total Language Count = "
                    + languages.size() + ", Total Plugins = " + assemblyPlugins.size() );
        }
    }

    /**
     * Remove all stored values in preparation for a reload.
     */
    @Override
    protected void clear()
    {
        assemblyPlugins.clear();
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
