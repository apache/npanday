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
package npanday.plugin.resolver;

import npanday.PlatformUnsupportedException;
import npanday.artifact.ArtifactContext;
import npanday.artifact.NPandayArtifactResolutionException;
import npanday.artifact.NetDependenciesRepository;
import npanday.artifact.NetDependencyMatchPolicy;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.model.netdependency.NetDependency;
import npanday.registry.RepositoryRegistry;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shane Isbell
 * @goal resolve
 * @phase process-resources
 * @description Resolves the .NET assemblies that NPanday needs to run.
 */
public class NetDependencyResolverMojo
    extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    private File pomFile;

    /**
     * @parameter
     */
    private NetDependency[] netDependencies;

    /**
     * The Vendor for the executable.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @parameter expression = "${installGacDependencies}" default-value="false"
     */
    private boolean isGacInstall;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @parameter default-value="false"
     */
    private boolean skip;

    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( skip )
        {
            return;
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        String profile = System.getProperty( "dependencyProfile" );

        if ( netDependencies == null )
        {
            netDependencies = new NetDependency[0];
        }
        List<Dependency> dependencies = new ArrayList<Dependency>();
        for ( NetDependency netDependency : netDependencies )
        {
            Dependency dependency = new Dependency();
            dependency.setGroupId( netDependency.getGroupId() );
            dependency.setArtifactId( netDependency.getArtifactId() );
            dependency.setVersion( netDependency.getVersion() );
            dependency.setScope( Artifact.SCOPE_RUNTIME );
            dependency.setType( netDependency.getType() );
            dependencies.add( dependency );
        }

        artifactContext.init( project, project.getRemoteArtifactRepositories(), localRepository );
        if ( !new File( localRepository, "npanday.artifacts.resolved" ).exists() ) //performance optimization
        {
            try
            {
                artifactContext.getArtifactInstaller().resolveAndInstallNetDependenciesForProfile( profile,
                                                                                                   dependencies, null );
            }
            catch ( NPandayArtifactResolutionException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            new File( localRepository, "npanday.artifacts.resolved" ).mkdir();
        }

        //Do GAC Install, if needed
        //TODO: Add in the dependencies from the MOJO config
        if ( isGacInstall )
        {
            NetDependenciesRepository repository =
                (NetDependenciesRepository) repositoryRegistry.find( "net-dependencies" );
            getLog().info( "NPANDAY-1600-001: Found net dependencies: Number = " + dependencies.size() );

            List<NetDependencyMatchPolicy> gacInstallPolicies = new ArrayList<NetDependencyMatchPolicy>();
            gacInstallPolicies.add( new GacMatchPolicy( true ) );
            List<Dependency> gacInstallDependencies = repository.getDependenciesFor( gacInstallPolicies );
            for ( Dependency dependency : gacInstallDependencies )
            {
                List<Artifact> artifacts =
                    artifactContext.getArtifactsFor( dependency.getGroupId(), dependency.getArtifactId(),
                                                     dependency.getVersion(), dependency.getType() );
                try
                {
                    NetExecutable netExecutable = netExecutableFactory.getNetExecutableFor(
                        new ExecutableRequirement( vendor, null, frameworkVersion, "GACUTIL" ),
                        getGacInstallCommandsFor( artifacts.get( 0 ) ), null );
                    netExecutable.execute();
                    getLog().info( "NPANDAY-1600-004: Installed Assembly into GAC: Assembly = "
                                       + artifacts.get( 0 ).getFile().getAbsolutePath() + ",  Vendor = "
                                       + netExecutable.getVendor().getVendorName() );
                }
                catch ( ExecutionException e )
                {
                    throw new MojoExecutionException(
                        "NPANDAY-1600-005: Unable to execute gacutil: Vendor " + vendor + ", frameworkVersion = "
                            + frameworkVersion + ", Profile = " + profile, e );
                }
                catch ( PlatformUnsupportedException e )
                {
                    throw new MojoExecutionException(
                        "NPANDAY-1600-006: Platform Unsupported: Vendor " + vendor + ", frameworkVersion = "
                            + frameworkVersion + ", Profile = " + profile, e );
                }
            }
        }

        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }

    public List<String> getGacInstallCommandsFor( Artifact artifact )
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( "/nologo" );
        commands.add( "/i" );
        commands.add( artifact.getFile().getAbsolutePath() );
        return commands;
    }

    private class GacMatchPolicy
        implements NetDependencyMatchPolicy
    {

        private boolean isGacInstall;

        public GacMatchPolicy( boolean isGacInstall )
        {
            this.isGacInstall = isGacInstall;
        }

        public boolean match( NetDependency netDependency )
        {
            return netDependency.isIsGacInstall() == isGacInstall;
        }
    }
}
