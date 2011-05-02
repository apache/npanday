/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package npanday.plugin.fxcop;

import npanday.artifact.NPandayArtifactResolutionException;
import npanday.registry.NPandayRepositoryException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import npanday.artifact.AssemblyResolver;
import npanday.ArtifactType;
import npanday.executable.ExecutionException;
import npanday.PlatformUnsupportedException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Runs the FxCop Code Analysis Tool for the specified project's assembly and all of its dependencies.
 *
 * @author Shane Isbell
 * @goal aggregate
 * @aggregator false
 * @description Runs the FxCop Code Analysis Tool for the specified project's assembly and all of its dependencies
 */
public class FxCopAggregateMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

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
     * @parameter expression = "${profile}" default-value = "Microsoft:FxCop:FxCopCmd"
     */
    private String profile;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @parameter expression = "${project.build.directory}"
     */
    private File targetDirectory;

    /**
     * The artifact factory component, which is used for creating artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;


    public void execute()
        throws MojoExecutionException
    {
        Model model = project.getModel();
        List<Dependency> aggregateDependencies = new ArrayList<Dependency>();
        aggregateDependencies.addAll( model.getDependencies() );
        try
        {
            aggregateDependencies.addAll( addDependenciesFrom( project.getFile() ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException("NPANDAY-xxx-000: Unable to add dependencies from " + project.getFile(), e);
        }

        try
        {
            assemblyResolver.resolveTransitivelyFor( project, aggregateDependencies,
                                                     project.getRemoteArtifactRepositories(), localRepository,
                                                     true );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }
        catch( NPandayArtifactResolutionException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            if ( artifact.getFile() != null && !artifact.getGroupId().startsWith( "NUnit"))
            {
                try
                {
                    FileUtils.copyFileToDirectory( artifact.getFile(), new File( targetDirectory, "fxcop" ) );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "NPANDAY-xxx-002: Artifact = " + artifact.toString(), e );
                }
            }
        }

        try
        {
            netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion, profile, getCommands(),
                                                      null ).execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-003: Unable to execute: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-004: Platform Unsupported: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();

        String targetPath = "target" + File.separator + "fxcop";
        String outputPath = targetPath + File.separator + "Output.xml";

        commands.add( "/f:" + targetPath );
        commands.add( "/o:" + outputPath );
        return commands;
    }

    private Model fileToPom( File pomFile )
        throws IOException
    {
        FileReader fileReader = new FileReader( pomFile );
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        try
        {
            model = reader.read( fileReader );
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( "NPANDAY-xxx-005: Unable to read pom file" );
        }
        return model;
    }

    private List<Dependency> addDependenciesFrom( File pomFile )
        throws IOException
    {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        Model model = fileToPom( pomFile );
        if ( !model.getPackaging().equals( "pom" ) )
        {
            Dependency rootDependency = new Dependency();
            rootDependency.setArtifactId( model.getArtifactId() );
            rootDependency.setGroupId( model.getGroupId() );
            rootDependency.setVersion( model.getVersion() );
            rootDependency.setType(
                ArtifactType.getArtifactTypeForPackagingName( model.getPackaging() ).getPackagingType() );
            rootDependency.setScope( Artifact.SCOPE_COMPILE );
            dependencies.add( rootDependency );
        }

        List<String> modules = ( model != null ) ? model.getModules() : new ArrayList<String>();
        for ( String module : modules )
        {
            File childPomFile = new File( pomFile.getParent() + "/" + module + "/pom.xml" );
            if ( childPomFile.exists() )
            {
                dependencies.addAll( fileToPom( childPomFile ).getDependencies() );
                dependencies.addAll( addDependenciesFrom( childPomFile ) );
            }
        }
        return dependencies;
    }
}
