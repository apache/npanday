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
package npanday.executable;

import npanday.executable.execution.DefaultCommandExecutor;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.List;


/**
 * Provides services for executing commands (executables or compilers). A <code>NetExecutable</code> or
 * <code>CompilerExecutable</code> implementation can use the services of this interface for executing commands.
 *
 * @author Shane Isbell
 */
public interface CommandExecutor
{
    /**
     * Sets the plexus logger.
     *
     * @param logger the plexus logger
     */
    void setLogger( Logger logger );

    /**
     * Executes the command for the specified executable and list of command options.
     *
     * @param executable the name of the executable (csc, xsd, etc).
     * @param commands   the command options for the compiler/executable
     * @throws ExecutionException if compiler or executable writes anything to the standard error stream or if the process
     *                            returns a process result != 0.
     */
    void executeCommand( String executable, List<String> commands )
        throws ExecutionException;

    /**
     * Executes the command for the specified executable and list of command options.
     *
     * @param executable         the name of the executable (csc, xsd, etc).
     * @param commands           the commands options for the compiler/executable
     * @param failsOnErrorOutput if true, throws an <code>ExecutionException</code> if there the compiler or executable
     *                           writes anything to the error output stream. By default, this value is true
     * @throws ExecutionException if compiler or executable writes anything to the standard error stream (provided the
     *                            failsOnErrorOutput is not false) or if the process returns a process result != 0.
     */
    void executeCommand( String executable, List<String> commands, boolean failsOnErrorOutput )
        throws ExecutionException;

    /**
     * Executes the command for the specified executable and list of command options. If the compiler or executable is
     * not within the environmental path, you should use this method to specify the working directory. Always use this
     * method for executables located within the local maven repository.
     *
     * @param executable       the name of the executable (csc, xsd, etc).
     * @param commands         the command options for the compiler/executable
     * @param workingDirectory the directory where the command will be executed
     * @throws ExecutionException if compiler or executable writes anything to the standard error stream (provided the
     *                            failsOnErrorOutput is not false) or if the process returns a process result != 0.
     */
    void executeCommand( String executable, List<String> commands, File workingDirectory, boolean failsOnErrorOutput )
        throws ExecutionException;

    /**
     * Returns the process result of executing the command. Typically a value of 0 means that the process executed
     * successfully.
     *
     * @return the process result of executing the command
     */
    int getResult();

    /**
     * Returns the standard output from executing the command.
     *
     * @return the standard output from executing the command
     */
    String getStandardOut();

    /**
     * Returns the standard error from executing the command.
     *
     * @return the standard error from executing the command
     */
    String getStandardError();

    /**
     * Provides factory services for creating a default instance of the command executor.
     */
    public static class Factory
    {

        /**
         * Constructor
         */
        private Factory()
        {
        }

        /**
         * Returns a default instance of the command executor
         *
         * @return a default instance of the command executor
         */
        public static CommandExecutor createDefaultCommmandExecutor()
        {
            return new DefaultCommandExecutor();
        }

    }

}
