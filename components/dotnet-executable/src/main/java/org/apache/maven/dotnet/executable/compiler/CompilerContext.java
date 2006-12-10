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
package org.apache.maven.dotnet.executable.compiler;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.io.File;

import org.apache.maven.dotnet.executable.compiler.CompilerCapability;
import org.apache.maven.dotnet.executable.compiler.CompilerConfig;
import org.apache.maven.dotnet.executable.*;
import org.apache.maven.dotnet.NMavenContext;
import org.apache.maven.dotnet.PlatformUnsupportedException;

/**
 * Interface defining compiler services.
 *
 * @author Shane Isbell
 */
public interface CompilerContext
    extends NMavenContext
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
     * Return the <code>CompilerCapability</code> associated with this context.
     *
     * @return the <code>CompilerCapability</code> associated with this context.
     */
    CompilerCapability getCompilerCapability();


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

    /**
     * Returns a list of library (dll) dependencies of the class files.
     *
     * @return a list of library (dll) dependencies of the class files.
     */
    List<Artifact> getLibraryDependencies();

    /**
     * Returns a list of module (netmodule) dependencies of the class files.
     *
     * @return a list of module (netmodule) dependencies of the class files.
     */
    List<Artifact> getModuleDependencies();

    /**
     * Returns the user provided configuration associated to this context. This is a live copy, so any changes to the
     * config will be reflected during compilation.
     *
     * @return the user provided configuration associated to this context
     */
    CompilerConfig getNetCompilerConfig();

    /**
     * Requirements used to match the compiler plugin associated with this context.
     *
     * @return Requirements used to match the compiler plugin associated with this context.
     */
    CompilerRequirement getCompilerRequirement();

    /**
     * Returns the source directory (or test source directory) path of the class files. These are defined in the pom.xml
     * by the properties ${build.sourceDirectory} or ${build.testSourceDirectory}.
     *
     * @return Returns the source directory (or test source directory) path of the class files.
     */
    String getSourceDirectoryName();

    /**
     * Returns an instance of the NetExecutable appropriate for given language/vendor/OS.
     *
     * @return an instance of the NetExecutable appropriate for given language/vendor/OS.
     * @throws org.apache.maven.dotnet.executable.ExecutionException
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
     */
    List<File> getEmbeddedResources();

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
     * Initializes the context
     *
     * @param compilerRequirement
     * @param config
     * @param project
     * @param capabilityMatcher
     * @throws PlatformUnsupportedException
     */
    void init( CompilerRequirement compilerRequirement, CompilerConfig config, MavenProject project,
               CapabilityMatcher capabilityMatcher )
        throws PlatformUnsupportedException;

}
