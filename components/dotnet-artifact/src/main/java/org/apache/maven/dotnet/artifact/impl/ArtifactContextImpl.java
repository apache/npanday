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

import java.util.*;
import java.io.File;

import org.apache.maven.dotnet.artifact.*;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.*;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;

/**
 * Provides an implemenation of the <code>ArtifactContext</code> interface.
 *
 * @author Shane Isbell
 */
public final class ArtifactContextImpl
    implements ArtifactContext, LogEnabled
{

    /**
     * A layout component used in the creation of a .NET artifact repository.
     */
    private ArtifactRepositoryLayout layout;

    /**
     * Metadata component used by the <code>ArtifactResolver</code>.
     */
    private ArtifactMetadataSource metadata;

    /**
     * A factory component for creating artifacts
     */
    private ArtifactFactory artifactFactory;

    /**
     * An installer component for installing artifacts into a local Maven repository.
     */
    private org.apache.maven.dotnet.artifact.ArtifactInstaller artifactInstaller;

    /**
     * A registry component of repository (config) files
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * An artifact resolver component for locating artifacts and pulling them into the local repo if they do not
     * already exist.
     */
    private ArtifactResolver artifactResolver;

    /**
     * The maven project
     */
    private MavenProject project;

    /**
     * Root path of the local Maven repository
     */
    private String localRepository;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    private List<ArtifactHandler> artifactHandlers;

    private ArtifactHandlerManager artifactHandlerManager;

    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     */
    public ArtifactContextImpl()
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
     * @see org.apache.maven.dotnet.artifact.ArtifactContext#getLocalRepository()
     */
    public File getLocalRepository()
    {
        return new File( localRepository );
    }

    /**
     * @see ArtifactContext#getArtifactsFor(String, String, String, String)
     */
    public List<Artifact> getArtifactsFor( String groupId, String artifactId, String version, String type )
    {
        NetDependenciesRepositoryImpl repository =
            (NetDependenciesRepositoryImpl) repositoryRegistry.find( "net-dependencies" );
        if ( repository == null )
        {
            logger.warn(
                "NMAVEN-000-009: Could not locate artifact (net dependencies repository not found): Group ID = " +
                    groupId + ", Artifact ID = " + artifactId + ", Version = " + version + ", Type = " + type );
            return new ArrayList<Artifact>();
        }
        repository.init( artifactFactory );
        List<Artifact> artifacts = repository.getArtifactsFor( groupId, artifactId, version, type );
        AssemblyRepositoryLayout layout = new AssemblyRepositoryLayout();
        for ( Artifact artifact : artifacts )
        {
            artifact.setFile( new File( localRepository + File.separator + layout.pathOf( artifact ) ) );
        }
        return artifacts;
    }

    /**
     * @see ArtifactContext#getArtifactByID(String)
     */
    public Artifact getArtifactByID( String id )
    {
        NetDependenciesRepositoryImpl repository =
            (NetDependenciesRepositoryImpl) repositoryRegistry.find( "net-dependencies" );
        repository.init( artifactFactory );
        return repository.getArtifactByID( id );
    }

    /**
     * @see org.apache.maven.dotnet.artifact.ArtifactContext#getArtifactInstaller()
     */
    public ArtifactInstaller getArtifactInstaller()
    {
        return artifactInstaller;
    }

    /**
     * @see ArtifactContext#getApplicationConfigFor(org.apache.maven.artifact.Artifact)
     */
    public ApplicationConfig getApplicationConfigFor( Artifact artifact )
    {
        return ApplicationConfig.Factory.createDefaultApplicationConfig( artifact, project.getBasedir(), new File(
            project.getBuild().getDirectory() ) );
    }

    /**
     * @see ArtifactContext#getDirectDependenciesFor(org.apache.maven.artifact.Artifact,
     *      java.util.List<org.apache.maven.dotnet.artifact.ArtifactMatchPolicy>)
     */
    public List<Artifact> getDirectDependenciesFor( Artifact artifact, List<ArtifactMatchPolicy> matchPolicies )
        throws ArtifactException
    {
        if ( artifact == null )
        {
            throw new ArtifactException( "NMAVEN-000-001: Cannot get dependenct artifacts of a null artifact" );
        }

        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<ArtifactMatchPolicy>();
        }

        List<Artifact> dependencies = new ArrayList<Artifact>();
        ArtifactResolutionResult result;
        ArtifactRepository ar = new DefaultArtifactRepository( "local", "file://" + localRepository, layout );

        Set depSet = new HashSet();
        List<Dependency> dep = project.getDependencies();

        for ( Dependency dependency : dep )
        {
            String scope = ( dependency.getScope() == null ) ? Artifact.SCOPE_COMPILE : dependency.getScope();
            Artifact art = artifactFactory.createDependencyArtifact( dependency.getGroupId(),
                                                                     dependency.getArtifactId(),
                                                                     VersionRange.createFromVersion(
                                                                         dependency.getVersion() ),
                                                                     dependency.getType(), dependency.getClassifier(),
                                                                     scope, null );
            if ( !art.getType().startsWith( "gac" ) )
            {
                depSet.add( art );
            }
        }

        try
        {
            result = artifactResolver.resolveTransitively( depSet, project.getArtifact(),
                                                           project.getRemoteArtifactRepositories(), ar, metadata );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ArtifactException( "NMAVEN-000-002: Failed to Resolve Artifact: File = " + artifact.getFile() +
                ", Local Repository = " + localRepository, e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new ArtifactException( "NMAVEN-000-003: Artifact Not Found: File = " + artifact.getFile() +
                ", Local Repository = " + localRepository, e );
        }
        Set<ResolutionNode> nodes = result.getArtifactResolutionNodes();
        if ( nodes.size() == 0 )
        {
            logger.debug(
                "NMAVEN-000-004: Project has no direct or transitive dependencies. " + project.getArtifacts().size() );
        }

        for ( ResolutionNode node : nodes )
        {
            Artifact nodeArtifact = node.getArtifact();
            boolean isMatched = matchArtifacts( nodeArtifact, matchPolicies );
            logger.debug( "NMAVEN-000-005: Artifact = " + nodeArtifact.getFile().getAbsolutePath() + ", Depth = " +
                node.getDepth() + ", Matches Policies = " + isMatched );
            if ( isMatched && node.getDepth() == 1 )
            {
                dependencies.add( nodeArtifact );
            }
        }

        logger.debug( "NMAVEN-000-006: Resolved Artifact Dependencies: Count = " + dependencies.size() );
        return dependencies;
    }

    /**
     * @see ArtifactContext#getNetModulesFor(org.apache.maven.artifact.Artifact)
     */
    public List<Artifact> getNetModulesFor( Artifact artifact )
        throws ArtifactException
    {
        if ( artifact == null )
        {
            throw new ArtifactException( "NMAVEN-000-007: Cannot get .NET modules dependencies of a null artifact" );
        }
        List<ArtifactMatchPolicy> matchPolicies = new ArrayList<ArtifactMatchPolicy>();
        matchPolicies.add( new NetModuleMatchPolicy() );
        return getDirectDependenciesFor( artifact, matchPolicies );
    }

    public List<Artifact> getAllNetArtifactsFromRepository( File repository )
    {
        return null;
    }

    /**
     * @see ArtifactContext#init(org.apache.maven.project.MavenProject,java.util.List, File
     */
    public void init( MavenProject mavenProject, List<ArtifactRepository> remoteArtifactRepositories,
                      File localRepository )
    {
        this.project = mavenProject;
        this.localRepository = localRepository.getAbsolutePath();
        artifactInstaller.init( this, mavenProject, remoteArtifactRepositories, localRepository );
        Map<String, ArtifactHandler> map = new HashMap<String, ArtifactHandler>();
        for ( ArtifactHandler artifactHandler : artifactHandlers )
        {
            //If I add a handler that already exists, the runtime breaks.
            if ( isDotNetHandler( artifactHandler ) )
            {
                map.put( artifactHandler.getPackaging(), artifactHandler );
            }
        }
        artifactHandlerManager.addHandlers( map );
    }

    private boolean isDotNetHandler( ArtifactHandler artifactHandler )
    {
        String extension = artifactHandler.getExtension();
        return extension.equals( "dll" ) || extension.equals( "nar" ) || extension.equals( "exe" ) ||
            extension.equals( "exe.config" );
    }

    /**
     * Returns true if the artifact matches <i>all</i> match policies, otherwise returns false.
     *
     * @param artifact      the artifact to match against the match policies
     * @param matchPolicies the match policies
     * @return true if the artifact matches <i>all</i> match policies, otherwise returns false
     */
    private boolean matchArtifacts( Artifact artifact, List<ArtifactMatchPolicy> matchPolicies )
    {
        for ( ArtifactMatchPolicy matchPolicy : matchPolicies )
        {
            if ( !matchPolicy.match( artifact ) )
            {
                return false;
            }
        }
        return true;
    }
}
