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

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class NPandayResolutionListener
    extends AbstractLogEnabled
    implements ResolutionListener
{
    private DefaultNPandayArtifactResolver resolver;

    private ArtifactFilter filter;
    
    private ArtifactRepository localRepository;
    
    private List remoteRepositories;

    public NPandayResolutionListener( DefaultNPandayArtifactResolver resolver, ArtifactFilter filter,
            ArtifactRepository localRepository, List remoteRepositories )
    {
        this.resolver = resolver;
        this.filter = filter;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }

    public void testArtifact( Artifact node )
    {

    }

    public void startProcessChildren( Artifact artifact )
    {
    }

    public void endProcessChildren( Artifact artifact )
    {
    }

    public void includeArtifact( Artifact artifact )
    {
        if ( filter != null && !filter.include( artifact ) )
        {
            return;
        }

        try
        {
            resolver.runArtifactContributors( artifact, localRepository, remoteRepositories );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new RuntimeException( "NPANDAY-159-000: Error finding artifact " + artifact, e );
        }
    }

    public void omitForNearer( Artifact omitted, Artifact kept )
    {
    }

    public void updateScope( Artifact artifact, String scope )
    {
    }

    public void manageArtifact( Artifact artifact, Artifact replacement )
    {
    }

    public void omitForCycle( Artifact artifact )
    {
    }

    public void updateScopeCurrentPom( Artifact artifact, String ignoredScope )
    {
    }

    public void selectVersionFromRange( Artifact artifact )
    {
    }

    public void restrictRange( Artifact artifact, Artifact replacement, VersionRange newRange )
    {
    }
}
