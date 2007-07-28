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

import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.model.netdependency.NetDependency;
import org.apache.maven.dotnet.model.netdependency.NetDependencyModel;
import org.apache.maven.dotnet.model.netdependency.io.xpp3.NetDependencyXpp3Reader;
import org.apache.maven.dotnet.artifact.NetDependenciesRepository;
import org.apache.maven.dotnet.artifact.NetDependencyMatchPolicy;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.factory.ArtifactFactory;

import java.util.List;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;

/**
 * Provides methods for loading and reading the net dependency config file.
 *
 * @author Shane Isbell
 */
public class NetDependenciesRepositoryImpl
    implements NetDependenciesRepository
{

    /**
     * List of net dependencies. These dependencies are intended to be executed directly from the local Maven repository,
     * not to be compiled against.
     */
    private List<NetDependency> netDependencies;

    /**
     * The artifact factory, used for creating artifacts.
     */
    private ArtifactFactory artifactFactory;

    /**
     * Constructor. This method is intended to be invoked by the <code>RepositoryRegistry<code>, not by the
     * application developer.
     */
    public NetDependenciesRepositoryImpl()
    {
    }

    /**
     * @see Repository#load(java.io.InputStream, java.util.Hashtable)
     */
    public void load( InputStream inputStream, Hashtable properties )
        throws IOException
    {
        NetDependencyXpp3Reader xpp3Reader = new NetDependencyXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        NetDependencyModel model;
        try
        {
            model = xpp3Reader.read( reader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NMAVEN-062-000: Could not read net-dependencies.xml" );
        }
        netDependencies = model.getNetDependencies();
        String nmavenVersion = (String) properties.get( "nmaven.version" );
        for ( NetDependency dependency : netDependencies )
        {
            if ( dependency.getVersion() == null && dependency.getGroupId().startsWith( "NMaven" ) )
            {
                dependency.setVersion( nmavenVersion );
            }
        }
    }


    /**
     * @see Repository#setRepositoryRegistry(org.apache.maven.dotnet.registry.RepositoryRegistry)
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
    }

    /**
     * Returns a list of .NET dependencies as given within the net dependencies config file. This dependency list
     * is external to the pom file dependencies. This separation is necessary since some Java Maven plugins
     * - which themselves are necessary for building .NET applications - may have  .NET executable dependencies that
     * have not been built yet and can't be resolved.
     *
     * @return a list of .NET dependencies as given within the net dependencies config file
     */
    public List<Dependency> getDependencies()
    {
        return getDependenciesFor( null );
    }

    /**
     * @see NetDependenciesRepository#getDependenciesFor(java.util.List<org.apache.maven.dotnet.artifact.NetDependencyMatchPolicy>)
     */
    public List<Dependency> getDependenciesFor( List<NetDependencyMatchPolicy> matchPolicies )
    {
        if ( matchPolicies == null )
        {
            matchPolicies = new ArrayList<NetDependencyMatchPolicy>();
        }

        List<Dependency> dependencies = new ArrayList<Dependency>();
        for ( NetDependency netDependency : netDependencies )
        {
            if ( isMatch( netDependency, matchPolicies ) )
            {
                dependencies.add( netDependencyToDependency( netDependency ) );
            }
        }
        return dependencies;
    }

    /**
     * Intializes this repository.
     *
     * @param artifactFactory the artifact factory
     */
    void init( ArtifactFactory artifactFactory )
    {
        this.artifactFactory = artifactFactory;
    }


    /**
     * Return true is the specified net dependency matches ALL of the specified match policies, otherwise returns false.
     *
     * @param netDependency the net dependency to match
     * @param matchPolicies the match policies to use in matching the net dependency
     * @return true is the specified net dependency matches ALL of the specified match policies, otherwise returns false
     */
    private boolean isMatch( NetDependency netDependency, List<NetDependencyMatchPolicy> matchPolicies )
    {
        for ( NetDependencyMatchPolicy matchPolicy : matchPolicies )
        {
            if ( !matchPolicy.match( netDependency ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a list of artifacts that match the specified parameters. If the version or type parameters are null,
     * then the returned list will include all versions and types.
     *
     * @param groupId    the group ID of the artifact to match. This value should not be null.
     * @param artifactId the artifact ID of the artifact to match. This value should not be null.
     * @param version    the version if the artifact to match.
     * @param type       the type of artifact to match
     * @return a list of artifacts that match the specified parameters
     */
    List<Artifact> getArtifactsFor( String groupId, String artifactId, String version, String type )
    {
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( NetDependency netDependency : netDependencies )
        {
            if ( netDependency.getGroupId().equals( groupId ) && netDependency.getArtifactId().equals( artifactId ) &&
                ( version == null || netDependency.getVersion().equals( version ) ) &&
                ( type == null || netDependency.getType().equals( type ) ) )
            {
                artifacts.add( netDependencyToArtifact( netDependency ) );
            }

        }
        return artifacts;
    }

    /**
     * Returns the artifact associated with the specified id.
     *
     * @param id the artifact ID
     * @return the artifact associated with the specified id
     */
    Artifact getArtifactByID( String id )
    {
        for ( NetDependency netDependency : netDependencies )
        {
            if ( netDependency.getId() != null && netDependency.getId().equals( id ) )
            {
                return netDependencyToArtifact( netDependency );
            }
        }
        return null;
    }

    /**
     * Copies the information from a <code>NetDependency</code> object to a <code>Dependency</code> object. This method
     * is for converting from an NMaven specific model to a Maven model that can be used within the general Maven
     * framework. Note that all artifacts automatically have a runtime scope since <code>NetDependencies</code> are
     * always executables that are intended to be executed directly from the local Maven repository.
     *
     * @param netDependency the net dependency, which is the source to copy information from.
     * @return dependency
     */
    private Dependency netDependencyToDependency( NetDependency netDependency )
    {
        Dependency dependency = new Dependency();
        dependency.setArtifactId( netDependency.getArtifactId() );
        dependency.setGroupId( netDependency.getGroupId() );
        dependency.setType( netDependency.getType() );
        dependency.setScope( Artifact.SCOPE_RUNTIME );
        dependency.setVersion( netDependency.getVersion() );
        dependency.setClassifier( netDependency.getPublicKeyToken() );
        return dependency;
    }

    /**
     * Creates an artifact based on the information from a <code>NetDepencency</code> object. Note that all artifacts
     * automatically have a runtime scope since <code>NetDependencies</code> are always executables that
     * are intended to be executed directly from the local Maven repository.
     *
     * @param dependency the net dependency, which is the source to copy information from.
     * @return artifact
     */
    private Artifact netDependencyToArtifact( NetDependency dependency )
    {
        return artifactFactory.createDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(),
                                                         VersionRange.createFromVersion( dependency.getVersion() ),
                                                         dependency.getType(), dependency.getPublicKeyToken(),
                                                         Artifact.SCOPE_RUNTIME, null );
    }
}
