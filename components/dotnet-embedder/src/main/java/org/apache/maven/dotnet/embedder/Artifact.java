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

import org.codehaus.xfire.aegis.type.java5.*;

/**
 * Provides web services for obtaining artifact information.
 *
 * @author Shane Isbell
 */
@XmlType(namespace = "urn:maven-embedder")
public interface Artifact
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = Artifact.class.getName();

    @XmlElement(name = "pomPath", namespace = "urn:maven-embedder")
    String getPomPath();

    /**
     * Returns the group id of the artifact.
     *
     * @return the group id of the artifact
     */
    @XmlElement(name = "groupId", namespace = "urn:maven-embedder")
    String getGroupId();

    /**
     * Returns the artifact id of the artifact.
     *
     * @return the artifact id of the artifact
     */
    @XmlElement(name = "artifactId", namespace = "urn:maven-embedder")
    String getArtifactId();

    /**
     * Returns the version of the artfact.
     *
     * @return the version of the artifact
     */
    @XmlElement(name = "version", namespace = "urn:maven-embedder")
    String getVersion();
}
