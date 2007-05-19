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
package org.apache.maven.dotnet.plugin;

import org.apache.maven.project.MavenProject;
import org.apache.maven.dotnet.executable.NetExecutableFactory;
import org.w3c.dom.Document;

import javax.xml.transform.dom.DOMSource;

/**
 * Provides services for the base abstract mojo to use in configuring the plugin environment. The methods should be
 * implemented by classes that extend the <code>AbstractMojo</code>
 *
 * @author Shane Isbell
 */

public interface DotNetMojo
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = DotNetMojo.class.getName();

    /**
     * Returns the path to the local maven repository.
     *
     * @return the path to the local maven repository
     */
    String getLocalRepository();

    /**
     * Returns the maven project
     *
     * @return the maven project
     */
    MavenProject getMavenProject();

    /**
     * Returns the DOM source for the specified document. The source contains the parameters that the .NET version of
     * the AbstractMojo plugin will use for injecting field properties.
     *
     * @param document
     * @return the DOM source for the specified document
     */
    DOMSource getDOMSourceFor( Document document );

    /**
     * Returns the net executable factory
     *
     * @return the net executable factory
     */
    NetExecutableFactory getNetExecutableFactory();

   // PluginContext getPluginContext();
}
