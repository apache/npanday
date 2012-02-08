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

package npanday.plugin.application;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import npanday.packaging.DirectoryPackagePreparer;
import npanday.packaging.MixinAsssemblyReader;
import npanday.packaging.PackagePreparationConfigurationSource;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.filtering.MavenFileFilter;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.toArray;

/**
 * Maven Mojo preparing the packaging of .NET applications.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @phase prepare-package
 * @goal assemble-package-files
 * @since 1.5.0-incubating
 */
public class AssemblePackageFilesMojo
    extends AbstractMojo
{
    Splitter COMMANDLINE_LISTS_SPLITTER = Splitter.on( ";" ).omitEmptyStrings().trimResults();

    /**
     * Specifies if the goal should be skipped.
     *
     * @parameter expression="$(skipPackaging)" default-value="false"
     */
    private boolean skip;

    /**
     * Assembly XML Descriptor file. This must be the path to your customized descriptor file.
     * <br><b>assemblyDescriptor will take precedence over {@see assemblyDescriptorRef}</b>
     *
     * @parameter expression="${assemblyDescriptor}"
     */
    private String assemblyDescriptor;

    /**
     * Commandline version of {@see mixinAssemblyComponentDescriptors}. Spearate each item with a semicolon.
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
     * @parameter expression="${assemblyDescriptorRef}" default-value="application-defaults"
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
     * @component
     */
    private DirectoryPackagePreparer packagePreparer;


    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "NPANDAY-127-000: Application packaging was configured to be skipped" );
            return;
        }

        long startTime = System.currentTimeMillis();

        setupParameters();

        final PackagePreparationConfigurationSource packageConfig = new PackagePreparationConfigurationSource(
            mavenSession, fileFilter
        );

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
            throw new MojoFailureException(
                "NPANDAY-127-008: Please configure either assemblyDescriptorRef or assemblyDescriptor"
            );
        }

        assemblyReader.setComponentDescriptors(
            Objects.firstNonNull( mixinAssemblyComponentDescriptors, new String[0] )
        );

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
            mixinAssemblyComponentDescriptors = toArray(
                COMMANDLINE_LISTS_SPLITTER.split( mixinAssemblyComponentDescriptorList ), String.class
            );
        }
    }
}

