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

package npanday.plugin.aspnet;

import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import npanday.vendor.VendorRequirement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class AbstractNPandaySettingsAwareMojo
    extends AbstractNPandayMojo
{

    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    protected String settingsPath;

    /**
     * The vendor of the framework, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendor}"
     */
    protected String vendor;

    /**
     * The version of the framework vendor, the executable is provided by or compatible with.
     *
     * @parameter expression="${vendorVersion}"
     */
    protected String vendorVersion;

    /**
     * The framework version, the executable is compatible with.
     *
     * @parameter expression = "${frameworkVersion}"
     */
    protected String frameworkVersion;

    /**
     * @component
     */
    protected RepositoryRegistry repositoryRegistry;

    /**
     * @component
     */
    protected npanday.executable.NetExecutableFactory netExecutableFactory;

    @Override
    protected void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );
    }

    protected VendorRequirement getVendorRequirement(){
        return new VendorRequirement( vendor, vendorVersion, frameworkVersion);
    }
}
