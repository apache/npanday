package org.apache.maven.dotnet.repository;

public class ProjectDependency extends Project
{
    private String scope;

    public String getScope()
    {
        return scope;
    }

    public void setScope( String scope )
    {
        this.scope = scope;
    }

}
