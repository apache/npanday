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
import npanday.PlatformUnsupportedException;
import npanday.executable.CommandFilter;
import npanday.executable.ExecutableContext;
import npanday.executable.ExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Interface defining compiler services.
 *
 * @author Shane Isbell
 */
public interface CompilerContext
    extends ExecutableContext
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = CompilerContext.class.getName();

    /**
     * Returns the artifact that the compiler generated. Typically, this is needed by a plugin to attach to the
     * project for installation in the local repo. Example:
     * <p/>
     * <code>project.getArtifact().setFile(compilerContext.getArtifact());</code>
     *
     * @return the artifact that the compiler generated.
     * @throws InvalidArtifactException
     */
    File getArtifact()
        throws InvalidArtifactException;


    /**
     * Returns assembly names that should be referenced by the compiler. If the List is emtpy, then all core assemblies
     * should be includes. These are assemblies that are not standard
     * dependencies, but are system assemblies that replace the core .NET framework assemblies (used to create profiles)
     *
     * @return assembly names that should be referenced by the compiler.
     */
    List<String> getCoreAssemblyNames();

    /**
     * Returns a list of module (netmodule) dependencies that exist directly within the invoking projects pom
     * (no transitive module dependencies).
     *
     * @return a list of module (netmodule) dependencies of the class files.
     */
    List<Artifact> getDirectModuleDependencies();

    KeyInfo getKeyInfo();

    /**
     * Returns a list of library (dll) dependencies of the class files.
     *
     * @return a list of library (dll) dependencies of the class files.
     */
    List<Artifact> getLibraryDependencies();

    /**
     * Returns a list of non transitive library (dll) dependencies of the class files.
     *
     * @return a list of non transitive library (dll) dependencies of the class files.
     */
    List<Artifact> getDirectLibraryDependencies();
    
    /**
     * Returns a list of module (netmodule) dependencies of the class files.
     *
     * @return a list of module (netmodule) dependencies of the class files.
     */
    List<Artifact> getModuleDependencies();

    /**
     * Returns the source directory (or test source directory) path of the class files. These are defined in the pom.xml
     * by the properties ${build.sourceDirectory} or ${build.testSourceDirectory}.
     *
     * @return Returns the source directory (or test source directory) path of the class files.
     */
    String getSourceDirectoryName();
    
    File getTargetDirectory();

    /**
     * Returns an instance of the NetExecutable appropriate for given language/vendor/OS.
     *
     * @return an instance of the NetExecutable appropriate for given language/vendor/OS.
     * @throws npanday.executable.ExecutionException
     *          if there is a problem finding an appropriate executable.
     */
    CompilerExecutable getCompilerExecutable()
        throws ExecutionException;

    /**
     * Creates a command filter. If the includes parameter is null, then the filter will return all commands that are
     * not in the exlude filters. If the excludes parameter is null, then the filter will only return what is in the
     * includes list. If both parameters are null...
     */
    CommandFilter getCommandFilter();

    /**
     * Returns a list of resources that the compiler should link to the compiled assembly
     *
     * @return a list of resources that the compiler should link to the compiled assembly
     */
    List<File> getLinkedResources();

    /**
     * Returns a list of resources that the compiler should embed in the compiled assembly. These may of any mime-type
     * or it may be a generated .resource file.
     *
     * @return a list of resources that the compiler should embed in the compiled assembly.
     *
     */
    List<File> getEmbeddedResources();

    List<String> getEmbeddedResourceArgs();

    /**
     * Returns the icon that the assembly should display when viewed. Should not be used in conjunction with win32res.
     *
     * @return the icon that the assembly should display when viewed.
     */
    File getWin32Icon();

    /**
     * Returns a list of win32 resources. Should not be used in conjunction with win32icon.
     *
     * @return a list of win32 resources.
     */
    List<File> getWin32Resources();

    /**
     * Gets the framework version of the current vendor used.
     */
    String getFrameworkVersion();

    /**
     * Returns the path to the core assemblies.
     */
    File getAssemblyPath();

    /**
     * Returns the framework to compile for.
     */
    String getTargetFramework();

    /**
     * Gets the profile the compiler runs with.
     */
    String getTargetProfile();

    /**
     * Gets the resulting artifacts type
     */
    ArtifactType getTargetArtifactType();

    /**
     * Gets, if the current compile is a test compile.
     */
    boolean isTestCompile();

    /**
     * The list of sources to be included in the compilation.
     */
    List<String> getIncludeSources();

    /**
     * The directory to store the compile output too.
     */
    File getOutputDirectory();

    /**
     * Initializes the context
     *
     *
     * @param capability
     * @param config
     * @param project
     * @throws PlatformUnsupportedException
     */
    void init( CompilerCapability capability, CompilerConfig config, MavenProject project )
        throws PlatformUnsupportedException;
}
