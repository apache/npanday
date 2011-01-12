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

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Goal which converts xml to html for code coverage reports.
 *
 * @goal convert
 * 
 * @phase package
 */
public class PartCoverConverterMojo
    extends AbstractPartCoverMojo
{ 
    
    /**
     * Directory where all reports will be generated.
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/partcover-reports"
     */
    private File outputDirectory;
     
    /**
     * The path where msxsl.exe file is located.
     * This executable converts the xml report to a more readable html report.
     * This should be added in the "C:/WINDOWS/msxsle.exe"
     *
     * @parameter expression="${msxsl}" default-value="C:\\WINDOWS\\msxsl.exe"
     */
    private String msxsl;
 
    public void execute()
        throws MojoExecutionException
    { 
        try 
        {

            // copy resources
            
            copyFileToOutputDirectory( "/partcover-report-by-class.xslt", "partcover-report-by-class.xslt");
            copyFileToOutputDirectory( "/common-footer.xslt", "common-footer.xslt" );
            copyFileToOutputDirectory( "/common-header.xslt", "common-header.xslt" );
            copyFileToOutputDirectory( "/common-partcover-report.xslt", "common-partcover-report.xslt" );
            copyFileToOutputDirectory( "/partcover-report-by-assembly.xslt", "partcover-report-by-assembly.xslt" );
            copyFileToOutputDirectory( "/coverage-report.css", "coverage-report.css" );
            
            // convert xml result using xslt;            
                      
            String line = " \"" + msxsl + "\" \"" + outputDirectory + "/coverage.xml\" \"" + outputDirectory
                + "/common-partcover-report.xslt\" -o  \"" + outputDirectory + "/coverage.html\"";


            int exitValue = executeCommandLine( line );
                    
            if ( exitValue != 0 )
            {
                throw new MojoExecutionException( "Problem executing coverage, return code " + exitValue );
            }
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
    
    private File copyFileToOutputDirectory( String resourcePath, String file )
        throws MojoExecutionException
    {
        URL resourceUrl = getClass().getResource( resourcePath );
        File outputFile = new File( outputDirectory, file );
        
        try
        {
            FileUtils.copyURLToFile( resourceUrl, outputFile );
        }
        catch ( IOException e )
        {
           throw new MojoExecutionException( "Unable to copy file to " + outputFile, e );
        }

        return outputFile;
    }
}
