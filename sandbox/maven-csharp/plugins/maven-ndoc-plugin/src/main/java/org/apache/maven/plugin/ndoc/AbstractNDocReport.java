package org.apache.maven.plugin.ndoc;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.csharp.helper.PackagingHelper;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class AbstractNDocReport
    extends AbstractMavenReport
{
    /**
     * Typically the assembly is automatically detected by looking for :
     * ${project.build.outputDirectory}/${project.build.finalName}.dll
     * and
     * ${project.build.outputDirectory}/${project.build.finalName}.exe
     * <p/>
     * If neither of these is appropriate, the user should provide a value.
     *
     * @parameter
     */
    private File assembly;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File buildOutputDirectory;

    /**
     * @parameter expression="${project.build.finalName}"
     * @required
     * @readonly
     */
    private String buildFinalName;

    /**
     * @parameter expression="${component.org.apache.maven.doxia.siterenderer.Renderer}"
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="MSDN"
     */
    private String documentor;

    /**
     * @parameter default-value="false"
     */
    private boolean verbose;

    /**
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * @parameter expression="${ndocCommand}"
     * @required
     */
    private File executable;

    protected abstract File getAssembly();

    protected abstract File getXmlDoc();

    protected abstract File getNamespaceSummaries();

    protected abstract File getWorkDirectory();

    protected abstract File getOutputDirectoryAsFile();

//    public boolean canGenerateReport()
//    {
//        //File xmlFile = new File( xmlPath );
//        //return xmlFile.exists();
//    }

    private File cachedMainAssembly;

    protected File getMainAssembly()
    {
        if ( this.cachedMainAssembly == null )
        {
            if ( this.assembly != null )
            {
                this.cachedMainAssembly = this.assembly;
            }

            File exeAssembly = new File( this.buildOutputDirectory, this.buildFinalName + ".exe" );
            if ( exeAssembly.exists() )
            {
                this.cachedMainAssembly = exeAssembly;
            }

            File libAssembly = new File( this.buildOutputDirectory, this.buildFinalName + ".dll" );
            this.cachedMainAssembly = libAssembly;
        }
        return this.cachedMainAssembly;
    }

    public boolean canGenerateReport()
    {
        return !this.getProject().getPackaging().equals( "pom" );
    }

    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected String getOutputDirectory()
    {
        return this.getOutputDirectoryAsFile().getAbsoluteFile().toString();
    }

    protected ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "ndoc-report", locale, this.getClass().getClassLoader() );
    }

    protected void validateConfiguration()
        throws MavenReportException
    {
        // valid assembly
        if ( !this.getAssembly().exists() || !this.getAssembly().isFile() )
        {
            throw new MavenReportException( "Invalid ndoc-maven-plugin configuration: invalid assembly file '" +
                this.getAssembly().getAbsolutePath() + "'." );
        }

        // valid xmldoc
        if ( !this.getXmlDoc().exists() || !this.getXmlDoc().isFile() )
        {
            throw new MavenReportException( "Invalid ndoc-maven-plugin configuration: invalid XML doc file '" +
                this.getXmlDoc().getAbsolutePath() + "'." );
        }

        // valid namespace summary file
        if ( this.getNamespaceSummaries() != null )
        {
            if ( !this.getNamespaceSummaries().exists() || !this.getNamespaceSummaries().isFile() )
            {
                throw new MavenReportException(
                    "Invalid ndoc-maven-plugin configuration: invalid namespace summaries file '" +
                        this.getNamespaceSummaries().getAbsolutePath() + "'." );
            }
        }
    }

    private String[] buildCommandArguments()
    {
        List args = new ArrayList();

        args.add( this.getAssembly().getAbsolutePath() + "," + this.getXmlDoc().getAbsolutePath() );

        if ( this.getNamespaceSummaries() != null )
        {
            args.add( "-namespacesummaries=" + this.getNamespaceSummaries().getAbsolutePath() );
        }

        args.add( "-documentor=" + documentor );

        if ( verbose )
        {
            args.add( "-verbose" );
        }

        if ( documentor.equals( "MSDN" ) )
        {
            args.add( "-OutputDirectory=" + this.getOutputDirectoryAsFile().getAbsolutePath() );
        }

        if ( this.getWorkDirectory().exists() )
        {

            args.add( "-referencepath=" + this.getWorkDirectory().getAbsolutePath() );
        }

        String[] ret = new String[args.size()];
        args.toArray( ret );

        return ret;
    }

    protected void copyDependencies( List artifacts )
        throws MavenReportException
    {

        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {

            Artifact a = (Artifact) i.next();

            if ( !a.getFile().exists() )
            {
                throw new MavenReportException( "Artifact file:" + a.getFile() + " does not exist, path provided:" +
                    a.getFile().getAbsolutePath() );
            }

            //TODO: Not sure if its ok to skip system dependencies.
            //TODO: Need to filter by scope better (only compile scope for main, and test scope for test, etc.
            //only copy non-syetem deps to directory (system deps you get for free.. :-)
            if ( !a.getFile().isDirectory() && ( !a.getScope().equals( Artifact.SCOPE_SYSTEM ) ) )
            {
                //if reference is a non csharp (could be java for an integration test, then ignore it.
                if ( PackagingHelper.isDotnetPackaging( a.getType() ) )
                {
                    try
                    {
                        FileUtils.copyFileToDirectory( a.getFile(), this.getWorkDirectory() );
                    }
                    catch ( IOException e )
                    {
                        throw new MavenReportException( "Encountered problems copying a dependency", e );
                    }
                }
            }
        }
    }

    public boolean isExternalReport()
    {
        return false;
    }

    protected void executeNDocCommand( Locale locale )
        throws MavenReportException
    {

        this.getOutputDirectoryAsFile().mkdirs();

        Commandline cli = new Commandline();
        cli.setWorkingDirectory( basedir.getAbsolutePath() );
        cli.setExecutable( executable.getAbsolutePath() );
        cli.addArguments( buildCommandArguments() );

        Writer stringWriter = new StringWriter();
        StreamConsumer out = new WriterStreamConsumer( stringWriter );
        StreamConsumer err = new WriterStreamConsumer( stringWriter );

        try
        {
            getLog().info( "About to make NDoc command line call:" + cli.toString() );

            int returnCode = CommandLineUtils.executeCommandLine( cli, out, err );

            getLog().info( "NDoc output:" );
            getLog().info( stringWriter.toString() );

            if ( returnCode != 0 )
            {
                throw new MavenReportException( "Failed to generate NDoc documentation." );
            }

        }
        catch ( CommandLineException ex )
        {
            throw new MavenReportException( "Error while executing NDoc.", ex );
        }
    }

    protected void writeRedirectToSink( Locale locale, String baseurl )
    {
        this.getLog().debug( "writting redirect to sink" );
        Sink sink = this.getSink();

        sink.text( getBundle( locale ).getString( "report.ndoc.linkintro" ) );

        sink.list();

        File htmlIndexFile = new File( this.getOutputDirectoryAsFile(), "/index.html" );
        this.getLog().debug( "htmlIndexFile:" + htmlIndexFile );
        if ( htmlIndexFile.exists() )
        {
            sink.listItem();
            sink.link( baseurl + "/index.html" );
            sink.text( getBundle( locale ).getString( "report.ndoc.htmllinkname" ) );
            //sink.text("Html based NDoc content");
            sink.link_();
            sink.listItem_();
        }

        File msHelpFile = new File( this.getOutputDirectoryAsFile(), "/Documentation.chm" );
        this.getLog().debug( "msHelpFile:" + msHelpFile );
        if ( msHelpFile.exists() )
        {
            sink.listItem();
            sink.link( baseurl + "/Documentation.chm" );
            sink.text( getBundle( locale ).getString( "report.ndoc.mshelplinkname" ) );
            //sink.text("MS Help based NDoc content");
            sink.link_();
            sink.listItem_();
        }

        sink.list_();
        this.getSink().close();
    }
}
