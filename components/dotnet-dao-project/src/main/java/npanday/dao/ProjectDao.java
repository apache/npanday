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
package npanday.dao;

import npanday.registry.DataAccessObject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.openrdf.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    void removeProjectFor( String groupId, String artifactId, String version, String artifactType ) throws ProjectDaoException;

    /**
     * Returns a project that matches the specified parameters.
     *
     *
     *
     *
     * @param groupId          the group id of the project
     * @param artifactId       the artifact id of the project
     * @param version          the version of the project
     * @param artifactType     the type of artifact: library, exe, winexe, netmodule
     * @param publicKeyTokenId the public key token id. This should match the token id within the manifest of a signed
     *                         .NET assesmbly. This value may be null.
     * @param localRepository
     * @param outputDir
     * @return a project that matches the specified parameters
     * @throws ProjectDaoException if there was a problem retrieving the project
     */
    Project getProjectFor( String groupId, String artifactId, String version, String artifactType,
                           String publicKeyTokenId, File localRepository, File outputDir )
            throws ProjectDaoException;

    /**
     * Stores the specified project and resolves and stores the project's dependencies.
     *
     *
     * @param project              the project to store
     * @param localRepository      the local artifact repository
     * @param artifactRepositories the remote artifact repositories used in resolving dependencies
     * @param outputDir
     * @return a set of artifacts, including the project and its dependencies
     * @throws IOException if there was a problem in storing or resolving the artifacts
     * @throws ProjectDaoException if there was a problem in resolving the artifacts
     */
    Set<Artifact> storeProjectAndResolveDependencies( Project project, File localRepository,
                                                      List<ArtifactRepository> artifactRepositories, File outputDir )
            throws IOException, ProjectDaoException;

    /**
     * Initializes the data access object
     *
     * @param artifactFactory the artifact factory used in creating artifacts
     *@param artifactResolver    for snapshot artifact
     */
    void init( ArtifactFactory artifactFactory, ArtifactResolver artifactResolver );

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
