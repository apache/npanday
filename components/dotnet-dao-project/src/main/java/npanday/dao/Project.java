package npanday.dao;

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
     * Has the project artifact been resolved
     */
    private boolean isResolved = false;

    /**
     * The type of artifact: {@see ArtifactType}
     */
    private String artifactType = "dotnet-library";

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
     * @return the set of requirements for the project
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

    /**
     * Returns the public key token id (classifier) of the project. This is the id from creating a strong name for an
     * assembly.
     *
     * @return the public key token id (classifier) of the project
     */
    public String getPublicKeyTokenId()
    {
        return publicKeyTokenId;
    }

    /**
     * Sets the public key token id (classifier) of the project.
     *
     * @param publicKeyTokenId the public key token id (classifier) of the project
     */
    public void setPublicKeyTokenId( String publicKeyTokenId )
    {
        this.publicKeyTokenId = publicKeyTokenId;
    }

    /**
     * Returns true is the project artifact been resolved, otherwise false. An artifact is considered resolved if the
     * assembly exists either within the user assembly cache or the global assembly cache and if the meta-data for
     * the assembly has been persisted.
     *
     * @return true is the project artifact been resolved, otherwise false
     */
    public boolean isResolved()
    {
        return isResolved;
    }

    /**
     * Set if the artifact has been resolved.
     *
     * @param resolved has the artifact been resolved
     */
    public void setResolved( boolean resolved )
    {
        isResolved = resolved;
    }

    /**
     * Returns the type of artifact: library, exe, winexe, netmodule.
     *
     * @return the type of artifact: library, exe, winexe, netmodule
     */
    public String getArtifactType()
    {
        return artifactType;
    }

    /**
     * Sets the type of artifact: library, exe, winexe, netmodule.
     *
     * @param artifactType the type of artifact: library, exe, winexe, netmodule
     */
    public void setArtifactType( String artifactType )
    {
        this.artifactType = artifactType;
    }

    /**
     * Returns the set of project dependencies for this project.
     *
     * @return the set of project dependencies for this project
     */
    public Set<ProjectDependency> getProjectDependencies()
    {
        return projectDependencies;
    }

    /**
     * Adds a project dependency for this project.
     *
     * @param projectDependency a project dependency for this project
     */
    public void addProjectDependency( ProjectDependency projectDependency )
    {
        projectDependencies.add( projectDependency );
    }

    /**
     * Sets the set of project dependencies for this project. This will override any dependencies added through
     * Project#addProjectDependency.
     *
     * @param projectDependencies the set of project dependencies for this project
     */
    public void setProjectDependencies( Set<ProjectDependency> projectDependencies )
    {
        this.projectDependencies = projectDependencies;
    }

    /**
     * Returns the group id of the project.
     *
     * @return the group id of the project
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Sets the group id of the project.
     *
     * @param groupId the group id of the project
     */
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /**
     * Returns the artifact id of the project.
     *
     * @return the artifact id of the project
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Sets the artifact id of the project.
     *
     * @param artifactId the artifact id of the project
     */
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    /**
     * Returns the version of the project.
     *
     * @return the version of the project
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets the version of the project.
     *
     * @param version the version of the project
     */
    public void setVersion( String version )
    {
        this.version = version;
    }

    /**
     * Returns true if the artifact id, artifact type, group id, version and public key token (optional) match, otherwise
     * returns false.
     *
     * @param o object to compare
     * @return true if the artifact id, artifact type, group id, version and public key token (optional) match, otherwise
     *         returns false.
     */
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
        if ( !artifactType.equals( project.artifactType ) )
        {
            return false;
        }
        if ( !groupId.equals( project.groupId ) )
        {
            return false;
        }
        if ( publicKeyTokenId != null ? !publicKeyTokenId.equals( project.publicKeyTokenId )
            : project.publicKeyTokenId != null )
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
        result = 29 * result + ( publicKeyTokenId != null ? publicKeyTokenId.hashCode() : 0 );
        result = 29 * result + artifactType.hashCode();
        return result;
    }
}
