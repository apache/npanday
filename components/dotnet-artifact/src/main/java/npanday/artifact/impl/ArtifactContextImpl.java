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

import java.util.*;
import java.io.File;

import npanday.ArtifactTypeHelper;
import npanday.artifact.*;
import npanday.registry.RepositoryRegistry;
import npanday.PathUtil;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

/**
 * Provides an implemenation of the <code>ArtifactContext</code> interface.
 *
 * @author Shane Isbell
 */
public final class ArtifactContextImpl
    implements ArtifactContext, LogEnabled
{

    /**
     * A factory component for creating artifacts
     */
    private ArtifactFactory artifactFactory;

    /**
     * An installer component for installing artifacts into a local Maven repository.
     */
    private npanday.artifact.ArtifactInstaller artifactInstaller;

    /**
     * A registry component of repository (config) files
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The maven project
     */
    private MavenProject project;

    /**
     * Root path of the local Maven repository
     */
    private File localRepository;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

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
     * @see ArtifactContext#getArtifactsFor(String, String, String, String)
     */
    public List<Artifact> getArtifactsFor( String groupId, String artifactId, String version, String type )
    {
        NetDependenciesRepositoryImpl repository =
            (NetDependenciesRepositoryImpl) repositoryRegistry.find( "net-dependencies" );
        if ( repository == null )
        {
            logger.warn(
                "NPANDAY-000-001: Could not locate artifact (net dependencies repository not found): Group ID = " +
                    groupId + ", Artifact ID = " + artifactId + ", Version = " + version + ", Type = " + type );
            return new ArrayList<Artifact>();
        }
        repository.init( artifactFactory );
        List<Artifact> artifacts = repository.getArtifactsFor( groupId, artifactId, version, type );
        for ( Artifact artifact : artifacts )
        {
            logger.debug("NPANDAY-000-002: set file");        
            artifact.setFile( PathUtil.getMavenLocalRepositoryFileFor( artifact, localRepository ) );
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
     * @see npanday.artifact.ArtifactContext#getArtifactInstaller()
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
     * @see ArtifactContext#getNetModulesFor(org.apache.maven.artifact.Artifact)
     */
    //TODO: support temporarily removed
    public List<Artifact> getNetModulesFor( Artifact artifact )
        throws ArtifactException
    {
        if ( artifact == null )
        {
            throw new ArtifactException( "NPANDAY-000-002: Cannot get .NET modules dependencies of a null artifact" );
        }
        List<ArtifactMatchPolicy> matchPolicies = new ArrayList<ArtifactMatchPolicy>();
        matchPolicies.add( new NetModuleMatchPolicy() );
        return null;
        //return getDirectDependenciesFor( artifact, matchPolicies );
    }

    /**
     * @see ArtifactContext#init(org.apache.maven.project.MavenProject,java.util.List, File)
     */
    public void init( MavenProject mavenProject, List<ArtifactRepository> remoteArtifactRepositories,
                      File localRepository )
    {
        this.project = mavenProject;
        this.localRepository = localRepository;
        artifactInstaller.init( this, remoteArtifactRepositories, localRepository );
    }

    /*
    * Matches .NET module artifacts.
    */
    private static class NetModuleMatchPolicy
        implements ArtifactMatchPolicy
    {

        /**
         * Matches artifacts of type module
         *
         * @param artifact the artifact to match
         * @return true if artifact is of type module, otherwise returns false.
         */
        public boolean match( Artifact artifact )
        {
            return ArtifactTypeHelper.isDotnetModule( artifact.getType() );
        }
    }
}
