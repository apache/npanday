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

package npanday.plugin.libraryimporter.resolve;

import com.google.common.collect.Lists;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutable;
import npanday.plugin.libraryimporter.model.NugetPackage;
import npanday.plugin.libraryimporter.skeletons.AbstractHandleEachImportMojo;
import npanday.plugin.libraryimporter.skeletons.AbstractLibraryImportsProvidingMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * Generate manifest-info for each.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal generate-package-manifestinfos
 */
public class GeneratePackageManifestInfosMojo
    extends AbstractHandleEachImportMojo
{
    /**
     * The executable identifier used to locate the right configurations from executable-plugins.xml. Can't be changed.
     */
    private String executableIdentifier = "MANIFESTINFO";

    /**
     * The configured executable version, from executable-plugins.xml, to be used. Should align to a installed
     * Azure SDK version.
     *
     * @parameter expression="${nuget.version}" default-value="1.0"
     */
    private String executableVersion;

    /**
     * The configured executable profile, from executable-plugins.xml, to be used.
     *
     * @parameter expression="${nuget.profile}"
     */
    private String executableProfile;

    protected ExecutableRequirement getExecutableRequirement()
    {
        // TODO: profile is actually an identifier; the real profile has yet to be supported
        return new ExecutableRequirement( getVendorRequirement(), executableIdentifier, executableVersion );
    }

    @Override
    protected void handleNugetPackage( NugetPackage nuget ) throws MojoExecutionException, MojoFailureException
    {
        for ( File libDir : nuget.getLibraryDirectories() )
        {
            File manifestInfoFile = new File( libDir, "manifestinfo.xml" );

            if ( manifestInfoFile.exists() )
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug(
                        "NPANDAY-140-002: skipping; manifest info does already exist for " + libDir
                    );
                }
                continue;
            }

            if ( getLog().isDebugEnabled() )
            {
                getLog().debug(
                    "NPANDAY-140-002: running manifestinfo for " + libDir
                );
            }

            final NetExecutable executable;
            try
            {

                executable = netExecutableFactory.getExecutable(
                    getExecutableRequirement(), Lists.newArrayList(
                    libDir.getAbsolutePath(), "-x", "-r", "-q", "-o", manifestInfoFile.getAbsolutePath()
                ), null
                );

                executable.execute();
            }
            catch ( ExecutionException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-140-000: Error occured when running manifestinfo for " + libDir, e
                );
            }
            catch ( PlatformUnsupportedException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-140-001: Error occured when running manifestinfo for " + libDir, e
                );
            }
        }
    }
}
