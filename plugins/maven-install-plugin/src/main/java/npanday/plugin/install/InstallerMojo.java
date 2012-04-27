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
package npanday.plugin.install;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Installs assembly into the local repository
 *
 * @goal install
 * @phase install
 * @description Installs assembly into the local repository
 */
public class InstallerMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * @parameter expression="${project.file}"
     */
    private File pomFile;

    /**
     * Install into the GAC?
     *
     * @parameter expression="${isGacInstall}" default-value = "false"
     */
    private boolean isGacInstall;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

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
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "GACUTIL"
     */
    private String profile;

    /**
     * The artifact factory component, which is used for creating artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * Used for resolving snapshot
     *
     * @component
     */
    private ArtifactResolver artifactResolver;

    public void execute()
        throws MojoExecutionException
    {
        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );

        if ( !project.getPackaging().equals( "pom" ) )
        {
            if ( isGacInstall )
            {
                try
                {
                    NetExecutable netExecutable = netExecutableFactory.getExecutable(
                        new ExecutableRequirement( vendor, null, frameworkVersion, profile ), getCommands(), null
                    );
                    netExecutable.execute();
                    getLog().info( "NPANDAY-1400-003: Installed Assembly into GAC: Assembly = " +
                        project.getArtifact().getFile() + ",  Vendor = " + netExecutable.getVendor().getVendorName() );
                }
                catch ( ExecutionException e )
                {
                    throw new MojoExecutionException( "NPANDAY-1400-000: Unable to execute gacutil: Vendor " + vendor +
                        ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
                }
                catch ( PlatformUnsupportedException e )
                {
                    throw new MojoExecutionException( "NPANDAY-1400-001: Platform Unsupported: Vendor " + vendor +
                        ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
                }
            }

            getLog().warn( "NPANDAY-251: removed UAC?? install here!" );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( "/i" );
        commands.add( project.getArtifact().getFile().getAbsolutePath() );
        return commands;
    }
}
