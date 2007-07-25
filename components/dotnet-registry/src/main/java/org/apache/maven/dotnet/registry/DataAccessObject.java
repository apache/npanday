package org.apache.maven.dotnet.registry;

public interface DataAccessObject
{
    /**
     * Class name accessor.
     *
     * @return class name of the DataAccessObject
     */
    String getClassName();

    /**
     * Accessor for ID
     *
     * @return id of the DataAccessObject
     */
    String getID();

    void init(Object dataStoreObject, String id, String className ) throws IllegalArgumentException;

    void setRepositoryRegistry(RepositoryRegistry repositoryRegistry);
}
