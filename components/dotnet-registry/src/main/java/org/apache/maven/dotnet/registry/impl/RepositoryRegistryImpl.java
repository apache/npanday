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
package org.apache.maven.dotnet.registry.impl;

import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.registry.RepositoryLoader;
import org.apache.maven.dotnet.registry.RegistryLoader;
import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.DataAccessObjectRegistry;

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
            loadFromResource( "/META-INF/nmaven/registry-config.xml", this.getClass() );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "NMAVEN-000-000:", e );
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
                org.apache.maven.dotnet.registry.RepositoryRegistry.class.getResourceAsStream( "/registry.properties" );
            if ( stream == null )
            {
                throw new IOException( "JV-000-001: Could not find /registry.properties file with the jar" );
            }

            Properties prop = new Properties();
            prop.load( stream );

            if ( repositoryLoader == null )
            {
                String loaderClassName = prop.getProperty( "repositoryLoader" );
                if ( loaderClassName == null )
                {
                    throw new IOException( "JV-000-002: Missing the repositoryLoader from the /registry.properties" );
                }

                String message = "Repository Loader = " + loaderClassName;
                try
                {
                    Class c = Class.forName( loaderClassName );
                    repositoryLoader = (RepositoryLoader) c.newInstance();
                }
                catch ( Exception e )
                {
                    throw new IOException( "JV-000-003: " + e.toString() + " : " + message );
                }
                catch ( Error e )
                {
                    throw new IOException( "JV-000-004: " + e.toString() + " : " + message );
                }
            }

            if ( registryLoader == null )
            {
                String loaderClassName = prop.getProperty( "registryLoader" );
                if ( loaderClassName == null )
                {
                    throw new IOException( "JV-000-005: Missing the registryLoader from the /registry.properties" );
                }

                String message = "Registry Loader = " + loaderClassName;
                try
                {
                    Class c = Class.forName( loaderClassName );
                    registryLoader = (RegistryLoader) c.newInstance();
                }
                catch ( Exception e )
                {
                    throw new IOException( "JV-000-006: " + e.toString() + " : " + message );
                }
                catch ( Error e )
                {
                    throw new IOException( "JV-000-007: " + e.toString() + " : " + message );
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
            throw new IOException( "JV-000-008: The class cannot be null when loading from a resource" );
        }
        InputStream stream = sourceClass.getResourceAsStream( fileName );

        if ( stream == null )
        {
            throw new IOException( "JV-000-009: Could not locate resource: File Name = " + fileName );
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

