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
package org.apache.maven.dotnet.dao;

import java.util.Set;
import java.util.HashSet;

/**
 * Class for accessing information about a project.
 */
public class Project
{
    /**
     * The group id of the project
     */
    private String groupId;

    /**
     * The artifact id of the project
     */
    private String artifactId;

    /**
     * The version of the project
     */
    private String version;

    /**
     * The public key token id (classifier) of the project
     */
    private String publicKeyTokenId;

    /**
     * Have the project artifacts been resolved
     */
    private boolean isResolved = false;

    /**
     * The type of artifact: library, exe, winexe, netmodule
     */
    private String artifactType = "library";

    /**
     * The set of project dependencies for this project
     */
    private Set<ProjectDependency> projectDependencies = new HashSet<ProjectDependency>();

    /**
     * The set of requirements for this project
     */
    private Set<Requirement> requirements = new HashSet<Requirement>();

    /**
     * The parent project
     */
    private Project parentProject;

    /**
     * Returns the parent project
     *
     * @return the parent project
     */
    public Project getParentProject()
    {
        return parentProject;
    }

    /**
     * Sets the parent project
     *
     * @param parentProject the parent project
     */
    public void setParentProject( Project parentProject )
    {
        this.parentProject = parentProject;
    }

    /**
     * Adds a requirement to the project.
     *
     * @param requirement a requirement needed for the artifact to run
     */
    public void addRequirement( Requirement requirement )
    {
        requirements.add( requirement );
    }

    /**
     * Returns the set of requirements for the project.
     *
     * @return he set of requirements for the project
     */
    public Set<Requirement> getRequirements()
    {
        return requirements;
    }

    /**
     * Sets all requirements for the project. This will override any requirements added through Project#addRequirement
     *
     * @param requirements the project requirements
     */
    public void setRequirements( Set<Requirement> requirements )
    {
        this.requirements = requirements;
    }

    public String getPublicKeyTokenId()
    {
        return publicKeyTokenId;
    }

    public void setPublicKeyTokenId( String publicKeyTokenId )
    {
        this.publicKeyTokenId = publicKeyTokenId;
    }

    public boolean isResolved()
    {
        return isResolved;
    }

    public void setResolved( boolean resolved )
    {
        isResolved = resolved;
    }

    public String getArtifactType()
    {
        return artifactType;
    }

    public void setArtifactType( String artifactType )
    {
        this.artifactType = artifactType;
    }

    public Set<ProjectDependency> getProjectDependencies()
    {
        return projectDependencies;
    }

    public void addProjectDependency( ProjectDependency projectDependency )
    {
        projectDependencies.add( projectDependency );
    }

    public void setProjectDependencies( Set<ProjectDependency> projectDependencies )
    {
        this.projectDependencies = projectDependencies;
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

        final Project project = (Project) o;

        if ( !artifactId.equals( project.artifactId ) )
        {
            return false;
        }
        if ( !groupId.equals( project.groupId ) )
        {
            return false;
        }
        if ( !version.equals( project.version ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = groupId.hashCode();
        result = 29 * result + artifactId.hashCode();
        result = 29 * result + version.hashCode();
        return result;
    }
}
