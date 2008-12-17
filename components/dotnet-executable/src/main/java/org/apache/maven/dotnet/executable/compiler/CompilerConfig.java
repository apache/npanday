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

import npanday.ArtifactType;
import npanday.executable.ExecutableConfig;

import java.io.File;
import java.util.List;

/**
 * Provides configuration Information for the .NET Compile Environment
 *
 * @author Shane Isbell
 */
public interface CompilerConfig
    extends ExecutableConfig
{

    /**
     * The target artifact for the compile: library, module, exe, winexe or nar.
     *
     * @return target artifact for the compile
     */
    ArtifactType getArtifactType();

    /**
     * Returns true if the compiler plugin should compile the test classes, otherwise returns false.
     *
     * @return true if the compiler plugin should compile the test classes, otherwise returns false.
     */
    boolean isTestCompile();

    /**
     * Returns key info used for signing assemblies.
     *
     * @return key info used for signing assemblies
     */
    KeyInfo getKeyInfo();

    /**
     * Returns local repository
     *
     * @return local repository
     */
    File getLocalRepository();

    /**
     * Sets the artifact type for the compiler plugin: library, module, exe, winexe or nar
     *
     * @param artifactType
     */
    void setArtifactType( ArtifactType artifactType );

    /**
     * If true, tells the compiler to compile the test classes, otherwise tells the compiler to compile the main classes.
     *
     * @param testCompile
     */
    void setTestCompile( boolean testCompile );

    /**
     * Sets local repository
     *
     * @param localRepository
     */
    void setLocalRepository( File localRepository );

    /**
     * Sets key info used for signing assemblies.
     *
     * @param keyInfo key info used for signing assemblies
     */
    void setKeyInfo(KeyInfo keyInfo);
    
    
    /**
     * Sets Include Sources
     *
     * @param inlcude sources file List
     */
    void setIncludeSources(List<String> includeSources);
    
    
    /**
     * Gets Include Sources
     */
    List<String> getIncludeSources();



    /**
     * Gets Output Directory
     */
    File getOutputDirectory();

    /**
     * Sets OutputDirectory
     *
     * @param output directory
     */
    void setOutputDirectory(File outputDirectory);

}
