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
package org.apache.maven.dotnet.plugin.resgen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.util.List;

import java.util.Iterator;
import java.io.File;
import java.io.IOException;


/**
 * Copies resources to target directory
 *
 * @author Shane Isbell
 * @goal copy-resources
 * @phase process-resources
 */
public class ResourceCopierMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        String targetDirectory = project.getBuild().getDirectory();
        List<Resource> resources = project.getResources();
        if ( resources.isEmpty() )
        {
            getLog().info( "NPANDAY-1500-000: No resources found" );
            return;
        }
        for ( Resource resource : resources )
        {
            if ( !new File( resource.getDirectory() ).exists() )
            {
                continue;
            }
            copyResourceDirectory( resource.getDirectory(), targetDirectory, resource.getIncludes(),
                                   resource.getExcludes() );
        }
        try
        {
            FileUtils.copyDirectory( new File( project.getBasedir(), "src/main/config" ),
                                     new File( project.getBuild().getDirectory() ), "*.exe.config", null );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-1500-004: Failed to copy config file.", e );
        }
    }

    private void copyResourceDirectory( String sourceDirectory, String outputDirectory, List<String> includes,
                                        List<String> excludes )
        throws MojoExecutionException
    {
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory );
        if ( !includes.isEmpty() )
        {
            directoryScanner.setIncludes( includes.toArray( new String[includes.size()] ) );
        }
        if ( !excludes.isEmpty() )
        {
            directoryScanner.setExcludes( excludes.toArray( new String[excludes.size()] ) );
        }
        directoryScanner.addDefaultExcludes();
        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        for ( String file : files )
        {
            File sourceFile = new File( sourceDirectory, file );
            File destinationFile = new File( outputDirectory, "assembly-resources" + File.separator + file );
            try
            {
                FileUtils.copyFile( sourceFile, destinationFile );
                getLog().debug( "NPANDAY-1500-001: Copied Resource File: Source File = " + sourceFile.getAbsolutePath() +
                    ", Destination File = " + destinationFile.getAbsolutePath() );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-1500-002: Unable to copy resource file: Source File = " + sourceFile.getAbsolutePath(), e );
            }
        }
        getLog().info( "NPANDAY-1500-003: Copied resource directory: Number of Resources = " + files.length +
            ", Resource Directory = " + sourceDirectory + ", Destination Directory = " + outputDirectory + File
            .separator + "assembly-resources" );
    }
}
