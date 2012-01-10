package npanday.plugin.aspx;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;
import java.io.IOException;

/**
 * Maven Mojo for packaging .Net Web applications
 * 
 * @goal package
 * @phase package
 */
public class AspxPackageMojo
    extends AbstractMojo
{
    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The directory for the compilated web application
     *
     * @parameter  expression = "${outputDirectory}" default-value = "${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The Zip archiver.
     * 
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
     * @required
     */
    private ZipArchiver archiver;

    public void execute()
        throws MojoExecutionException
    {

        long startTime = System.currentTimeMillis();

        File webappDir = new File( outputDirectory, project.getArtifactId() );

        if ( !webappDir.exists() )
        {
            return;
        }

        performPackaging( webappDir );

        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }

    private void performPackaging( File webappDir )
        throws MojoExecutionException
    {
        File destinationFile = new File( outputDirectory, project.getArtifactId() + ".zip" );
        archiver.setDestFile( destinationFile );

        getLog().info( "Generating package " + destinationFile.getAbsolutePath() );

        try
        {
            archiver.addDirectory( webappDir );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Unable to package web application from " + webappDir.getAbsolutePath(),
                                              e );
        }

        // create archive
        try
        {
            archiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Unable to package web application in " +
                destinationFile.getAbsolutePath(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to package web application in " +
                destinationFile.getAbsolutePath(), e );
        }

        project.getArtifact().setFile( destinationFile );
    }
}