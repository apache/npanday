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
package npanday.registry.impl;

import com.google.common.base.Preconditions;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.RepositoryLoader;
import npanday.registry.RepositoryRegistry;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * The default repository loader. This class can be extended
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.registry.RepositoryLoader"
 */
public class StandardRepositoryLoader
    extends AbstractLogEnabled
    implements RepositoryLoader, Contextualizable
{
    /**
     * Can't be provided as a component, because that would result in a circular reference.
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The plexus context, which we can use to dynamically add components.
     */
    private PlexusContainer container;

    /**
     * Takes information from the registry-config file and dynamically builds a <code>Repository</code>
     *
     * @param location        name of the repository's configuration file. It may be located on the file system
     *                        or on the current threads classpath. If multiple files are found on the classpath,
     *                        their contents will be merged by the corresponding repository implementation.
     * @param repositoryClass name of the repository class
     * @param initParams      <code>Hashtable</code> containing the repository's configuration parameters.
     * @return instance of repository
     * @throws java.io.IOException
     */
    public Repository loadRepository( String location, String repositoryClass, Hashtable initParams )
        throws IOException, NPandayRepositoryException
    {
        Preconditions.checkNotNull( repositoryRegistry, "NPANDAY-084-000: The repository registry has not been set." );
        Preconditions.checkNotNull( container, "NPANDAY-084-012: The context has not been set; make sure this instance is loaded as a component." );

        Hashtable props = ( initParams != null ) ? initParams : new Hashtable();

        if ( location == null || location.trim().equals( "" ) )
        {
            throw new IOException( "NPANDAY-084-001: File uri must be provided." );
        }
        if ( repositoryClass == null || repositoryClass.trim().equals( "" ) )
        {
            throw new IOException(
                "NPANDAY-084-002: Repository class name must be provided: File Name = " + location + ", Properties = "
                    + props.toString() );
        }

        final Set<URL> sources = findSources( location );

        boolean optional = "true".equalsIgnoreCase( (String) initParams.get( "optional" ) );

        Repository repository = initializeRepository( repositoryClass, props );

        if ( sources.size() == 0 && !optional )
        {
            throw new IOException(
                String.format( "NPANDAY-084-003: Unable to find any repository source files or resources named: %s",
                               location ) );
        }
        else{
            loadFoundSources( location, repositoryClass, props, sources, repository );
        }

        return repository;
    }

    private Set<URL> findSources( String location )
        throws IOException
    {
        final Set<URL> sources = new HashSet<URL>();

        final File locationAsFile = new File( location );
        if ( locationAsFile.exists() )
        {
            final URL fileUrl = locationAsFile.toURI().toURL();
            getLogger().debug(
                String.format( "NPANDAY-084-007: Searched file with '%s', found: [%s]", location, fileUrl ) );
            sources.add( fileUrl );
        }
        else
        {
            // The Class Loader used in Maven 3 doesn't find anything if we have a leading slash
            String classPathLocation = ( location.startsWith( "/" ) ) ? location.substring( 1 ) : location;

            ClassLoader cloader = getClass().getClassLoader();
            final ArrayList<URL> currentClassPath = Collections.list( cloader.getResources( classPathLocation ) );
            sources.addAll( currentClassPath );

            getLogger().debug( String.format( "NPANDAY-084-012: Searched dotnet-core classpath with '%s', found: [%s]",
                                              classPathLocation, sources ) );
        }

        return sources;
    }

    private Repository initializeRepository( String repositoryClass, Hashtable props )
        throws NPandayRepositoryException
    {
        Repository repository;
        try
        {
            repository = (Repository) container.lookup( repositoryClass );
            repository.setProperties( props );
        }
        catch ( Exception e )
        {
            throw new NPandayRepositoryException(
                String.format( "NPANDAY-084-005: Error on initializing %s ", repositoryClass ), e );
        }
        catch ( Error e )
        {
            throw new NPandayRepositoryException(
                String.format( "NPANDAY-084-011: Error on initializing %s ", repositoryClass ), e );
        }
        return repository;
    }

    private void loadFoundSources( String location, String repositoryClass, Hashtable props, Set<URL> sources,
                                   Repository repository )
        throws NPandayRepositoryException
    {
        for ( URL source : sources )
        {
            getLogger().debug(
                String.format( "NPANDAY-084-009: loading '%s' into the repository %s", location, repositoryClass ) );

            try
            {
                getLogger().debug( String.format( "NPANDAY-084-009: loading '%s' into the repository %s", location,
                                                  repositoryClass ) );

                repository.load( source );
            }
            catch ( NPandayRepositoryException e )
            {
                throw new NPandayRepositoryException(
                    String.format( "NPANDAY-084-010: Error on loading '%s' into the repository %s", location,
                                   repositoryClass ) , e);

            }
        }
    }

    public String getLoaderName()
    {
        return this.getClass().getName();
    }

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = repositoryRegistry;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}

