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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.io.File;

/**
 * Provides services for obtaining artifact information and dependencies.
 *
 * @author Shane Isbell
 */
public interface ArtifactContext
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = ArtifactContext.class.getName();

    /**
     * Returns the list of .NET module dependency artifacts that exist directly within the pom for the specified artifact
     * (no transitive dependencies). This is a convenience method that has the same behavior as
     * <code>getDirectDependenciesFor</code> but adds a .netmodule match policy. To get net modules, with additional match policies,
     * use the <code>getDirectDependenciesFor</code> method directly.
     *
     * @param artifact the artifact from which to get the list of direct .NET module dependencies. This value should not be null.
     * @return the list of .NET module dependency artifacts that exist directly within the pom for the given artifact.
     *         This list will not be null.
     * @throws ArtifactException if there is a problem in matching the dependencies of the specified artifact
     */
    List<Artifact> getNetModulesFor( Artifact artifact )
        throws ArtifactException;

    /**
     * Returns a list of net executable artifacts that match entry(s) within the net-executable.xml file.
     * The parameters are used to match the appropriate executable, which is located within the local maven repository.
     * If either of the groupId or artifactId parameters is null, then this method will return an empty list of
     * artifacts. A null version/type parameters will match all versions and types, respectively.
     *
     * @param groupId    the group id to match to a net executable artifact
     * @param artifactId the artifact id to match to a net executable artifact
     * @param version    the version to match to a net executable artifact
     * @param type       the type (or classifier) to match to a net executable artifact. This could be the framework
     *                   version or a platform identifer (windows, linux, etc).
     * @return the list of artifacts that match entry(s) within the net-executable.xml file. This list may be empty
     *         but not null.
     */
    List<Artifact> getArtifactsFor( String groupId, String artifactId, String version, String type );

    /**
     * Returns the artifact for the specified ID
     *
     * @param id the artifact ID, as given in the net-dependencies.xml file
     * @return the artifact for the specified ID.
     */
    Artifact getArtifactByID( String id );

    /**
     * Returns an artifact installer used for installing NMaven artifacts into the local Maven repository.
     *
     * @return an artifact installer for NMaven artifacts
     */
    ArtifactInstaller getArtifactInstaller();

    /**
     * Returns the application config for the specified artifact. This artifact should be a .NET executable type that
     * is defined within the net-executable.xml file. The returned config can be used to get the location of the
     * exe.config file for the artifact.
     *
     * @param artifact the artifact associated with the returned application config. This parameter should not be null.
     * @return the application config for the specified artifact
     * @throws NullPointerException if the artifact is null
     */
    ApplicationConfig getApplicationConfigFor( Artifact artifact );

    /**
     * Returns the directory containing the local Maven repository
     *
     * @return the directory containing the local Maven repository
     */
    File getLocalRepository();

    /**
     * Returns a list of .NET artifacts that reside within the specified repository.
     *
     * @param repository the local repository directory. If value is null, the localRepository reference will default to
     *                   the specified localRepository passed to the init method of the context.
     * @return list of .NET artifacts that reside within the specified repository.
     */
    List<Artifact> getAllNetArtifactsFromRepository( File repository );

    /**
     * Initializes this artifact context. Neither parameter value should be null.
     *
     * @param mavenProject               the maven project
     * @param remoteArtifactRepositories the remote artifact repositories
     * @param localRepository            the file location of the local maven repository
     * @throws NullPointerException if localRepository parameter is null
     */
    void init( MavenProject mavenProject, List<ArtifactRepository> remoteArtifactRepositories, File localRepository );
}
