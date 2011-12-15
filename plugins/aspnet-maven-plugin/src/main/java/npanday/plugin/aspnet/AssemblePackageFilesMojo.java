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
import com.google.common.collect.Iterables;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.io.AssemblyReadException;
import org.apache.maven.plugin.assembly.io.AssemblyReader;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.shared.filtering.MavenFileFilter;

import java.util.List;

import static com.google.common.base.Strings.*;
import static com.google.common.collect.Iterables.toArray;

/**
 * Maven Mojo preparing the packaging of ASP.NET web applications.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal assemble-package-files
 * @description Maven Mojo for preparing the packaging of ASP.NET web applications
 * @since 1.5.0-incubating
 */
public class AssemblePackageFilesMojo
    extends AbstractMojo
{
    Splitter COMMANDLINE_LISTS_SPLITTER = Splitter.on( "," ).omitEmptyStrings().trimResults();

    /**
     * @parameter expression="${assemblyDescriptor}"
     * @description Assembly XML Descriptor file. This must be the path to your customized descriptor file.
     * <br><b>assemblyDescriptor will take precedence over {@see assemblyDescriptorRef}</b>
     */
    private String assemblyDescriptor;

    /**
     * @parameter expression="${mixinAssemblyComponentDescriptorList}"
     * @description Commandline version of {@see mixinAssemblyComponentDescriptors}. Spearate each item with a comma.
     */
    private String mixinAssemblyComponentDescriptorList;

    /**
     * @parameter
     * @description Component descriptors to be mixed into the main assembly descriptor from
     * {@see assemblyDescriptorRef} or {@see assemblyDescriptor}.
     * <br/>This is particularly useful, when the assembly descriptor is provided via the classpath.
     */
    private String[] mixinAssemblyComponentDescriptors;

    /**
     * @parameter expression="${assemblyDescriptorRef}" default-value="aspnet-webapp-defaults"
     * @description A references to an assembly descriptor available on the plugin's classpath. The default
     * classpath includes these built-in descriptors. You can add others by adding dependencies to the plugin.
     * <br><b>assemblyDescriptorRef will be ignored, if {@see assemblyDescriptor} is configured</b>
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
    private MixinAsssemblyReader assemblyReader;

    /**
     * @component
     */
    private AssemblyArchiver assemblyArchiver;

    /**
     * @component
     */
    private MavenFileFilter fileFilter;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        long startTime = System.currentTimeMillis();

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

        createPackage( packageConfig );

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

    public void createPackage( AssemblerConfigurationSource configurationSource )
        throws MojoExecutionException, MojoFailureException
    {
        List<Assembly> assemblies;
        try
        {
            assemblies = assemblyReader.readAssemblies( configurationSource );
        }
        catch ( final AssemblyReadException e )
        {
            throw new MojoExecutionException( "NPANDAY-109-001: Error reading assembly descriptors: " + e.getMessage(),
                                              e );
        }
        catch ( final InvalidAssemblerConfigurationException e )
        {
            throw new MojoFailureException( assemblyReader, e.getMessage(),
                                            "NPANDAY-109-002: Mojo configuration is invalid: " + e.getMessage() );
        }

        if ( assemblies.size() == 0 )
        {
            throw new MojoExecutionException(
                "NPANDAY-109-003: Somehow the assembly reader couldn't find the configured assembly descriptor" );
        }

        if ( assemblies.size() > 1 )
        {
            throw new MojoExecutionException(
                "NPANDAY-109-004: Somehow the assembly reader provided multiple assembly descriptors, which is not supported by this mojo." );
        }

        Assembly singleAssembly = assemblies.get( 0 );
        createDirectory( singleAssembly, configurationSource );
    }

    private void createDirectory( final Assembly assembly, AssemblerConfigurationSource configurationSource )
        throws MojoExecutionException, MojoFailureException
    {
        final AssemblyArchiver archiver = assemblyArchiver;

        String fullName = configurationSource.getFinalName();

        try
        {
            archiver.createArchive( assembly, fullName, "dir", configurationSource );
            getLog().info(
                "NPANDAY-109-009: Prepared package " + fullName + " using assembly " + assembly.getId() + " in '"
                    + configurationSource.getOutputDirectory() + "'." );
        }
        catch ( final ArchiveCreationException e )
        {
            throw new MojoExecutionException( "NPANDAY-109-005: Error creating aspnet package: " + e.getMessage(), e );
        }
        catch ( final AssemblyFormattingException e )
        {
            throw new MojoExecutionException( "NPANDAY-109-006: Error creating aspnet package: " + e.getMessage(), e );
        }
        catch ( final InvalidAssemblerConfigurationException e )
        {
            throw new MojoFailureException( assembly,
                                            "NPANDAY-109-007: Assembly for aspnet package is incorrectly configured: "
                                                + assembly.getId(),
                                            "Assembly: " + assembly.getId() + " is not configured correctly: "
                                                + e.getMessage() );
        }
    }
}