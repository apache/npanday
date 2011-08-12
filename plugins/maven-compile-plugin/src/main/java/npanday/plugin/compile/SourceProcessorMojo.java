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
package npanday.plugin.compile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import npanday.assembler.AssemblerContext;
import npanday.PlatformUnsupportedException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Copies source files to target directory.
 *
 * @author Shane Isbell
 * @goal process-sources
 * @phase process-sources
 * @description Copies source files to target directory.
 */

public class SourceProcessorMojo
    extends AbstractMojo
{

    /**
     * Source directory containing the copied class files.
     *
     * @parameter expression = "${sourceDirectory}" default-value="${project.build.sourceDirectory}"
     * @required
     */
    private File sourceDirectory;

    /**
     * Output directory
     *
     * @parameter expression = "${outputDirectory}" default-value="${project.build.directory}/build-sources"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression = "${includes}"
     */
    private String[] includes;

    /**
     * @parameter expression = "${excludes}"
     */
    private String[] excludes;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    /**
     * @component
     */
    private AssemblerContext assemblerContext;

    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( !sourceDirectory.exists() )
        {
            getLog().info( "NPANDAY-904-001: No source files to copy" );
            return;
        }
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory.getAbsolutePath() );

        List<String> excludeList = new ArrayList<String>();
        //target files
        excludeList.add( "obj/**" );
        excludeList.add( "bin/**" );
        excludeList.add( "target/**" );
        //Misc
        excludeList.add( "Resources/**" );
        excludeList.add( "Test/**" );

        List<String> includeList = new ArrayList<String>();
        try
        {
            includeList.add( "**/*." + assemblerContext.getClassExtensionFor( language ) );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-904-003: Language is not supported: Language = " + language, e );
        }
    	for (int i = 0; i < includes.length; ++i)
    	{
    		includeList.add(includes[i]);
    	}
        directoryScanner.setIncludes( includeList.toArray( includes ) );
        for (int i = 0; i < excludes.length; ++i)
        {
        	excludeList.add(excludes[i]);
        }
        directoryScanner.setExcludes( excludeList.toArray( excludes ) );
        directoryScanner.addDefaultExcludes();

        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        getLog().info( "NPANDAY-904-002: Copying source files: From = " + sourceDirectory + ",  To = " +
            outputDirectory + ", File Count = " + files.length );

        super.getPluginContext().put( "SOURCE_FILES_UP_TO_DATE", Boolean.TRUE );
        for ( String file : files )
        {
            try
            {
                File sourceFile = new File( sourceDirectory, file );
                File targetFile = new File( outputDirectory, file );
                if ( sourceFile.lastModified() > targetFile.lastModified() )
                {
                    super.getPluginContext().put( "SOURCE_FILES_UP_TO_DATE", Boolean.FALSE );
                    FileUtils.copyFile( sourceFile, targetFile );
                    targetFile.setLastModified( System.currentTimeMillis() );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NPANDAY-904-000: Unable to process sources", e );
            }
        }
    }
}
