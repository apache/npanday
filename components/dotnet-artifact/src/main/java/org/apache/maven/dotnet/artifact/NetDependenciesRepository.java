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
package org.apache.maven.dotnet.artifact;

import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.model.Dependency;

import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

/**
 * Provides methods for loading and reading the net dependency config file.
 *
 * @author Shane Isbell
 */
public interface NetDependenciesRepository
    extends Repository
{
    /**
     * @see org.apache.maven.dotnet.registry.Repository#load(java.io.InputStream, java.util.Hashtable)
     */
    void load( InputStream inputStream, Hashtable properties )
        throws IOException;

    /**
     * @see org.apache.maven.dotnet.registry.Repository#setRepositoryRegistry(org.apache.maven.dotnet.registry.RepositoryRegistry)
     */
    void setRepositoryRegistry( RepositoryRegistry repositoryRegistry );

    /**
     * Returns a list of .NET dependencies as given within the net dependencies config file. This dependency list
     * is external to the pom file dependencies. This separation is necessary since some Java Maven plugins
     * - which themselves are necessary for building .NET applications - may have  .NET executable dependencies that
     * have not been built yet and can't be resolved.
     *
     * @return a list of .NET dependencies as given within the net dependencies config file
     */
    List<Dependency> getDependencies();

    /**
     * Returns a list of .NET dependencies as given within the net dependencies config file that matches ALL of the
     * specified match policies.
     *
     * @param matchPolicies the policies to match against the dependencies
     * @return a list of .NET dependencies as given within the net dependencies config file that matches ALL of the
     *         specified match policies
     */
    List<Dependency> getDependenciesFor( List<NetDependencyMatchPolicy> matchPolicies );

    String getProperty(String key);
}
