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
package org.apache.maven.dotnet.dao;

import org.apache.maven.dotnet.registry.DataAccessObject;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Model;
import org.openrdf.repository.Repository;

import java.util.Set;
import java.util.List;
import java.io.IOException;
import java.io.File;

/**
 * Provides methods for storing and retreiving project information.
 */
public interface ProjectDao
    extends DataAccessObject
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = ProjectDao.class.getName();

    void removeProjectFor( String groupId, String artifactId, String version, String artifactType ) throws IOException;
    /**
     * Returns a project that matches the specified parameters.
     *
     * @param groupId          the group id of the project
     * @param artifactId       the artifact id of the project
     * @param version          the version of the project
     * @param artifactType     the type of artifact: library, exe, winexe, netmodule
     * @param publicKeyTokenId the public key token id. This should match the token id within the manifest of a signed
     *                         .NET assesmbly. This value may be null.
     * @return a project that matches the specified parameters
     * @throws IOException if there was a problem retrieving the project
     */
    Project getProjectFor( String groupId, String artifactId, String version, String artifactType,
                           String publicKeyTokenId )
        throws IOException;

    /**
     * Returns a project that matches the information contained within the specified maven project.
     *
     * @param mavenProject the maven project used in finding the returned project
     * @return a project that matches the information contained within the specified maven project
     * @throws IOException if there was a problem retrieving the project
     */
    Project getProjectFor( MavenProject mavenProject )
        throws IOException;

    /**
     * Method not implemented.
     *
     * @param project
     * @param localRepository
     * @param artifactRepositories
     * @throws IOException
     */
    void storeProject( Project project, File localRepository, List<ArtifactRepository> artifactRepositories )
        throws IOException;

    /**
     * Stores the specified project and resolves and stores the project's dependencies.
     *
     * @param project              the project to store
     * @param localRepository      the local artifact repository
     * @param artifactRepositories the remote artifact repositories used in resolving dependencies
     * @return a set of artifacts, including the project and its dependencies
     * @throws IOException if there was a problem in storing or resolving the artifacts
     */
    Set<Artifact> storeProjectAndResolveDependencies( Project project, File localRepository,
                                                      List<ArtifactRepository> artifactRepositories )
        throws IOException;

    /**
     * Stores the project object model and resolves and stores the model's dependencies.
     *
     * @param model                   the project object model
     * @param pomFileDirectory        the directory containing the pom file
     * @param localArtifactRepository the local repository
     * @param artifactRepositories    the remote artifact repositories used in resolving dependencies
     * @return a set of artifacts, including the model and its dependencies
     * @throws IOException if there was a problem in storing or resolving the artifacts
     */
    Set<Artifact> storeModelAndResolveDependencies( Model model, File pomFileDirectory, File localArtifactRepository,
                                                    List<ArtifactRepository> artifactRepositories )
        throws IOException;

    /**
     * Initializes the data access object
     *
     * @param artifactFactory the artifact factory used in creating artifacts
     * @param wagonManager    the manager used for downloading artifacts
     */
    void init( ArtifactFactory artifactFactory, WagonManager wagonManager );

    /**
     * Initializes the data access object
     *
     * @param artifactFactory the artifact factory used in creating artifacts
     * @param wagonManager    the manager used for downloading artifacts
     *@param artifactResolver    for snapshot artifact
     */
    void init( ArtifactFactory artifactFactory, WagonManager wagonManager, ArtifactResolver artifactResolver );
    
    /**
     * Returns all projects.
     *
     * @return all projects
     * @throws IOException if there is a problem retrieving the projects
     */
    Set<Project> getAllProjects()
        throws IOException;

    /**
     * Sets the repository for the data access object. This method overrides the data source object set in the
     * DataAccessObject#init
     *
     * @param repository the rdf repository
     */
    void setRdfRepository( Repository repository );

    /**
     * Opens the repository connection specified within ProjectDao#setRdfRepository or DataAccessObject#init method
     *
     * @return true if the rdf repository successfully opens, otherwise return false
     */
    boolean openConnection();

    /**
     * Closes the repository connection specified within ProjectDao#setRdfRepository or DataAccessObject#init method
     *
     * @return true if the rdf repository successfully closes, otherwise return false
     */
    boolean closeConnection();

}
