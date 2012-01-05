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

import npanday.NPandayContext;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.CommandExecutor;
import npanday.executable.CommandFilter;
import npanday.executable.ExecutableContext;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.vendor.Vendor;
import org.codehaus.plexus.logging.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
    protected Logger logger;

    private Collection<String> commands;

    private Properties configuration;

    public List<String> getCommands() throws ExecutionException, PlatformUnsupportedException
    {
        // TODO: should it fail on unsupported commands?
        CommandFilter filter = executableContext.getCommandFilter();
        return Collections.unmodifiableList( filter.filter( commands ) );
    }

    public void execute() throws ExecutionException, PlatformUnsupportedException
    {
        innerExecute();
    }

    public void innerExecute() throws ExecutionException, PlatformUnsupportedException
    {
        List<String> commands = getCommands();

        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor((String)configuration.get( "switchformats" ));
        commandExecutor.setLogger( logger );
        commandExecutor.executeCommand(
            PathUtil.getExecutable(
                executableContext.getExecutableName(), executableContext.getProbingPaths(), logger
            ), commands, null, true
        );
    }

    public Vendor getVendor()
    {
        return executableContext.getVendor();
    }

    public void init( NPandayContext npandayContext, Properties properties )
    {
        configuration = properties;
        this.executableContext = (ExecutableContext) npandayContext;
        this.logger = executableContext.getLogger();
        commands = executableContext.getCommands();
    }
}