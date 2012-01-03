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

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class CustomPlexusUtilsCommandline
    extends Commandline
{
    protected Map envVars = Collections.synchronizedMap( new LinkedHashMap() );

    public Process execute() throws CommandLineException
    {
        Process process;

        String[] environment = getEnvironmentVariables();

        File workingDir = getWorkingDirectory();

        try
        {
            String[] cmd = getEscapedShellCommandline();

            if ( workingDir == null )
            {
                process = Runtime.getRuntime().exec( cmd, environment );
            }
            else
            {
                if ( !workingDir.exists() )
                {
                    throw new CommandLineException(
                        "NPANDAY-040-010: Working directory \"" + workingDir.getPath() + "\" does not exist!"
                    );
                }
                else if ( !workingDir.isDirectory() )
                {
                    throw new CommandLineException(
                        "NPANDAY-040-009: Path \"" + workingDir.getPath() + "\" does not specify a directory."
                    );
                }

                process = Runtime.getRuntime().exec( cmd, environment, workingDir );
            }
        }
        catch ( IOException ex )
        {
            throw new CommandLineException( "NPANDAY-040-008: Error while executing process.", ex );
        }

        return process;
    }

    public String[] getEnvironmentVariables() throws CommandLineException
    {
        try
        {
            addSystemEnvironment();
        }
        catch ( Exception e )
        {
            throw new CommandLineException(
                "NPANDAY-040-007: Error setting up environmental variables", e
            );
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
        envVars.put( name, value );
    }

    /**
     * Add system environment variables
     */
    public void addSystemEnvironment() throws Exception
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
        StringBuffer strBuff = new StringBuffer( "" );
        for ( String command : getEscapedShellCommandline() )
        {
            strBuff.append( " " );
            strBuff.append( command );
        }
        return strBuff.toString();
    }

    public String[] getEscapedShellCommandline()
    {
        String[] scl = getShellCommandline();
        for ( int i = 0; i < scl.length; i++ )
        {
            scl[i] = escapeCmdParams( scl[i] );
        }
        return scl;
    }

    // escaped to make use of dotnet style of command escapes .
    // Eg. /define:"CONFIG=\"Debug\",DEBUG=-1,TRACE=-1,_MyType=\"Windows\",PLATFORM=\"AnyCPU\""
    private String escapeCmdParams( String param )
    {
        if ( param == null )
        {
            return null;
        }

        String str = param;
        if ( param.startsWith( "/" ) && param.indexOf( ":" ) > 0 )
        {
            int delem = param.indexOf( ":" ) + 1;
            String command = param.substring( 0, delem );
            String value = param.substring( delem );

            if ( value.indexOf( " " ) > 0 || value.indexOf( "\"" ) > 0 )
            {
                value = "\"" + value.replaceAll( "\"", "\\\\\"" ) + "\"";
            }

            str = command + value;
        }
        else if ( param.startsWith( "@" ) )
        {
            str = param;
        }

        return str;
    }
}
