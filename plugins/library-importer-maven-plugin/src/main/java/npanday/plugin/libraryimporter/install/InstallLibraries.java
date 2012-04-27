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

package npanday.plugin.libraryimporter.install;

import npanday.plugin.libraryimporter.model.NugetPackageLibrary;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Mojo for installing the resolved libraries to the local repository.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal install-libraries
 */
public class InstallLibraries
    extends AbstractInstallMojo
{
    @Override
    protected void handleGeneratedArtifacts( NugetPackageLibrary lib, Artifact artifact ) throws MojoExecutionException
    {
        ArtifactRepository repo = getLocalArtifactRepository();

        if (lib.getMarkerFileFor( artifact, repo ).exists()){
            if ( getLog().isDebugEnabled() )
            {
                getLog().debug(
                    "NPANDAY-146-002: artifact has yet been installed to local repository: " + artifact.getId()
                );
            }
            return;
        }

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug(
                "NPANDAY-146-001: installing artifact to local repository: " + artifact.getId()
            );
        }

        try
        {
            install( lib, artifact, repo );

            markDeployed( lib, artifact, repo);
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException( "NPANDAY-146-000: Error on installing artifact: " + artifact.getId(), e );
        }
        if ( createChecksums ) {
            installChecksums( artifact );
        }
    }
}
