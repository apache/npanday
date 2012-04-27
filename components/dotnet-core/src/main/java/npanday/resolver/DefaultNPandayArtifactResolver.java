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

import com.google.common.collect.Sets;
import npanday.resolver.resolvers.GacResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.WarningResolutionListener;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wraps the default Maven artifact resolver and executes all implementations
 * of {@link npanday.resolver.ArtifactResolvingContributor} for each artiact.
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class DefaultNPandayArtifactResolver
    extends AbstractLogEnabled
    implements NPandayArtifactResolver, Contextualizable, Initializable
{
    private ArtifactResolvingContributor[] contributors;

    ArtifactResolver original;

    private PlexusContainer container;

    private Set<Artifact> customResolveCache = Sets.newHashSet();

    public void resolve( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        runCustomResolvers( artifact );

        original.resolve(
            artifact, remoteRepositories, localRepository
        );
    }

    public void resolveAlways( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        runCustomResolvers( artifact );

        original.resolveAlways(
            artifact, remoteRepositories, localRepository
        );
    }

    public ArtifactResolutionResult resolveTransitively(
        Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository,
        List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter, List listeners ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        filter = intercept( filter );

        return original.resolveTransitively(
            artifacts, originatingArtifact, managedVersions, localRepository, remoteRepositories, source, filter,
            listeners
        );
    }

    private void runCustomResolvers( Artifact artifact )
    {
        if ( contributors == null || contributors.length == 0 )
        {
            contributors = new ArtifactResolvingContributor[]{ new GacResolver() };
        }

        for ( ArtifactResolvingContributor contributor : contributors )
        {
            if ( !artifact.isResolved() )
            {
                Set<Artifact> additionalDependenciesCollector = Sets.newHashSet();
                contributor.contribute( artifact, additionalDependenciesCollector);

                if (additionalDependenciesCollector.size() > 0){
                    getLogger().error(
                        "NPANDAY-147-006: " + artifact.getId()
                            + " required additional dependencies to be added, but we do not support that yet."
                    );
                }

                if ( artifact.isResolved() )
                {
                    customResolveCache.add( artifact );

                    getLogger().info(
                        "NPANDAY-147-001: " + contributor.getClass().getName() + " resolved " + artifact.getId()
                            + " to " + artifact.getFile()
                    );
                }
            }
        }
    }

    private ArtifactFilter intercept( ArtifactFilter filter )
    {
        AndArtifactFilter andArtifactFilter = new AndArtifactFilter();
        if ( filter != null )
        {
            andArtifactFilter.add( filter );
        }
        andArtifactFilter.add(
            new ArtifactFilter()
            {
                public boolean include( Artifact artifact )
                {
                    runCustomResolvers( artifact );
                    return true;
                }
            }
        );
        return andArtifactFilter;
    }

    public void contextualize( Context context ) throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize() throws InitializationException
    {
        try
        {
            List list = container.lookupList(
                ArtifactResolvingContributor.role
            );

            contributors = (ArtifactResolvingContributor[]) list.toArray( new ArtifactResolvingContributor[0] );

            if (contributors.length == 0) {
                getLogger().warn( "NPANDAY-147-005: could not find any custom artifact resolving contributors!" );
            }
            else{
                getLogger().debug( "NPANDAY-147-004: resolved " + contributors.length + " contributors: " + list );
            }
        }
        catch ( ComponentLookupException e )
        {
            getLogger().error( "NPANDAY-147-003: could not resolve contributors" );
        }
    }

    /* Copies of the convenience overloads in DefaultArtifactResolver */

    public ArtifactResolutionResult resolveTransitively( Set artifacts, Artifact originatingArtifact,
                                                         Map managedVersions, ArtifactRepository localRepository,
                                                         List remoteRepositories, ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository,
                                    remoteRepositories, source, null );
    }

    public ArtifactResolutionResult resolveTransitively( Set artifacts, Artifact originatingArtifact,
                                                         Map managedVersions, ArtifactRepository localRepository,
                                                         List remoteRepositories, ArtifactMetadataSource source,
                                                         ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO: this is simplistic
        List listeners = new ArrayList();
        if ( getLogger().isDebugEnabled() )
        {
            listeners.add( new DebugResolutionListener( getLogger() ) );
        }

        listeners.add( new WarningResolutionListener( getLogger() ) );

        return resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository,
                                    remoteRepositories, source, filter, listeners );

    }

    public ArtifactResolutionResult resolveTransitively( Set artifacts, Artifact originatingArtifact,
                                                         ArtifactRepository localRepository, List remoteRepositories,
                                                         ArtifactMetadataSource source, ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, Collections.EMPTY_MAP, localRepository,
                                    remoteRepositories, source, filter );

    }

    public ArtifactResolutionResult resolveTransitively( Set artifacts, Artifact originatingArtifact,
                                                         List remoteRepositories, ArtifactRepository localRepository,
                                                         ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, localRepository, remoteRepositories, source, null );
    }

    public ArtifactResolutionResult resolveTransitively( Set artifacts, Artifact originatingArtifact,
                                                         List remoteRepositories, ArtifactRepository localRepository,
                                                         ArtifactMetadataSource source, List listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, Collections.EMPTY_MAP, localRepository,
                                    remoteRepositories, source, null, listeners );
    }

    public Set<Artifact> getCustomResolveCache()
    {
        return customResolveCache;
    }
}
