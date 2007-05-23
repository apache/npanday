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

import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.executable.*;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Shane Isbell
 */
public class DefaultRepositoryNetExecutable
    implements NetExecutable
{

    private RepositoryExecutableContext executableContext;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    private List<String> commands;

    public List<String> getCommands()
        throws ExecutionException
    {
        return commands;
    }

    public void resetCommands( List<String> commands )
    {
        List<String> tmp = new ArrayList<String>();
        tmp.addAll(commands);
        this.commands = Collections.unmodifiableList( tmp );
    }

    public File getExecutionPath()
    {
        if ( executableContext == null )
        {
            logger.info( "NMAVEN-063-002: Executable has not been initialized with a context" );
            return null;
        }
        List<String> executablePaths = executableContext.getExecutableConfig().getExecutionPaths();
        if ( executablePaths != null )
        {
            for ( String executablePath : executablePaths )
            {
                File exe = new File( executablePath );
                if ( exe.exists() )
                {
                    return new File( executablePath ).getParentFile();
                }
            }
        }
        return null;
    }

    public void execute()
        throws ExecutionException
    {
        List<String> commands = getCommands();

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        try
        {
            commandExecutor.setLogger( logger );
            commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), true );
        }
        catch ( ExecutionException e )
        {
            throw new ExecutionException( "NMAVEN-063-000: Execution Path = " +
                ( ( getExecutionPath() != null ) ? getExecutionPath().getAbsolutePath() : "unknown" ) + ", Command = " +
                commands, e );
        }
        if ( commandExecutor.getStandardOut().contains( "error" ) )
        {
            throw new ExecutionException(
                "NMAVEN-063-001: Executable = " + getExecutable() + ",Command = " + commands );
        }
    }


    public String getExecutable()
        throws ExecutionException
    {
        if ( executableContext == null )
        {
            throw new ExecutionException( "NMAVEN-063-002: Executable has not been initialized with a context" );
        }

        List<String> executablePaths = executableContext.getExecutableConfig().getExecutionPaths();
        if ( executablePaths != null )
        {
            for ( String executablePath : executablePaths )
            {
                File exe = new File( executablePath );
                logger.debug( "NMAVEN-063-004: Checking executable path = " + exe.getAbsolutePath() );
                if ( exe.exists() )
                {
                    return new File( executablePath ).getName();
                }
                else if ( executablePath.equals( "mono" ) )
                {
                    return executablePath;
                }
            }
        }
        throw new ExecutionException( "NMAVEN-063-003: Executable path has not been set or is invalid" );
    }

    public Vendor getVendor()
    {
        try
        {
            return executableContext.getNetExecutable().getVendor();
        }
        catch ( ExecutionException e )
        {
            return Vendor.NULL;
        }
    }

    public void init( NMavenContext nmavenContext )
    {
        this.executableContext = (RepositoryExecutableContext) nmavenContext;
        this.logger = executableContext.getLogger();
        commands = Collections.unmodifiableList( executableContext.getExecutableConfig().getCommands() );
    }
}
