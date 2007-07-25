package org.apache.maven.dotnet.registry.impl;

import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.registry.DataAccessObject;
import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.DataAccessObjectRegistry;
import org.apache.maven.dotnet.registry.ConnectionsRepository;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.logging.Logger;
import java.io.IOException;

public class DataAccessObjectRegistryImpl
    implements DataAccessObjectRegistry
{

    private RepositoryRegistry repositoryRegistry;

    private static Logger logger = Logger.getAnonymousLogger();

    private boolean isConnectionsRepoLoaded = false;

    public Set<DataAccessObject> findAll()
    {
        Set<DataAccessObject> daos = new HashSet<DataAccessObject>();
        if ( repositoryRegistry == null )
        {
            logger.warning( "Repository registry has not been set" );
            return Collections.unmodifiableSet( daos );
        }
        for ( String name : repositoryRegistry.getRepositoryNames() )
        {
            Repository repository = repositoryRegistry.find( name );
            if ( repository instanceof ConnectionsRepository )
            {
                ConnectionsRepository connectionsRepository = (ConnectionsRepository) repository;
                if ( !isConnectionsRepoLoaded )
                {
                    try
                    {
                        connectionsRepository.lazyLoad();
                        isConnectionsRepoLoaded = true;
                    }
                    catch ( IOException e )
                    {
                        return daos;
                    }
                }
                daos.addAll( ( (ConnectionsRepository) repository ).getDataAccessObjects() );
            }
        }
        return Collections.unmodifiableSet( daos );
    }

    public DataAccessObject find( String daoId )
    {
        if ( repositoryRegistry == null )
        {
            logger.warning( "Repository registry has not been set" );
            return null;
        }
        for ( String name : repositoryRegistry.getRepositoryNames() )
        {
            Repository repository = repositoryRegistry.find( name );
            if ( repository instanceof ConnectionsRepository )
            {
                ConnectionsRepository connectionsRepository = (ConnectionsRepository) repository;
                if ( !isConnectionsRepoLoaded )
                {
                    try
                    {
                        connectionsRepository.lazyLoad();
                        isConnectionsRepoLoaded = true;
                    }
                    catch ( IOException e )
                    {
                        return null;
                    }
                }
                for ( DataAccessObject dao : ( connectionsRepository ).getDataAccessObjects() )
                {
                    if ( dao.getID().trim().equals( daoId ) )
                    {
                        return dao;
                    }
                }
            }
        }
        return null;
    }

    public Set<String> getDaoIds()
    {
        Set<String> daoIds = new HashSet<String>();
        if ( repositoryRegistry == null )
        {
            logger.warning( "Repository registry has not been set" );
            return Collections.unmodifiableSet( daoIds );
        }
        for ( String name : repositoryRegistry.getRepositoryNames() )
        {
            Repository repository = repositoryRegistry.find( name );
            if ( repository instanceof ConnectionsRepository )
            {
                ConnectionsRepository connectionsRepository = (ConnectionsRepository) repository;
                if ( !isConnectionsRepoLoaded )
                {
                    try
                    {
                        connectionsRepository.lazyLoad();
                        isConnectionsRepoLoaded = true;
                    }
                    catch ( IOException e )
                    {
                        return Collections.unmodifiableSet( daoIds );
                    }
                }
                for ( DataAccessObject dao : ( (ConnectionsRepository) repository ).getDataAccessObjects() )
                {
                    daoIds.add( dao.getID() );
                }
            }
        }
        return Collections.unmodifiableSet( daoIds );
    }

}
