package npanday.plugin.azure;

import com.google.common.collect.Lists;
import npanday.ArtifactType;
import npanday.PathUtil;
import npanday.plugin.azure.AbstractCSPackDeployMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Packages roles specified in the dependencies into a common cloud service package (*.cspkg), accompanied
 * with a configuration template (*.cscfg).
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal create-cloud-service-package
 * @requiresDependencyResolution runtime
 */
public class PackageAzureCloudServiceMojo
    extends AbstractCSPackDeployMojo
{
    private File packageFile, templateConfigurationFile;

    /**
     * @parameter expression="${cspack.serviceDefinition}" default-value="${basedir}/ServiceDefinition.csdef"
     */
    private File serviceDefinitionFile;

    /**
     * @parameter expression="${cspack.generateConfigurationFile}" default-value="true"
     */
    private boolean generateConfigurationFile;

    @Override
    protected void afterCommandExecution() throws MojoExecutionException
    {
        attachPackageFile();

        if ( generateConfigurationFile )
        {
            attachConfigurationFile();
        }
    }

    private void attachPackageFile() throws MojoExecutionException
    {
        if ( !packageFile.exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-123-001: CSPack seemed to fail on creating the package " + packageFile.getAbsolutePath()
            );
        }

        project.getArtifact().setFile( packageFile );
    }

    private void attachConfigurationFile() throws MojoExecutionException
    {
        if ( !templateConfigurationFile.exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-123-002: CSPack seemed to fail on creating the template configuration file "
                    + packageFile.getAbsolutePath()
            );

        }

        projectHelper.attachArtifact(
            project, ArtifactType.AZURE_CLOUD_SERVICE_CONFIGURATION.getPackagingType(), "configtemplate",
            templateConfigurationFile
        );
    }

    @Override
    protected void beforeCommandExecution()
    {
        packageFile = new File(
            project.getBuild().getDirectory(),
            project.getArtifactId() + "." + ArtifactType.AZURE_CLOUD_SERVICE.getExtension()
        );

        templateConfigurationFile = new File(
            project.getBuild().getDirectory(), project.getArtifactId() + "-configtemplate" + "."
            + ArtifactType.AZURE_CLOUD_SERVICE_CONFIGURATION.getExtension()
        );
    }

    @Override
    protected List<String> getCommands() throws MojoExecutionException
    {
        List<String> commands = Lists.newArrayList();

        commands.add( serviceDefinitionFile.getAbsolutePath() );

        if ( generateConfigurationFile )
        {
            commands.add( "/generateConfigurationFile:\"" + templateConfigurationFile.getAbsolutePath() + "\"" );
        }

        commands.add( "/out:" + packageFile.getAbsolutePath() );

        final Set projectDependencyArtifacts = project.getDependencyArtifacts();
        for ( Object artifactAsObject : projectDependencyArtifacts )
        {
            Artifact artifact = (Artifact) artifactAsObject;
            if ( artifact.getType().equals( ArtifactType.MSDEPLOY_PACKAGE.getPackagingType() )
                || artifact.getType().equals( ArtifactType.MSDEPLOY_PACKAGE.getExtension() ) )
            {
                final File webRoot = new File(
                    PathUtil.getPreparedPackageFolder( project ), artifact.getArtifactId()
                );

                commands.add(
                    "/role:" + artifact.getArtifactId() + ";" + webRoot.getAbsolutePath()
                );

                // TODO: 'Web/' is hardcoded here; where to get it from?1 (310) 728-2143 x2813
                commands.add(
                    "/sitePhysicalDirectories:" + artifact.getArtifactId() + ";Web/;" + webRoot.getAbsolutePath()
                );
            }
        }

        return commands;
    }


}
