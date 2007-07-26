package org.apache.maven.dotnet.dao;

import java.util.Set;
import java.util.HashSet;

public class Project
{
    private String groupId;

    private String artifactId;

    private String version;

    private String publicKeyTokenId;

    private boolean isResolved = false;

    private String artifactType = "library";

    private Set<ProjectDependency> projectDependencies = new HashSet<ProjectDependency>();

    private Set<Requirement> requirements = new HashSet<Requirement>();

    private Project parentProject;

    public Project getParentProject()
    {
        return parentProject;
    }

    public void setParentProject( Project parentProject )
    {
        this.parentProject = parentProject;
    }

    public void addRequirement( Requirement requirement )
    {
        requirements.add( requirement );
    }

    public Set<Requirement> getRequirements()
    {
        return requirements;
    }

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
