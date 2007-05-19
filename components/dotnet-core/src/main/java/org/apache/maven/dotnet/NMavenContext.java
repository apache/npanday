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
package org.apache.maven.dotnet;

import org.apache.maven.project.MavenProject;

import org.apache.maven.dotnet.registry.Repository;
import org.codehaus.plexus.logging.Logger;

/**
 * Provides services for obtaining repositories, a plexus logger and the maven project.
 *
 * @author Shane Isbell
 */

public interface NMavenContext
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = NMavenContext.class.getName();

    /**
     * This method is used to obtain an instance of a <code>Repository</code> specified within the registry-config.xml file.
     * This method allows the application developer to create configuration files and have them accessible through the
     * NMaven Context. For more information on how the registry/repository works, go to
     * http://jvending.sourceforge.net/registry-cdc/
     *
     * @param repositoryName the name of the repository. This name should match the /<repository-name> field within the
     *                       registry-config.xml file.
     * @return repository for the specified repository name
     * @throws RepositoryNotFoundException if repository is not found.
     */
    Repository find( String repositoryName )
        throws RepositoryNotFoundException;

    /**
     * Returns Maven Plexus logger: <code>Log</code>.
     *
     * @return the plugin logger
     */
    Logger getLogger();

}
