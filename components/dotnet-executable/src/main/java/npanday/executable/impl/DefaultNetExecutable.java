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
package npanday.executable.impl;

import npanday.executable.ExecutionException;
import npanday.executable.*;
import npanday.NPandayContext;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.File;

/**
 * Provides the default implementation of the net executable.
 *
 * @author Shane Isbell
 */
public class DefaultNetExecutable
    implements NetExecutable
{

    protected ExecutableContext executableContext;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    private List<String> commands;

    public List<String> getCommands()
        throws ExecutionException
    {
        CommandFilter filter = executableContext.getCommandFilter();
        return Collections.unmodifiableList(filter.filter( commands ));
    }

    public void resetCommands(List<String> commands)
    {
        this.commands = new ArrayList<String>();
        this.commands.addAll(commands);//TODO: should be unmodifiable here: fail on filter?
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
                File exe = new File( executablePath + File.separator + executable );
                if ( exe.exists() )
                {
                    return new File( executablePath );
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
            throw new ExecutionException( "NPANDAY-070-000: Execution Path = " +
                ( ( getExecutionPath() != null ) ? getExecutionPath().getAbsolutePath() : "unknown" ) + ", Command = " +
                commands, e );
        }
        
        // This check is too broad, as seen in Issue #9903
        // I have not been able to identify an error it is trying to catch that is not already reported by the exit code above
        //
        //if ( commandExecutor.getStandardOut().contains( "error" ) )
        //{
        //    throw new ExecutionException( "NPANDAY-070-001: Command = " + commands );
        //}
    }

    public String getExecutable()
        throws ExecutionException
    {
        if ( executableContext == null )
        {
            throw new ExecutionException( "NPANDAY-070-002: Executable has not been initialized with a context" );
        }
        return executableContext.getExecutableCapability().getExecutable();
    }

    public Vendor getVendor()
    {
        return executableContext.getExecutableCapability().getVendor();
    }

    public void init( NPandayContext npandayContext )
    {
        this.executableContext = (ExecutableContext) npandayContext;
        this.logger = executableContext.getLogger();
        commands = executableContext.getExecutableConfig().getCommands();
    }
}
