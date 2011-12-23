package npanday.registry;

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

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Provides services for loading registry config files and accessing and managing repositories.
 *
 * @author Shane Isbell
 */

public interface RepositoryRegistry
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = RepositoryRegistry.class.getName();

    /**
     * Mutator for setting the <code>RepositoryLoader</code>
     *
     * @param repositoryLoader
     */
    void setRepositoryLoader( RepositoryLoader repositoryLoader );

    /**
     * Mutator for setting the <code>RegistryLoader</code>
     *
     * @param registryLoader
     */
    void setRegistryLoader( RegistryLoader registryLoader );

    /**
     * Loads the registry from inputStream. Multiple config files may be loaded into the registry.
     *
     * @param inputStream contains the jvending-config file.
     * @throws java.io.IOException thrown on interrupted I/O
     * @throws npanday.registry.NPandayRepositoryException
     */
    void loadFromInputStream( InputStream inputStream )
            throws IOException, NPandayRepositoryException;

    /**
     * Convenience method for loading a file off of a file system.
     *
     * @param fileName relative or absolute path of the file
     * @throws IOException thrown on interrupted I/O
     * @throws npanday.registry.NPandayRepositoryException
     */
    void loadFromFile( String fileName )
        throws IOException, NPandayRepositoryException;


    /**
     * Convenience method for loading from a JAR or Resource.
     *
     * @param fileName relative or absolute path of the file
     * @throws IOException thrown on interrupted I/O
     * @throws npanday.registry.NPandayRepositoryException
     */
    void loadFromResource( String fileName, Class sourceClass )
        throws IOException, NPandayRepositoryException;

    /**
     * Adds a repository to the registry. If the repository name already exists, this method will overwrite the old
     * Repository instance within the registry.
     *
     * @param name       name of the repository
     * @param repository instance of the repository
     */
    void addRepository( String name, Repository repository );

    /**
     * Finds a repository from the registry.
     *
     * @param name name of the repository.
     * @return instance of the Repository or null if instance does not exist
     */
    Repository find( String name );

    /**
     * Removes a repository from the registry
     *
     * @param name name of the repository
     */
    void removeRepository( String name );

    /**
     * Accessor for repository names.
     *
     * @return unmodifiable set of repository names
     */
    Set<String> getRepositoryNames();

    /**
     * Empties all of the repositories from the registry.
     */
    void empty();

    /**
     * Returns true if the registry is empty, otherwise returns true.
     *
     * @return true if the registry is empty, otherwise returns true.
     */
    boolean isEmpty();

}
