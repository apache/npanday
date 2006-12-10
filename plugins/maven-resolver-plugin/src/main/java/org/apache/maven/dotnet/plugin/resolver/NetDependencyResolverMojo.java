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
package org.apache.maven.dotnet.plugin.resolver;

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.dotnet.registry.RepositoryRegistry;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.artifact.impl.NetDependenciesRepository;

/**
 * @author Shane Isbell
 * @goal resolve
 * @phase process-resources
 */
public class NetDependencyResolverMojo extends AbstractMojo {
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
     * @parameter
     */
    private NetDependency[] netDependencies;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    public void execute() throws MojoExecutionException {
        if(System.getProperty("bootstrap") != null)  return;

        RepositoryRegistry repositoryRegistry;
        try {
            repositoryRegistry = nmavenRegistry.createRepositoryRegistry();
        } catch (IOException e) {
            throw new MojoExecutionException("NMAVEN-1600-000: Failed to create the repository registry for this plugin", e);
        }

        if (netDependencies == null) netDependencies = new NetDependency[0];
        List<Dependency> dependencies = new ArrayList<Dependency>();
        for (NetDependency netDependency : netDependencies) {
            Dependency dependency = new Dependency();
            dependency.setGroupId(netDependency.getGroupId());
            dependency.setArtifactId(netDependency.getArtifactId());
            dependency.setVersion(netDependency.getVersion());
            dependency.setScope(Artifact.SCOPE_RUNTIME);
            dependency.setType(netDependency.getType());
            dependencies.add(dependency);
        }

        NetDependenciesRepository repository = (NetDependenciesRepository) repositoryRegistry.find("net-dependencies");
        dependencies.addAll(repository.getDependencies());
        getLog().info("NMAVEN-1600-001: Found net dependencies: Number = " + dependencies.size());

        try {
            assemblyResolver.resolveTransitivelyFor(project, project.getArtifact(), dependencies,
                    pomFile, localRepository, false);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("NMAVEN-1600-002: Unable to resolve assemblies", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("NMAVEN-1600-003: Unable to resolve assemblies", e);
        }

    }
}
