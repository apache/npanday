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
package org.apache.maven.dotnet.plugin.webapp;

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.util.*;
import java.io.IOException;
import java.io.File;

/**
 * Generates a .NET Archive
 *
 * @author Shane Isbell
 * @goal package
 * @phase package
 */
public class NetAchiverMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    protected org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout}"
     * @role-hint default
     * @required
     * @readonly
     */
    protected org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout layout;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.metadata.ArtifactMetadataSource}"
     * @required
     * @readonly
     */
    protected ArtifactMetadataSource src;

    public void execute()
        throws MojoExecutionException
    {
        String outputDirectory = project.getBuild().getDirectory() + File.separator + project.getArtifactId();
        String sourceDirectory = project.getBasedir() + "/src/main/webapp";
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory );
        directoryScanner.addDefaultExcludes();
        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        for ( String file : files )
        {
            try
            {
                FileUtils.copyFile( new File( sourceDirectory + File.separator + file ),
                                    new File( outputDirectory + File.separator + file ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NMAVEN-1200-000: Unable to process sources", e );
            }
        }

        Set<Artifact> artifacts = project.getDependencyArtifacts();
        Set<Artifact> dependencies = new HashSet<Artifact>();
        dependencies.add( project.getArtifact() );
        for ( Artifact artifact : artifacts )
        {
            if ( ( artifact.getType().equals( "library" ) || artifact.getType().equals( "module" ) ) &&
                artifact.getScope().equals( Artifact.SCOPE_COMPILE ) ||
                artifact.getScope().equals( Artifact.SCOPE_RUNTIME ) )
            {
                dependencies.add( artifact );
            }
        }

        for ( Artifact artifact : dependencies )
        {
            try
            {
                FileUtils.copyFileToDirectory( artifact.getFile(), new File( outputDirectory + "/bin" ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NMAVEN-1200-001", e );
            }
        }

        ZipArchiver zipArchiver = new ZipArchiver();
        zipArchiver.setDestFile( new File( outputDirectory + ".nar" ) );
        try
        {
            zipArchiver.addDirectory( new File( outputDirectory ) );
            zipArchiver.createArchive();
            getLog().info( "NMAVEN-1200-004: Created .NET Archive: File = " + outputDirectory + ".nar" );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "NMAVEN-1200-002: Unable to create .NET archive:", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NMAVEN-1200-003: Unable to create .NET archive", e );
        }
    }
}

