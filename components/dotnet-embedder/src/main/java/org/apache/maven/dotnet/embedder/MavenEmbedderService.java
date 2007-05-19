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
package org.apache.maven.dotnet.embedder;

import java.util.Set;

/**
 * Provides web services for interacting with the maven embedder.
 *
 * @author Shane Isbell
 */
public interface MavenEmbedderService
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = MavenEmbedderService.class.getName();

    /**
     * Executes the specified maven execute request, which is a request to execute a goal on a pom file. This method
     * is asynchronous and must return immediately.
     *
     * @param request the maven execute request
     */
    void execute( MavenExecutionRequest request );

    /**
     * Returns a set of maven projects that exist under (recursive) the base directory.
     *
     * @param basedir the base directory from which to look for pom files
     * @return a set of maven projects that exist under (recursive) the base directory
     */
    Set<MavenProject> getMavenProjectsFor( String basedir ) throws EmbedderException;

    /**
     * Returns a set of .NET artifacts that exist within the local maven repository. To be part of the returned set, the following
     * condition must be met: artifact packaging type must be an element of {library, nar, exe, winexe, netplugin, netmodule}.
     *
     * @return a set of .NET artifacts that exist within the local maven repository
     */
    Set<Artifact> getRepositoryArtifacts();

}
