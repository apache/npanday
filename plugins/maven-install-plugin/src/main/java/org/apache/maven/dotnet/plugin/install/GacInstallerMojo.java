package org.apache.maven.dotnet.plugin.install;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.NetExecutable;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;

import java.util.List;
import java.util.ArrayList;

/**
 * @goal gac-install
 * @phase install
 */
public class GacInstallerMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The Vendor for the executable.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "GACUTIL"
     */
    private String profile;

    /**
     * Install into the GAC?
     *
     * @parameter expression="${isGacInstall}" default-value = "false"
     */
    private boolean isGacInstall;

    public void execute()
        throws MojoExecutionException
    {
        if ( !isGacInstall )
        {
            getLog().info( "NMAVEN-xxx-004: Skipping GAC Install.");
            return;
        }

        try
        {
            NetExecutable netExecutable = netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion, profile,
                                                                                    getCommands(), null );
            netExecutable.execute();
            getLog().info( "NMAVEN-xxx-003: Installed Assembly into GAC: Assembly = " +
                project.getArtifact().getFile() + ",  Vendor = " + netExecutable.getVendor().getVendorName() );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-1400-000: Unable to execute gacutil: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-1400-001: Platform Unsupported: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        Artifact artifact = project.getArtifact();
        commands.add( "/i");
        commands.add(artifact.getFile().getAbsolutePath() );
        return commands;
    }

}
