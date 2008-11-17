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

import java.io.File;

import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;

/**
 * Installs assembly file into the local repository
 *
 * @goal install-file
 * @requiresProject false
 * @requiresDirectInvocation true
 * @description Installs assembly file into the local repository
 */

public class FileInstallerMojo
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
     * @parameter expression = "${file}
     * @required
     */
    private File artifactFile;

    /**
     * @parameter expression = "${pomFile}
     */
    private File pomFile;

    /**
     * @parameter expression = "${groupId}
     * @required
     */
    private String groupId;

    /**
     * @parameter expression = "${artifactId}
     * @required
     */
    private String artifactId;

    /**
     * @parameter expression = "${artifactVersion}
     * @required
     */
    private String version;

    /**
     * @parameter expression = "${packaging}
     * @required
     */
    private String packaging;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    /**
     * The artifact factory component, which is used for creating artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    /**
     * @component
     */
    private org.apache.maven.artifact.manager.WagonManager wagonManager;
    
    /**
     * Used for resolving snapshot
     */
    private ArtifactResolver artifactResolver;

    public void execute()
        throws MojoExecutionException
    {
        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager, artifactResolver );
        dao.openConnection();

        artifactContext.init( project, project.getRemoteArtifactRepositories(), localRepository );
        if ( pomFile != null && !pomFile.exists() )
        {
            throw new MojoExecutionException(
                "NMAVEN-1000-002: Pom  File is missing: File = " + pomFile.getAbsolutePath() );
        }
        else if ( pomFile == null )
        {
            try
            {
                this.getLog().info( "NMAVEN-xxx-000: Installing file with generated pom" );
                artifactContext.getArtifactInstaller().installFileWithoutPom( groupId, artifactId, version, packaging,
                                                                              artifactFile );
            }
            catch ( org.apache.maven.artifact.installer.ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1000-000: Failed to install artifact file", e );
            }
        }
        else
        {
            Artifact sourceArtifact =
                artifactFactory.createArtifact( groupId, artifactId, version, "compile", packaging );
            sourceArtifact.setFile( artifactFile );
            this.getLog().info( "NMAVEN-xxx-000: Installing file with specified pom" );
            try
            {
                artifactContext.getArtifactInstaller().installArtifactWithPom( sourceArtifact, pomFile, false );
            }
            catch ( ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1000-003: Failed to install artifact file", e );
            }
        }
    }
}
