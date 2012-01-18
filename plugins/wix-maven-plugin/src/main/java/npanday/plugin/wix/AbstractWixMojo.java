package npanday.plugin.wix;

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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class AbstractWixMojo
    extends AbstractMojo
{
    /**
    * WiX extensions to use
    * @parameter
    */
    protected String[] extensions;

    /**
     * Arguments to pass to WiX executable as is
     * @parameter expression="${arguments}"
     */

    protected String arguments;

    /**
     * @parameter expression="${wix.home}" default-value="${env.WIX}"
     */
    private File wixHome;

    /**
     * Suppress schema validation of documents (performance boost)
     *
     * @parameter expression="${suppressSchemaValidation}"
     */
    private boolean suppressSchemaValidation;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            CommandLine commandLine = new CommandLine( getWixPath( getCommand() ) );

            if ( extensions != null )
            {
                for ( String ext : extensions )
                {
                    commandLine.addArgument( "-ext " + ext );
                }
            }

            if ( suppressSchemaValidation )
            {
                commandLine.addArgument( "-ss" );
            }

            if ( arguments != null )
            {
                commandLine.addArgument( arguments );
            }

            commandLine.addArguments( getArguments().toArray( new String[0] ) );

            getLog().info( "Executing " + commandLine );

            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute( commandLine );
            if ( exitValue != 0 )
            {
                throw new MojoExecutionException( "Problem executing " + getCommand() + ", return code " + exitValue );
            }
        }
        catch ( ExecuteException e )
        {
            throw new MojoExecutionException( "Problem executing " + getCommand(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Problem executing " + getCommand(), e );
        }
    }

    private String getWixPath( String name )
    {
         if ( wixHome != null )
         {
             return new File( new File( wixHome, "bin" ), name ).getAbsolutePath();
         }
         return name;
     }

    public abstract String getCommand();

    public abstract List<String> getArguments()
        throws MojoExecutionException;
}
