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
package org.apache.maven.dotnet.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;

import java.io.File;
import java.util.HashMap;

/**
 * Deploys the .NET assembly file to the remote repository.
 *
 * @goal deploy-file
 * @phase deploy
 * @requiresProject false
 * @description Deploys the .NET assembly file to the remote repository
 */
public class DeployFileMojo
    extends AbstractMojo
{

    /**
     * The local Maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * The artifact file to deploy.
     *
     * @parameter expression = "${file}
     * @required
     */
    private File artifactFile;

    /**
     * The pom file of the artifact to deploy.
     *
     * @parameter expression = "${pomFile}
     */
    private File pomFile;

    /**
     * The groupId of the artifact to deploy.
     *
     * @parameter expression = "${groupId}
     * @required
     */
    private String groupId;

    /**
     * The artifact id of the artifact to deploy.
     *
     * @parameter expression = "${artifactId}
     * @required
     */
    private String artifactId;

    /**
     * The version of the artifact to deploy.
     *
     * @parameter expression = "${artifactVersion}
     * @required
     */
    private String version;

    /**
     * The id of the repository to deploy the artifact to.
     *
     * @parameter expression = "${repositoryId}
     */
    private String repositoryId;


    /**
     * The artifact packaging type.
     *
     * @parameter expression = "${packaging}
     * @required
     */
    private String packaging;

    /**
     * The project classifier type
     *
     * @parameter
     */
    private String classifier;

    /**
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    /**
     * Component used to create a repository
     *
     * @component
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * The URL of the repository to deploy the artifact to.
     *
     * @parameter expression="${url}"
     * @required
     */
    private String url;
    static HashMap<String, String> __packagingExtensions = new HashMap<String, String>();
    static
    {
        __packagingExtensions.put("exe", "exe");
        __packagingExtensions.put("winexe", "exe");
        __packagingExtensions.put("dll", "dll");
        __packagingExtensions.put("library", "dll");
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, packaging, __packagingExtensions.get(packaging) );
        ArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, pomFile );
        artifact.addMetadata( metadata );

        ArtifactRepository deploymentRepository = repositoryFactory.createDeploymentArtifactRepository( repositoryId,
                                                                                                        url,
                                                                                                        new DefaultRepositoryLayout(),
                                                                                                        true );

        try
        {
            artifactDeployer.deploy( artifactFile, artifact, deploymentRepository, localRepository );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "NPANDAY-DEPLOY: Deploy Failed", e );
        }
    }
}
