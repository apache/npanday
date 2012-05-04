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
package npanday.plugin.generator;

import npanday.ArtifactType;
import npanday.LocalRepositoryUtil;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Java Bindings for .NET Mojos.
 *
 * @author Shane Isbell
 * @goal generate-bindings
 * @phase deploy
 * @description Generates Java Bindings for .NET Mojos
 */
public class MojoGeneratorMojo
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
     * The base directory of the project.
     *
     * @parameter expression="${basedir}"
     */
    public String basedir;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * The vendor version to compile under. For Microsoft, this will be the same as the framework version.
     *
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * The framework version to compile under: 1.1, 2.0, 3.0
     *
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
    private ArtifactFactory artifactFactory;

    /** @parameter default-value="false" */
    private boolean skip;

    // TODO: get npandayVersion injected somehow
    private String npandayVersion = "1.5.0-incubating-SNAPSHOT";

    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            return;
        }

        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );

        try
        {    
            List<String> commands = new ArrayList<String>();
            VendorRequirement vendorRequirement = new VendorRequirement( vendor, vendorVersion, frameworkVersion );

            File targetDir = PathUtil.getPrivateApplicationBaseDirectory( project );
            File targetAssemblyFile =
                new File( targetDir, project.getArtifactId() + ".dll" );
            commands.add( "targetAssemblyFile=" + targetAssemblyFile.getAbsolutePath() );
            commands.add( "outputDirectory=" + basedir );
            commands.add( "groupId=" + project.getGroupId() );
            commands.add( "artifactId=" + project.getArtifactId() );
            commands.add( "artifactVersion=" + project.getVersion());

            Artifact artifact = artifactFactory.createDependencyArtifact(
                "org.apache.npanday.plugins",
                "NPanday.Plugin.MojoGenerator",
                VersionRange.createFromVersion( npandayVersion ),
                ArtifactType.DOTNET_EXECUTABLE.getPackagingType(),
                null,
                "runtime"
            );

            netExecutableFactory.getPluginRunner(
                project, artifact, null, vendorRequirement, LocalRepositoryUtil.create( localRepository ), commands,
                targetDir, npandayVersion
            ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }
}
