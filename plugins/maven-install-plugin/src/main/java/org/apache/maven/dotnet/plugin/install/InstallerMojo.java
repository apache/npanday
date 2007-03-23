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
package org.apache.maven.dotnet.plugin.install;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.ArtifactType;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.util.List;

/**
 * @goal install
 * @phase install
 */
public class InstallerMojo
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
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private File localRepository;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    private File pomFile;

    /**
     * @component
     */
    private ArtifactInstaller artifactInstaller;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    public void execute()
        throws MojoExecutionException
    {
        Artifact artifact = project.getArtifact();
        try
        {
            artifactContext.getArtifactInstaller().installArtifact( artifact, pomFile );
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException( "NMAVEN-1001-000: Failed to install artifacts", e );
        }

        //To allow executables to be runnable from the repo
        if ( artifact.getType().equals( ArtifactType.EXE.getArtifactTypeName() ) )
        {
            List<Dependency> dependencies = project.getDependencies();
            try
            {
                artifactContext.getArtifactInstaller().installLibraryDependencies( artifact, dependencies );
            }
            catch ( ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1001-002: Failed to install artifact file", e );
            }
        }

        //For the IDE: If we see a dll with same name as netmodule, copy dll to the local repo.
        /*
        File linkedFile =
            new File( artifact.getFile().getParent() + File.separatorChar + artifact.getArtifactId() + ".dll" );
        if ( linkedFile.exists() && artifact.getType().equals( ArtifactType.MODULE.getArtifactTypeName() ) )
        {
            try
            {
                artifactContext.getArtifactInstaller().installFileWithNoPom( artifact.getGroupId(),
                                                                             artifact.getArtifactId(),
                                                                             artifact.getVersion(), linkedFile );
            }
            catch ( org.apache.maven.artifact.installer.ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1001-001: Failed to install artifact file", e );
            }
        }
        */

    }
}
