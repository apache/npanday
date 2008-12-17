package npanday.repository.impl;

import npanday.registry.DataAccessObjectRegistry;
import npanday.registry.DataAccessObject;
import npanday.dao.impl.ProjectDaoImpl;

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
