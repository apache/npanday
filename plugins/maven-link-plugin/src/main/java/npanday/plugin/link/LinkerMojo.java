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

package npanday.plugin.link;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Link modules into assemblies. 
 *
 * @author Shane Isbell
 * @goal package
 * @phase package
 * @description Links the specified netmodules to the project's assembly
 */
public class LinkerMojo
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
     * @parameter expression = "${profile}" default-value = "AL"
     */
    private String profile;

    /**
     * The netmodules to link.
     *
     * @parameter expression = "${netModules}"
     */
    private ArrayList<String> netModules;

    /**
     * The output file to link to.
     *
     * @parameter expression = "${outputFile}"
     */
    private File outputFile;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException
    {
        if ( netModules == null || netModules.size() == 0 || outputFile == null )
        {
            return;
        }
        getLog().info( "NPANDAY-1400-002: Linking modules to assembly: Output File = " + outputFile.getAbsolutePath() );

        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );

        try
        {
            netExecutableFactory.getExecutable(
                new ExecutableRequirement( vendor, null, frameworkVersion, profile ), getCommands(), null
            )
                .execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-1400-000: Unable to execute al: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-1400-001: Platform Unsupported: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        for ( String file : netModules )
        {
            commands.add( new File(file).getAbsolutePath() );
        }

        commands.add( "/out:" + outputFile.getAbsolutePath() );
        return commands;
    }
}
