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
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;

import org.apache.maven.dotnet.InitializationException;
import org.apache.maven.dotnet.artifact.AssemblyResolver;

import java.io.File;

/**
 * This class initializes and validates the setup.
 *
 * @author Shane Isbell
 * @goal initialize
 * @phase compile
 */
public class ComponentInitializerMojo extends AbstractMojo {

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
     * @readonly
     */
    private String localRepository;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    private File pomFile;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.CapabilityMatcher capabilityMatcher;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    public void execute() throws MojoExecutionException {
        try {
            assemblyResolver.resolveTransitivelyFor(project, project.getArtifact(), project.getDependencies(), pomFile,
                    localRepository, true);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("NMAVEN-901-000: Unable to resolve assemblies", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("NMAVEN-901-001: Unable to resolve assemblies", e);
        }
    }
}
