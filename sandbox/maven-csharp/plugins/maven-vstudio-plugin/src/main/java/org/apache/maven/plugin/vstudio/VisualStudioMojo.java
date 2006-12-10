package org.apache.maven.plugin.vstudio;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.vstudio.xml.VisualStudio2003WebInfoWriter;
import org.apache.maven.plugin.vstudio.xml.VisualStudio2003Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Generates a Visual Studio Project
 * file from the pom.xml
 *
 * @requiresDependencyResolution test
 * @goal vstudio
 */
public class VisualStudioMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The source directories containing the sources to be compiled.
     *
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List compileSourceRoots;

    /**
     * The list of resources we want to transfer.
     *
     * @parameter expression="${project.resources}"
     * @required
     */
    private List resources;

    /**
     * This value is the path to the framework firectory
     * on your local machine. Please see the <a href="/getting-started.html">Getting Started</a>.
     * guide on how to do this.
     *
     * @parameter
     * @required
     */
    private String frameworkHome;

    /**
     * This is all the stuff that can be set up about a vs project. It is a big object.
     * Most of which is optional. See the java doc and your own project for details.
     *
     * @parameter
     */
    private ProjectSettings projectSettings;

    /**
     * A list of inclusion filters for the .csproj.
     * ex.
     * <p/>
     * **\/*.cs
     * **\/*.xslt
     * (ignore back slash)
     *
     * @parameter
     */
    private Set includes = new HashSet();

    /**
     * A list of exclusion filters for the .csproj.
     *
     * @parameter
     */
    private Set excludes = new HashSet();

    /**
     * A list of excluded dependencies for the .csproj.
     *
     * @parameter
     */
    private Set excludedDependencies = new HashSet();

    /**
     * A list of System references with paths to resolve for IDE.
     * These are required for the IDE's intellisense but are added
     * to the classpath (;o) at
     * a) compile through the addition of MS Core Lib
     * or
     * b) runtime through the bootstrapper function is MS dotnet or the mono
     * runtime exe for mono.
     *
     * @parameter
     */
    private List references = new ArrayList();

    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    protected ArtifactFactory factory;

    public void execute()
        throws MojoExecutionException
    {
        this.getLog().info( "framework.home=" + frameworkHome );

        //if its null use the defaults.
        if ( projectSettings == null )
        {
            projectSettings = new ProjectSettings();
        }

        projectSettings.setRootNamespace( project.getGroupId() );
        projectSettings.setAssemblyName( project.getBuild().getFinalName() );
        projectSettings.setOutputType( getOutputTypeFromType( project.getArtifact().getType() ) );
        projectSettings.setProjectType( getProjectTypeFromType( project.getArtifact().getType() ) );

        if ( project.getArtifact().getType().equals( "dotnet-web" ) && (
            projectSettings.getWebProjectUrlPath() == null ||
                StringUtils.isEmpty( projectSettings.getWebProjectUrlPath() ) ) )
        {
            throw new MojoExecutionException(
                "if you are creating a project of type dotnet-web you need to provide the parameter projectSettings/webProjectUrlPath" );
        }

        for ( Iterator i = project.getCompileSourceRoots().iterator(); i.hasNext(); )
        {
            this.getLog().info( "source root:" + i.next() );
        }

        for ( Iterator i = project.getTestCompileSourceRoots().iterator(); i.hasNext(); )
        {
            this.getLog().info( "source root:" + i.next() );
        }

        for ( Iterator i = project.getResources().iterator(); i.hasNext(); )
        {
            getLog().info( "res:" + i.next() );
        }

        VisualStudioFile[] mainsource = VisualStudioUtil.getSourceFiles( project.getBasedir(),
                                                                         project.getCompileSourceRoots(), includes,
                                                                         excludes );
        VisualStudioFile[] testsource = VisualStudioUtil.getSourceFiles( project.getBasedir(),
                                                                         project.getTestCompileSourceRoots(), includes,
                                                                         excludes );
        VisualStudioFile[] resources =
            VisualStudioUtil.getResourceFiles( project.getBasedir(), project.getResources() );
        VisualStudioFile pom = VisualStudioUtil.getPom( project );

        File projectFile = new File( project.getBasedir(), project.getArtifactId() + ".csproj" );

        try
        {

            this.getLog().info( "Creating project file [" + projectFile.getAbsolutePath() + "]" );

            projectFile.createNewFile();

            FileWriter filewriter = new FileWriter( projectFile );

            XMLWriter writer = new PrettyPrintXMLWriter( filewriter );

            File frameworkDir = new File( frameworkHome );

            if ( !frameworkDir.exists() || !frameworkDir.isDirectory() )
            {
                throw new MojoExecutionException( "\nframeworkHome is null or incorrect" + frameworkHome +
                    "\nframeworkHome needs to be set in your settings.xml and passed to plugin via pom.xml\n" +
                    getUsage() );
            }

            this.getLog().info( "Creating dependencies from references... " );

            Set referenceArtifacts = createReferenceArtifacts( references );

            this.getLog().info( "Created " + referenceArtifacts.size() + " dependencies." );

            Set projectArtifacts = this.getFilteredProjectArtifacts();

            new VisualStudio2003Writer( this.getLog() ).write( writer, projectSettings, mainsource, testsource,
                                                               resources, projectArtifacts, frameworkDir, pom,
                                                               referenceArtifacts );

            this.getLog().info( "Created project file [" + projectFile.getAbsolutePath() + "]" );

            filewriter.close();

            if ( project.getArtifact().getType().equals( "dotnet-web" ) )
            {
                createWebInfoFile( new File( project.getBasedir(), project.getArtifactId() + ".csproj.webinfo" ),
                                   projectSettings );
            }

        }
        catch ( IOException ioex )
        {
            throw new MojoExecutionException( ioex.getMessage(), ioex );
        }
    }

    private Set getFilteredProjectArtifacts()
    {
        Set filteredProjectArtifacts = new HashSet();
        for ( Iterator i = this.project.getArtifacts().iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();
            ExcludedDependency artifactAsExDep =
                new ExcludedDependency( artifact.getGroupId(), artifact.getArtifactId() );
            if ( !this.excludedDependencies.contains( artifactAsExDep ) )
            {
                filteredProjectArtifacts.add( artifact );
            }
            else
            {
                this.getLog().info( "Excluding artifact from vstudio project: " + artifact.toString() );
            }
        }
        return filteredProjectArtifacts;
    }

    private void createWebInfoFile( File webInfoFile, ProjectSettings settings )
        throws IOException
    {

        if ( webInfoFile.exists() )
        {
            FileUtils.copyFile( webInfoFile, new File( webInfoFile.getAbsoluteFile() + ".backup" ) );
            webInfoFile.delete();
        }

        webInfoFile.createNewFile();

        FileWriter filewriter = new FileWriter( webInfoFile );

        XMLWriter writer = new PrettyPrintXMLWriter( filewriter );

        new VisualStudio2003WebInfoWriter( this.getLog() ).write( writer, settings.getWebProjectUrlPath() );

        filewriter.close();
    }


    private static String getUsage()
    {

        String usage = "To configure this plugin you should add an entry to your local settings like so:\n" + "....\n" +
            "<profiles>\n" + "<profile>\n" + "<id>default</id>\n" + "<properties>\n" +
            "<dotnet.home>C:/WINDOWS/Microsoft.NET/Framework/v1.1.4322</dotnet.home>\n" + "</properties>\n" + "....\n" +
            "</profile>\n" + "</profiles>\n" + "<activeProfiles>\n" + "<activeProfile>default</activeProfile>\n" +
            "</activeProfiles>\n";

        usage = usage + "\n";

        usage = usage + "Add pass this into the plgin in this manner:\n";

        usage = usage + "<plugin>\n" + "<groupId>org.apache.maven.plugins</groupId>\n" +
            "<artifactId>maven-studio-plugin</artifactId>\n" + "<configuration>\n" +
            "<frameworkHome>${dotnet.home}</frameworkHome>\n" + "</configuration>\n" + "</plugin>\n";

        return usage;
    }

    private String getOutputTypeFromType( String type )
        throws MojoExecutionException
    {

        String outputType = "Library";

        if ( type.equals( "dotnet-library" ) )
        {
            outputType = "Library";
        }
        else if ( type.equals( "dotnet-exe" ) )
        {
            outputType = "Exe";
        }
        else if ( type.equals( "dotnet-winexe" ) )
        {
            outputType = "Exe";
        }
        else if ( type.equals( "dotnet-webapp" ) )
        {
            outputType = "Web";
        }
        else
        {
            throw new MojoExecutionException( "No Visual Studio output type defined for type:" + type );
        }

        return outputType;
    }

    private String getProjectTypeFromType( String type )
        throws MojoExecutionException
    {
        String outputType = "Local";
        if ( type.equals( "dotnet-webapp" ) )
        {
            outputType = "Web";
        }
        return outputType;
    }

    private Set createReferenceArtifacts( List references )
        throws MojoExecutionException
    {

        Set artifacts = new HashSet();

        for ( Iterator i = references.iterator(); i.hasNext(); )
        {
            Reference ref = (Reference) i.next();
            Artifact a = createArtifact( ref );
            if ( a != null )
            {
                artifacts.add( a );
            }
        }

        return artifacts;
    }

    private Artifact createArtifact( Reference ref )
        throws MojoExecutionException
    {

        File f;
        if ( ref.getAbsolute() )
        {
            f = new File( ref.getPath() );
        }
        else
        {
            f = new File( this.frameworkHome, ref.getPath() );
        }

        if ( ( !f.exists() ) || f.isDirectory() )
        {
            throw new MojoExecutionException( "File [" + f.getAbsolutePath() + "] doesn't appear to exist." );
        }

        String name = FilenameUtils.getBaseName( f.getName() );
        String packaging = getPackagingForExtension( FilenameUtils.getExtension( f.getName() ) );

        Artifact a = factory.createDependencyArtifact( "system", name, VersionRange.createFromVersion( "1.0" ),
                                                       packaging, null, Artifact.SCOPE_SYSTEM, null, false );

        a.setFile( f );

        return a;
    }


    private static String getPackagingForExtension( String extension )
    {

        String packaging = null;

        if ( extension.toLowerCase().equals( "dll" ) )
        {
            packaging = "dotnet-library";
        }
        else if ( extension.toLowerCase().equals( "exe" ) )
        {
            packaging = "dotnet-exe";
        }
        else if ( extension.toLowerCase().equals( "exe" ) )
        {
            packaging = "dotnet-winexe";
        }
        else if ( extension.toLowerCase().equals( "dll" ) )
        {
            packaging = "dotnet-module";
        }
        else if ( extension.toLowerCase().equals( "dll" ) )
        {
            packaging = "dotnet-webapp";
        }

        return packaging;
    }
}
