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

import npanday.PlatformUnsupportedException;
import npanday.nuget.NugetException;
import npanday.nuget.NugetInstallParameters;
import npanday.plugin.libraryimporter.model.NugetPackage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;

/**
 * Resolve configured Nuget packages and install them to a cache directory.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal resolve-packages
 */
public class ResolvePackagesMojo
    extends AbstractNugetMojo
{
    @Override
    protected void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        super.innerExecute();

        for ( NugetPackage nuget : getNugetImports() )
        {
            if ( nuget.getDirectory().exists() )
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "NPANDAY-138-004: package " + nuget + " is already installed." );
                }
                continue;
            }

            NugetInstallParameters params = new NugetInstallParameters( nuget.getName() );
            params.setVersion( nuget.getVersion().toString() );
            params.setOutputDirectory( packageCacheDirectory );

            // NOTE: since we only have explicit versions, it is ok, if they are pre-released
            params.setPrerelease( true );

            List<String> sources = nuget.getSources();
            if (sources != null){
                params.setSources( sources );
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "NPANDAY-138-005: package " + nuget + " has custom sources configured: " + sources );
                }
            }
            else {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "NPANDAY-138-006: package " + nuget + " will be retrieved from default sources" );
                }
            }

            try
            {
                nugetInvoker.install(
                    getExecutableRequirement(), params
                );
            }
            catch ( PlatformUnsupportedException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-138-002: An error occurred while trying to install package " + nuget, e
                );
            }
            catch ( NugetException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-138-003: An error occurred while trying to install package " + nuget, e
                );
            }
        }
    }
}

