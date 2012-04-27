package npanday.executable;

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

import npanday.PlatformUnsupportedException;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.CompilerRequirement;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Provides services to obtain executables. This interface is intended to be used by <code>AbstractMojo</code>
 * implementations to obtain the appropriate executable or compiler.
 *
 * @author Shane Isbell
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 */
public interface NetExecutableFactory
{
    String ROLE = NetExecutableFactory.class.getName();


    NetExecutable getExecutable(
        ExecutableRequirement executableRequirement, List<String> commands, File netHome ) throws
        PlatformUnsupportedException;

    CompilerExecutable getCompilerExecutable(
        CompilerRequirement compilerRequirement, CompilerConfig compilerConfig, MavenProject project ) throws
        PlatformUnsupportedException;

    NetExecutable getArtifactExecutable(
        MavenProject project, Artifact executableArtifact, Set<Artifact> additionalDependencies,
        VendorRequirement vendorRequirement, ArtifactRepository localRepository, List<String> commands,
        File targetDir ) throws

        PlatformUnsupportedException, ArtifactResolutionException, ArtifactNotFoundException;

    NetExecutable getPluginExecutable(
        MavenProject project, Artifact artifact, VendorRequirement vendorRequirement,
        ArtifactRepository localRepository, File parameterFile, String mojoName, File targetDir ) throws
        PlatformUnsupportedException,
        ArtifactResolutionException,
        ArtifactNotFoundException;

    public NetExecutable getPluginRunner(
        MavenProject project, Artifact pluginArtifact, Set<Artifact> additionalDependencies,
        VendorRequirement vendorRequirement, ArtifactRepository localRepository, List<String> commands,
        File targetDir ) throws

        PlatformUnsupportedException,
        ArtifactResolutionException,
        ArtifactNotFoundException;
}
