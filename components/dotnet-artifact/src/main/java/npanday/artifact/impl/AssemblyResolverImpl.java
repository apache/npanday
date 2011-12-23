package npanday.artifact.impl;

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

import npanday.PathUtil;
import npanday.artifact.AssemblyResolver;
import npanday.artifact.NPandayArtifactResolutionException;
import npanday.dao.ProjectDao;
import npanday.dao.Project;
import npanday.dao.ProjectDaoException;
import npanday.dao.ProjectDependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * @see AssemblyResolver#resolveTransitivelyFor(org.apache.maven.project.MavenProject, java.util.List, java.util.List, java.io.File, boolean)
     */
    public void resolveTransitivelyFor( MavenProject mavenProject, List<Dependency> dependencies,
                                        List<ArtifactRepository> remoteArtifactRepositories,
                                        File localArtifactRepository, boolean addResolvedDependenciesToProject )
            throws IOException, NPandayArtifactResolutionException
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
        
        artifactResolver = new AssemblyArtifactResolver( artifactResolver, mavenProject );

        ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
        dao.init( artifactFactory, artifactResolver );
        dao.openConnection();

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        try
        {
            // TODO: do we really need to copy these?
            File targetDirectory = PathUtil.getPrivateApplicationBaseDirectory( mavenProject );
            artifactDependencies =
                dao.storeProjectAndResolveDependencies( project, localArtifactRepository, remoteArtifactRepositories,
                                                        targetDirectory );
        }
        catch( ProjectDaoException e )
        {
            throw new NPandayArtifactResolutionException( "NPANDAY-089-000: " + e.getMessage(), e );
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

    private static final class AssemblyArtifactResolver implements ArtifactResolver
    {
        private final ArtifactResolver delegate;
        private final Map mavenProjectRefs;

        public AssemblyArtifactResolver( ArtifactResolver delegate, MavenProject mavenProject )
        {
            this.delegate = delegate;
            this.mavenProjectRefs = mavenProject.getProjectReferences();
        }

        public void resolve(Artifact artifact, List remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            String mavenProjectRefId = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
            MavenProject mavenProjectRef = (MavenProject) mavenProjectRefs.get( mavenProjectRefId );
            if ( mavenProjectRef != null )
            {
                if ( "pom".equals( artifact.getType() ) )
                {
                    artifact.setFile( mavenProjectRef.getFile() );
                    return;
                }
                else
                {
                    Map artifactMapByArtifactId = new HashMap();
                    artifactMapByArtifactId.putAll( ArtifactUtils.artifactMapByVersionlessId( Collections.singleton( mavenProjectRef.getArtifact() ) ) );
                    artifactMapByArtifactId.putAll( ArtifactUtils.artifactMapByVersionlessId( mavenProjectRef.getArtifacts() ) );
                    artifactMapByArtifactId.putAll( ArtifactUtils.artifactMapByVersionlessId( mavenProjectRef.getAttachedArtifacts() ) );

                    Artifact projectArtifact = (Artifact) artifactMapByArtifactId.get( artifact.getId() );
                    if ( projectArtifact != null )
                    {
                        artifact.setFile( projectArtifact.getFile() );
                        return;
                    }
                }
            }

            delegate.resolve( artifact, remoteRepositories, localRepository );
        }

        public void resolveAlways(Artifact artifact, List remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            delegate.resolveAlways( artifact, remoteRepositories, localRepository );
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            return delegate.resolveTransitively( artifacts, originatingArtifact, localRepository, remoteRepositories, source, filter);
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, List remoteRepositories, ArtifactRepository localRepository, ArtifactMetadataSource source) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            return delegate.resolveTransitively( artifacts, originatingArtifact, remoteRepositories, localRepository, source);
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, List remoteRepositories, ArtifactRepository localRepository, ArtifactMetadataSource source, List listeners) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            return delegate.resolveTransitively( artifacts, originatingArtifact, remoteRepositories, localRepository, source, listeners);
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            return delegate.resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository, remoteRepositories, source);
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            return delegate.resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository, remoteRepositories, source, filter);
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter, List listeners) throws ArtifactResolutionException, ArtifactNotFoundException
        {
            return delegate.resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository, remoteRepositories, source, filter, listeners);
        }
    }

}