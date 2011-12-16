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
package npanday.executable.impl;

import npanday.InitializationException;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.artifact.ArtifactContext;
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
import npanday.vendor.IllegalStateException;
import npanday.vendor.StateMachineProcessor;
import npanday.vendor.Vendor;
import npanday.vendor.VendorInfo;
import npanday.vendor.VendorInfoRepository;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private ArtifactContext artifactContext;

    /**
     * @plexus.requirement
     */
    private RepositoryExecutableContext repositoryExecutableContext;

    /**
     * @plexus.requirement
     */
    private ExecutableContext executableContext;

    /**
     * @plexus.requirement
     */
    private CompilerContext compilerContext;

    /**
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @plexus.requirement
     */
    private VendorInfoRepository vendorInfoRepository;

    /**
     * @plexus.requirement
     */
    private StateMachineProcessor processor;


    /**
     * @see NetExecutableFactory
     */
    public NetExecutable getNetExecutableFor( ExecutableRequirement executableRequirement, List<String> commands, File netHome )
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
     * @see NetExecutableFactory#getCompilerExecutableFor(npanday.executable.compiler.CompilerRequirement,
     *      npanday.executable.compiler.CompilerConfig, org.apache.maven.project.MavenProject)
     */
    public CompilerExecutable getCompilerExecutableFor( CompilerRequirement compilerRequirement,
                                                        CompilerConfig compilerConfig, MavenProject project)
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
            String netDependencyId = compilerCapability.getNetDependencyId();

            if ( netDependencyId != null )
            {
                Artifact artifact = artifactContext.getArtifactByID( netDependencyId );
                if ( artifact != null )
                {
                    File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, compilerConfig.getLocalRepository(), targetDir );
                    executionPaths.add( artifactPath.getParentFile().getAbsolutePath() );
                }
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

    public NetExecutable getNetExecutableFromRepository( String groupId, String artifactId,
                                                         VendorRequirement vendorRequirement, File localRepository,
                                                         List<String> commands, boolean isIsolatedAppDomain, File targetDir )
        throws PlatformUnsupportedException
    {
        if ( isIsolatedAppDomain )
        {
            List<Artifact> artifacts = artifactContext.getArtifactsFor( groupId, artifactId, null, null );
            if ( artifacts.size() == 0 )
            {
                throw new PlatformUnsupportedException(
                    "NPANDAY-066-024: Could not locate the executable - missing entry in the net-dependencies.xml file: GroupId = "
                        + groupId + ", ArtifactId = " + artifactId );
            }

            Artifact artifact = artifacts.get( 0 );
            if ( artifact == null )
            {
                throw new PlatformUnsupportedException(
                    "NPANDAY-066-025: Could not locate the executable: GroupId = " + groupId + ", ArtifactId = "
                        + artifactId );
            }

            File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, localRepository, targetDir );
            commands.add( "startProcessAssembly=" + artifactPath.getAbsolutePath() );
            //TODO: Replace
            String pluginArtifactPath = PathUtil.getPrivateApplicationBaseFileFor(
                artifactContext.getArtifactsFor( "org.apache.npanday.plugins", "NPanday.Plugin", null, null ).get( 0 ),
                localRepository, targetDir ).getAbsolutePath();

            commands.add( "pluginArtifactPath=" + pluginArtifactPath );
            return getNetExecutableFromRepository( "org.apache.npanday.plugins", "NPanday.Plugin.Runner",
                                                   vendorRequirement, localRepository, commands, false, targetDir );
        }

        if ( commands == null )
        {
            commands = new ArrayList<String>();
        }

        VendorInfo vendorInfo;
        try
        {
            vendorInfo = processor.process( vendorRequirement );
        }
        catch ( IllegalStateException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-010: Illegal State: Vendor Info = " + vendorRequirement, e );
        }

        if ( vendorInfo.getVendor() == null || vendorInfo.getFrameworkVersion() == null )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-020: Missing Vendor Information: " + vendorInfo );
        }
        List<Artifact> artifacts = artifactContext.getArtifactsFor( groupId, artifactId, null, null );
        if ( artifacts.size() == 0 )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-022: Could not locate the executable- missing entry in the net-dependencies.xml: GroupId = "
                    + groupId + ", ArtifactId = " + artifactId );
        }
        Artifact artifact = artifacts.get( 0 );

        getLogger().debug( "NPANDAY-066-003: Found Vendor: " + vendorInfo );

        File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, localRepository, targetDir );
        List<String> modifiedCommands = new ArrayList<String>();
        String exe = null;

        // TODO: this should be a separate implementation of NetExecutable, configured only for MONO!!!
        if ( vendorInfo.getVendor().equals( Vendor.MONO ) )
        {
            List<File> executablePaths = vendorInfo.getExecutablePaths();
            if ( executablePaths != null )
            {
                for ( File executablePath : executablePaths )
                {
                    if ( new File( executablePath.getAbsolutePath(), "mono.exe" ).exists() )
                    {
                        exe = new File( executablePath.getAbsolutePath(), "mono.exe" ).getAbsolutePath();
                        commands.add( "vendor=MONO" );//if forked process, it needs to know.
                        break;
                    }
                }
            }

            if ( exe == null )
            {
                getLogger().info(
                    "NPANDAY-066-005: Executable path for mono does not exist. Will attempt to execute MONO using"
                        + " the main PATH variable." );
                exe = "mono";
                commands.add( "vendor=MONO" );//if forked process, it needs to know.
            }
            modifiedCommands.add( artifactPath.getAbsolutePath() );
            for ( String command : commands )
            {
                modifiedCommands.add( command );
            }
        }
        else
        {
            exe = artifactPath.getAbsolutePath();
            modifiedCommands = commands;
        }
        //TODO: DotGNU on Linux?
        ExecutableConfig executableConfig = new ExecutableConfig();
        executableConfig.setExecutionPaths( Arrays.asList( exe ) );
        executableConfig.setCommands( modifiedCommands );

        try
        {
            repositoryExecutableContext.init( executableConfig );
        }
        catch ( InitializationException e )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-006: Unable to initialize the repository executable context", e );
        }

        try
        {
            return repositoryExecutableContext.getNetExecutable();
        }
        catch ( ExecutionException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-004: Unable to find net executable", e );
        }
    }

    public NetExecutable getPluginLoaderFor( Artifact artifact, VendorRequirement vendorRequirement, String localRepository,
                                             File parameterFile, String mojoName, File targetDir )
        throws PlatformUnsupportedException
    {
        //AssemblyRepositoryLayout layout = new AssemblyRepositoryLayout();
        File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, new File( localRepository ), targetDir );

        List<String> commands = new ArrayList<String>();
        commands.add( "parameterFile=" + parameterFile.getAbsolutePath() );
        commands.add( "assemblyFile=" + artifactPath.getAbsolutePath() );
        commands.add( "mojoName=" + mojoName );//ArtifactId = namespace

        // make sure plugin artifact is present to run with in the application base
        // TODO: can we do this transitively from the loader or the plugin artifact? Hardcoding the transitive deps
        // here
        Artifact modelArtifact = artifactContext.getArtifactsFor(
            "org.apache.npanday", "NPanday.Model.Pom", null, null
        ).get( 0 );

        PathUtil.getPrivateApplicationBaseFileFor( modelArtifact, new File( localRepository ), targetDir );

        Artifact pluginArtifact = artifactContext.getArtifactsFor(
            "org.apache.npanday.plugins", "NPanday.Plugin", null, null
        ).get( 0 );

        PathUtil.getPrivateApplicationBaseFileFor( pluginArtifact, new File( localRepository ), targetDir );

        Artifact pluginLoaderArtifact =
            artifactContext.getArtifactsFor( "org.apache.npanday.plugins", "NPanday.Plugin.Loader", null, null ).get(
                0 );
        artifactPath = PathUtil.getPrivateApplicationBaseFileFor( pluginLoaderArtifact, new File( localRepository ), targetDir );
        commands.add( "startProcessAssembly=" + artifactPath.getAbsolutePath() );

        return getNetExecutableFromRepository( "org.apache.npanday.plugins", "NPanday.Plugin.Runner", vendorRequirement,
                                               new File( localRepository ), commands, false, targetDir );
    }

    public Artifact getArtifactFor( String groupId, String artifactId )
        throws PlatformUnsupportedException
    {
        List<Artifact> artifacts = artifactContext.getArtifactsFor( groupId, artifactId, null, null );
        if ( artifacts.size() == 0 )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-023: Could not locate the plugin - missing entry in the net-dependencies.xml file: GroupId = "
                    + groupId + ", ArtifactId = " + artifactId );
        }

        Artifact artifact = artifacts.get( 0 );
        if ( artifact == null )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-021: Could not locate the plugin: GroupId = " + groupId + ", ArtifactId = " + artifactId );
        }
        return artifact;
    }
}
