package org.apache.maven.dotnet.repository.impl;

import org.apache.maven.dotnet.registry.DataAccessObjectRegistry;
import org.apache.maven.dotnet.registry.DataAccessObject;
import org.apache.maven.dotnet.dao.impl.ProjectDaoImpl;

import java.util.Set;


public class DataAccessObjectRegistryStub
    implements DataAccessObjectRegistry
{

    public Set<DataAccessObject> findAll()
    {
        return null;
    }

    public DataAccessObject find( String daoId )
    {
        return new ProjectDaoImpl();
    }

    public Set<String> getDaoIds()
    {
        return null;
    }

}
