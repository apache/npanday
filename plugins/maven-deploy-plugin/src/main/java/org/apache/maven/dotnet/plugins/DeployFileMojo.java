package org.apache.maven.dotnet.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.dotnet.artifact.AssemblyRepositoryLayout;

import java.io.File;

/**
 * @goal deploy-file
 * @phase deploy
 * @requiresProject false
 */
public class DeployFileMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression = "${file}
     * @required
     */
    private File artifactFile;

    /**
     * @parameter expression = "${pomFile}
     */
    private File pomFile;

    /**
     * @parameter expression = "${groupId}
     * @required
     */
    private String groupId;

    /**
     * @parameter expression = "${artifactId}
     * @required
     */
    private String artifactId;

    /**
     * @parameter expression = "${artifactVersion}
     * @required
     */
    private String version;

    /**
     * @parameter expression = "${repositoryId}
     */
    private String repositoryId;


    /**
     * @parameter expression = "${packaging}
     * @required
     */
    private String packaging;

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
     *
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, packaging, "dll" );
        ArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, pomFile );
        artifact.addMetadata( metadata );

        ArtifactRepository deploymentRepository =
            repositoryFactory.createDeploymentArtifactRepository( repositoryId, url, new AssemblyRepositoryLayout(), true );

        try
        {
            artifactDeployer.deploy( artifactFile, artifact, deploymentRepository, localRepository );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "NMAVEN-DEPLOY: Deploy Failed", e );
        }
    }
}
