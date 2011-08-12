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
 
import npanday.PlatformUnsupportedException;
import npanday.assembler.AssemblerContext;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Copies test source files to target directory.
 *
 * @author Shane Isbell
 * @goal process-test-sources
 * @phase process-sources
 * @description Copies test source files to target directory.
 */
public class TestSourceProcessorMojo
    extends AbstractMojo
{

    /**
     * Source directory containing the copied test class files.
     *
     * @parameter expression = "${sourceDirectory}" default-value="${project.build.testSourceDirectory}"
     * @required
     */
    private File sourceDirectory;

    /**
     * Output directory for the test sources.
     *
     * @parameter expression = "${outputDirectory}" default-value="${project.build.directory}/build-test-sources"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression = "${testExcludes}"
     */
    private String[] testExcludes;
    
    /**
     * @parameter expression = "${includes}"
     */
    private String[] includes;    
    
    /**
     * @component
     */
    private AssemblerContext assemblerContext;
    
    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( !sourceDirectory.exists() )
        {
            getLog().info( "NPANDAY-905-001: No test source files to copy" );
            return;
        }
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory.getAbsolutePath() );

        List<String> excludeList = new ArrayList<String>();
        excludeList.add( "*.suo" );
        excludeList.add( "*.csproj" );
        excludeList.add( "*.vbproj" );
        excludeList.add( "*.sln" );
        excludeList.add( "obj/**" );
        excludeList.add( "bin/**" );
        excludeList.add( "target/**" );
        
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
        
        for ( int i = 0; i < testExcludes.length; ++i )
        {
            excludeList.add( testExcludes[i] );
        }
        directoryScanner.setExcludes( excludeList.toArray( new String[excludeList.size()] ) );

        directoryScanner.addDefaultExcludes();
        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        getLog().info(
            "NPANDAY-905-002: Copying test source files: From = " + sourceDirectory + ",  To = " + outputDirectory );
        for ( String file : files )
        {
            try
            {
                File sourceFile = new File( sourceDirectory, file );
                File targetFile = new File( outputDirectory, file );
                if ( sourceFile.lastModified() > targetFile.lastModified() )
                {
                    FileUtils.copyFile( sourceFile, targetFile );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NPANDAY-905-000: Unable to process test sources", e );
            }
        }
        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }
}
