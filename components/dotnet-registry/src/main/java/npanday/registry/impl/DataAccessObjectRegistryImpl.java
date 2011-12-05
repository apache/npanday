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

import npanday.registry.ConnectionsRepository;
import npanday.registry.DataAccessObject;
import npanday.registry.DataAccessObjectRegistry;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Component(role=DataAccessObjectRegistry.class)
public class DataAccessObjectRegistryImpl
    implements DataAccessObjectRegistry
{
   @Requirement
    private RepositoryRegistry repositoryRegistry;

    private static Logger logger = Logger.getAnonymousLogger();

    private boolean isConnectionsRepoLoaded = false;

    public Set<DataAccessObject> findAll()
    {
        Set<DataAccessObject> daos = new HashSet<DataAccessObject>();
        if ( repositoryRegistry == null )
        {
            logger.warning( "NPANDAY-081-000: Repository registry has not been set" );
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
                    catch ( NPandayRepositoryException e )
                    {
                        logger.warning( "NPANDAY-081-003: " + e.getMessage() );
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
            logger.warning( "NPANDAY-081-001: Repository registry has not been set" );
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
                    catch ( NPandayRepositoryException e )
                    {
                        logger.warning( "NPANDAY-081-004: " + e.getMessage() );
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
            logger.warning( "NPANDAY-081-002: Repository registry has not been set" );
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
                    catch ( NPandayRepositoryException e )
                    {
                        logger.warning( "NPANDAY-081-005: " + e.getMessage() );
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
