package npanday.plugin.msdeploy;

import npanday.ArtifactType;
import npanday.PathUtil;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class CreatePackageIterationItem
{
    private String classifier;

    private File packageFile;

    private File packageSource;

    public CreatePackageIterationItem( MavenProject project )
    {
        this.classifier = null;
        this.packageSource = PathUtil.getPreparedPackageFolder( project );
        this.packageFile = new File(
            project.getBuild().getDirectory(),
            project.getArtifactId() + "." + ArtifactType.MSDEPLOY_PACKAGE.getExtension()
        );
    }

    public String getClassifier()
    {
        return classifier;
    }

    public File getPackageFile()
    {
        return packageFile;
    }

    public File getPackageSource()
    {
        return packageSource;
    }

    @Override
    public String toString()
    {
        return "CreatePackageIterationItem{" + "classifier='" + classifier + '\'' + '}';
    }
}
