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
     * This executable enables you to perform command line xsl transformations using the MS XSL processor
     * This should be added in the "C:/WINDOWS/"
     *
     * @parameter expression="${msxsl}" default-value="C:\\WINDOWS\\msxsl.exe"
     */
    private String msxsl;

    /**
     * The path where xslt file is located.
     * This files converts the xml report to a more readable html report.
     * @parameter expression="${xslt}" default-value="C:\\Program Files\\PartCover\\PartCover .NET 4.0\\xslt\\Report By Assembly.xslt"
     * @required
     */
    private String xslt;
   
    public void execute()
        throws MojoExecutionException
    { 
        try 
        {

            String line = " \"" + msxsl + "\" \"" + outputDirectory + "/coverage.xml\" \"" 
                + xslt + "\" -o  \"" + outputDirectory + "/coverage.html\"";

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
