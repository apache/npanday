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

import npanday.plugin.libraryimporter.install.AbstractInstallMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Providing functionality for deploying artifacts.
 *
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class AbstractDeployMojo
    extends AbstractInstallMojo
{
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile( "(.+)::(.+)::(.+)" );

    /**
     * Specifies an alternative repository to which the project artifacts should be deployed ( other
     * than those specified in &lt;distributionManagement&gt; ).
     * <br/>
     * Format: id::layout::url
     *
     * @parameter expression="${libimport.deploymentRepository}"
     */
    private String deploymentRepository;

    /**
     * Set this to 'true' to bypass artifact deploy
     *
     * @parameter expression="${libimport.deploy.skip}" default-value="false"
     * @since 2.4
     */
    private boolean skip;

    /**
     * @component
     */
    private ArtifactDeployer deployer;

    /**
     * Map that contains the layouts.
     *
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    protected Map repositoryLayouts;

    /**
     * Flag whether Maven is currently in online/offline mode.
     *
     * @parameter default-value="${settings.offline}"
     * @readonly
     */
    private boolean offline;

    /**
     * Parameter used to control how many times a failed deployment will be retried before giving up and failing.
     * If a value outside the range 1-10 is specified it will be pulled to the nearest value within the range 1-10.
     *
     * @parameter expression="${libimport.deploy.retryCount}" default-value="1"
     */
    private int retryFailedDeploymentCount;

    void failIfOffline() throws MojoFailureException
    {
        if ( offline )
        {
            throw new MojoFailureException( "NPANDAY-147-001:Cannot deploy artifacts when Maven is in offline mode" );
        }
    }

    ArtifactRepositoryLayout getLayout( String id ) throws MojoExecutionException
    {
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts.get( id );

        if ( layout == null )
        {
            throw new MojoExecutionException( "NPANDAY-147-007: Invalid repository layout: " + id );
        }

        return layout;
    }

    /**
     * Deploy an artifact from a particular file.
     *
     * @param source   the file to deploy
     * @param artifact the artifact definition
     * @param deploymentRepo
     * @throws ArtifactDeploymentException if an error occurred deploying the artifact
     */
    protected void deploy(
        File source, Artifact artifact, ArtifactRepository deploymentRepo ) throws MojoExecutionException, MojoFailureException
    {
        int retryFailedDeploymentCount = Math.max( 1, Math.min( 10, this.retryFailedDeploymentCount ) );
        ArtifactDeploymentException exception = null;
        for ( int count = 0; count < retryFailedDeploymentCount; count++ )
        {
            try
            {
                if ( count > 0 )
                {
                    getLog().info(
                        "NPANDAY-147-001: Retrying deployment attempt " + ( count + 1 ) + " of "
                            + retryFailedDeploymentCount
                    );
                }
                deployer.deploy( source, artifact, deploymentRepo, getLocalArtifactRepository() );

                exception = null;
                break;
            }
            catch ( ArtifactDeploymentException e )
            {
                if ( count + 1 < retryFailedDeploymentCount )
                {
                    getLog().warn( "NPANDAY-147-002: Encountered issue during deployment: " + e.getLocalizedMessage() );
                    getLog().debug( e );
                }
                if ( exception == null )
                {
                    exception = e;
                }
            }
        }
        if ( exception != null )
        {
            throw new MojoExecutionException(
                "NPANDAY-147-003: Deployment for artifact ultimatively failed: " + artifact.getId(), exception
            );
        }
    }

    protected ArtifactRepository getDeploymentRepository() throws MojoExecutionException, MojoFailureException
    {
        ArtifactRepository repo = null;

        if ( deploymentRepository != null )
        {
            getLog().info( "NPANDAY-147-004: Using alternate deployment repository " + deploymentRepository );

            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher( deploymentRepository );

            if ( !matcher.matches() )
            {
                throw new MojoFailureException(
                    deploymentRepository, "NPANDAY-147-005:Invalid syntax for repository.",
                    "Invalid syntax for alternative repository. Use \"id::layout::url\"."
                );
            }
            else
            {
                String id = matcher.group( 1 ).trim();
                String layout = matcher.group( 2 ).trim();
                String url = matcher.group( 3 ).trim();

                ArtifactRepositoryLayout repoLayout = getLayout( layout );

                repo = artifactRepositoryFactory.createDeploymentArtifactRepository( id, url, repoLayout, true );
            }
        }

        if ( repo == null )
        {
            repo = project.getDistributionManagementArtifactRepository();
        }

        if ( repo == null )
        {
            String msg = "NPANDAY-147-006: Deployment failed: repository element was not specified in the POM inside"
                + " distributionManagement element or in -DaltDeploymentRepository=id::layout::url parameter";

            throw new MojoExecutionException( msg );
        }

        return repo;
    }

    @Override
    protected void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "NPANDAY-147-008: deploying imported libraries was explicitely skipped" );
            return;
        }

        failIfOffline();

        ArtifactRepository repo = getDeploymentRepository();

        String protocol = repo.getProtocol();

        if ( protocol.equalsIgnoreCase( "scp" ) )
        {
            File sshFile = new File( System.getProperty( "user.home" ), ".ssh" );

            if ( !sshFile.exists() )
            {
                sshFile.mkdirs();
            }
        }

        super.innerExecute();
    }
}