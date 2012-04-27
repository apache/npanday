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

package npanday.plugin.azure;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;


/**
 * Abstract Mojo for interaction with CSPack
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractCSPackDeployMojo
    extends AbstractNPandaySettingsAwareMojo
{

    /**
     * The executable identifier used to locate the right configurations from executable-plugins.xml. Can't be changed.
     */
    private String executableIdentifier = "CSPACK";

    /**
     * The configured executable version, from executable-plugins.xml, to be used. Should align to a installed
     * Azure SDK Version.
     *
     * @parameter expression="${azuresdk.version}" default-value="1.6"
     */
    private String executableVersion;

    /**
     * The configured executable profile, from executable-plugins.xml, to be used.
     *
     * @parameter expression="${cspack.profile}"
     */
    private String executableProfile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        super.execute();

        beforeCommandExecution();

        try
        {
            // TODO: confusion of 'identifier' and 'profile' / align to new NPANDAY-499
            final NetExecutable executable = netExecutableFactory.getExecutable(
                new ExecutableRequirement(
                    vendor, vendorVersion, frameworkVersion, executableIdentifier, executableVersion
                    /*, executableProfile*/
                ), getCommands(), null
            );
            executable.execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-122-000: Unable to execute profile '" + executableIdentifier + "' for vendor " + vendor + " v"
                    + vendorVersion + " and frameworkVersion = " + frameworkVersion, e
            );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-122-001: Unsupported platform configuration. Could not find profile '" + executableIdentifier
                    + "' for vendor " + vendor + " v" + vendorVersion + " and frameworkVersion = " +
                    frameworkVersion, e
            );
        }

        afterCommandExecution();
    }

    protected abstract void afterCommandExecution() throws MojoExecutionException;

    protected abstract void beforeCommandExecution();

    protected abstract List<String> getCommands() throws MojoExecutionException, MojoFailureException;
}
