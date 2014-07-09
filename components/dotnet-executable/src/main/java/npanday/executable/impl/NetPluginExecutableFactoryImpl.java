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
import npanday.executable.*;
import npanday.resolver.NPandayArtifactResolver;
import npanday.vendor.IllegalStateException;
import npanday.vendor.*;
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
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Provides an implementation of <code>NetExecutableFactory</code>.
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component role="npanday.executable.NetPluginExecutableFactory"
 */
public class NetPluginExecutableFactoryImpl
    extends AbstractLogEnabled
    implements NetPluginExecutableFactory
{
    /**
     * @plexus.requirement
     */
    private RepositoryExecutableContext repositoryExecutableContext;

    /**
     * @plexus.requirement
     */
    private StateMachineProcessor processor;

    /**
     * @plexus.requirement
     */
    private NPandayArtifactResolver artifactResolver;

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * @plexus.requirement
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * Returns the path of the artifact within the private application base.
     *
     *
     *
     * @param artifact        the artifact to find the path of.  This value should not be null.
     * @param outputDir
     * @return the path of the artifact within the private application base or null if either of the specified
     *         parameters is null
     */
    private File getPrivateApplicationBaseFileFor(Artifact artifact, File outputDir) throws ArtifactResolutionException {
        if ( artifact == null )
        {
            getLogger().warn( "NPANDAY-040-003: Artifact is null - Cannot get application file." );
            return null;
        }

        outputDir.mkdir();

        String filename = artifact.getArtifactId() + "." + artifact.getArtifactHandler().getExtension();
        File targetFile = new File(outputDir, filename );

        try
        {
            FileUtils.copyFile(artifact.getFile(), targetFile);
        }
        catch (IOException ioe)
        {
            throw new ArtifactResolutionException("NPANDAY-1005-0001: Error copying dependency", artifact, ioe);
        }

        return targetFile;
    }

    public NetExecutable getPluginRunner(
        MavenProject project, Artifact pluginArtifact, Set<Artifact> additionalDependencies,
        VendorRequirement vendorRequirement, ArtifactRepository localRepository, List<String> commands, File targetDir,
        String npandayVersion ) throws

        PlatformUnsupportedException,
        ArtifactResolutionException,
        ArtifactNotFoundException{

        Set dependencies = Sets.newHashSet(pluginArtifact);
        if (additionalDependencies != null)
        {
            dependencies.addAll( additionalDependencies );
        }

        // need to resolve what we can here since we need the path!
        Set<Artifact> artifacts = makeAvailable(
            project.getArtifact(), project.getManagedVersionMap(), dependencies, targetDir, localRepository,
            // TODO: consider, if this must be getRemotePluginRepositories()!!
            project.getRemoteArtifactRepositories()
        );

        commands.add( "startProcessAssembly=" + pluginArtifact.getFile().getAbsolutePath() );

        String pluginArtifactPath = findArtifact( artifacts, "NPanday.Plugin").getFile().getAbsolutePath();
        commands.add( "pluginArtifactPath=" + pluginArtifactPath );

        return getArtifactExecutable(
            project, createPluginRunnerArtifact( npandayVersion ), dependencies, vendorRequirement, localRepository, commands, targetDir
        );
    }

    public NetExecutable getArtifactExecutable(
        MavenProject project, Artifact executableArtifact, Set<Artifact> additionalDependencies,
        VendorRequirement vendorRequirement, ArtifactRepository localRepository, List<String> commands,
        File targetDir ) throws

        PlatformUnsupportedException,
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        Set dependencies = Sets.newHashSet(executableArtifact);
        if (additionalDependencies != null){
            dependencies.addAll( additionalDependencies );
        }

        makeAvailable(
            project.getArtifact(), project.getManagedVersionMap(), dependencies, targetDir, localRepository,
            // TODO: consider, if this must be getRemotePluginRepositories()!!
            project.getRemoteArtifactRepositories()
        );

        File artifactPath = executableArtifact.getFile();

        if ( commands == null )
        {
            commands = new ArrayList<String>();
        }

        // TODO: this should be a separate implementation of NetExecutable, configured only for MONO!!!

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
        getLogger().debug( "NPANDAY-066-003: Found Vendor: " + vendorInfo );



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
                getLogger().info(
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

    public Artifact findArtifact( Set<Artifact> artifacts, String artifactId ) throws ArtifactNotFoundException
    {
        List<String> ids = Lists.newArrayList();
        for ( Artifact a : artifacts )
        {
            ids.add( a.getArtifactId() );

            if ( a.getArtifactId().equalsIgnoreCase( artifactId ) )
            {
                return a;
            }
        }

        throw new ArtifactNotFoundException(
            "NPANDAY-066-11: Could not find artifact " + artifactId + " among " + ids, "*", artifactId, "*", "*", null,
            null, null, null, null
        );
    }

    public NetExecutable getPluginExecutable(
        MavenProject project, Artifact pluginArtifact, VendorRequirement vendorRequirement,
        ArtifactRepository localRepository, File parameterFile, String mojoName, File targetDir, String npandayVersion ) throws
        PlatformUnsupportedException,
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        Set<Artifact> dependencies = Sets.newHashSet(pluginArtifact);

        Artifact loaderArtifact = artifactFactory.createDependencyArtifact(
            "org.apache.npanday.plugins", "NPanday.Plugin.Loader",
            VersionRange.createFromVersion( npandayVersion ),
            ArtifactType.DOTNET_EXECUTABLE.getPackagingType(), null, "runtime"
        );
        dependencies.add(
            pluginArtifact
        );

        // preresolve this one
        artifactResolver.resolve( pluginArtifact, project.getRemoteArtifactRepositories(), localRepository );
        File pluginArtifactPath = getPrivateApplicationBaseFileFor(pluginArtifact, targetDir);

        List<String> commands = new ArrayList<String>();
        commands.add( "parameterFile=" + parameterFile.getAbsolutePath() );
        commands.add( "assemblyFile=" + pluginArtifactPath.getAbsolutePath() );
        commands.add( "mojoName=" + mojoName );//ArtifactId = namespace

        return getPluginRunner(
            project, loaderArtifact, dependencies, vendorRequirement, localRepository, commands, targetDir,
            npandayVersion
        );
    }

    private Artifact createPluginRunnerArtifact( String npandayVersion )
    {
        return artifactFactory.createDependencyArtifact(
                "org.apache.npanday.plugins", "NPanday.Plugin.Runner",
                VersionRange.createFromVersion( npandayVersion ),
                ArtifactType.DOTNET_EXECUTABLE.getPackagingType(), null, "runtime"
            );
    }

    private Set<Artifact> makeAvailable(
        Artifact originating, Map managedVersions, Set<Artifact> artifacts, File targetDir,
        ArtifactRepository localRepository, List remoteArtifactRepositories ) throws
        ArtifactResolutionException,
        ArtifactNotFoundException
    {
        ArtifactResolutionResult results = artifactResolver.resolveTransitively(
            artifacts, originating, managedVersions, localRepository, remoteArtifactRepositories, artifactMetadataSource,
            new ScopeArtifactFilter( "runtime" )
        );

        for(Object ao : results.getArtifacts()){
            Artifact a = (Artifact)ao;
            a.setFile( getPrivateApplicationBaseFileFor(a, targetDir) );
        }

        return results.getArtifacts();
    }
}
