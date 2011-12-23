package npanday.assembler;

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

import npanday.InitializationException;
import npanday.PlatformUnsupportedException;
import org.apache.maven.project.MavenProject;

/**
 * Provides services for generating of AssemblyInfo.* file.
 *
 * @author Shane Isbell
 */
public interface AssemblerContext
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = AssemblerContext.class.getName();

    /**
     * Returns the assembly info associated with this context.
     *
     * @return the assembly info associated with this context.
     */
    AssemblyInfo getAssemblyInfo();

    /**
     * Returns the marshaller for the given language
     *
     * @param language the .NET language
     * @return the marshaller for the specified language
     * @throws AssemblyInfoException if no marshaller can be found for the specified language
     */
    AssemblyInfoMarshaller getAssemblyInfoMarshallerFor( String language )
        throws AssemblyInfoException;

    /**
     * Returns the class extension (cs, vb) for the specified language.
     *
     * @param language the class language. Must match language within the assembly-plugins.xml file.
     * @return the class extension (cs, vb) for the specified language.
     * @throws PlatformUnsupportedException the language is not supported
     */
    String getClassExtensionFor(String language) throws PlatformUnsupportedException;

    /**
     * Initializes the context
     *
     * @param mavenProject the maven project
     * @throws InitializationException if the context cannot be initialized
     */
    void init( MavenProject mavenProject )
        throws InitializationException;

}
