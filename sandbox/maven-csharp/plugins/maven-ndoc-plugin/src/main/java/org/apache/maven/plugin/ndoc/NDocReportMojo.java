package org.apache.maven.plugin.ndoc;

import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Goal which creates an NDoc report for the main sources.
 *
 * @author <a href="mailto:james.le.carpenter@jpmorgan.com">James Carpenter</a>
 * @goal report
 * @execute phase="process-classes"
 * @requiresDependencyResolution compile
 */
public class NDocReportMojo
    extends AbstractNDocReport
{
    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}/ndoc"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project.build.outputDirectory}/${project.build.finalName}.xml"
     */
    private File xmlDoc;

    /**
     * @parameter expression="${project.build.directory}/csharp-workarea/maven-ndoc-plugin/dependencies"
     */
    private File workDirectory;

    /**
     * @parameter
     */
    private File namespaceSummaries;

    protected File getAssembly()
    {
        return this.getMainAssembly();
    }

    protected File getXmlDoc()
    {
        return this.xmlDoc;
    }

    protected File getNamespaceSummaries()
    {
        return this.namespaceSummaries;
    }

    protected File getWorkDirectory()
    {
        return this.workDirectory;
    }

    protected File getOutputDirectoryAsFile()
    {
        return this.outputDirectory;
    }

    public String getOutputName()
    {
        //return "ndoc/index";
        return "ndoc-report";
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.ndoc.name" );
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.ndoc.description" );
    }


    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        this.validateConfiguration();
        List compileDependencyArtifacts = this.getProject().getCompileArtifacts();
        //List compileDependencyArtifacts = this.getProject().getCompileDependencies();
        this.copyDependencies( compileDependencyArtifacts );
        this.executeNDocCommand( locale );
        this.writeRedirectToSink( locale, "ndoc" );
    }
}
