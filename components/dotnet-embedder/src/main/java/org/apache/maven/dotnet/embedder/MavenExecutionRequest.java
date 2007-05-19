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
 * Provides services for obtaining information about the client execution request for the maven embedder.
 *
 * @author Shane Isbell
 */
@XmlType(namespace = "urn:maven-embedder")
public interface MavenExecutionRequest
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = MavenExecutionRequest.class.getName();

    /**
     * Returns the pom file that the maven embedder should execute.
     *
     * @return the pom file that the maven embedder should execute
     */
    @XmlElement(name = "pomFile", namespace = "urn:maven-embedder")
    String getPomFile();

    /**
     *  Sets the pom file that the maven embedder should execute.
     *
     * @param string the pom file that the maven embedder should execute
     */
    void setPomFile( java.lang.String string );

    /**
     * Returns the goal that the maven embedder should execute (install, clean, etc).
     *
     * @return the goal that the maven embedder should execute (install, clean, etc)
     */
    @XmlElement(name = "goal", namespace = "urn:maven-embedder")
    String getGoal();

    /**
     * Sets the goal that the maven embedder should execute (install, clean, etc).
     *
     * @param goal the goal that the maven embedder should execute (install, clean, etc)
     */
    void setGoal( String goal );

    /**
     * Returns  the socket port (of the requesting client) that the maven embedder should write its log messages to.
     *
     * @return  the socket port (of the requesting client) that the maven embedder should write its log messages to
     */
    @XmlElement(name = "loggerPort", namespace = "urn:maven-embedder")
    int getLoggerPort();

    /**
     * Sets the socket port (of the requesting client) that the maven embedder should write its log messages to.
     *
     * @param port the socket port (of the requesting client) that the maven embedder should write its log messages to
     */
    void setLoggerPort( int port );

}
