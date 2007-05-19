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

import org.apache.maven.dotnet.embedder.MavenProject;
import org.apache.maven.model.Model;

import java.util.Set;

/**
 * Provides an implementation of the MavenProject.
 *
 * @author Shane Isbell
 */
public class MavenProjectImpl
    implements MavenProject
{

    private String pomPath;

    private String groupId;

    private String artifactId;

    private String version;

    private Set<MavenProject> mavenProjects;

    private Model model;

    private boolean isOrphaned = false;

    /**
     * Constructor. This method is intended to by invoked by xfire, not by the application developer.
     */    
    public MavenProjectImpl()
    {
    }

    public Set<MavenProject> getMavenProjects()
    {
        return mavenProjects;
    }

    public Model getModel()
    {
        return model;
    }

    public void setModel( Model model )
    {
        this.model = model;
    }

    public void setMavenProjects( Set<MavenProject> mavenProjects )
    {
        this.mavenProjects = mavenProjects;
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

    public boolean isOrphaned() {
        return isOrphaned;
    }

    public void setIsOrphaned(boolean isOrphaned)
    {
        this.isOrphaned = isOrphaned;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final MavenProjectImpl that = (MavenProjectImpl) o;

        if ( !pomPath.equals( that.pomPath ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return pomPath.hashCode();
    }

    public int compareTo( Object o )
        throws ClassCastException
    {
        MavenProject mavenProject = (MavenProject) o;
        int otherRank = getRank( mavenProject.getPomPath() );
        int thisRank = getRank( pomPath );
        if ( otherRank < thisRank )
        {
            return -1;
        }
        else if ( otherRank > thisRank )
        {
            return 1;
        }
        return 0;
    }

    private int getRank( String path )
    {
        return path.split( "[/]" ).length;
    }
}
