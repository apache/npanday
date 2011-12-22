package npanday.executable.execution;

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

import npanday.executable.CommandExecutor;
import npanday.executable.ExecutionException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class CommandExecutorSkeleton
    implements CommandExecutor
{
    /**
     * Instance of a plugin logger.
     */
    private Logger logger;

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }

    public void executeCommand( String executable, List<String> commands ) throws ExecutionException
    {
        executeCommand( executable, commands, null, true );
    }

    public void executeCommand( String executable, List<String> commands, boolean failsOnErrorOutput ) throws
        ExecutionException
    {
        executeCommand( executable, commands, null, failsOnErrorOutput );
    }

    public abstract void executeCommand(
        String executable, List<String> commands, File workingDirectory, boolean failsOnErrorOutput ) throws
        ExecutionException;

    public Logger getLogger()
    {
        if (logger == null)
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "implicit");

        return logger;
    }
}
