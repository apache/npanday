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
import java.io.IOException;

import org.apache.maven.dotnet.artifact.ArtifactContext;

/**
 * @goal install-file
 * @phase install
 * @requiresProject false
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
     * @parameter expression = "${version}
     * @required
     */
    private String version;

    /**
     * @parameter expression = "${packaging}
     * @required
     */
    private String packaging;

    /**
     * @parameter expression = "${generatePom} default-value = "true"
     */
    private boolean generatePom;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    public void execute()
        throws MojoExecutionException
    {
        artifactContext.init( project, localRepository );
        if ( generatePom )
        {
            try
            {
                artifactContext.getArtifactInstaller().installFile( groupId, artifactId, version, packaging,
                                                                    artifactFile );
            }
            catch ( org.apache.maven.artifact.installer.ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1000-000: Failed to install artifact file", e );
            }
        }
        else
        {
            try
            {
                artifactContext.getArtifactInstaller().installFileWithNoPom( groupId, artifactId, version,
                                                                             artifactFile );
            }
            catch ( org.apache.maven.artifact.installer.ArtifactInstallationException e )
            {
                throw new MojoExecutionException( "NMAVEN-1000-001: Failed to install artifact file", e );
            }
        }
    }
}
