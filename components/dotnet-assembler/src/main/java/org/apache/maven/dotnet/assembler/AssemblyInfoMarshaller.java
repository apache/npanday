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
package org.apache.maven.dotnet.assembler;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.project.MavenProject;
import org.apache.maven.dotnet.model.assembly.plugins.AssemblyPlugin;

/**
 * Provides services for creating an AssemblyInfo class.
 *
 * @author Shane Isbell
 */
public interface AssemblyInfoMarshaller
{
    /**
     * Writes the assembly info to AssemblyInfo.[language-extension].
     *
     * @param assemblyInfo the assembly info
     * @param mavenProject the maven project
     * @param outputStream the output stream to write to (currently unused)
     * @throws IOException if there was a problem writing out the class file
     */
    void marshal( AssemblyInfo assemblyInfo, MavenProject mavenProject, OutputStream outputStream )
        throws IOException;

    /**
     * Unmarshalls an AssemblyInfo.* class file.
     *
     * @param inputStream the input stream of the AssemblyInfo.* class file
     * @return an AssemblyInfo object for the specified input stream
     * @throws IOException if there was a problem reading the AssemblyInfo class file
     */
    AssemblyInfo unmarshall( InputStream inputStream) throws IOException;    

    /**
     * Initializes the marshaller.
     *
     * @param plugin the assembly plugin model associated with this marshaller (plugin specified within the
     *               assembly-plugins.xml file)
     */
    void init( AssemblyPlugin plugin );

}
