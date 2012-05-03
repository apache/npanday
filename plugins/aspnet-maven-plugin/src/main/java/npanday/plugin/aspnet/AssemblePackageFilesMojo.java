package npanday.plugin.aspnet;

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

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import npanday.LocalRepositoryUtil;
import npanday.packaging.DirectoryPackagePreparer;
import npanday.packaging.MixinAsssemblyReader;
import npanday.packaging.PackagePreparationConfigurationSource;
import npanday.resolver.NPandayDependencyResolution;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFileFilter;

import java.io.File;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.toArray;

/**
 * Maven Mojo preparing the packaging of ASP.NET web applications.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @phase prepare-package
 * @goal assemble-package-files
 * @since 1.5.0-incubating
 */
public class AssemblePackageFilesMojo
    extends AbstractMojo
{
    Splitter COMMANDLINE_LISTS_SPLITTER = Splitter.on( "," ).omitEmptyStrings().trimResults();

    /**
     * Assembly XML Descriptor file. This must be the path to your customized descriptor file.
     * <br><b>assemblyDescriptor will take precedence over {@see assemblyDescriptorRef}</b>
     *
     * @parameter expression="${assemblyDescriptor}"
     */
    private String assemblyDescriptor;

    /**
     * Commandline version of {@see mixinAssemblyComponentDescriptors}. Spearate each item with a comma.
     *
     * @parameter expression="${mixinAssemblyComponentDescriptorList}"
     */
    private String mixinAssemblyComponentDescriptorList;

    /**
     * Component descriptors to be mixed into the main assembly descriptor from
     * {@see assemblyDescriptorRef} or {@see assemblyDescriptor}.
     * <br/>This is particularly useful, when the assembly descriptor is provided via the classpath.
     *
     * @parameter
     */
    private String[] mixinAssemblyComponentDescriptors;

    /**
     * A references to an assembly descriptor available on the plugin's classpath. The default
     * classpath includes these built-in descriptors. You can add others by adding dependencies to the plugin.
     * <br><b>assemblyDescriptorRef will be ignored, if {@see assemblyDescriptor} is configured</b>
     *
     * @parameter expression="${assemblyDescriptorRef}" default-value="aspnet-webapp-defaults"
     */
    private String assemblyDescriptorRef;

    /**
     * The Maven Session Object providing both the
     * current project and the local repository.
     *
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession mavenSession;

    /**
     * @component
     */
    private MavenFileFilter fileFilter;

    /**
     * @component
     */
    private MixinAsssemblyReader assemblyReader;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * @component
     */
    private NPandayDependencyResolution dependencyResolution;

    /**
     * The location of the local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    protected File localRepository;

    /**
     * @component
     */
    private DirectoryPackagePreparer packagePreparer;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        long startTime = System.currentTimeMillis();

        try
        {
            dependencyResolution.require( project, LocalRepositoryUtil.create( localRepository ), "runtime" );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-130-006: Could not resolve all runtime dependencies", e
            );
        }


        setupParameters();

        final PackagePreparationConfigurationSource packageConfig =
            new PackagePreparationConfigurationSource( mavenSession, fileFilter );

        if ( !isNullOrEmpty( assemblyDescriptor ) )
        {
            packageConfig.setDescriptorFile( assemblyDescriptor );
        }
        else if ( !isNullOrEmpty( assemblyDescriptorRef ) )
        {
            packageConfig.setDescriptorRef( assemblyDescriptorRef );
        }
        else
        {
            throw new MojoFailureException( "NPANDAY-109-008: Please configure either assemblyDescriptorRef or assemblyDescriptor" );
        }

        assemblyReader.setComponentDescriptors( Objects.firstNonNull( mixinAssemblyComponentDescriptors, new String[0] ));

        packagePreparer.preparePackage( packageConfig );

        long endTime = System.currentTimeMillis();
        getLog().debug( "Mojo Execution Time = " + ( endTime - startTime ) );
    }

    /**
     * Copies from commandline parameters to "real" parameters.
     */
    private void setupParameters()
    {
        if ( !isNullOrEmpty( mixinAssemblyComponentDescriptorList ) )
        {
            mixinAssemblyComponentDescriptors =
                toArray( COMMANDLINE_LISTS_SPLITTER.split( mixinAssemblyComponentDescriptorList ), String.class );
        }
    }
}

