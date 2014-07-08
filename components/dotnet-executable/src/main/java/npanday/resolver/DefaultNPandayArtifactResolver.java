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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.WarningResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Wraps the default Maven artifact resolver and executes all implementations
 * of {@link npanday.resolver.ArtifactResolvingContributor} for each artiact.
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class DefaultNPandayArtifactResolver
    extends AbstractLogEnabled
    implements NPandayArtifactResolver, Initializable
{
    private List<ArtifactResolvingContributor> contributors;

    ArtifactResolver original;

    private Set<Artifact> customResolveCache = Sets.newHashSet();
    private Set<Artifact> customDependenciesCache = Sets.newHashSet();

    private ArtifactFilter filter;

    public void resolve( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        runCustomResolvers( artifact );

        original.resolve(
            artifact, remoteRepositories, localRepository
        );
        
        runCustomDependencyContributors(artifact, localRepository, remoteRepositories);
    }

    public void resolveAlways( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        runCustomResolvers( artifact );

        original.resolveAlways(
            artifact, remoteRepositories, localRepository
        );
        
        runCustomDependencyContributors(artifact, localRepository, remoteRepositories);
    }

    public ArtifactResolutionResult resolveTransitively(
        Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository,
        List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter, List listeners ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        listeners = intercept( listeners, filter, localRepository, remoteRepositories );

        return original.resolveTransitively(
            artifacts, originatingArtifact, managedVersions, localRepository, remoteRepositories, source, filter,
            listeners
        );
        
    }



    private List intercept( List listeners, ArtifactFilter filter, ArtifactRepository localRepository,
            List remoteRepositories )
    {

        if (listeners == null)
            listeners = Lists.newArrayList();


        NPandayResolutionListener listener = new NPandayResolutionListener(this, filter, localRepository, 
                remoteRepositories);
        listener.enableLogging( getLogger() );

        listeners.add( listener );

        return listeners;
    }

    public void runArtifactContributors(Artifact artifact, ArtifactRepository localRepository,
            List remoteRepositories) throws ArtifactNotFoundException 
    {
        if(!artifact.isResolved()) {
            runCustomResolvers(artifact);
        }

        if(artifact.isResolved()) {
            runCustomDependencyContributors(artifact, localRepository, remoteRepositories);
        }
    }

    protected void runCustomResolvers( Artifact artifact ) throws ArtifactNotFoundException
    {
        if ( artifact.isResolved() )
        {
            return;
        }

        getLogger().debug(
            "NPANDAY-147-007: trying to resolve " + artifact.getId() + " using resolving contributors"
        );

        for ( ArtifactResolvingContributor contributor : contributors )
        {
            Set<Artifact> additionalDependenciesCollector = Sets.newHashSet();
            contributor.tryResolve( artifact, additionalDependenciesCollector, filter );

            if ( artifact.isResolved() )
            {
                customResolveCache.add( artifact );

                getLogger().info(
                    "NPANDAY-147-001: " + contributor.getClass().getName() + " resolved " + artifact.getId() + " to "
                        + artifact.getFile()
                );

                if ( additionalDependenciesCollector.size() > 0 )
                {
                   addContributeArtifactsToDependenciesCache(additionalDependenciesCollector);
                }
                return;
            }
        }
    }
    
    protected void runCustomDependencyContributors(Artifact artifact, ArtifactRepository localRepository, List remoteRepositories) throws ArtifactNotFoundException
    {
        if ( !artifact.isResolved() )
        {
            throw new IllegalArgumentException(
                    String.format("NPANDAY-147-008: Artifact[groupId:'%s', artifactId:'%s', type:'%s', version:'%s']" +
                    		" could not be unresolved",
                            artifact.getGroupId(), artifact.getArtifactId(), artifact.getType(), artifact.getVersion()));
        }

        getLogger().debug(
                "NPANDAY-147-009: trying to find complementary artifacts for " + artifact.getId() + " using resolving contributors"
        );

        for ( ArtifactResolvingContributor contributor : contributors )
        {
            Set<Artifact> additionalDependenciesCollector = Sets.newHashSet();
            contributor.contribute(artifact, localRepository, remoteRepositories, additionalDependenciesCollector, filter);

            if ( additionalDependenciesCollector.size() > 0 )
            {
               addContributeArtifactsToDependenciesCache(additionalDependenciesCollector);
            }
        }

    }
    
    private void addContributeArtifactsToDependenciesCache(Set<Artifact> artifacts)
    {
    	if (artifacts == null || artifacts.isEmpty()) 
    	{
    		return;
    	}
    	
		Set<Artifact> resolvedArtifacts = Sets.filter(artifacts, new Predicate<Artifact>(){
			public boolean apply(@Nullable Artifact artifact) {
				return artifact !=null && artifact.isResolved() && artifact.getFile() != null 
						&& artifact.getFile().exists();
			}});
		
		customDependenciesCache.addAll(resolvedArtifacts);
    }

    public void initialize() {
        if (contributors.size() == 0) {
            getLogger().warn( "NPANDAY-147-005: could not find any custom artifact resolving contributors!" );
        }
        else{
            getLogger().debug( "NPANDAY-147-004: resolved " + contributors.size() + " contributors: " + contributors );
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

    public void initializeWithFilter(ArtifactFilter filter) {
        this.filter = filter;
    }

    public Set<Artifact> getCustomResolveCache()
    {
        return customResolveCache;
    }
    
    public Set<Artifact> getCustomDependenciesCache() 
    {
        return customDependenciesCache;
    }
}
