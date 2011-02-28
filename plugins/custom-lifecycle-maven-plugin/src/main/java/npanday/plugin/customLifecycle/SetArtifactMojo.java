package npanday.plugin.customLifecycle;

import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * If an artifact with final name is found, it is added.
 *
 * @author Lars Corneliussen
 * @goal set-artifact
 * @phase package
 */
public class SetArtifactMojo
        extends AbstractMojo
{

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /** @parameter default-value="false" */
    protected boolean skip;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip)  {
            return;
        }

        if (project.getArtifact().getFile() != null) {
            getLog().debug("NPANDAY-105-004: Execution skipped, because the artifact file is already set: "
                    + project.getArtifact().getFile().getAbsolutePath() );
            return;
        }

        String outputDirectory = project.getBuild().getDirectory();
        String finalName = project.getBuild().getFinalName();
        ArtifactType type = ArtifactType.getArtifactTypeForPackagingName(project.getPackaging());

        if (type == ArtifactType.NULL) {
            throw new MojoFailureException("NPANDAY-105-000: Packaging " + project.getPackaging() + " is not supported by this Mojo!");
        }

        File artifact = new File(outputDirectory, finalName + "." + type.getExtension());
        if (!artifact.exists()) {
            getLog().warn("NPANDAY-105-001: Could not set expected artifact to '" + artifact.getAbsolutePath() + "', because it does not exist");
            return;
        }

        getLog().debug("NPANDAY-105-003: Set the artifact file to '" + artifact.getAbsolutePath() + "'.");
        project.getArtifact().setFile(artifact);
    }
}
