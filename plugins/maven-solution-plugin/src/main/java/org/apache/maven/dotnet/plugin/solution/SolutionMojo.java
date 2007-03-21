package org.apache.maven.dotnet.plugin.solution;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Generates resources
 *
 * @author Shane Isbell
 * @goal solution
 * @phase package
 */
public class SolutionMojo extends AbstractMojo
{
    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private String localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    public void execute() throws MojoExecutionException
    {
        String profile = System.getProperty( "pomProfile");
        String pomFile = System.getProperty( "pomFile");
        
        String basedir = System.getProperty( "user.dir");
        //String basedir1 = "C:\\Documents and Settings\\shane\\nmaven-apache\\SI_IDE\\assemblies";
        List<String> commands = new ArrayList<String>();
        commands.add("pomFile=" + new File(basedir).getAbsolutePath() + File.separator + "pom.xml");
        if(profile != null) commands.add("pomProfile=" + profile);
        try
        {
            VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
            vendorInfo.setVendor( VendorFactory.createVendorFromName( "MICROSOFT" ) );
            netExecutableFactory.getNetExecutableFromRepository( "NMaven.Plugin", "NMaven.Plugin.Solution", vendorInfo,
                                                                 project, localRepository, commands ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "", e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }
}
