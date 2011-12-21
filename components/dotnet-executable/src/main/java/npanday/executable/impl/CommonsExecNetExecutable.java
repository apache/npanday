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

import npanday.NPandayContext;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.CommandExecutor;
import npanday.executable.CommandFilter;
import npanday.executable.ExecutableContext;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.executable.execution.CommonsExecCommandExecutor;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides the default implementation of the net executable.
 *
 * @author Shane Isbell
 */
public class CommonsExecNetExecutable
    implements NetExecutable
{

    protected ExecutableContext executableContext;

    /**
     * A logger for writing log messages
     */
    protected Logger logger;

    private Collection<String> commands;

    public List<String> getCommands() throws ExecutionException, PlatformUnsupportedException
    {
        // TODO: should it fail on unsupported commands?
        CommandFilter filter = executableContext.getCommandFilter();
        return Collections.unmodifiableList(filter.filter( commands ));
    }

    private String getExecutable()
    {
        String executable = executableContext.getExecutableName();

        Collection<String> executablePaths = executableContext.getProbingPaths();
        if ( executablePaths != null && executablePaths.size() > 0 )
        {
            for ( String executablePath : executablePaths )
            {
                File executableFile = PathUtil.getExecutable( new File(executablePath), executable );
                if ( executableFile != null )
                {
                    logger.info("NPANDAY-126-001: Found executable file for " + executable + ": \"" + executableFile + "\"");

                    return executableFile.getAbsolutePath();
                }
            }
        }
        logger.warn( "NPANDAY-126-002: Did not find path for " + executable + " in " + executablePaths );

        return executable;
    }

    public void execute()
        throws ExecutionException, PlatformUnsupportedException
    {
        List<String> commands = getCommands();

        CommandExecutor commandExecutor = new CommonsExecCommandExecutor();
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand( getExecutable(), commands, null, true );
    }

    public Vendor getVendor()
    {
        return executableContext.getVendor();
    }

    public void init( NPandayContext npandayContext )
    {
        this.executableContext = (ExecutableContext) npandayContext;
        this.logger = executableContext.getLogger();
        commands = executableContext.getCommands();
    }
}