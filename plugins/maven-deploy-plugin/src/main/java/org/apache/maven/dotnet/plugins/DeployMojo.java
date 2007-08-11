package org.apache.maven.dotnet.plugins;

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
import org.apache.maven.dotnet.artifact.ApplicationConfig;

import java.io.File;
import java.util.Set;
import java.util.HashSet;

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
                throw new MojoExecutionException( "NMAVEN-DEPLOY: Deploy Failed", e );
            }
        }

        try
        {
            artifactDeployer.deploy( project.getArtifact().getFile(), projectArtifact,
                                     project.getDistributionManagementArtifactRepository(), localRepo );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "NMAVEN-DEPLOY: Deploy Failed", e );
        }
    }
}
