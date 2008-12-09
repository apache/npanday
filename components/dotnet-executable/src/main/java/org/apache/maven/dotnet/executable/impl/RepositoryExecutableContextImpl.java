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

import org.apache.maven.dotnet.executable.*;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.RepositoryNotFoundException;
import org.apache.maven.dotnet.InitializationException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.registry.Repository;

/**
 * Provides an implementation of the repository executable context.
 *
 * @author Shane Isbell
 */
public class RepositoryExecutableContextImpl
    implements RepositoryExecutableContext, LogEnabled
{

    private RepositoryRegistry repositoryRegistry;

    private NetExecutable netExecutable;

    private ExecutableConfig executableConfig;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public ExecutableConfig getExecutableConfig()
    {
        return executableConfig;
    }

    public NetExecutable getNetExecutable()
        throws ExecutionException
    {
        return netExecutable;
    }

    public Repository find( String repositoryName )
        throws RepositoryNotFoundException
    {
        Repository repository = repositoryRegistry.find( repositoryName );
        if ( repository == null )
        {
            throw new RepositoryNotFoundException(
                "NPANDAY-064-000: Could not find repository: Name = " + repositoryName );
        }
        return repository;
    }

    public void init( ExecutableConfig executableConfig )
        throws InitializationException
    {
        this.executableConfig = executableConfig;
        netExecutable = new DefaultRepositoryNetExecutable();
        netExecutable.init( this );
    }
}
