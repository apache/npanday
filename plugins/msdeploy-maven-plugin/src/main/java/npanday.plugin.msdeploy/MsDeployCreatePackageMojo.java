package npanday.plugin.msdeploy;

import com.google.common.collect.Lists;
import npanday.ArtifactType;
import npanday.PathUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal create-package
 */
public class MsDeployCreatePackageMojo
    extends AbstractMsDeployMojo
{
    private File packageFile;

    @Override
    protected void afterCommandExecution() throws MojoExecutionException
    {
        if ( !packageFile.exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-121-001: MSDeploy seemed to fail on creating the package " + packageFile.getAbsolutePath()
            );
        }

        projectHelper.attachArtifact( project, ArtifactType.MSDEPLOY_PACKAGE.getPackagingType(), packageFile );
    }

    @Override
    protected void beforeCommandExecution()
    {
        packageFile = new File(
            project.getBuild().getDirectory(),
            project.getArtifactId() + "." + ArtifactType.MSDEPLOY_PACKAGE.getExtension()
        );
    }

    @Override
    List<String> getCommands() throws MojoExecutionException
    {
        List<String> commands = Lists.newArrayList();

        File packageSource = PathUtil.getPreparedPackageFolder( project );

        commands.add( "-verb:sync" );
        commands.add( "-source:contentPath=" + packageSource.getAbsolutePath() );
        commands.add( "-dest:package=" + packageFile.getAbsolutePath() );

        return commands;
    }
}
