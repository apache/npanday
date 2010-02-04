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

package NPanday.Plugin.Msbuild;

import java.io.File;
import java.io.IOException;

import npanday.plugin.FieldAnnotation;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * @phase validate
 * @goal compile
 * @requiresDependencyResolution test
 */
public class MsbuildMojo
    extends npanday.plugin.AbstractMojo
{
    /**
     * @parameter expression = "${settings.localRepository}"
     */
    @FieldAnnotation()
    public java.lang.String repository;

    /**
     * @parameter expression = "${project}"
     */
    @FieldAnnotation()
    public org.apache.maven.project.MavenProject mavenProject;

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

    /**
     * @parameter default-value=".references"
     */
    private File referencesDirectory;

    /**
     * @parameter default-value="true"
     */
    private boolean copyReferences = true;

    public String getMojoArtifactId()
    {
        return "NPanday.Plugin.Msbuild";
    }

    public String getMojoGroupId()
    {
        return "npanday.plugin";
    }

    public String getClassName()
    {
        return "NPanday.Plugin.Msbuild.MsbuildMojo";
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

    @Override
    public boolean preExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( copyReferences )
        {
            for ( Object artifact : project.getDependencyArtifacts() )
            {
                Artifact a = (Artifact) artifact;

                String path =
                    a.getGroupId() + "/" + a.getArtifactId() + "-" + a.getBaseVersion() + "/" + a.getArtifactId() + "." +
                        a.getArtifactHandler().getExtension();
                File targetFile = new File( referencesDirectory, path );

                if ( !targetFile.exists() )
                {
                    targetFile.getParentFile().mkdirs();

                    try
                    {
                        FileUtils.copyFile( a.getFile(), targetFile );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException(
                            "Error copying reference from the local repository to .references: " + e.getMessage(), e );
                    }
                }
            }
        }

        return super.preExecute();
    }
}
