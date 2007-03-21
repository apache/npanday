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
package org.apache.maven.dotnet.plugin.compile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Copies source files to target directory.
 *
 * @author Shane Isbell
 * @goal process-test-sources
 * @phase process-sources
 */
public class TestSourceProcessorMojo
    extends AbstractMojo
{

    /**
     * Source directory
     *
     * @parameter expression = "${sourceDirectory}" default-value="${project.build.testSourceDirectory}"
     * @required
     */
    private String sourceDirectory;

    /**
     * Output directory
     *
     * @parameter expression = "${outputDirectory}" default-value="${project.build.directory}${file.separator}build-test-sources"
     * @required
     */
    private String outputDirectory;

    public void execute()
        throws MojoExecutionException
    {
        if ( !new File( sourceDirectory ).exists() )
        {
            getLog().info( "NMAVEN-905-001: No test source files to copy" );
            return;
        }
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory );

        List<String> excludeList = new ArrayList<String>();
        excludeList.add( "*.suo" );
        excludeList.add( "*.csproj" );
        excludeList.add( "*.sln" );
        excludeList.add( "obj/**" );
        directoryScanner.setExcludes( excludeList.toArray( new String[excludeList.size()] ) );

        directoryScanner.addDefaultExcludes();
        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        getLog().info(
            "NMAVEN-905-002: Copying test source files: From = " + sourceDirectory + ",  To = " + outputDirectory );
        for ( String file : files )
        {
            try
            {
                FileUtils.copyFile( new File( sourceDirectory + File.separator + file ),
                                    new File( outputDirectory + File.separator + file ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NMAVEN-905-000: Unable to process test sources", e );
            }
        }
    }
}
