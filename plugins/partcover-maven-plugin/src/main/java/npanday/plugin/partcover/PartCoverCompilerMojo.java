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
     * Base Directory where all reports are written to
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/partcover-reports"
     */
    private File outputDirectory;
	
	/**
	 * Nunit installed
	 * @parameter expression="${nUnit}" 
     */
    private File nUnit;	
	
	/**
	 * PartCover installed
	 * @parameter expression="${partCover}" 
	 */
	private File partCover;
	
	/** 
	 * The maven project
	 * @parameter expression="${project}"
	 * @readonly
	 */
	protected MavenProject project;
	
    public void execute()
        throws MojoExecutionException
    {
	    try 
	    {
		    if ( !outputDirectory.exists() )
			{
			    outputDirectory.mkdirs();
			}
	        		
		    String line = " \"" +partCover + "\"" + " --target " + "\"" + nUnit + "\"" + " --target-args " + assemblyName + " --include [*]* --output " + outputDirectory + "/coverage.xml";

            int exitValue = executeCommandLine( line );
            
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