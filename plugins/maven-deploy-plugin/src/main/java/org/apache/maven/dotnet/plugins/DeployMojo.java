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
package npanday.plugins;

import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import npanday.artifact.ApplicationConfig;

import java.io.File;

/**
 * Deploys the .NET assembly to the remote repository
 *
 * @author Evan Worley
 * @author Zak Jacobson
 * @goal deploy
 * @phase deploy
 * @description Deploys the .NET assembly to the remote repository
 */
public class DeployMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The local maven repository
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepo;

    /**
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    /**
     * The project packaging type
     *
     * @parameter expression="${project.packaging}"
     * @required
     * @readonly
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
    private ArtifactFactory artifactFactory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Artifact projectArtifact = project.getArtifact();

        if ( ! "pom".equals( packaging ) )
        {
            projectArtifact.addMetadata( new ProjectArtifactMetadata( projectArtifact, project.getFile() ) );
        }

        ApplicationConfig config = ApplicationConfig.Factory.createDefaultApplicationConfig( project.getArtifact(),
                                                                                             project.getBasedir(),
                                                                                             new File(
                                                                                                 project.getBuild().getDirectory() ) );
        File exePath = config.getRepositoryPath( new File( localRepo.getBasedir() ) );
        if ( exePath.exists() )
        {
            Artifact attachedArtifact = artifactFactory.createArtifact( projectArtifact.getGroupId(),
                                                                        projectArtifact.getArtifactId(),
                                                                        project.getVersion(), packaging, "exe.config" );
            try
            {
                artifactDeployer.deploy( exePath, attachedArtifact,
                                         project.getDistributionManagementArtifactRepository(), localRepo );
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( "NPANDAY-DEPLOY: Deploy Failed", e );
            }
        }

        if ( classifier != null )
        {
            Artifact attachedArtifact = artifactFactory.createArtifactWithClassifier( projectArtifact.getGroupId(),
                                                                        projectArtifact.getArtifactId(),
                                                                        project.getVersion(), packaging, classifier );
            try
            {
                artifactDeployer.deploy( project.getArtifact().getFile(), attachedArtifact,
                                         project.getDistributionManagementArtifactRepository(), localRepo );
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( "NPANDAY-DEPLOY: Deploy Failed", e );
            }
        }

        try
        {
            artifactDeployer.deploy( project.getArtifact().getFile(), projectArtifact,
                                     project.getDistributionManagementArtifactRepository(), localRepo );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "NPANDAY-DEPLOY: Deploy Failed", e );
        }
    }
}
