package org.apache.maven.dotnet.plugins;

import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Deploy's dlls
 *
 * @author Evan Worley
 * @author Zak Jacobson
 * @goal deploy-dotnet
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

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            artifactDeployer.deploy(  project.getArtifact().getFile(), project.getArtifact(),
                                     project.getDistributionManagementArtifactRepository(), localRepo );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "NMAVEN-DEPLOY: Deploy Failed", e );
        }
    }
}
