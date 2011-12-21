package npanday.plugin.msdeploy;

import npanday.PathUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class UnpackDependencyIterationItem
{
    private File packageSource;

    private File packageTarget;

    private Artifact artifact;

    public UnpackDependencyIterationItem( MavenProject project, Artifact artifact ) throws MojoFailureException
    {
        this.artifact = artifact;

        if (!artifact.isResolved()){
           throw new MojoFailureException( "NPANDAY-124-000: The artifact should already have been resolved: " + artifact);
        }

        // TODO: very, very temporary fix for NPANDAY-509


        packageSource = artifact.getFile();
        assert packageSource != null : "package source should not be null here";

        packageTarget = new File( PathUtil.getPreparedPackageFolder( project ), artifact.getArtifactId() );
    }

    public File getPackageSource()
    {
        return packageSource;
    }

    public File getPackageTarget()
    {
        return packageTarget;
    }

    @Override
    public String toString()
    {
        return "UnpackDependencyIterationItem{" + "packageSource=" + packageSource + ", packageTarget=" + packageTarget
            + ", artifact=" + artifact + '}';
    }
}
