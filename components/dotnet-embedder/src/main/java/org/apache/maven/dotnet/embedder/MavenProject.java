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

import org.codehaus.xfire.aegis.type.java5.*;

/**
 * Provides web services for obtaining maven project information.
 *
 * @author Shane Isbell
 */
@XmlType(namespace = "urn:maven-embedder")
public interface MavenProject
    extends Comparable
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = MavenProject.class.getName();

    /**
     * Returns the path to the maven project's pom.
     *
     * @return the path to the maven project's pom
     */
    @XmlElement(name = "pomPath", namespace = "urn:maven-embedder")
    String getPomPath();

    /**
     * Returns the group id of the maven project.
     *
     * @return the group id of the maven project.
     */
    @XmlElement(name = "groupId", namespace = "urn:maven-embedder")
    String getGroupId();

    /**
     * Returns the artifact id of the maven project.
     *
     * @return the artifact id of the maven project
     */
    @XmlElement(name = "artifactId", namespace = "urn:maven-embedder")
    String getArtifactId();

    /**
     * Returns the version of the maven project.
     *
     * @return the version of the maven project
     */
    @XmlElement(name = "version", namespace = "urn:maven-embedder")
    String getVersion();

    /**
     * Returns a set of maven project children (typically the modules of a parent pom).
     *
     * @return a set of maven project children
     */
    @XmlElement(name = "mavenProjects", namespace = "urn:maven-embedder")
    Set<MavenProject> getMavenProjects();

    /**
     * Returns true if both of the following conditions apply:
     * 1) the project is not in the root directory AND
     * 2) the project is not attached (as a module) to a pom that exists in the root directory,
     * otherwise returns false.
     *
     * @return true if both of the following conditions apply:
     * 1) the project is not in the root directory AND
     * 2) the project is not attached (as a module) to a pom that exists in the root directory,
     * otherwise returns false.
     */
    @XmlElement(name = "isOrphaned", namespace = "urn:maven-embedder")
    boolean isOrphaned();
}