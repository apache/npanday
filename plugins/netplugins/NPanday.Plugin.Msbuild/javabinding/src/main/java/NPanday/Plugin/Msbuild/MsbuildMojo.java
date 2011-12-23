package NPanday.Plugin.Msbuild;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import npanday.plugin.FieldAnnotation;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
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
     * @parameter expression = "${reactorProjects}"
     */
    private List<org.apache.maven.project.MavenProject> reactorProjects;

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
        return "org.apache.npanday.plugins";
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
            Map<String,MavenProject> projects = new HashMap<String,MavenProject>();
            for ( MavenProject p : reactorProjects )
            {
                projects.put( ArtifactUtils.versionlessKey( p.getGroupId(), p.getArtifactId() ), p );
            }
            getLog().info( "projects = " + projects.keySet() );

            for ( Object artifact : project.getDependencyArtifacts() )
            {
                Artifact a = (Artifact) artifact;

                File targetDir;
                String vKey = ArtifactUtils.versionlessKey( a );
                if ( !projects.containsKey( vKey ) )
                {
                    String path =
                        a.getGroupId() + "/" + a.getArtifactId() + "-" + a.getBaseVersion();
                    targetDir = new File( referencesDirectory, path );
                }
                else
                {
                    // Likely a project reference in MSBuild. 
                    // If the other project was not built with MSBuild, make sure the artifact is present where it will look for it
                    // Note: deliberately limited for now - will only work with reactor projects and doesn't test what are references and what are not
                    // TODO: support other configurations, or more aligned MSBuild-based builds
                    targetDir = new File( projects.get( vKey ).getBasedir(), "bin/Debug" );
                }
                File targetFile = new File( targetDir, a.getArtifactId() + "." + a.getArtifactHandler().getExtension() );
    
                getLog().info( "Copying reference " + vKey + " to " + targetFile );
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

        // #12549 - add resources generated by MSBuild to the project
        // TODO: should be configurable?
        Resource resource = new Resource();
        resource.setDirectory( "obj/Debug" );
        resource.addInclude( "**/*.resources" );
        project.addResource( resource );

        return super.preExecute();
    }
}
