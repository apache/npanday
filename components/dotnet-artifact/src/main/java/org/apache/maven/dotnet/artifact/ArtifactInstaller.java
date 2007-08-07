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
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Provides services for installing artifacts.
 *
 * @author Shane Isbell
 */
public interface ArtifactInstaller
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = ArtifactInstaller.class.getName();

    /**
     * Installs artifacts into the local Maven repository. Unlike the <code>installFile</code> method, this method
     * will also check whether an exe.config file is associated with the artifact and install the exe.config into
     * the local maven repository. This will allow installed artifacts, with their associated configuration information,
     * to be directly executed from the local maven repository.
     * <p/>
     * Typically the artifact parameter will be obtained directly through the maven project:
     * <code>MavenProject.getArtifact</code>. In those cases where the MavenProject object is unavailable, then
     * you can use the <code>installFile</code> method from this interface.
     *
     * @param artifact              the artifact to install
     * @param pomFile               the pom file of the installed artifact
     * @param modifyProjectMetadata
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    void installArtifactWithPom( Artifact artifact, File pomFile, boolean modifyProjectMetadata )
        throws ArtifactInstallationException;

    /**
     * Installs a non-maven artifact into the local maven repository so that the artifact can be used within
     * a Maven build process.
     *
     * @param groupId      the group id of the artifact to install
     * @param artifactId   the artifact id of the artifact to install
     * @param version      the version of the artifact to install
     * @param packaging    the packaging type of the artifact to install
     * @param artifactFile the artifact to install
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    void installFileWithoutPom( String groupId, String artifactId, String version, String packaging, File artifactFile )
        throws ArtifactInstallationException;

    /**
     * Resolves and installs the .NET artifacts (as given in the net-dependencies.xml file) and the
     * specified .NET and java dependencies. If a profile is specified, this method will includes dependencies
     * with that profile.
     *
     * @param profile          the specified profile to resolve. This value may be null.
     * @param netDependencies  additional .NET artifacts to resolve and install.
     * @param javaDependencies the Java Dependencies to resolve. Typically these should be the java bindings for the
     *                         .NET plugins.
     * @throws IOException if there is a problem with installation
     */
    void resolveAndInstallNetDependenciesForProfile( String profile, List<Dependency> netDependencies,
                                                     List<Dependency> javaDependencies )
        throws IOException;

    /**
     * Installs both the artifact and all of its dependencies into the private application base.
     *
     * @param applicationBase the root directory of the private application base
     * @param artifact        the artifact to install
     * @param dependencies    the dependencies to install
     * @throws IOException if there is a problem installing any of the artifacts into the private application base
     */
    void installArtifactAndDependenciesIntoPrivateApplicationBase( File applicationBase, Artifact artifact,
                                                                   List<Dependency> dependencies )
        throws IOException;

    /**
     * Initializes the installer.
     *
     * @param artifactContext            the artifact context associated with this installer
     * @param remoteArtifactRepositories the list of remote artifact repositories
     * @param localRepository            the location of the local maven repository
     */
    void init( ArtifactContext artifactContext, List<ArtifactRepository> remoteArtifactRepositories,
               File localRepository );

}
