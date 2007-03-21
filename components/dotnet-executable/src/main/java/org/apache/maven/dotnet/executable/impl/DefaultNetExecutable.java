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
import org.apache.maven.dotnet.executable.*;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.io.File;

/**
 * @author Shane Isbell
 */
public class DefaultNetExecutable
    implements NetExecutable
{

    private ExecutableContext executableContext;

    private MavenProject project;

    private Logger logger;

    public List<String> getCommands()
        throws ExecutionException
    {
        CommandFilter filter = executableContext.getCommandFilter();
        return filter.filter( executableContext.getExecutableConfig().getCommands() );
    }

    public File getExecutionPath()
    {
        String executable;
        try
        {
            executable = getExecutable();
        }
        catch ( ExecutionException e )
        {
            return null;
        }
        List<String> executablePaths = executableContext.getExecutableConfig().getExecutionPaths();
        if ( executablePaths != null )
        {
            for ( String executablePath : executablePaths )
            {
                File exe = new File( executablePath + File.separator +  executable);
                if ( exe.exists() )
                {
                    return new File(executablePath);
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
            throw new ExecutionException( "NMAVEN-063-000: Command = " + commands, e );
        }
        if ( commandExecutor.getStandardOut().contains( "error" ) )
        {
            throw new ExecutionException( "NMAVEN-063-001: Command = " + commands );
        }
    }

    public String getExecutable()
        throws ExecutionException
    {
        if ( executableContext == null )
        {
            throw new ExecutionException( "NMAVEN-063-002: Executable has not been initialized with a context" );
        }
        return executableContext.getExecutableCapability().getExecutable();
    }

    public void init( NMavenContext nmavenContext )
    {
        this.executableContext = (ExecutableContext) nmavenContext;
        this.project = executableContext.getMavenProject();
        this.logger = executableContext.getLogger();
    }
}
