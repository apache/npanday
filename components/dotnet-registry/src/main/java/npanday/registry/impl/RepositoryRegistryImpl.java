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

import npanday.registry.RepositoryRegistry;
import npanday.registry.RepositoryLoader;
import npanday.registry.RegistryLoader;
import npanday.registry.Repository;
import npanday.registry.DataAccessObjectRegistry;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @author Shane Isbell
 */
public class RepositoryRegistryImpl
    implements RepositoryRegistry, Initializable
{

    private Hashtable repositories = new Hashtable();

    private RepositoryLoader repositoryLoader;

    private RegistryLoader registryLoader;


    /**
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        try
        {
            loadFromResource( "/META-INF/npanday/registry-config.xml", this.getClass() );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "NPANDAY-082-000: Message = ", e );
        }
    }

    public boolean isEmpty()
    {
        return repositories.isEmpty();
    }

    public synchronized void setRepositoryLoader( RepositoryLoader loader )
    {
        repositoryLoader = loader;
    }

    public synchronized void setRegistryLoader( RegistryLoader loader )
    {
        registryLoader = loader;
    }

    public synchronized void loadFromInputStream( InputStream inputStream )
        throws IOException
    {

        if ( repositoryLoader == null || registryLoader == null )
        {
            InputStream stream =
                npanday.registry.RepositoryRegistry.class.getResourceAsStream( "/registry.properties" );
            if ( stream == null )
            {
                throw new IOException( "NPANDAY-082-001: Could not find /registry.properties file with the jar" );
            }

            Properties prop = new Properties();
            prop.load( stream );

            if ( repositoryLoader == null )
            {
                String loaderClassName = prop.getProperty( "repositoryLoader" );
                if ( loaderClassName == null )
                {
                    throw new IOException( "NPANDAY-082-002: Missing the repositoryLoader from the /registry.properties" );
                }

                String message = "Repository Loader = " + loaderClassName;
                try
                {
                    Class c = Class.forName( loaderClassName );
                    repositoryLoader = (RepositoryLoader) c.newInstance();
                }
                catch ( Exception e )
                {
                    throw new IOException( "NPANDAY-082-003: Unable to load repository: " + message, e );
                }
                catch ( Error e )
                {
                    throw new IOException( "NPANDAY-082-004: Unable to load repository: " + message, e );
                }
            }

            if ( registryLoader == null )
            {
                String loaderClassName = prop.getProperty( "registryLoader" );
                if ( loaderClassName == null )
                {
                    throw new IOException( "NPANDAY-082-005: Missing the registryLoader from the /registry.properties" );
                }

                String message = "Registry Loader = " + loaderClassName;
                try
                {
                    Class c = Class.forName( loaderClassName );
                    registryLoader = (RegistryLoader) c.newInstance();
                }
                catch ( Exception e )
                {
                    throw new IOException( "NPANDAY-082-006: Unable to load registry: " + message, e );
                }
                catch ( Error e )
                {
                    throw new IOException( "NPANDAY-082-007: Unable to load registry: " + message, e );
                }
            }
        }
        repositoryLoader.setRepositoryRegistry( this );
        registryLoader.setRepositoryLoader( repositoryLoader );
        registryLoader.loadRegistry( inputStream );
        repositories.putAll( registryLoader.getRepositories() );
    }

    public synchronized void loadFromFile( String fileName )
        throws IOException
    {
        FileInputStream fis = new FileInputStream( fileName );
        loadFromInputStream( fis );
    }


    public synchronized void loadFromResource( String fileName, Class sourceClass )
        throws IOException
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

