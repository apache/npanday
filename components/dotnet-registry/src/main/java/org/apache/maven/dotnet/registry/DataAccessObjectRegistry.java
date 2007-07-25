package org.apache.maven.dotnet.registry;

import java.util.Set;

public interface DataAccessObjectRegistry
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = DataAccessObjectRegistry.class.getName();

    /**
     * Returns all DataAccessObjects from the registry.
     *
     * @return a Set of all DataAccessObjects.
     */
    Set<DataAccessObject> findAll();

    /**
     * Finds a DataAccessObject from the registry.
     *
     * @param daoId the id of the DAO to find.
     * @return a DataAccessObject or null if DAO cannot be found.
     */
    DataAccessObject find( String daoId );

    /**
     * Accessor for DAO ids
     *
     * @return unmodifiable set of DataAccessObject ids
     */
    Set<String> getDaoIds();
}
