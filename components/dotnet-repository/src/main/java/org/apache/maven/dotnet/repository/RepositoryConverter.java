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
package org.apache.maven.dotnet.repository;

import org.openrdf.repository.Repository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.dotnet.artifact.ApplicationConfig;

import java.io.File;
import java.io.IOException;

/**
 * Provides services for converting from RDF repository into the defau;t Maven repository format.
 */
public interface RepositoryConverter
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = RepositoryConverter.class.getName();

    /**
     * Converts the specified RDF repository into the default local repository format.
     *
     * @param repository      the RDF repository
     * @param mavenRepository the base directory where the converted repository, with the default local repository
     *                        format, should be placed
     * @throws IOException if there is a problem in converting the repository
     */
    void convertRepositoryFormat( Repository repository, File mavenRepository )
        throws IOException;

    void convertRepositoryFormatFor( Artifact artifact, ApplicationConfig applicationConfig, Repository repository,
                                     File mavenRepository )
        throws IOException;
}
