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
package npanday.artifact;

import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import org.apache.maven.model.Dependency;

import java.net.URL;
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
     * Loads a list of .NET dependencies from a config file or resource.
     */
    void load( URL source )
        throws NPandayRepositoryException;

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
