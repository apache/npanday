package npanday.executable.compiler;

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

import npanday.ArtifactType;
import npanday.executable.ExecutableConfig;

import java.io.File;
import java.util.List;

/**
 * Provides configuration Information for the .NET Compile Environment
 *
 * @author Shane Isbell
 */
public class CompilerConfig
    extends ExecutableConfig
{

    private KeyInfo keyInfo;

    private ArtifactType artifactType;

    private boolean isTestCompile = false;

    private File localRepository;

    private List<String> deprecatedIncludeSourcesConfiguration;

    private File outputDirectory;

    private File assemblyPath;

    private String[] includes;

    private String[] excludes;

    private String[] testIncludes;

    private String[] testExcludes;

    private String language;

    private String languageFileExtension;


    /**
     * The target artifact for the compile: library, module, exe, winexe or nar.
     *
     * @return target artifact for the compile
     */
    public ArtifactType getArtifactType()
    {
        return artifactType;
    }

    /**
     * Returns true if the compiler plugin should compile the test classes, otherwise returns false.
     *
     * @return true if the compiler plugin should compile the test classes, otherwise returns false.
     */
    public boolean isTestCompile()
    {
        return isTestCompile;
    }

    /**
     * Returns key info used for signing assemblies.
     *
     * @return key info used for signing assemblies
     */
    public KeyInfo getKeyInfo()
    {
        return keyInfo;
    }

    /**
     * Returns local repository
     *
     * @return local repository
     */
    public File getLocalRepository()
    {
        return localRepository;
    }

    /**
     * Sets the artifact type for the compiler plugin: library, module, exe, winexe or nar
     *
     * @param artifactType
     */
    public void setArtifactType( ArtifactType artifactType )
    {
        this.artifactType = artifactType;
    }

    /**
     * If true, tells the compiler to compile the test classes, otherwise tells the compiler to compile the main classes.
     *
     * @param testCompile
     */
    public void setTestCompile( boolean testCompile )
    {
        this.isTestCompile = testCompile;
    }

    /**
     * Sets local repository
     *
     * @param localRepository
     */
    public void setLocalRepository( File localRepository )
    {
        this.localRepository = localRepository;
    }

    /**
     * Sets key info used for signing assemblies.
     *
     * @param keyInfo key info used for signing assemblies
     */
    public void setKeyInfo( KeyInfo keyInfo )
    {
        this.keyInfo = keyInfo;
    }


    /**
     * @deprecated Rather use setSourceExcludes + setSourceIncludes!
     *
     * @param deprecatedIncludeSourcesConfiguration sources file List
     */
    public void setDeprecatedIncludeSourcesConfiguration( List<String> deprecatedIncludeSourcesConfiguration )
    {
        this.deprecatedIncludeSourcesConfiguration = deprecatedIncludeSourcesConfiguration;
    }

    /**
     * Gets Include Sources
     */
    public List<String> getDeprecatedIncludeSourcesConfiguration()
    {
        return deprecatedIncludeSourcesConfiguration;
    }

    /**
     * Gets Output Directory
     */
    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Sets OutputDirectory
     *
     * @param outputDirectory output directory
     */
    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Sets the path to find the default reference assemblies on.
     */
    public void setAssemblyPath( File assemblyPath )
    {
        this.assemblyPath = assemblyPath;
    }

    /**
     * The path to find the default reference assemblies on.
     */
    public File getAssemblyPath()
    {
        return assemblyPath;
    }

    public void setSourcePatterns( String[] includes, String[] excludes, String[] testIncludes, String[] testExcludes)
    {
        this.includes = includes;
        this.excludes = excludes;
        this.testIncludes = testIncludes;
        this.testExcludes = testExcludes;
    }

    public String[] getIncludes()
    {
        return includes;
    }

    public String[] getExcludes()
    {
        return excludes;
    }

    public String[] getTestIncludes()
    {
        return testIncludes;
    }

    public String[] getTestExcludes()
    {
        return testExcludes;
    }

    public void setLanguage( String language, String languageFileExtension )
    {
       this.language = language;
        this.languageFileExtension = languageFileExtension;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getLanguageFileExtension()
    {
        return languageFileExtension;
    }
}
