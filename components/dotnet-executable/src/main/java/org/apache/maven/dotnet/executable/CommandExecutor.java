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
package org.apache.maven.dotnet.executable;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.cli.*;

import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
//import org.codehaus.plexus.util.cli.shell.BourneShell;
//import org.codehaus.plexus.util.cli.shell.CmdShell;
//import org.codehaus.plexus.util.cli.shell.CommandShell;
import org.codehaus.plexus.util.cli.shell.Shell;
import java.io.IOException;

  

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.File;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;


import java.lang.reflect.*;


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
            return new CommandExecutor()
            {
                /**
                 * Instance of a plugin logger.
                 */
                private Logger logger;

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

                public void setLogger( Logger logger )
                {
                    this.logger = logger;
                }


                public void executeCommand( String executable, List<String> commands )
                    throws ExecutionException
                {
                    executeCommand( executable, commands, null, true );
                }

                public void executeCommand( String executable, List<String> commands, boolean failsOnErrorOutput )
                    throws ExecutionException
                {
                    executeCommand( executable, commands, null, failsOnErrorOutput );
                }

                public void executeCommand( String executable, List<String> commands, File workingDirectory,
                                            boolean failsOnErrorOutput )
                    throws ExecutionException
                {
                    if ( commands == null )
                    {
                        commands = new ArrayList<String>();
                    }
                    stdOut = new StreamConsumerImpl();
                    stdErr = new ErrorStreamConsumer();

                    Commandline commandline = new Commandline()
                    {
                         protected Map envVars = Collections.synchronizedMap( new LinkedHashMap() );
                         
                         
                         public Process execute()
                             throws CommandLineException
                         {
                             // TODO: Provided only for backward compat. with <= 1.4
                             //verifyShellState();
                     
                             Process process;
                     
                             //addEnvironment( "MAVEN_TEST_ENVAR", "MAVEN_TEST_ENVAR_VALUE" );
                     
                             String[] environment = getEnvironmentVariables();
                             
                             File workingDir = getWorkingDirectory();
                     
                             try
                             {
                                 String cmd = this.toString();
                                 
                                 if ( workingDir == null )
                                 {
                                     //process = Runtime.getRuntime().exec( getShellCommandline(), environment );
                                     process = Runtime.getRuntime().exec( cmd, environment );
                                 }
                                 else
                                 {
                                     if ( !workingDir.exists() )
                                     {
                                         throw new CommandLineException( "Working directory \"" + workingDir.getPath()
                                             + "\" does not exist!" );
                                     }
                                     else if ( !workingDir.isDirectory() )
                                     {
                                         throw new CommandLineException( "Path \"" + workingDir.getPath()
                                             + "\" does not specify a directory." );
                                     }
                     
                                     //process = Runtime.getRuntime().exec( getShellCommandline(), environment, workingDir );
                                     process = Runtime.getRuntime().exec( cmd, environment, workingDir );
                                 }
                             }
                             catch ( IOException ex )
                             {
                                 throw new CommandLineException( "Error while executing process.", ex );
                             }
                     
                             return process;
                         }
                         
                         public String[] getEnvironmentVariables()
                             throws CommandLineException
                         {
                             try
                             {
                                 addSystemEnvironment();
                             }
                             catch ( Exception e )
                             {
                                 throw new CommandLineException( "Error setting up environmental variables", e );
                             }
                             String[] environmentVars = new String[envVars.size()];
                             int i = 0;
                             for ( Iterator iterator = envVars.keySet().iterator(); iterator.hasNext(); )
                             {
                                 String name = (String) iterator.next();
                                 String value = (String) envVars.get( name );
                                 environmentVars[i] = name + "=" + value;
                                 i++;
                             }
                             return environmentVars;
                         }
                         
                         
                         
                         public void addEnvironment( String name, String value )
                         {
                             //envVars.add( name + "=" + value );
                             envVars.put( name, value );
                         }
                     
                         /**
                                                                    * Add system environment variables
                                                                    */
                         public void addSystemEnvironment()
                             throws Exception
                        {
                             Properties systemEnvVars = CommandLineUtils.getSystemEnvVars();
                     
                             for ( Iterator i = systemEnvVars.keySet().iterator(); i.hasNext(); )
                             {
                                 String key = (String) i.next();
                                 if ( !envVars.containsKey( key ) )
                                 {
                                     addEnvironment( key, systemEnvVars.getProperty( key ) );
                                 }
                             }
                        }
                        
                        public String toString()
                        {
                            StringBuffer strBuff = new StringBuffer("");
                            for(String command : getShellCommandline())
                            {
                                strBuff.append(" ");
                                strBuff.append(escapeCmdParams(command));
                            }
                            return strBuff.toString().trim();
                        }
                        
                        // escaped to make use of dotnet style of command escapes .
                        // Eg. /define:"CONFIG=\"Debug\",DEBUG=-1,TRACE=-1,_MyType=\"Windows\",PLATFORM=\"AnyCPU\""
                        private String escapeCmdParams(String param)
                        {
                            if(param == null)
                                return null;
                            
                            String str = param;
                            if(param.startsWith("/") && param.indexOf(":") > 0)
                            {
                                int delem = param.indexOf(":") + 1;
                                String command = param.substring(0, delem);
                                String value = param.substring(delem);
                                
                                if(value.indexOf(" ") > 0 || value.indexOf("\"") > 0)
                                {
                                    value = "\"" + value.replaceAll("\"", "\\\\\"")  + "\"";
                                }
                                
                                str = command + value;
                            }
                            else if(param.indexOf(" ") > 0)
                            {
                                str = "\"" + param  + "\"";
                            }
                            
                            return str;
                        }




                    
                    
                    };
                    
                    
                    
                    commandline.setExecutable( executable );
                    commandline.addArguments( commands.toArray( new String[commands.size()]));
                    if ( workingDirectory != null && workingDirectory.exists() )
                    {
                        commandline.setWorkingDirectory( workingDirectory.getAbsolutePath() );
                    }
                    try
                    {
                        result = CommandLineUtils.executeCommandLine( commandline, stdOut, stdErr );
                        if ( logger != null )
                        {
                            logger.debug( "NMAVEN-040-000: Executed command: Commandline = " + commandline +
                                ", Result = " + result );
                        }
                        else
                        {
                            System.out.println( "NMAVEN-040-000: Executed command: Commandline = " + commandline +
                                ", Result = " + result );
                        }
                        if ( ( failsOnErrorOutput && stdErr.hasError() ) || result != 0 )
                        {
                            throw new ExecutionException( "NMAVEN-040-001: Could not execute: Command = " +
                                commandline.toString() + ", Result = " + result );
                        }
                    }
                    catch ( CommandLineException e )
                    {
                        throw new ExecutionException(
                            "NMAVEN-040-002: Could not execute: Command = " + commandline.toString() );
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

                /**
                 * Provides behavior for determining whether the command utility wrote anything to the Standard Error Stream.
                 * NOTE: I am using this to decide whether to fail the NMaven build. If the compiler implementation chooses
                 * to write warnings to the error stream, then the build will fail on warnings!!!
                 */
                class ErrorStreamConsumer
                    implements StreamConsumer
                {

                    /**
                     * Is true if there was anything consumed from the stream, otherwise false
                     */
                    private boolean error;

                    /**
                     * Buffer to store the stream
                     */
                    private StringBuffer sbe = new StringBuffer();

                    public ErrorStreamConsumer()
                    {
                        if ( logger == null )
                        {
                            System.out.println( "NMAVEN-040-003: Error Log not set: Will not output error logs" );
                        }
                        error = false;
                    }

                    public void consumeLine( String line )
                    {
                        sbe.append( line );
                        if ( logger != null )
                        {
                            logger.error( line );
                        }
                        error = true;
                    }

                    /**
                     * Returns false if the command utility wrote to the Standard Error Stream, otherwise returns true.
                     *
                     * @return false if the command utility wrote to the Standard Error Stream, otherwise returns true.
                     */
                    public boolean hasError()
                    {
                        return error;
                    }

                    /**
                     * Returns the error stream
                     *
                     * @return error stream
                     */
                    public String toString()
                    {
                        return sbe.toString();
                    }
                }

                /**
                 * StreamConsumer instance that buffers the entire output
                 */
                class StreamConsumerImpl
                    implements StreamConsumer
                {

                    private DefaultConsumer consumer;

                    private StringBuffer sb = new StringBuffer();

                    public StreamConsumerImpl()
                    {
                        consumer = new DefaultConsumer();
                    }

                    public void consumeLine( String line )
                    {
                        sb.append( line );
                        if ( logger != null )
                        {
                            consumer.consumeLine( line );
                        }
                    }

                    /**
                     * Returns the stream
                     *
                     * @return the stream
                     */
                    public String toString()
                    {
                        return sb.toString();
                    }
                }
            };

        }
    }
}
