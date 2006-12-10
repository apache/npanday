package org.apache.maven.plugin.vstudio;

public class ExcludedDependency
{

    private String groupId;

    private String artifactId;

    public ExcludedDependency()
    {
    }

    public ExcludedDependency( String groupId, String artifactId )
    {
        this.setGroupId( groupId );
        this.setArtifactId( artifactId );
    }

    /**
     * @return Returns the groupId of the excluded dependency
     */
    public String getGroupId()
    {
        return this.groupId;
    }

    /**
     * @param groupId groupId of excluded dependency
     */
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /**
     * @return Returns the artifactId of the excluded dependency
     */
    public String getArtifactId()
    {
        return this.artifactId;
    }

    /**
     * @param artifactId artifactId of excluded dependency
     */
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
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

        final ExcludedDependency that = (ExcludedDependency) o;

        if ( artifactId != null ? !artifactId.equals( that.artifactId ) : that.artifactId != null )
        {
            return false;
        }
        return !( groupId != null ? !groupId.equals( that.groupId ) : that.groupId != null );
    }

    public int hashCode()
    {
        int result;
        result = ( groupId != null ? groupId.hashCode() : 0 );
        result = 29 * result + ( artifactId != null ? artifactId.hashCode() : 0 );
        return result;
    }

}
