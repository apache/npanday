package org.apache.maven.plugin.ndoc;

import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Goal which creates an NDoc report for the test sources.
 *
 * @author <a href="mailto:james.le.carpenter@jpmorgan.com">James Carpenter</a>
 * @goal test-report
 * @execute phase="test-compile"
 * @requiresDependencyResolution test
 */
public class NDocTestReportMojo
    extends AbstractNDocReport
{
    /**
     * @parameter expression="${project.build.testOutputDirectory}/unit-tests.dll"
     */
    private File testAssembly;

    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}/test-ndoc"
     * @required
     * @readonly
     */
    private File testOutputDirectory;

    //TODO: The below expression smells wrong.  The bug is in the AbstractCompilerMojo which fails to
    //use different values for the outputFileName based on whether the main or test source is being compiled.
    /**
     * @parameter expression="${project.build.testOutputDirectory}/${project.build.finalName}.xml"
     */
    private File testXmlDoc;

    /**
     * @parameter expression="${project.build.directory}/csharp-workarea/maven-ndoc-plugin/test-dependencies"
     */
    private File testWorkDirectory;

    /**
     * @parameter
     */
    private File testNamespaceSummaries;

    protected File getAssembly()
    {
        return this.testAssembly;
    }

    protected File getXmlDoc()
    {
        return this.testXmlDoc;
    }

    protected File getNamespaceSummaries()
    {
        return this.testNamespaceSummaries;
    }

    protected File getWorkDirectory()
    {
        return this.testWorkDirectory;
    }

    protected File getOutputDirectoryAsFile()
    {
        return this.testOutputDirectory;
    }

    public String getOutputName()
    {
        //return "test-ndoc/index";
        return "ndoc-test-report";
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.ndoc.test.name" );
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.ndoc.test.description" );
    }

    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        this.validateConfiguration();
        List testDependencyArtifacts = this.getProject().getTestArtifacts();
        this.copyDependencies( testDependencyArtifacts );

        //test code may require main src artifact
        try
        {
            File mainAssemblyFile = this.getMainAssembly();
            if ( mainAssemblyFile.exists() )
            {
                FileUtils.copyFileToDirectory( this.getMainAssembly(), this.getWorkDirectory() );
            }
            else
            {
                this.getLog().warn(
                    "The main assembly doesn't exist.  This may cause problems for the ndoc test-report" );
            }
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Encountered problems copying main assembly file", e );
        }

        this.executeNDocCommand( locale );
        this.writeRedirectToSink( locale, "test-ndoc" );
    }
}
