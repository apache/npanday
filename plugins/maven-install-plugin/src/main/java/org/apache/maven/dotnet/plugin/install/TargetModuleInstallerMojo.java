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
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.dotnet.artifact.ArtifactContext;

import java.io.File;
import java.io.IOException;

/**
 * Installs .NET modules into the project's target directory.
 *
 * @author Shane Isbell
 * @goal target-install
 * @phase process-classes
 */

public class TargetModuleInstallerMojo extends AbstractMojo {

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private File localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    public void execute() throws MojoExecutionException {
        try {
            nmavenRegistry.createRepositoryRegistry();
        } catch (IOException e) {
            throw new MojoExecutionException("NMAVEN-1002-001: Failed to create the repository registry for this plugin", e);
        }

        artifactContext.init(project, localRepository);
        try {
            artifactContext.getArtifactInstaller().installNetModulesToTargetDirectory(project.getArtifact());
        } catch (ArtifactInstallationException e) {
            throw new MojoExecutionException("NMAVEN-1002-000: Failed to install artifacts into target directory", e);
        }
    }
}
