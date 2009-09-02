package npanday.plugin.wix;

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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * Goal which executes WiX candle to create a .wixobj file.
 *
 * @goal candle
 * 
 * @phase package
 */
public class CandleMojo
    extends AbstractMojo
{
    /**
     * Location of the WiX source files.
     * @parameter expression="${sourceFiles}"
     * @required
     */
    private File[] sourceFiles;
    
    /**
     * Output file
     * @parameter expression="${outputDirectory}"
	 */
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException
    {
        String paths = "";
        for (int x = 0; x < sourceFiles.length; x++) {
          File f = sourceFiles[x];
          if ( !f.exists() )
          {
         	throw new MojoExecutionException( "Source file does not exist " + sourceFiles[x] );
          } else {
	        paths = paths + sourceFiles[x].getAbsolutePath() + " ";
          }
        }

        try {
          String line = "candle -nologo "; 
          if(outputDirectory != null)
          {
            if (!outputDirectory.exists()) 
            {
              outputDirectory.mkdir();
              line = line + "-out " + outputDirectory.getAbsolutePath() + "\\";
            }
            else
            {
              line = line + "-out " + outputDirectory.getAbsolutePath() + "\\";
            }
          }
          line += " " + paths;
          CommandLine commandLine = CommandLine.parse(line);
          DefaultExecutor executor = new DefaultExecutor();
          int exitValue = executor.execute(commandLine);
          
          if ( exitValue != 0 ) {
        	  throw new MojoExecutionException( "Problem executing candle, return code " + exitValue );
          }
         
        } catch (ExecuteException e) {
          throw new MojoExecutionException( "Problem executing candle", e );
        } catch (IOException e ) {
          throw new MojoExecutionException( "Problem executing candle", e );
        }
    }
}
