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
package npanday.registry;

import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Provides services for loading the registry and for getting the loaded repositories from the registry. 
 *
 * @author Shane Isbell
 */

public interface RegistryLoader
{

    /**
     * Loads the registry from the specified input strean
     *
     * @param inputStream of the registry-config.xml. This may be of a custom format.
     * @throws IOException if there is a problem loading the registry
     * @throws NPandayRepositoryException if there is a problem loading the repository
     */
    void loadRegistry( InputStream inputStream )
        throws IOException, NPandayRepositoryException;

    /**
     * Returns the repositories.
     *
     * @return repositories
     */
    Hashtable getRepositories();

    void setRepositoryLoader( RepositoryLoader repositoryLoader );
}
