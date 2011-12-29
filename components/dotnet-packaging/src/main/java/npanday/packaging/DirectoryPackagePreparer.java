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

package npanday.packaging;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.io.AssemblyReadException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.List;

/**
 * Utilizes the Maven Assembly Plugin infrastructure for creating
 * package folders that can later be used by packaging-components in order to package
 * the project's contents.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @plexus.component role="npanday.packaging.DirectoryPackagePreparer"
 */
// TODO: split impl and interface?
public class DirectoryPackagePreparer
    extends AbstractLogEnabled
{
    /**
     * @plexus.requirement
     */
    private AssemblyArchiver assemblyArchiver;

    /**
     * @plexus.requirement
     */
    private MixinAsssemblyReader assemblyReader;


    public void preparePackage( AssemblerConfigurationSource configurationSource )
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

    private void createDirectory( final Assembly assembly, AssemblerConfigurationSource configurationSource ) throws
        MojoExecutionException,
        MojoFailureException
    {

        final AssemblyArchiver archiver = assemblyArchiver;

        String fullName = configurationSource.getFinalName();

        try
        {
            archiver.createArchive( assembly, fullName, "dir", configurationSource );
            getLogger().info(
                "NPANDAY-109-009: Prepared package " + fullName + " using assembly " + assembly.getId() + " in '"
                    + configurationSource.getOutputDirectory() + "'."
            );
        }
        catch ( final ArchiveCreationException e )
        {
            throw new MojoExecutionException( "NPANDAY-109-005: Error creating package: " + e.getMessage(), e );
        }
        catch ( final AssemblyFormattingException e )
        {
            throw new MojoExecutionException( "NPANDAY-109-006: Error creating package: " + e.getMessage(), e );
        }
        catch ( final InvalidAssemblerConfigurationException e )
        {
            throw new MojoFailureException(
                assembly, "NPANDAY-109-007: Assembly for package is incorrectly configured: " + assembly.getId
                (),
                "Assembly: " + assembly.getId() + " is not configured correctly: " + e.getMessage()
            );
        }
    }
}
