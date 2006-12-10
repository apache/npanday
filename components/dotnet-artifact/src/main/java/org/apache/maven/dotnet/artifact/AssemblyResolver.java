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
package org.apache.maven.dotnet.artifact;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;

import java.io.File;
import java.util.List;

/**
 * Provides services for resolving .NET assembly artifacts.
 *
 * @author Shane Isbell
 */
public interface AssemblyResolver
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = AssemblyResolver.class.getName();

    /**
     * Resolves transitive dependencies for the project.
     *
     * @param project             the maven project
     * @param sourceArtifact      the artifact to which the resolved dependencies belong
     * @param dependencies        the list of dependencies of the specified artifact
     * @param pomFile             the pom file of the specified artifact
     * @param localRepositoryPath the path of the local Maven repository
     * @param addResolvedDependenciesToProject
     *                            true, if the resolved dependencies should be added the the specified
     *                            maven project, otherwise false. This value should be set to false if it is resolving
     *                            .NET executables that are intended to be executed as an external process. If it is
     *                            resolving assemblies that the source artifact needs to compile against, this value
     *                            should be set to true.
     * @throws ArtifactResolutionException
     * @throws ArtifactNotFoundException
     */

    void resolveTransitivelyFor( MavenProject project, Artifact sourceArtifact, List<Dependency> dependencies,
                                 File pomFile, String localRepositoryPath, boolean addResolvedDependenciesToProject )
        throws ArtifactResolutionException, ArtifactNotFoundException;


}
