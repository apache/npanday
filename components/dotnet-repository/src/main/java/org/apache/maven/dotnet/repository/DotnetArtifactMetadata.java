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
package npanday.repository;

import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.io.File;

public class DotnetArtifactMetadata
    extends ProjectArtifactMetadata
{

    private Artifact artifact;

    public DotnetArtifactMetadata( Artifact artifact )
    {
        super( artifact );
        this.artifact = artifact;
    }

    public DotnetArtifactMetadata( Artifact artifact, File file )
    {
        super( artifact, file );
        this.artifact = artifact;
    }

    public String getRemoteFilename()
    {
        return getFilename();
    }

    public String getLocalFilename( ArtifactRepository repository )
    {
        return getFilename();
    }

    private String getFilename()
    {
        return ( artifact.getClassifier() != null ) ? artifact.getGroupId() + "-" + artifact.getVersion() + "-" +
            artifact.getClassifier() + ".pom" : artifact.getGroupId() + "-" + artifact.getVersion() + ".pom";
    }
}
