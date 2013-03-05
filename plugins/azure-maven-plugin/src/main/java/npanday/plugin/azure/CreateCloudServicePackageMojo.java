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

import com.google.common.collect.Lists;
import npanday.ArtifactType;
import npanday.LocalRepositoryUtil;
import npanday.PathUtil;
import npanday.resolver.NPandayDependencyResolution;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Packages roles specified in the dependencies into a common cloud service package (*.cspkg), accompanied
 * with a configuration template (*.cscfg).
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal create-package
 */
public class CreateCloudServicePackageMojo
    extends AbstractCSPackDeployMojo
{
    private File packageFile, templateConfigurationFile;

    /**
     * The service definition file that get passed along to cspack.
     *
     * @parameter expression="${azure.serviceDefinition}" default-value="${basedir}/ServiceDefinition.csdef"
     */
    private File serviceDefinitionFile;

    /**
     * If a vanilla cloud service configuration file should be generated and attached along with the package.
     *
     * @parameter expression="${azure.generateConfigurationFile}" default-value="true"
     */
    private boolean generateConfigurationFile;

    /**
     * @component
     */
    private NPandayDependencyResolution dependencyResolution;

    /**
     * The scope up to which dependencies should be included.
     *
     * @parameter default-value="runtime"
     */
    private String scope;

    /**
     * The location of the local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    private void resolveDependencies() throws MojoExecutionException
    {
        try
        {
            dependencyResolution.require( project, LocalRepositoryUtil.create( localRepository ), scope );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-131-004: Could not satisfy required dependencies of scope " + scope, e
            );
        }
    }

    /**
     * Dependency aliases to use, in case they don't match the artifact ID.
     *
     * @parameter
     */
    private Properties aliases;

    @Override
    protected void afterCommandExecution() throws MojoExecutionException
    {
        attachPackageFile();

        if ( generateConfigurationFile )
        {
            attachConfigurationFile();
        }
    }

    private void attachPackageFile() throws MojoExecutionException
    {
        if ( !packageFile.exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-123-001: CSPack seemed to fail on creating the package " + packageFile.getAbsolutePath()
            );
        }

        project.getArtifact().setFile( packageFile );
    }

    private void attachConfigurationFile() throws MojoExecutionException
    {
        if ( !templateConfigurationFile.exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-123-002: CSPack seemed to fail on creating the template configuration file "
                    + packageFile.getAbsolutePath()
            );

        }

        projectHelper.attachArtifact(
            project, ArtifactType.AZURE_CLOUD_SERVICE_CONFIGURATION.getPackagingType(), "generated",
            templateConfigurationFile
        );
    }

    @Override
    protected void beforeCommandExecution()
    {
        packageFile = new File(
            project.getBuild().getDirectory(),
            project.getArtifactId() + "." + ArtifactType.AZURE_CLOUD_SERVICE.getExtension()
        );

        templateConfigurationFile = new File(
            project.getBuild().getDirectory(), project.getArtifactId() + "-configtemplate" + "."
            + ArtifactType.AZURE_CLOUD_SERVICE_CONFIGURATION.getExtension()
        );
    }

    @Override
    protected List<String> getCommands() throws MojoExecutionException, MojoFailureException
    {
        List<String> commands = Lists.newArrayList();

        commands.add( serviceDefinitionFile.getAbsolutePath() );

        if ( generateConfigurationFile )
        {
            commands.add( "/generateConfigurationFile:" + templateConfigurationFile.getAbsolutePath() );
        }

        commands.add( "/out:" + packageFile.getAbsolutePath() );

        resolveDependencies();
        ScopeArtifactFilter filter = new ScopeArtifactFilter( scope );

        final Set projectDependencyArtifacts = project.getDependencyArtifacts();
        for ( Object artifactAsObject : projectDependencyArtifacts )
        {
            Artifact artifact = (Artifact) artifactAsObject;

            if (!filter.include( artifact ))
            {
                continue;
            }

            final boolean isWebRole = artifact.getType().equals(
                ArtifactType.MSDEPLOY_PACKAGE.getPackagingType()
            ) || artifact.getType().equals(
                ArtifactType.MSDEPLOY_PACKAGE.getExtension()
            );

            final boolean isWorkerRole = artifact.getType().equals(
                ArtifactType.DOTNET_APPLICATION.getPackagingType()
            ) || artifact.getType().equals(
                ArtifactType.DOTNET_APPLICATION.getExtension()
            );

            // if the artifact ID is a key, replace it, otherwise use the original
            String dependencyArtifactId = aliases.getProperty( artifact.getArtifactId(), artifact.getArtifactId() );

            if ( !isWebRole && !isWorkerRole )
            {
                throw new MojoExecutionException(
                    "NPANDAY-123-005: Artifact type " + artifact.getType() + " of artifact " + dependencyArtifactId
                        + " is not supported for azure cloud services.\n\nPlease use "
                        + ArtifactType.DOTNET_APPLICATION.getPackagingType() + " for worker roles, and "
                        + ArtifactType.MSDEPLOY_PACKAGE.getPackagingType() + " for web roles"
                );
            }

            final File roleRoot = new File(
                PathUtil.getPreparedPackageFolder( project ), artifact.getArtifactId()
            );

            if ( isWebRole )
            {
                getLog().debug( "NPANDAY-123-003: Found web role " + dependencyArtifactId );
            }
            else if ( isWorkerRole )
            {
                getLog().debug( "NPANDAY-123-004: Found worker role " + dependencyArtifactId );
            }

            if ( !roleRoot.exists() )
            {
                throw new MojoExecutionException(
                    "NPANDAY-123-006: Could not find worker/web role root for " + dependencyArtifactId + ": "
                        + roleRoot
                );
            }

            File entryPoint = null; 
            if ( isWebRole )
            {
                String name = "bin" + File.separator + dependencyArtifactId + ".dll";
                entryPoint = new File( roleRoot, name );
                if ( entryPoint.exists() )
                {
                    commands.add(
                        "/role:" + dependencyArtifactId + ";" + roleRoot.getAbsolutePath() + ";" + name
                    );
                }
                else
                {
                    getLog().warn( "NPANDAY-123-005: entry point '" + entryPoint + "' could not be found" );
                    commands.add(
                        "/role:" + dependencyArtifactId + ";" + roleRoot.getAbsolutePath()
                    );
                }

                // TODO: 'Web/' is hardcoded here; where to get it from?
                commands.add(
                    "/sitePhysicalDirectories:" + dependencyArtifactId + ";Web;" + canonical( roleRoot )
                );
            }
            else if ( isWorkerRole )
            {
                entryPoint = new File( roleRoot, dependencyArtifactId + ".dll" );
                if ( !entryPoint.exists() )
                {
                    throw new MojoExecutionException(
                        "NPANDAY-123-007: Could not find entry point dll for " + dependencyArtifactId + ": "
                            + entryPoint
                    );
                }

                commands.add(
                    "/role:" + dependencyArtifactId + ";" + roleRoot.getAbsolutePath() + ";"
                        + entryPoint.getName()
                );
            }

            // TODO: enable configuration of different framework pr. role; default to frameworkVersion
            Properties properties = new Properties();
            String v = frameworkVersion != null ? "v" + frameworkVersion : "v4.0";
            properties.setProperty( "TargetFrameworkVersion", v );
            if ( entryPoint != null && entryPoint.exists() )
            {
                properties.setProperty( "EntryPoint", entryPoint.getName() );
            }

            File rolePropertiesFile = new File(project.getBuild().getDirectory(), dependencyArtifactId + ".roleproperties");
            PrintWriter writer = null;
            try
            {
                writer = new PrintWriter( new FileWriter( rolePropertiesFile ) );
                // can't use properties.store(), it writes a comment that CSPACK doesn't understand
                for ( String property : properties.stringPropertyNames() )
                {
                    writer.println( property + "=" + properties.getProperty( property ) );
                }

                commands.add(
                    "/rolePropertiesFile:" + dependencyArtifactId + ";" + rolePropertiesFile.getAbsolutePath()
                );
            }
            catch ( java.io.IOException e )
            {
                throw new MojoFailureException(
                    "NPANDAY-123-008: Error while creating role properties file for " + dependencyArtifactId, e );
            }
            finally
            {
                IOUtil.close( writer );
            }
        }

        return commands;
    }

    private String canonical( File f )
    {
        String path = f.getAbsolutePath();

        // CSPack is very fussy - lowercase the drive letter and you get
        // sitesroot, uppercase and it is omitted (!)
        // See: http://social.msdn.microsoft.com/Forums/bs-Latn-BA/windowsazuredevelopment/thread/014ce124-5ca6-46ce-b1ea-3d677a092f65
        return path.substring( 0, 1 ).toLowerCase() + path.substring( 1 );
    }
}
