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
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;

import java.io.File;
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
     * @param artifact the artifact to install
     * @param pomFile  the pom file of the installed artifact
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    void installArtifact( Artifact artifact, File pomFile )
        throws ArtifactInstallationException;

    /**
     * Installs a non-maven artifact into the local maven repository so that the artifact can be used within
     * a Maven build process.
     *
     * @param groupId    the group id of the artifact to install
     * @param artifactId the artifact id of the artifact to install
     * @param version    the version of the artifact to install
     * @param packaging  the packaging type of the artifact to install
     * @param pomFile    the pom file of the artifact to install
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    void installFile( String groupId, String artifactId, String version, String packaging, File pomFile )
        throws ArtifactInstallationException;

    /**
     * Installs a file into the local maven repository, without generating a pom.xml. This is used for placing
     * files and resources into the local repository, where there are no explicit dependencies (as given in the pom file).
     *
     * @param groupId     the group id of the file to install
     * @param artifactId  the artifact id of the file to install
     * @param version     the version of the file to install
     * @param installFile the file to install
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    void installFileWithNoPom( String groupId, String artifactId, String version, File installFile )
        throws ArtifactInstallationException;

    /**
     * Installs the dependent libraries (or assemblies) of the specified artifact. 
     *
     * @param artifact the artifact associated with the specified dependencies
     * @param dependencies a list of dependencies of the specified artifact
     * @throws ArtifactInstallationException if there is a problem installing the artifact
     */
    void installLibraryDependencies( Artifact artifact, List<Dependency> dependencies )
        throws ArtifactInstallationException;

    /**
     * Copies .netmodules, that the project is dependenct upon, from the local repo to the project's target directory.
     * This method handles placing all the .netmodules in the same directory as the compiled project artifact for use
     * in unit testing and/or packaging.
     *
     * @param projectArtifact the artifact that has .netmodule dependencies
     * @throws ArtifactInstallationException if there is a problem installing the net module artifact(s)
     */
    void installNetModulesToTargetDirectory( Artifact projectArtifact )
        throws ArtifactInstallationException;

    /**
     * Initializes the installer.
     *
     * @param artifactContext the artifact context associated with this installer
     * @param mavenProject    the maven project associated with the invoking plugin
     * @param localRepository the location of the local maven repository
     */
    void init( ArtifactContext artifactContext, MavenProject mavenProject, File localRepository );

}
