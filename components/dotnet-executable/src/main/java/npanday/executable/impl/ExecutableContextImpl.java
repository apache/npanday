package npanday.executable.impl;

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

import npanday.executable.*;
import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import npanday.PlatformUnsupportedException;
import npanday.RepositoryNotFoundException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LogEnabled;

/**
 * Provides an implementation of the executable context.
 *
 * @author Shane Isbell
 */
public class ExecutableContextImpl
    implements ExecutableContext, LogEnabled
{

    private ExecutableRequirement executableRequirement;

    private RepositoryRegistry repositoryRegistry;

    private ExecutableCapability executableCapability;

    private NetExecutable netExecutable;

    private CommandFilter commandFilter;

    private ExecutableConfig executableConfig;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

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

    public ExecutableCapability getExecutableCapability()
    {
        return executableCapability;
    }

    public ExecutableRequirement getExecutableRequirement()
    {
        return executableRequirement;
    }

    public NetExecutable getNetExecutable()
        throws ExecutionException
    {
        return netExecutable;
    }

    public CommandFilter getCommandFilter()
    {
        return commandFilter;
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

    public void init( ExecutableRequirement executableRequirement, ExecutableConfig executableConfig,
                      CapabilityMatcher capabilityMatcher )
        throws PlatformUnsupportedException
    {
        this.executableRequirement = executableRequirement;
        this.executableConfig = executableConfig;

        executableCapability = capabilityMatcher.matchExecutableCapabilityFor( executableRequirement );
        String className = executableCapability.getPluginClassName();

        try
        {
            Class cc = Class.forName( className );
            netExecutable = (NetExecutable) cc.newInstance();
            netExecutable.init( this );
        }
        catch ( ClassNotFoundException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-064-001: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( InstantiationException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-064-002: Unable to create NetCompiler: Class Name = " + className, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-064-003: Unable to create NetCompiler: Class Name = " + className, e );
        }
        commandFilter =
            CommandFilter.Factory.createDefaultCommandFilter( executableCapability.getCommandCapability(), logger );
    }
}
