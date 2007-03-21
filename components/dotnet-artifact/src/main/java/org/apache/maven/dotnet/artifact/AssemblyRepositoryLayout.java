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
package org.apache.maven.dotnet.artifact;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.metadata.ArtifactMetadata;

import java.io.File;

/**
 * Repository Layout for .NET Assemblies:
 * follows the format ${groupId}/${artifactId}/${version}/${.classifier}/${id}.{extension}
 *
 * @author Shane Isbell
 */
public class AssemblyRepositoryLayout
    implements ArtifactRepositoryLayout
{

    /**
     * Default constructor.
     */
    public AssemblyRepositoryLayout()
    {
    }

    /**
     * Returns the relative path (from the repository root) of the specified artifact.
     *
     * @param artifact the artifact to determine the path of.
     * @return the relative path (from the repository root) of the specified artifact
     * @throws NullPointerException if artifact is null
     */
    public String pathOf( Artifact artifact )
    {
        if ( artifact.getType().equals( "pom" ) )//Use standard format for pom packaging
        {
            ArtifactRepositoryLayout defaultLayout = new DefaultRepositoryLayout();
            return defaultLayout.pathOf( artifact );
        }

        StringBuffer artifactPath = new StringBuffer();
        for ( String groupId : artifact.getGroupId().split( "[.]" ) )
        {
            artifactPath.append( groupId ).append( File.separator );
        }

        artifactPath.append( artifact.getArtifactId() ).append( File.separator ).append( artifact.getBaseVersion() ).
            append( File.separator );
        if ( artifact.hasClassifier() )
        {
            artifactPath.append( artifact.getClassifier() ).append( File.separator );
        }

        artifactPath.append( artifact.getArtifactId() ).append( "." ).append(
            ( artifact.getArtifactHandler() ).getExtension() );
        return artifactPath.toString();
    }

    /**
     * Returns the path (relative to the specified local repository) of an artifact's metadata.
     *
     * @param metadata   the artifact metadata. This value may not be null.
     * @param repository the artifact repository that contains the metadata. This value may not be null.
     * @return the path of an artifact's metadata within the specified repository
     */
    public String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository )
    {

        StringBuffer path = new StringBuffer();
        for ( String groupId : metadata.getGroupId().split( "[.]" ) )
        {
            path.append( groupId ).append( File.separator );
        }

        if ( !metadata.storedInGroupDirectory() )
        {
            path.append( metadata.getArtifactId() ).append( File.separator );
            if ( metadata.storedInArtifactVersionDirectory() )
            {
                path.append( metadata.getBaseVersion() ).append( File.separator );
            }
        }
        return path.append( metadata.getLocalFilename( repository ) ).toString();
    }

    /**
     * Returns the path of an artifact's metadata within the the remote repository. This method is here because it is
     * part of the required API but it is not used within the context of the invoking framework.
     *
     * @param metadata the artifact metadata. This may not be null.
     * @return the path of an artifact's metadata within the specified repository.
     */
    public String pathOfRemoteRepositoryMetadata( ArtifactMetadata metadata )
    {
        StringBuffer path = new StringBuffer();
        for ( String groupId : metadata.getGroupId().split( "[.]" ) )
        {
            path.append( groupId ).append( File.separator );
        }

        if ( !metadata.storedInGroupDirectory() )
        {
            path.append( metadata.getArtifactId() ).append( File.separator );
            if ( metadata.storedInArtifactVersionDirectory() )
            {
                path.append( metadata.getBaseVersion() ).append( File.separator );
            }
        }
        return path.append( metadata.getRemoteFilename() ).toString();
    }
}
