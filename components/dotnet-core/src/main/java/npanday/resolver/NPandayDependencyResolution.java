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

package npanday.resolver;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.HashSet;
import java.util.Set;

/**
 * Resolves project dependencies to a required scope while executing custom
 * resolver logic for GAC, e.g.
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class NPandayDependencyResolution
    extends AbstractLogEnabled
{
    private NPandayArtifactResolver artifactResolver;
    private ArtifactFactory artifactFactory;
    private ArtifactMetadataSource metaDataSource;

    public Set<Artifact> require(MavenProject project, ArtifactRepository localRepository, String scope) throws
        ArtifactResolutionException
    {
        ArtifactFilter scopeFilter = null;
        if ( !Strings.isNullOrEmpty( scope )){
            scopeFilter = new ScopeArtifactFilter( scope );
        }

        return require( project, localRepository, scopeFilter );
    }

    public Set<Artifact> require(
        MavenProject project, ArtifactRepository localRepository, ArtifactFilter filter ) throws
        ArtifactResolutionException
    {
        long startTime = System.currentTimeMillis();

        artifactResolver.initializeWithFilter(filter);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "NPANDAY-148-007: Resolving dependencies for " + project.getArtifact() );
        }

        try
        {
            if ( project.getDependencyArtifacts() == null )
            {
                createArtifactsForMaven2BackCompat( project );
            }

            ArtifactResolutionResult result = artifactResolver.resolveTransitively(
                project.getDependencyArtifacts(), project.getArtifact(), project.getManagedVersionMap(),
                localRepository, project.getRemoteArtifactRepositories(), metaDataSource,
                filter
            );

            /*
            * WORKAROUND: transitive dependencies are not cached in MavenProject; in order to let
            * them "live" for following mojo executions, we'll add the custom resolved
            * dependencies to the projects DIRECT dependencies
            * */

            Set<Artifact> dependencyArtifacts = new HashSet<Artifact>( project.getDependencyArtifacts() );
            addResolvedSpecialsToProjectDependencies(result, dependencyArtifacts);

            // Add custom contribute dependencies to maven project dependencies
            dependencyArtifacts.addAll(artifactResolver.getCustomDependenciesCache());
            project.setDependencyArtifacts(dependencyArtifacts);

            Set<Artifact> resultRequire = Sets.newLinkedHashSet(result.getArtifacts());
            resultRequire.addAll(artifactResolver.getCustomDependenciesCache());

            if (getLogger().isInfoEnabled()) {
                long endTime = System.currentTimeMillis();
                getLogger().info( "NPANDAY-148-009: Took " + (endTime - startTime) + "ms to resolve dependencies for " + project.getArtifact() + " with filter " + filter.toString() );
            }

            return resultRequire;
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ArtifactResolutionException( "NPANDAY-148-001: Could not resolve project dependencies", project.getArtifact(), e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new ArtifactResolutionException( "NPANDAY-148-002: Could not resolve project dependencies", project.getArtifact(), e  );
        }
        catch ( InvalidDependencyVersionException e )
        {
            throw new ArtifactResolutionException( "NPANDAY-148-003: Could not resolve project dependencies", project.getArtifact(), e  );
        }
    }

    private void addResolvedSpecialsToProjectDependencies(ArtifactResolutionResult result, Set<Artifact> dependencyArtifacts)
    {
        Set resolvedArtifacts = result.getArtifacts();
        Sets.SetView intersection = Sets.intersection(
            resolvedArtifacts, artifactResolver.getCustomResolveCache()
        );

        for ( Object ao : intersection )
        {
            Artifact a = (Artifact) ao;
            if ( !dependencyArtifacts.contains(a) )
            {
                getLogger().info( "NPANDAY-148-005: Adding custom resolved " + a + " to project.dependencyArtifacts" );

                dependencyArtifacts.add(a);
            }
        }
        
    }

    private void createArtifactsForMaven2BackCompat( MavenProject project ) throws InvalidDependencyVersionException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "NPANDAY-148-008: creating dependency collection for maven 2 projects" );
        }

        project.setDependencyArtifacts( project.createArtifacts( artifactFactory, null, null ) );
    }
}
