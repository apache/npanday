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
package npanday.repository.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.util.FileUtils;

public class ArtifactResolverTestStub implements ArtifactResolver
{

    public void resolve( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository )
    throws ArtifactResolutionException, ArtifactNotFoundException
    {
        resolve( artifact, remoteRepositories, localRepository, false );
    }

    public void resolveAlways( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        resolve( artifact, remoteRepositories, localRepository, true );
    }

    private void resolve( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository, boolean force )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        //do nothing
    }

    public ArtifactResolutionResult resolveTransitively( Set arg0, Artifact arg1, List arg2, ArtifactRepository arg3,
                                                         ArtifactMetadataSource arg4 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ArtifactResolutionResult resolveTransitively( Set arg0, Artifact arg1, List arg2, ArtifactRepository arg3,
                                                         ArtifactMetadataSource arg4, List arg5 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ArtifactResolutionResult resolveTransitively( Set arg0, Artifact arg1, ArtifactRepository arg2, List arg3,
                                                         ArtifactMetadataSource arg4, ArtifactFilter arg5 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ArtifactResolutionResult resolveTransitively( Set arg0, Artifact arg1, Map arg2, ArtifactRepository arg3,
                                                         List arg4, ArtifactMetadataSource arg5 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ArtifactResolutionResult resolveTransitively( Set arg0, Artifact arg1, Map arg2, ArtifactRepository arg3,
                                                         List arg4, ArtifactMetadataSource arg5, ArtifactFilter arg6 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ArtifactResolutionResult resolveTransitively( Set arg0, Artifact arg1, Map arg2, ArtifactRepository arg3,
                                                         List arg4, ArtifactMetadataSource arg5, ArtifactFilter arg6,
                                                         List arg7 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

}