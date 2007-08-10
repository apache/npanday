package org.apache.maven.dotnet.plugins;

import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.dotnet.artifact.ArtifactContext;

import java.io.File;

/**
 * Deploy's dlls
 *
 * @author Evan Worley
 * @author Zak Jacobson
 * @goal deploy
 * @phase deploy
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
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepo;

    /**
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    /**
     * @parameter expression="${project.packaging}"
     * @required
     * @readonly
     */
    private String packaging;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Artifact artifact = project.getArtifact();

        if ( ! "pom".equals( packaging ) )
        {
            artifact.addMetadata( new ProjectArtifactMetadata( artifact, project.getFile() ) );
        }
        
        try
        {
            artifactDeployer.deploy( project.getArtifact().getFile(), artifact,
                                     project.getDistributionManagementArtifactRepository(), localRepo );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "NMAVEN-DEPLOY: Deploy Failed", e );
        }
    }
}
