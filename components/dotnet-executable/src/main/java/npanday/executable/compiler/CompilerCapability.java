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
package npanday.executable.compiler;

import npanday.executable.ExecutableCapability;

import java.io.File;
import java.util.List;

/**
 * Provides information about the capabilty of a specific compiler plugin.
 *
 * @author Shane Isbell
 */
public interface CompilerCapability
    extends ExecutableCapability
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = CompilerCapability.class.getName();

    /**
     * Returns assembly path of the core assemblies. For windows Compact Framework this would look something like:
     * C:\Program Files\Microsoft.NET\SDK\CompactFramework\v2.0\WindowsCE
     *
     * @return assemblyPath
     */
    File getAssemblyPath();

    /**
     * Returns a list of core assemblies names. These assemblies do not contain the artifact extension (.dll), but may contain
     * a path with the artifact name.
     *
     * @return list of core assemblies
     */
    List<String> getCoreAssemblies();

    /**
     * Returns the language of the plugin.
     *
     * @return the language of the plugin.
     */
    String getLanguage();

    /**
     * Returns true if the compiler has JIT functionality, otherwise returns false.
     *
     * @return true if the compiler has JIT functionality, otherwise returns false.
     */
    boolean isHasJustInTime();

    /**
     * Returns the target framework of the plugin.
     *
     * @return the target framework of the plugin.
     */
    String getTargetFramework();

    /**
     * Sets the assembly path.
     *
     * @param assemblyPath
     */
    void setAssemblyPath( File assemblyPath );

    /**
     * Sets the core assemblies. You may specify a path with the core assembly name, but do not give the extension (.dll),
     * as this is assumed.
     *
     * @param coreAssemblies
     */
    void setCoreAssemblies( List<String> coreAssemblies );

    /**
     * Set the language capability of the compiler.
     *
     * @param language
     */
    void setLanguage( String language );

    /**
     * Sets the JIT capability
     *
     * @param hasJustInTime
     */
    void setHasJustInTime( boolean hasJustInTime );

    /**
     * Set the target framework capability of the compiler.
     *
     * @param targetFramework
     */
    void setTargetFramework( String targetFramework );

}
