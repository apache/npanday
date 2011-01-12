package npanday.plugin.partcover;

/*
 * Copyright 2010
 *
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

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * Goal which generates the xml report of Part Cover
 *
 * @goal generate
 * 
 * @phase package
 */
public class PartCoverCompilerMojo
    extends AbstractPartCoverMojo
{
    /**
     * Location of the assembly files.
     * @parameter expression="${assemblyName}"
     * @required
     */
    private File assemblyName;
        
    /**
     * Directory where all reports will be generated.
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/partcover-reports"
     */
    private File outputDirectory;
    
    /**
     * The path where the nunit-console executable is located.
     * The version should be 2.5 up so that partcover will run successfully.
     *     
     * @parameter expression="${nUnit}" default-value="C:\\Program Files\\NUnit 2.5.7\\bin\\net-2.0\\nunit-console.exe"
     */
    private String nUnit;	
    
    /**
     * The path where the partcover executable is located.
     *
     * @parameter expression="${partCover}" default-value="C:\\Program Files\\PartCover\\PartCover .NET 4.0\\PartCover.exe"
     */
    private String partCover;
    
    /** 
     * The maven project
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;
    
    /** 
     * @parameter expression="${include}" default-value="[*]*"
     * @required
     */
    private String include;
    
    /**
     * @parameter expression="${exclude}" default-value="[nunit*]*"
     */
    private String exclude;
    
    public void execute()
        throws MojoExecutionException
    {
        try 
        {
            if ( !outputDirectory.exists() )
            {
                outputDirectory.mkdirs();
            }
            
            StringBuilder line = new StringBuilder();            
            line.append( " \"" ).append( partCover ).append( "\" --target \"" ).append( nUnit ).append( "\" --target-args " ).append( assemblyName );            

            if ( include != null && include.length() > 0 )
            {
                line.append( " --include " ).append( include );
            }
            if ( exclude != null && exclude.length() > 0 )
            {
                line.append( " --exclude " ).append( exclude );
            }                   
            
            line.append( " --output " ).append( outputDirectory ).append( "/coverage.xml" );
            
            int exitValue = executeCommandLine( line.toString() );
            
            // clean up 
            FileUtils.forceDeleteOnExit( new File( project.getBasedir(), "partcover.driver.log" ) );
            FileUtils.forceDeleteOnExit( new File( project.getBasedir(), "TestResult.xml" ) );
        }    
        catch ( ExecuteException e ) 
        {
            throw new MojoExecutionException( "Problem executing coverage", e );
        }        
        catch ( IOException e ) 
        {
            throw new MojoExecutionException( "Problem executing coverage", e );
        }    
   }
}