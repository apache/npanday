package npanday.plugin.partcover;

/*
 * Copyright ---
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.exec.ExecuteException; 
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import eu.cedarsoft.utils.ZipExtractor;

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
     * Base Directory where all reports are written to
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/partcover-reports"
     */
    private File outputDirectory;
	 
    /**
     * msxsl.exe File
     * @parameter expression="${msxsl}"
     */
    private File msxsl;
 
    public void execute()
        throws MojoExecutionException
    { 
	    try 
	    {

            // copy resources
            
            File templateFile = copyFileToOutputDirectory( "/templates.zip", "templates.zip" );
            extractZip( templateFile, outputDirectory);
            
            copyFileToOutputDirectory( "/coverage-report.css", "coverage-report.css" );
            
            // convert xml result using xslt;            
	        //String line = " \"" + msxsl + "\" \"" + outputDirectory + "/coverage.xml" \""  + outputDirectory + "/templates/common-partcover-report.xslt" -o \"" + outputDirectory + "/coverage.html\"";
            
            String line = " \"" + msxsl + "\" \"" + outputDirectory + "/coverage.xml\" \"" + outputDirectory
                + "/templates/common-partcover-report.xslt\" -o  \"" + outputDirectory + "/coverage.html\"";

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
    
    private void extractZip( File template, File outputDirectory )
        throws MojoExecutionException
    {
        try
        {
            ZipExtractor zip = new ZipExtractor ( template );
            zip.extract( outputDirectory );
        }
        
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error occured when trying to extract file.", e );
        }
    }
}
