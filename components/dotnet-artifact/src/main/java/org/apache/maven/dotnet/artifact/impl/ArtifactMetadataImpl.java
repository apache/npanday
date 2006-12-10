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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;

import java.io.File;

/**
 * Provides a specialization of <code>ProjectArtifactMetadata</code> for handling of .NET artifacts. Specifically, the
 * this implementation does not use version info within the pom file name.
 *
 * @author Shane Isbell
 */
public final class ArtifactMetadataImpl
    extends ProjectArtifactMetadata
{

    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     *
     * @param artifact the artifact associated with the artifact metadata
     */
    public ArtifactMetadataImpl( Artifact artifact )
    {
        this( artifact, null );
    }

    /**
     *  Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     *
     * @param artifact  the artifact associated with the artifact metadata
     * @param file      the pom file of the artifact
     */
    public ArtifactMetadataImpl( Artifact artifact, File file )
    {
        super( artifact, file );
    }

    /**
     * Returns the file name of the pom located on a remote repository. Unlike its parent class method, the pom
     * file name does not include version info.
     *
     * @return  the file name of the pom located on a remote repository
     * @see org.apache.maven.project.artifact.ProjectArtifactMetadata#getRemoteFilename()
     */
    public String getRemoteFilename()
    {
        return getArtifactId() + ".pom";
    }

    /**
     * Returns the file name of the pom located on a local repository.  Unlike its parent class method, the pom
     * file name does not include version info.
     *
     * @param repository the local artifact repository. This parameter is not used and can be null.
     * @return the file name of the pom located on a local repository
     * @see ProjectArtifactMetadata#getLocalFilename(org.apache.maven.artifact.repository.ArtifactRepository)
     */
    public String getLocalFilename( ArtifactRepository repository )
    {
        return getArtifactId() + ".pom";
    }


}
