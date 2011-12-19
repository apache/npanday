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
package npanday.plugin.msdeploy;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.util.List;


/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractMsDeployMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;


    /**
     * The vendor of the framework, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * The version of the framework vendor, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendor}"
     */
    private String vendorVersion;

    /**
     * The framework version, the executable is compatible with.
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The configured profile, from executable-plugins.xml, to be used.
     *
     * @parameter expression = "${profile}" default-value = "MSDEPLOY"
     */
    private String profile;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * The maven project helper.
     *
     * @component
     */
    protected MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException
    {
        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );

        beforeCommandExecution();

        try
        {
            final NetExecutable executable = netExecutableFactory.getNetExecutableFor(
                new ExecutableRequirement( vendor, vendorVersion, frameworkVersion, profile ), getCommands(), null
            );
            executable.execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-120-000: Unable to execute profile '" + profile + "' for vendor " + vendor + " v"
                    + vendorVersion + " and frameworkVersion = " + frameworkVersion, e
            );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-120-001: Unsupported platform configuration. Could not find profile '" + profile
                    + "' for vendor " + vendor + " v" + vendorVersion + " and frameworkVersion = " +
                    frameworkVersion, e
            );
        }

        afterCommandExecution();
    }

    protected abstract void afterCommandExecution() throws MojoExecutionException;

    protected abstract void beforeCommandExecution();

    abstract List<String> getCommands() throws MojoExecutionException;
}
