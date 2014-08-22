package npanday.plugin.wix;

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
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

public abstract class AbstractWixMojo
    extends AbstractMojo
{
    /**
     * The vendor of the framework, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * The version of the framework vendor, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * The framework version, the executable is compatible with.
     *
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The configured executable version, from executable-plugins.xml, to be used.
     *
     * @parameter expression="${wix.version}" default-value="3.0"
     */
    private String executableVersion;

    /**
     * WiX extensions to use
     *
     * @parameter
     */
    protected String[] extensions;

    /**
     * Arguments to pass to WiX executable as is
     *
     * @parameter expression="${arguments}"
     */

    protected String arguments;

    /**
     * @parameter expression="${wix.home}" default-value="${env.WIX}"
     */
    private File wixHome;

    /**
     * Suppress schema validation of documents (performance boost)
     *
     * @parameter expression="${suppressSchemaValidation}"
     */
    private boolean suppressSchemaValidation;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            List<String> commands = Lists.newArrayList();
                    
            if ( extensions != null )
            {
                for ( String ext : extensions )
                {
                    commands.add( "-ext" );
                    commands.add( ext );
                }
            }

            if ( suppressSchemaValidation )
            {
                commands.add( "-ss" );
            }

            if ( arguments != null )
            {
                commands.add( arguments );
            }

            commands.addAll( getArguments() );
            
            NetExecutable executor = netExecutableFactory.getExecutable(
                new ExecutableRequirement(
                        vendor, vendorVersion, frameworkVersion, getExecutableIdentifier(), executableVersion
                        ), commands, (wixHome != null) ? new File( wixHome, "bin" ) : null
                    );
            executor.execute();
        }
        catch (ExecutionException e) {
            throw new MojoExecutionException(
                "Unable to execute '" + getExecutableIdentifier() + "' for vendor " + vendor + " v"
                    + vendorVersion + " and frameworkVersion = " + frameworkVersion, e
                );
        }
        catch (PlatformUnsupportedException e) {
            throw new MojoExecutionException(
                "Unable to execute '" + getExecutableIdentifier() + "' for vendor " + vendor + " v"
                    + vendorVersion + " and frameworkVersion = " + frameworkVersion, e
                );
        }
    }

    public abstract String getExecutableIdentifier();
    
    public abstract List<String> getArguments()
        throws MojoExecutionException;
}
