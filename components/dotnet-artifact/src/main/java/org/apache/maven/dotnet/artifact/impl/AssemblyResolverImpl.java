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
package org.apache.maven.dotnet.artifact.impl;

import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.artifact.AssemblyRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

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
     * An artifact resolver component for locating artifacts and pulling them into the local repo if they do not
     * already exist.
     */
    private ArtifactResolver resolver;

    /**
     * Metadata component used by the <code>ArtifactResolver</code>.
     */
    private ArtifactMetadataSource metadata;

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
     * @see AssemblyResolver#resolveTransitivelyFor
     */
    public void resolveTransitivelyFor( MavenProject project, Artifact sourceArtifact, List<Dependency> dependencies,
                                        List<ArtifactRepository> remoteArtifactRepositories,
                                        ArtifactRepository localArtifactRepository,
                                        boolean addResolvedDependenciesToProject )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        List<ArtifactRepository> releaseRepositories = new ArrayList<ArtifactRepository>();
        List<ArtifactRepository> snapshotRepositories = new ArrayList<ArtifactRepository>();
        for ( ArtifactRepository artifactRepository : remoteArtifactRepositories )
        {
            if ( artifactRepository.getSnapshots().isEnabled() )
            {
                snapshotRepositories.add( artifactRepository );
            }
            if ( artifactRepository.getReleases().isEnabled() )
            {
                releaseRepositories.add( artifactRepository );
            }
        }

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        Set<Artifact> snapshotArtifactDependencies = new HashSet<Artifact>();
        Set<Artifact> gacDependencies = new HashSet<Artifact>();

        ArtifactFilter gacFilter = new GacFilter();
        for ( Dependency dependency : dependencies )
        {
            String scope = ( dependency.getScope() == null ) ? Artifact.SCOPE_COMPILE : dependency.getScope();
            Artifact artifact = artifactFactory.createDependencyArtifact( dependency.getGroupId(),
                                                                          dependency.getArtifactId(),
                                                                          VersionRange.createFromVersion(
                                                                              dependency.getVersion() ),
                                                                          dependency.getType(),
                                                                          dependency.getClassifier(), scope, null );
            if ( artifact.getType().startsWith( "gac" ) )
            {
                logger.debug(
                    "NMAVEN-000-000: GAC Dependency = " + artifact.getType() + ", ID = " + artifact.getArtifactId() );
                gacDependencies.add( artifact );
            }
            else if ( artifact.isSnapshot() )
            {
                logger.debug( "NMAVEN-000-000: Snapshot Dependency: Type  = " + artifact.getType() +
                    ", Artifact ID = " + artifact.getArtifactId() );
                snapshotArtifactDependencies.add( artifact );
            }
            else
            {
                logger.debug( "NMAVEN-000-000: Dependency: Type  = " + artifact.getType() + ", Artifact ID = " +
                    artifact.getArtifactId() );
                artifactDependencies.add( artifact );
            }
        }
        //Releases
        ArtifactResolutionResult result = resolver.resolveTransitively( artifactDependencies, sourceArtifact,
                                                                        localArtifactRepository, releaseRepositories,
                                                                        metadata, gacFilter );
        Set<Artifact> resolvedReleaseDependencies = result.getArtifacts();

        //Snapshots
        ArtifactRepository artifactRepository = new DefaultArtifactRepository( "local", "file://" +
            localArtifactRepository.getBasedir(), new DefaultRepositoryLayout() );

        ArtifactResolutionResult snapshotResult = resolver.resolveTransitively( snapshotArtifactDependencies,
                                                                                sourceArtifact, artifactRepository,
                                                                                snapshotRepositories, metadata,
                                                                                gacFilter );
        Set<Artifact> resolvedSnapshotDependencies = snapshotResult.getArtifacts();
        AssemblyRepositoryLayout layout = new AssemblyRepositoryLayout();
        for ( Artifact artifact : resolvedSnapshotDependencies )
        {
            File originalFileWithVersion = artifact.getFile();
            if ( artifact.isSnapshot() )
            {
                artifact.setVersion( artifact.getBaseVersion() );
            }
            File targetFileWithoutVersion =
                new File( localArtifactRepository.getBasedir() + "/" + layout.pathOf( artifact ) );
            if ( originalFileWithVersion.lastModified() > targetFileWithoutVersion.lastModified() )
            {
                logger.debug( "Original = " + originalFileWithVersion.getAbsolutePath() + ", Target = " +
                    targetFileWithoutVersion.getAbsolutePath() );
                try
                {
                    FileUtils.copyFile( originalFileWithVersion, targetFileWithoutVersion );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
            if ( originalFileWithVersion.length() != 0 && targetFileWithoutVersion.length() == 0 )
            {
                throw new ArtifactResolutionException( "NMAVEN-000-000: Artifact is corrupted:", artifact );
            }

            if ( targetFileWithoutVersion.exists() )
            {
                artifact.setFile( targetFileWithoutVersion.getAbsoluteFile() );
            }
        }
        if ( addResolvedDependenciesToProject )
        {
            resolvedReleaseDependencies.addAll( gacDependencies );
            resolvedReleaseDependencies.addAll( resolvedSnapshotDependencies );
            project.setDependencyArtifacts( resolvedReleaseDependencies );
        }
    }

    private static class GacFilter
        implements ArtifactFilter
    {
        public boolean include( org.apache.maven.artifact.Artifact artifact )
        {
            return !artifact.getType().startsWith( "gac" );
        }
    }
}
