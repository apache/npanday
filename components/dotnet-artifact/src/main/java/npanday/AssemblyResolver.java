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
package npanday.artifact;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.util.List;
import java.io.File;
import java.io.IOException;

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
     * @param mavenProject                the maven project
     * @param dependencies                the list of dependencies of the specified artifact
     * @param remoteArtifactRepositories  the list of remote artifact repositories to use in resolving
     * @param localArtifactRepository     the local artifact repository to use in resolving
     * @param addResolvedDependenciesToProject
     *                            true, if the resolved dependencies should be added the the specified
     *                            maven project, otherwise false. This value should be set to false if it is resolving
     *                            .NET executables that are intended to be executed as an external process. If it is
     *                            resolving assemblies that the source artifact needs to compile against, this value
     *                            should be set to true.
     * @throws IOException
     */
    void resolveTransitivelyFor( MavenProject mavenProject, List<Dependency> dependencies,
                                 List<ArtifactRepository> remoteArtifactRepositories, File localArtifactRepository,
                                 boolean addResolvedDependenciesToProject )
        throws IOException;
}
