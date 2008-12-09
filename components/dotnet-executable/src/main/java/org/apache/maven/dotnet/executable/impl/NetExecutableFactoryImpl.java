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
package org.apache.maven.dotnet.executable.impl;

import org.apache.maven.dotnet.executable.*;
import org.apache.maven.dotnet.executable.compiler.CompilerRequirement;
import org.apache.maven.dotnet.executable.compiler.CompilerConfig;
import org.apache.maven.dotnet.executable.compiler.CompilerContext;
import org.apache.maven.dotnet.executable.compiler.CompilerExecutable;
import org.apache.maven.dotnet.vendor.*;
import org.apache.maven.dotnet.vendor.IllegalStateException;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.PathUtil;
import org.apache.maven.dotnet.InitializationException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;

/**
 * Provides an implementation of <code>NetExecutableFactory</code>.
 *
 * @author Shane Isbell
 */
public class NetExecutableFactoryImpl
    implements NetExecutableFactory, LogEnabled
{

    /**
     * The capability matcher
     */
    private CapabilityMatcher capabilityMatcher;

    private ArtifactContext artifactContext;

    private RepositoryExecutableContext repositoryExecutableContext;

    private ExecutableContext executableContext;

    private CompilerContext compilerContext;

    private RepositoryRegistry repositoryRegistry;

    private VendorInfoRepository vendorInfoRepository;

    private StateMachineProcessor processor;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /**
     * @see NetExecutableFactory#getCompilerExecutableFor(org.apache.maven.dotnet.executable.compiler.CompilerRequirement,
     *      org.apache.maven.dotnet.executable.compiler.CompilerConfig, org.apache.maven.project.MavenProject, java.io.File)
     */
    public CompilerExecutable getCompilerExecutableFor( CompilerRequirement compilerRequirement,
                                                        CompilerConfig compilerConfig, MavenProject project,
                                                        File assemblyPath )
        throws PlatformUnsupportedException
    {

        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        vendorInfo.setVendorVersion( compilerRequirement.getVendorVersion() );
        vendorInfo.setFrameworkVersion( compilerRequirement.getFrameworkVersion() );
        vendorInfo.setVendor( compilerRequirement.getVendor() );
        try
        {
            processor.process( vendorInfo );
        }
        catch ( IllegalStateException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-011: Illegal State: Vendor Info = " + vendorInfo, e );
        }

        if ( vendorInfo.getVendor() == null || vendorInfo.getFrameworkVersion() == null )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-012: Missing Vendor Information: " + vendorInfo );
        }

        logger.info( "NPANDAY-066-013: Found Vendor = " + vendorInfo );
        compilerRequirement.setVendor( vendorInfo.getVendor() );
        compilerRequirement.setVendorVersion( vendorInfo.getVendorVersion() );
        compilerRequirement.setFrameworkVersion( vendorInfo.getFrameworkVersion() );
        List<String> executionPaths = ( compilerConfig.getExecutionPaths() == null ) ? new ArrayList<String>()
            : compilerConfig.getExecutionPaths();
        if ( vendorInfoRepository != null && vendorInfoRepository.exists() )
        {
            File sdkInstallRoot = null;
            try
            {
                sdkInstallRoot = vendorInfoRepository.getSdkInstallRootFor( vendorInfo );
            }
            catch ( PlatformUnsupportedException e )
            {
                logger.debug( "NPANDAY-066-017: Did not find an SDK install root: " + vendorInfo, e );
            }
            File installRoot = vendorInfoRepository.getInstallRootFor( vendorInfo );

            if ( installRoot != null )
            {
                executionPaths.add( installRoot.getAbsolutePath() );
            }
            if ( sdkInstallRoot != null )
            {
                executionPaths.add( sdkInstallRoot.getAbsolutePath() );
            }
        }

        compilerContext.init( compilerRequirement, compilerConfig, project, capabilityMatcher );
        if ( assemblyPath != null )
        {
            compilerContext.getCompilerCapability().setAssemblyPath( assemblyPath.getAbsolutePath() );
        }

        String netDependencyId = compilerContext.getCompilerCapability().getNetDependencyId();
        if ( netDependencyId != null )
        {
            Artifact artifact = artifactContext.getArtifactByID( netDependencyId );
            if ( artifact != null )
            {
                File artifactPath =
                    PathUtil.getPrivateApplicationBaseFileFor( artifact, compilerConfig.getLocalRepository() );
                executionPaths.add( artifactPath.getParentFile().getAbsolutePath() );
            }
        }
        compilerConfig.setExecutionPaths( executionPaths );
        try
        {
            return compilerContext.getCompilerExecutable();
        }
        catch ( ExecutionException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-007: Unable to find net executable", e );
        }
    }

    /**
     * @see NetExecutableFactory#getPluginLoaderFor(String,String,org.apache.maven.dotnet.vendor.VendorInfo,String,java.io.File,String)
     */
    public NetExecutable getPluginLoaderFor( String groupId, String artifactId, VendorInfo vendorInfo,
                                             String localRepository, File parameterFile, String mojoName )
        throws PlatformUnsupportedException
    {
        Artifact artifact = getArtifactFor(groupId, artifactId);
        return getPluginLoaderFor(artifact, vendorInfo, localRepository, parameterFile, mojoName);
    }

    public NetExecutable getPluginLoaderFor(Artifact artifact, VendorInfo vendorInfo, String localRepository, File parameterFile, String mojoName) throws PlatformUnsupportedException {
        //AssemblyRepositoryLayout layout = new AssemblyRepositoryLayout();
        File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, new File( localRepository ) );

        List<String> commands = new ArrayList<String>();
        commands.add( "parameterFile=" + parameterFile.getAbsolutePath() );
        commands.add( "assemblyFile=" + artifactPath.getAbsolutePath() );
        commands.add( "mojoName=" + mojoName );//ArtifactId = namespace

        Artifact pluginLoaderArtifact =
            artifactContext.getArtifactsFor( "NPanday.Plugin", "NPanday.Plugin.Loader", null, null ).get( 0 );
        artifactPath = PathUtil.getPrivateApplicationBaseFileFor( pluginLoaderArtifact, new File( localRepository ) );
        commands.add( "startProcessAssembly=" + artifactPath.getAbsolutePath() );

        return getNetExecutableFromRepository( "NPanday.Plugin", "NPanday.Plugin.Runner", vendorInfo,
                                               new File( localRepository ), commands, false );
    }

    public Artifact getArtifactFor(String groupId, String artifactId) throws PlatformUnsupportedException {
        List<Artifact> artifacts = artifactContext.getArtifactsFor( groupId, artifactId, null, null );
        if ( artifacts.size() == 0 )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-023: Could not locate the plugin - missing entry in the net-dependencies.xml file: GroupId = " +
                    groupId + ", ArtifactId = " + artifactId );
        }

        Artifact artifact = artifacts.get( 0 );
        if ( artifact == null )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-021: Could not locate the plugin: GroupId = " + groupId + ", ArtifactId = " + artifactId );
        }
        return artifact;
    }

    public NetExecutable getNetExecutableFromRepository( String groupId, String artifactId, VendorInfo vendorInfo,
                                                         File localRepository, List<String> commands,
                                                         boolean isIsolatedAppDomain )
        throws PlatformUnsupportedException
    {
        if ( isIsolatedAppDomain )
        {
            List<Artifact> artifacts = artifactContext.getArtifactsFor( groupId, artifactId, null, null );
            if ( artifacts.size() == 0 )
            {
                throw new PlatformUnsupportedException(
                    "NPANDAY-066-024: Could not locate the executable - missing entry in the net-dependencies.xml file: GroupId = " +
                        groupId + ", ArtifactId = " + artifactId );
            }

            Artifact artifact = artifacts.get( 0 );
            if ( artifact == null )
            {
                throw new PlatformUnsupportedException( "NPANDAY-066-025: Could not locate the executable: GroupId = " +
                    groupId + ", ArtifactId = " + artifactId );
            }

            File artifactPath = PathUtil.getPrivateApplicationBaseFileFor( artifact, localRepository );
            commands.add( "startProcessAssembly=" + artifactPath.getAbsolutePath() );
            //TODO: Replace
            String pluginArtifactPath = PathUtil.getPrivateApplicationBaseFileFor(
                artifactContext.getArtifactsFor( "NPanday.Plugin", "NPanday.Plugin", null, null ).get( 0 ),
                localRepository ).getAbsolutePath();

            commands.add( "pluginArtifactPath=" + pluginArtifactPath );
            return getNetExecutableFromRepository( "NPanday.Plugin", "NPanday.Plugin.Runner", vendorInfo, localRepository,
                                                   commands, false );
        }

        if ( commands == null )
        {
            commands = new ArrayList<String>();
        }

        try
        {
            processor.process( vendorInfo );
        }
        catch ( IllegalStateException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-010: Illegal State: Vendor Info = " + vendorInfo, e );
        }

        if ( vendorInfo.getVendor() == null || vendorInfo.getFrameworkVersion() == null )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-020: Missing Vendor Information: " + vendorInfo );
        }
        List<Artifact> artifacts = artifactContext.getArtifactsFor( groupId, artifactId, null, null );
        if ( artifacts.size() == 0 )
        {
            throw new PlatformUnsupportedException(
                "NPANDAY-066-022: Could not locate the executable- missing entry in the net-dependencies.xml: GroupId = " +
                    groupId + ", ArtifactId = " + artifactId );
        }
        Artifact artifact = artifacts.get( 0 );

        logger.debug( "NPANDAY-066-003: Found Vendor: " + vendorInfo );

        File artifactPath =  PathUtil.getPrivateApplicationBaseFileFor( artifact, localRepository );
        List<String> modifiedCommands = new ArrayList<String>();
        String exe = null;
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
                logger.info(
                    "NPANDAY-066-005: Executable path for mono does not exist. Will attempt to execute MONO using" +
                        " the main PATH variable." );
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
        ExecutableConfig executableConfig = ExecutableConfig.Factory.createDefaultExecutableConfig();
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

    public NetExecutable getJavaExecutableFromRepository( VendorInfo vendorInfo, List<String> commands )
        throws PlatformUnsupportedException
    {

        if ( commands == null )
        {
            commands = new ArrayList<String>();
        }

        try
        {
            processor.process( vendorInfo );
        }
        catch ( IllegalStateException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-010: Illegal State: Vendor Info = " + vendorInfo, e );
        }

        if ( vendorInfo.getVendor() == null || vendorInfo.getFrameworkVersion() == null ||
            vendorInfo.getVendorVersion() == null )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-018: Missing Vendor Information: " + vendorInfo );
        }

        ExecutableRequirement executableRequirement =
            ExecutableRequirement.Factory.createDefaultExecutableRequirement();
        executableRequirement.setVendor( vendorInfo.getVendor() );
        executableRequirement.setFrameworkVersion( vendorInfo.getFrameworkVersion() );
        executableRequirement.setVendorVersion( vendorInfo.getVendorVersion() );
        executableRequirement.setProfile( "dotnet-jetty:start" );//TODO: Remove hard-coded value

        ExecutableConfig executableConfig = ExecutableConfig.Factory.createDefaultExecutableConfig();
        executableConfig.setCommands( commands );

        executableConfig.setExecutionPaths( new ArrayList<String>() );
        executableContext.init( executableRequirement, executableConfig, capabilityMatcher );

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
     * @see NetExecutableFactory
     */
    public NetExecutable getNetExecutableFor( String vendor, String frameworkVersion, String profile,
                                              List<String> commands, File netHome )
        throws PlatformUnsupportedException
    {

        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        vendorInfo.setVendorVersion( "" );
        vendorInfo.setFrameworkVersion( frameworkVersion );
        if ( vendor != null )
        {
            vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
        }

        try
        {
            processor.process( vendorInfo );
        }
        catch ( IllegalStateException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-010: Illegal State: Vendor Info = " + vendorInfo, e );
        }

        if ( vendorInfo.getVendor() == null || vendorInfo.getFrameworkVersion() == null )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-019: Missing Vendor Information: " + vendorInfo );
        }

        logger.debug( "NPANDAY-066-003: Found Vendor: " + vendorInfo );
        ExecutableRequirement executableRequirement =
            ExecutableRequirement.Factory.createDefaultExecutableRequirement();
        executableRequirement.setVendor( vendorInfo.getVendor() );
        executableRequirement.setFrameworkVersion( vendorInfo.getFrameworkVersion() );
        executableRequirement.setVendorVersion( vendorInfo.getVendorVersion() );
        executableRequirement.setProfile( profile );

        ExecutableConfig executableConfig = ExecutableConfig.Factory.createDefaultExecutableConfig();
        executableConfig.setCommands( commands );

        List<String> executablePaths = ( executableConfig.getExecutionPaths() == null ) ? new ArrayList<String>()
            : executableConfig.getExecutionPaths();
        if ( netHome != null && netHome.exists() )
        {
            logger.info( "NPANDAY-066-014: Found executable path from pom: Path = " + netHome.getAbsolutePath() );
            executablePaths.add( netHome.getAbsolutePath() );
        }
        else if ( vendorInfo.getExecutablePaths() != null )
        {
            for ( File path : vendorInfo.getExecutablePaths() )
            {
                if ( path.exists() )
                {
                    logger.debug( "NPANDAY-066-015: Found executable path: Path = " + path.getAbsolutePath() );
                    executablePaths.add( path.getAbsolutePath() );
                }
            }
        }
        else
        {
            logger.info( "NPANDAY-066-016: Did not find executable path, will try system path" );
        }
        executableConfig.setExecutionPaths( executablePaths );
        executableContext.init( executableRequirement, executableConfig, capabilityMatcher );

        try
        {
            return executableContext.getNetExecutable();
        }
        catch ( ExecutionException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-066-001: Unable to find net executable", e );
        }
    }
}
