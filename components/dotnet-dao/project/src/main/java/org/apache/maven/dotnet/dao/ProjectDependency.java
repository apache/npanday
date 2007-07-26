package org.apache.maven.dotnet.dao;

import org.apache.maven.dotnet.dao.Project;

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
