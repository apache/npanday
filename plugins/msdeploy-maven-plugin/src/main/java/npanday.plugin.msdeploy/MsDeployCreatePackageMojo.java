package npanday.plugin.msdeploy;

import com.google.common.collect.Lists;
import npanday.ArtifactType;
import npanday.PathUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal create-package
 */
public class MsDeployCreatePackageMojo
    extends AbstractMsDeployMojo<CreatePackageIterationItem>
{
    @Override
    protected void afterCommandExecution( CreatePackageIterationItem iterationItem ) throws MojoExecutionException
    {
        if ( !iterationItem.getPackageFile().exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-121-001: MSDeploy seemed to fail on creating the package " + iterationItem.getPackageFile().getAbsolutePath()
            );
        }

        projectHelper.attachArtifact( project, ArtifactType.MSDEPLOY_PACKAGE.getPackagingType(), iterationItem.getPackageFile() );
    }

    @Override
    protected void beforeCommandExecution( CreatePackageIterationItem iterationItem )
    {

    }

    @Override
    protected List<CreatePackageIterationItem> prepareIterationItems()
    {
        // TODO: NPANDAY-497 Support multiple packages with different classifiers
        return newArrayList(
            new CreatePackageIterationItem(project)
        );
    }

    @Override
    protected List<String> getCommands(CreatePackageIterationItem item) throws MojoExecutionException
    {
        List<String> commands = Lists.newArrayList();

        commands.add( "-verb:sync" );
        commands.add( "-source:contentPath=" + item.getPackageSource().getAbsolutePath() );
        commands.add( "-dest:package=" + item.getPackageFile().getAbsolutePath() );

        return commands;
    }
}
