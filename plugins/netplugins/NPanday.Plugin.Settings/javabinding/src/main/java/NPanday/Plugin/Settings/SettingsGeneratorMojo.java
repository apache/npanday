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

 package NPanday.Plugin.Settings;

import npanday.plugin.FieldAnnotation;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * @phase validate
 * @goal generate-settings
 */
public class SettingsGeneratorMojo
    extends npanday.plugin.AbstractMojo
{
    /**
     * @parameter expression = "${project}"
     */
    private org.apache.maven.project.MavenProject project;

    /**
     * @parameter expression = "${settings.localRepository}"
     */
    private String localRepository;

    /**
     * @parameter expression = "${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @component
     */
    private npanday.plugin.PluginContext pluginContext;

    public String getMojoArtifactId()
    {
        return "NPanday.Plugin.Settings";
    }

    public String getMojoGroupId()
    {
        return "npanday.plugin";
    }

    public String getClassName()
    {
        return "NPanday.Plugin.Settings.SettingsGeneratorMojo";
    }

    public npanday.plugin.PluginContext getNetPluginContext()
    {
        return pluginContext;
    }

    public npanday.executable.NetExecutableFactory getNetExecutableFactory()
    {
        return netExecutableFactory;
    }

    public org.apache.maven.project.MavenProject getMavenProject()
    {
        return project;
    }

    public String getLocalRepository()
    {
        return localRepository;
    }

    public String getVendorVersion()
    {
        return vendorVersion;
    }

    public String getVendor()
    {
        return vendor;
    }

    public String getFrameworkVersion()
    {
        return frameworkVersion;
    }

    /** @parameter default-value="false" */
    private boolean skip;

    public boolean preExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !System.getProperty( "os.name" ).contains( "Windows" ) )
        {
            return false;
        }

        if ( skip )
        {
            return false;
        }

        return true;
    }
}
