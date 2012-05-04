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

package npanday.resolver.resolvers;

import npanday.ArtifactTypeHelper;
import npanday.PathUtil;
import npanday.resolver.ArtifactResolvingContributor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;

import java.io.File;
import java.util.Set;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class GacResolver
    implements ArtifactResolvingContributor
{
    public void contribute( Artifact artifact, Set<Artifact> additionalDependenciesCollector ) throws
        ArtifactNotFoundException
    {

        File artifactFile = null;
        String artifactType = artifact.getType();

        if ( ArtifactTypeHelper.isDotnetAnyGac( artifactType ) )
        {
            if ( !ArtifactTypeHelper.isDotnet4Gac( artifactType ) )
            {
                artifactFile = PathUtil.getGlobalAssemblyCacheFileFor(
                    artifact, new File( "C:\\WINDOWS\\assembly\\" )
                );
            }
            else
            {
                artifactFile = PathUtil.getGACFile4Artifact( artifact );
            }

            if ( artifactFile.exists() )
            {
                artifact.setFile( artifactFile );
                artifact.setResolved( true );
            }
            else
            {
                throw new ArtifactNotFoundException(
                    "NPANDAY-158-001: Could not resolve gac-dependency " + artifact + ", tried " + artifactFile,
                    artifact
                );
            }
        }

    }
}
