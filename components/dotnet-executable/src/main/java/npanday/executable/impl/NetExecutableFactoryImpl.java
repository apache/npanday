package npanday.executable.impl;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import npanday.ArtifactType;
import npanday.InitializationException;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.CapabilityMatcher;
import npanday.executable.ExecutableCapability;
import npanday.executable.ExecutableConfig;
import npanday.executable.ExecutableContext;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.executable.NetExecutableFactory;
import npanday.executable.RepositoryExecutableContext;
import npanday.executable.compiler.CompilerCapability;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerContext;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.CompilerRequirement;
import npanday.registry.RepositoryRegistry;
import npanday.resolver.NPandayArtifactResolver;
import npanday.vendor.IllegalStateException;
import npanday.vendor.StateMachineProcessor;
import npanday.vendor.Vendor;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides an implementation of <code>NetExecutableFactory</code>.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.executable.NetExecutableFactory"
 */
public class NetExecutableFactoryImpl
    extends AbstractLogEnabled
    implements NetExecutableFactory
{

    /**
     * @plexus.requirement
     */
    private CapabilityMatcher capabilityMatcher;

    /**
     * @plexus.requirement
     */
    private ExecutableContext executableContext;

    /**
     * @plexus.requirement
     */
    private CompilerContext compilerContext;

    /**
     * @see NetExecutableFactory
     */
    public NetExecutable getExecutable(
        ExecutableRequirement executableRequirement, List<String> commands, File netHome )
        throws PlatformUnsupportedException
    {
        // TODO: construct ExcecutableConfig from the outside
        ExecutableConfig executableConfig = new ExecutableConfig();
        executableConfig.setCommands( commands );


        List<String> executablePaths = ( executableConfig.getExecutionPaths() == null )
            ? new ArrayList<String>()
            : executableConfig.getExecutionPaths();

        if ( netHome != null )
        {
            getLogger().info( "NPANDAY-066-014: Found executable path in pom: Path = " + netHome.getAbsolutePath() );
            executableConfig.getExecutionPaths().add( netHome.getAbsolutePath() );
        }


        executableConfig.setExecutionPaths( executablePaths );

        final ExecutableCapability executableCapability =
                    capabilityMatcher.matchExecutableCapabilityFor( executableRequirement );

        executableContext.init( executableCapability, executableConfig );

        try
        {
            return executableContext.getNetExecutable();
        }
        catch ( ExecutionException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-001: Unable to find net executable", e );
        }
    }

    /**
     * @see NetExecutableFactory#getCompilerExecutable(npanday.executable.compiler.CompilerRequirement,
     *      npanday.executable.compiler.CompilerConfig, org.apache.maven.project.MavenProject)
     */
    public CompilerExecutable getCompilerExecutable(
        CompilerRequirement compilerRequirement, CompilerConfig compilerConfig, MavenProject project )
        throws PlatformUnsupportedException
    {
        File targetDir = PathUtil.getPrivateApplicationBaseDirectory( project );

        final CompilerCapability compilerCapability =
            capabilityMatcher.matchCompilerCapabilityFor( compilerRequirement );

        // init does not need the executable paths to be set
        compilerContext.init( compilerCapability, compilerConfig, project );

        List<String> executionPaths = ( compilerConfig.getExecutionPaths() == null )
            ? new ArrayList<String>()
            : compilerConfig.getExecutionPaths();

        if ( executionPaths == null || executionPaths.size() == 0 )
        {
            getLogger().warn( "NPANDAY-231: previously netDependencyId was used to resolve some private bin path..." );
            // TODO: remove!
            Artifact artifact = null;
            if ( artifact != null )
            {
                File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, compilerConfig.getLocalRepository(), targetDir );
                executionPaths.add( artifactPath.getParentFile().getAbsolutePath() );
            }

            compilerConfig.setExecutionPaths( executionPaths );
        }

        try
        {
            return compilerContext.getCompilerExecutable();
        }
        catch ( ExecutionException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-007: Unable to find net executable", e );
        }
    }
}
