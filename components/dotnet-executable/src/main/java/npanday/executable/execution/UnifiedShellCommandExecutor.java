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

package npanday.executable.execution;

import npanday.executable.ExecutionException;
import npanday.executable.execution.shells.ExtendedBourneShell;
import npanday.executable.execution.shells.ExtendedCmdShell;
import npanday.executable.execution.shells.ExtendedCommandShell;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A new unified approach to shell execution introduced in NPanday 1.5.0.
 *
 * <h2>Design considerations</h2>
 * <ul>
 * <li>In previous versions (< 1.5.0) the working directory was misused as the place
 * where the executable live. That lead to various problems.<br/>
 * Now, if we can locate the executable, it will be referenced to in an absolute way.
 * The working directory can then be used for what it should be.</li>
 * <li><b>Escaping strategies:</b> We need understand the commandline in order to choose the
 * correct quoting/quoting.</li>
 * </ul>
 *
 * <h2>Considered Issues</h2>
 * <ul>
 * <li><a href="https://issues.apache.org/jira/browse/NPANDAY-409">NPANDAY-409: Executable path ignored during
 * command execution while building NPanday on Linux</a></li>
 * <li><a href="https://issues.apache.org/jira/browse/NPANDAY-509">NPANDAY-509: CommandExecutor is confused with
 * MSDeploy-style commandline switches starting with "-" and containing both ":" and "="</a></li>
 * <li><a href="https://issues.apache.org/jira/browse/NPANDAY-341">NPANDAY-341: command execution fails if paths with
 * space</a></li>
 * <li><a href="https://issues.apache.org/jira/browse/NPANDAY-500">NPANDAY-500: potential compilation failures if
 * path contains a space</a></li>
 * <li><a href="https://issues.apache.org/jira/browse/NPANDAY-341">NPANDAY-341: command execution fails if paths with
 * space </a></li>
 * </ul>
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class UnifiedShellCommandExecutor
    extends CommandExecutorSkeleton
{
    private ArgumentQuotingStrategy quotingStrategy;

    public UnifiedShellCommandExecutor(ArgumentQuotingStrategy quotingStrategy){

        this.quotingStrategy = quotingStrategy;
    }

    /**
     * Standard Out
     */
    private StreamConsumer stdOut;

    /**
     * Standard Error
     */
    private ErrorStreamConsumer stdErr;

    /**
     * Process result
     */
    private int result;

    @Override
    public void executeCommand(
        String executable, List<String> commands, File workingDirectory, boolean failsOnErrorOutput )
        throws ExecutionException
    {
        if ( commands == null )
        {
            commands = new ArrayList<String>();
        }
        stdOut = new StandardStreamConsumer( getLogger() );
        stdErr = new ErrorStreamConsumer( getLogger() );

        Commandline commandline = new Commandline( getExtendedShell());

        commandline.setExecutable( executable );
        commandline.addArguments( commands.toArray( new String[commands.size()] ) );

        if ( workingDirectory != null )
        {
            commandline.setWorkingDirectory( workingDirectory.getAbsolutePath() );
        }

        boolean done = false;
        try
        {
            getLogger().info( " +--[ RUNNING: " + commandline.toString()  + "]");

            result = CommandLineUtils.executeCommandLine( commandline, stdOut, stdErr );

            if ( ( failsOnErrorOutput && stdErr.hasError() ) || result != 0 )
            {
                throw new ExecutionException(
                    "NPANDAY-040-001: Could not execute: Command = " + commandline.toString() + ", Result = " + result
                );
            }

            getLogger().info( " +--[ DONE ]");
            done = true;
        }
        catch ( CommandLineException e )
        {
            throw new ExecutionException(
                "NPANDAY-040-002: Could not execute: Command = " + commandline.toString(), e
            );
        }
        finally {
            if(!done){
                getLogger().info( " +--[ FAILED, result = " + result + ", error output = " + stdErr.hasError() + "]");
            }
        }
    }

    private Shell getExtendedShell()
    {
        // Workaround for https://jira.codehaus.org/browse/PLXUTILS-147

        //If this is windows set the shell to command.com or cmd.exe with correct arguments.
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            if ( Os.isFamily( Os.FAMILY_WIN9X ) )
            {
                return new ExtendedCommandShell(quotingStrategy);
            }
            else
            {
                return new ExtendedCmdShell(quotingStrategy);
            }
        }
        else
        {
            return new ExtendedBourneShell(quotingStrategy);
        }
    }

    public int getResult()
    {
        return result;
    }

    public String getStandardOut()
    {
        return stdOut.toString();
    }

    public String getStandardError()
    {
        return stdErr.toString();
    }
}
