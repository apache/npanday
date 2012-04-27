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
import npanday.LocalRepositoryUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;

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

    public void require(MavenProject project, ArtifactRepository localRepository, String scope) throws
        ArtifactResolutionException
    {
        try
        {
            if ( project.getDependencyArtifacts() == null )
            {
                createArtifactsForMaven2BackCompat( project );
            }

            ArtifactFilter scopeFilter = null;
            if ( !Strings.isNullOrEmpty( scope )){
                scopeFilter = new ScopeArtifactFilter( scope );
            }

            ArtifactResolutionResult result = artifactResolver.resolveTransitively(
                project.getDependencyArtifacts(), project.getArtifact(), project.getManagedVersionMap(),
                localRepository, project.getRemoteArtifactRepositories(), metaDataSource,
                scopeFilter
            );

            /*
            * WORKAROUND: transitive dependencies are not cached in MavenProject; in order to let
            * them "live" for following mojo executions, we'll add the custom resolved
            * dependencies to the projects DIRECT dependencies
            * */

            addResolvedSpecialsToProjectDependencies( project, result );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ArtifactResolutionException( "NPANDAY-148-001: Could not resolve project dependencies", project.getArtifact(), e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new ArtifactResolutionException( "NPANDAY-148-002: Could not resolve project dependencies", project.getArtifact(), e  );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new ArtifactResolutionException( "NPANDAY-148-003: Could not resolve project dependencies", project.getArtifact(), e  );
        }
    }

    private void addResolvedSpecialsToProjectDependencies( MavenProject project, ArtifactResolutionResult result )
    {
        Set resolvedArtifacts = result.getArtifacts();
        Sets.SetView intersection = Sets.intersection(
            resolvedArtifacts, artifactResolver.getCustomResolveCache()
        );

        for ( Object ao : intersection )
        {
            Artifact a = (Artifact) ao;
            if ( !project.getDependencyArtifacts().contains( a ) )
            {
                getLogger().info( "NPANDAY-184-005: Adding custom resolved " + a + " to project.dependencyArtifacts" );

                project.getDependencyArtifacts().add( a );
            }
            else
            {
                getLogger().info( "NPANDAY-184-006: " + a + " is yet in project.dependencyArtifacts" );
            }
        }
    }

    private void createArtifactsForMaven2BackCompat( MavenProject project ) throws InvalidVersionSpecificationException
    {
        project.setDependencyArtifacts( Sets.newHashSet() );
        for ( Object o : project.getDependencies() )
        {
            Dependency d = (Dependency) o;
            project.getDependencyArtifacts().add(
                artifactFactory.createDependencyArtifact(
                    d.getGroupId(), d.getArtifactId(), VersionRange.createFromVersionSpec( d.getVersion() ),
                    d.getType(), d.getClassifier(), d.getScope()
                )
            );
        }
    }
}
