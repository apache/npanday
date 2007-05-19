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
package org.apache.maven.dotnet.embedder.impl;

import org.apache.maven.dotnet.embedder.Artifact;
import org.apache.maven.model.Model;

/**
 * Provides an implementation of artifact.
 *
 * @author Shane Isbell
 */
public class ArtifactImpl
    implements Artifact
{
    private String pomPath;

    private String groupId;

    private String artifactId;

    private String version;

    private Model model;

    /**
     * Constructor. This method is intended to by invoked by xfire, not by the application developer.
     */
    public ArtifactImpl()
    {
    }

    public String getPomPath()
    {
        return pomPath;
    }

    public void setPomPath( String pomPath )
    {
        this.pomPath = pomPath;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public Model getModel()
    {
        return model;
    }

    public void setModel( Model model )
    {
        this.model = model;
    }
}
