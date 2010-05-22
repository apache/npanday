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
package npanday.artifact.impl;

import npanday.artifact.AssemblyResolver;
import npanday.dao.ProjectDao;
import npanday.dao.Project;
import npanday.dao.ProjectDependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * Provides a way to resolve transitive assemblies that do not have versions within their file name.
 *
 * @author Shane Isbell
 */
public class AssemblyResolverImpl
    implements AssemblyResolver, LogEnabled
{
    /**
     * The artifact factory component, which is used for creating artifacts.
     */
    private ArtifactFactory artifactFactory;

    /**
     * An artifact repository factory used to create repositories for use by the <code>ArtifactResolver</code>.
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * Registry used for finding DAOs
     */
    private npanday.registry.DataAccessObjectRegistry daoRegistry;

    /**
     * Manager used to download artifacts.
     */
    private org.apache.maven.artifact.manager.WagonManager wagonManager;
    
    /**
     * Used for resolving snapshot
     */
    private ArtifactResolver artifactResolver;

    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     */
    public AssemblyResolverImpl()
    {
    }

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /**
     * @see AssemblyResolver#resolveTransitivelyFor(org.apache.maven.project.MavenProject, java.util.List<org.apache.maven.model.Dependency>, java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>, java.io.File, boolean)
     */
    public void resolveTransitivelyFor( MavenProject mavenProject, List<Dependency> dependencies,
                                        List<ArtifactRepository> remoteArtifactRepositories,
                                        File localArtifactRepository, boolean addResolvedDependenciesToProject )
        throws IOException
    {
        //Check that the list of dependencies matches the first level RDF Repo
        //If not, resolve missing dependencies and add to repo or delete additional dependencies from repo

        Project project = new Project();
        project.setGroupId( mavenProject.getGroupId() );
        project.setArtifactId( mavenProject.getArtifactId() );
        project.setVersion( mavenProject.getVersion() );
        project.setArtifactType(
            ( mavenProject.getArtifact() != null ) ? mavenProject.getArtifact().getType() : "dotnet-library" );

        project.setPublicKeyTokenId(
            ( mavenProject.getArtifact() != null ) ? mavenProject.getArtifact().getClassifier() : null );

        for ( Dependency dependency : dependencies )
        {
            ProjectDependency projectDependency = new ProjectDependency();
            projectDependency.setGroupId( dependency.getGroupId() );
            projectDependency.setArtifactId( dependency.getArtifactId() );
            projectDependency.setVersion( dependency.getVersion() );
            projectDependency.setPublicKeyTokenId( dependency.getClassifier() );
            projectDependency.setArtifactType( dependency.getType() );
            projectDependency.setScope(dependency.getScope());
            projectDependency.setSystemPath(dependency.getSystemPath());
            project.addProjectDependency( projectDependency );
        }
        
        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, wagonManager, artifactResolver );
        dao.openConnection();

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        try
        {
            artifactDependencies =
                dao.storeProjectAndResolveDependencies( project, localArtifactRepository, remoteArtifactRepositories );
        }
        finally
        {
            dao.closeConnection();
        }

        if ( addResolvedDependenciesToProject )
        {
            mavenProject.setDependencyArtifacts( artifactDependencies );
        }
    }
}