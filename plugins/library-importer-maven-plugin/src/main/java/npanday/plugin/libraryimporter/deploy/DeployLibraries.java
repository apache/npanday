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

package npanday.plugin.libraryimporter.deploy;

import npanday.plugin.libraryimporter.model.NugetPackageLibrary;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo for deploying the resolved libraries to the local repository.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal deploy-libraries
 */
public class DeployLibraries
    extends AbstractDeployMojo
{
    @Override
    protected void handleGeneratedArtifacts( NugetPackageLibrary lib, Artifact artifact ) throws MojoExecutionException, MojoFailureException
    {
        ArtifactRepository repo = getDeploymentRepository();
        if (lib.getMarkerFileFor( artifact, repo ).exists()){
            if ( getLog().isDebugEnabled() )
            {
                getLog().debug(
                    "NPANDAY-152-002: artifact " + artifact.getId() + " has yet been deployed to " + repo.getUrl()
                );
            }
            return;
        }

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug(
                "NPANDAY-152-001: deploying artifact " + artifact.getId() + " to " + repo.getUrl()
            );
        }


        deploy( artifact.getFile(), artifact, repo );

        markDeployed( lib, artifact, repo );
    }

}
