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

import npanday.registry.NPandayRepositoryException;
import npanday.registry.RegistryLoader;
import npanday.registry.Repository;
import npanday.registry.RepositoryLoader;
import npanday.registry.RepositoryRegistry;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

/**
 * @author Shane Isbell
 * @plexus.component
 *   role="npanday.registry.RepositoryRegistry"
 */
public class RepositoryRegistryImpl
    extends AbstractLogEnabled
    implements RepositoryRegistry, Initializable
{
    private static int instanceCounter = 0;
    private int instance;

    private Hashtable repositories = new Hashtable();

    /**
     * @plexus.requirement
     */
    private RepositoryLoader repositoryLoader;

    /**
     * @plexus.requirement
     */
    private RegistryLoader registryLoader;

    public RepositoryRegistryImpl(){
        instance = instanceCounter++;
    }

    /**
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        getLogger().debug( "NPANDAY-082-011: Initializing RepositoryRegistry #" + instance );

        try
        {
            loadFromResource( "/META-INF/npanday/registry-config.xml", this.getClass() );
        }
        catch ( IOException e )
        {
            throw new InitializationException(
                "NPANDAY-082-000: Error loading registry-config.xml or one of the configured repositories", e );
        }
        catch ( NPandayRepositoryException e )
        {
            throw new InitializationException( "NPANDAY-082-010: Error loading registry-config.xml = ", e );
        }
    }

    public boolean isEmpty()
    {
        return repositories.isEmpty();
    }

    public synchronized void loadFromInputStream( InputStream inputStream )
        throws IOException, NPandayRepositoryException
    {

        repositoryLoader.setRepositoryRegistry( this );
        registryLoader.setRepositoryLoader( repositoryLoader );
        registryLoader.loadRegistry( inputStream );
        repositories.putAll( registryLoader.getRepositories() );
    }

    public synchronized void loadFromFile( String fileName )
        throws IOException, NPandayRepositoryException
    {
        FileInputStream fis = new FileInputStream( fileName );
        loadFromInputStream( fis );
    }


    public synchronized void loadFromResource( String fileName, Class sourceClass )
        throws IOException, NPandayRepositoryException
    {
        if ( sourceClass == null )
        {
            throw new IOException( "NPANDAY-082-008: The class cannot be null when loading from a resource" );
        }
        InputStream stream = sourceClass.getResourceAsStream( fileName );

        if ( stream == null )
        {
            throw new IOException( "NPANDAY-082-009: Could not locate resource: File Name = " + fileName );
        }
        loadFromInputStream( stream );
    }

    public synchronized void addRepository( String name, Repository repository )
    {
        repositories.put( name, repository );
    }


    public synchronized Repository find( String name )
    {
        return (Repository) repositories.get( name );
    }

    public synchronized void removeRepository( String name )
    {
        repositories.remove( name );
    }

    public synchronized Set getRepositoryNames()
    {
        return Collections.unmodifiableSet( repositories.keySet() );
    }

    public synchronized void empty()
    {
        repositories.clear();
    }
}

