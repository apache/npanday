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
package npanday.executable;

import npanday.PlatformUnsupportedException;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.CompilerRequirement;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Provides services to obtain executables. This interface is intended to be used by <code>AbstractMojo</code>
 * implementations to obtain the appropriate executable or compiler.
 *
 * @author Shane Isbell
 */
public interface NetExecutableFactory
{

    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = NetExecutableFactory.class.getName();

    /**
     * Returns an executable that matches the vendor, framework version and profile. These are typically framework-vendor
     * executables.
     *
     *
     * @param executableRequirement
     * @param commands         the user-defined command options to use with the executable
     * @param netHome          the install root of the .NET framework
     * @return the executable that matches the vendor, framework version and profile
     * @throws npanday.PlatformUnsupportedException
     *          if no executable is found
     */
    NetExecutable getNetExecutableFor( ExecutableRequirement executableRequirement, List<String> commands, File netHome )
        throws PlatformUnsupportedException;

    /**
     * Returns an executable that resides within a maven repository. These are typically user-implemented executables.
     *
     *
     * @param groupId             the group ID of the executable artifact (as specified within the maven repo)
     * @param artifactId          the artifact ID of the executable artifact (as specified within the maven repo)
     * @param vendorRequirement
     * @param localRepository     the local maven repository where the executable resides.
     * @param isIsolatedAppDomain the executable can load up assemblies into an isolated application domain. This should
     *                            be set to true if the application needs to load up assemblies into another app domain
     *                            and to remotly invoke methods on classes in the other app domain, otherwise it should be
     *                            set to false.
     * @param targetDir
     * @return the executable that resides within a maven repository.
     * @throws PlatformUnsupportedException if no executable is found
     */
    NetExecutable getNetExecutableFromRepository( String groupId, String artifactId,
                                                  VendorRequirement vendorRequirement, File localRepository,
                                                  List<String> commands, boolean isIsolatedAppDomain, File targetDir )
        throws PlatformUnsupportedException;

    /**
     * Returns an executable for compiling .NET applications.
     *
     * @param compilerRequirement the requirements for the compiler
     * @param compilerConfig      the configuration for the compiler
     * @param project             the maven project
     * @return the executable for compiling .NET applications
     * @throws npanday.PlatformUnsupportedException
     *          if no executable is found
     */
    CompilerExecutable getCompilerExecutableFor( CompilerRequirement compilerRequirement, CompilerConfig compilerConfig,
                                                 MavenProject project )
        throws PlatformUnsupportedException;


    /**
     * Returns a plugin loader for loading and executing a .NET plugin.
     *
     * @param artifact          the executable artifact
     * @param vendorRequirement
     * @param localRepository   the local maven repository where the executable resides.
     * @param parameterFile     the file containing parameter information to inject into the .NET plugin
     * @param mojoName          the name of the .NET Mojo implementation
     * @param targetDir
     * @return the plugin loader for executing a .NET plugin
     * @throws PlatformUnsupportedException if no executable is found
     */
    NetExecutable getPluginLoaderFor(
        Artifact artifact, VendorRequirement vendorRequirement, String localRepository, File parameterFile,
        String mojoName, File targetDir ) throws PlatformUnsupportedException;

    Artifact getArtifactFor(String groupId, String artifactId) throws PlatformUnsupportedException;
}
