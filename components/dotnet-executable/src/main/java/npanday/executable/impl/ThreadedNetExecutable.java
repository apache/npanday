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

import npanday.executable.NetExecutable;
import npanday.executable.ExecutableContext;
import npanday.executable.ExecutionException;
import npanday.executable.CommandFilter;
import npanday.executable.CommandExecutor;
import npanday.NPandayContext;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.io.File;

/**
 * Provides a service for executing a command within a separate thread. This will be used for executing long-running
 * processes.
 *
 * @author Shane Isbell
 */
public class ThreadedNetExecutable
    implements NetExecutable, Runnable
{
    private ExecutableContext executableContext;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    public void run()
    {
        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        try
        {
            commandExecutor.setLogger( logger );
            commandExecutor.executeCommand( getExecutable(), getCommands(), getExecutionPath(), true );
        }
        catch ( ExecutionException e )
        {
            //  throw new ExecutionException( "NPANDAY-063-000: Command = " + commands, e );
        }
        if ( commandExecutor.getStandardOut().contains( "error" ) )
        {
            //   t/w new ExecutionException( "NPANDAY-063-001: Command = " + commands );
        }
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        CommandFilter filter = executableContext.getCommandFilter();
        return filter.filter( executableContext.getExecutableConfig().getCommands() );
    }

    public void resetCommands( List<String> commands )
    {

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
                    logger.info("NPANDAY-063-005: Choose executable path for " + executable + ": " + executablePath);
                    return new File( executablePath );
                }
            }
        }
        logger.warn("NPANDAY-063-006: Did not find path for " + executable + " in " + executablePaths);
        return null;
    }

    public void execute()
        throws ExecutionException
    {
    }


    public String getExecutable()
        throws ExecutionException
    {
        if ( executableContext == null )
        {
            throw new ExecutionException( "NPANDAY-063-002: Executable has not been initialized with a context" );
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
    }
}
