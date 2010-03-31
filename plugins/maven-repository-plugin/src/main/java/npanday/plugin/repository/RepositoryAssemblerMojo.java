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
package npanday.plugin.repository;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import npanday.artifact.AssemblyResolver;
import npanday.artifact.ArtifactContext;
import npanday.repository.RepositoryConverter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 * @author Shane Isbell
 * @goal package
 * @description Converts and assembles all artifacts within the RDF repository into the default Maven repository format.
 */
public class RepositoryAssemblerMojo
    extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * Pull in assemblies from the global assembly cache.
     *
     * @parameter expression="${withGac}"
     */
    private boolean withGac = false;

    /**
     * Sets location of assembled artifacts.
     *
     * @parameter expression="${outputDirectory}" default-value = "archive-tmp/repository/releases"
     */
    private String outputDirectory;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    /**
     * Component used to create a repository
     *
     * @component
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private RepositoryConverter repositoryConverter;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File dataDir = new File( localRepository.getParentFile(), "/uac/rdfRepository" );
        org.openrdf.repository.Repository rdfRepository = new SailRepository( new MemoryStore( dataDir ) );
        try
        {
            rdfRepository.initialize();
        }
        catch ( RepositoryException e )
        {
            throw new MojoExecutionException( "NPANDAY-1700-007: Message = " + e.getMessage() );
        }

        artifactContext.init( project, project.getRemoteArtifactRepositories(), localRepository );

        List<Dependency> netDependencies = new ArrayList<Dependency>();

        for ( Dependency dependency : (List<Dependency>) project.getDependencies() )
        {
            netDependencies.add( dependency );
        }

        assemblyRepository( netDependencies, new DefaultRepositoryLayout() );

        //try
        //{
            //repositoryConverter.convertRepositoryFormat( rdfRepository, localRepository );
        //}
        //catch ( IOException e )
        //{
            //throw new MojoExecutionException("NPANDAY-1700-008: Message = " +  e.getMessage(), e );
        //}
    }

    private void assemblyRepository( List<Dependency> dependencies, ArtifactRepositoryLayout layout )
        throws MojoExecutionException, MojoFailureException
    {
        if ( dependencies.size() == 0 )
        {
            return;
        }

        ArtifactRepository localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, layout );
        ArtifactRepository deploymentRepository = repositoryFactory.createDeploymentArtifactRepository( "npanday.deploy",
                                                                                                        "file://" +
                                                                                                            project.getBuild().getDirectory() + File.separator +
                                                                                                            outputDirectory,
                                                                                                        layout, true );

        try
        {
            assemblyResolver.resolveTransitivelyFor( project, dependencies, project.getRemoteArtifactRepositories(),
                                                     localRepository, true );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-1700-009: Message = " + e.getMessage() );
        }

        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            Set<Artifact> pomParentArtifacts = getPomArtifactsFor( artifact.getGroupId(), artifact.getArtifactId(),
                                                                   artifact.getVersion(), layout, true );
            Set<Artifact> pomArtifacts = getPomArtifactsFor( artifact.getGroupId(), artifact.getArtifactId(),
                                                             artifact.getVersion(), layout, false );
            if ( pomArtifacts.size() == 1 )
            {
                ArtifactMetadata metadata =
                    new ProjectArtifactMetadata( artifact, pomArtifacts.toArray( new Artifact[1] )[0].getFile() );
                artifact.addMetadata( metadata );
            }

            try
            {
                if ( withGac || !artifact.getType().startsWith( "gac" ) )
                {
                    artifactDeployer.deploy( artifact.getFile(), artifact, deploymentRepository,
                                             localArtifactRepository );
                }
                //Deploy parent poms
                for ( Artifact pomArtifact : pomParentArtifacts )
                {
                    artifactDeployer.deploy( pomArtifact.getFile(), pomArtifact, deploymentRepository,
                                             localArtifactRepository );
                }
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( "NPANDAY-1700-000: Deploy Failed", e );
            }
        }

        TarArchiver tarArchiver = new TarArchiver();
        try
        {
            tarArchiver.addDirectory(
                new File( project.getBuild().getDirectory(), File.separator + outputDirectory ) );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "NPANDAY-1700-001", e );
        }

        TarArchiver.TarCompressionMethod tarCompressionMethod = new TarArchiver.TarCompressionMethod();
        tarArchiver.setDestFile( new File( project.getBuild().getDirectory(), project.getArtifactId() + ".tar.gz" ) );
        try
        {
            tarCompressionMethod.setValue( "gzip" );
            tarArchiver.setCompression( tarCompressionMethod );
            tarArchiver.setIncludeEmptyDirs( false );
            tarArchiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "NPANDAY-1700-002", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-1700-003", e );
        }

    }

    private Set<Artifact> getPomArtifactsFor( String groupId, String artifactId, String version,
                                              ArtifactRepositoryLayout layout, boolean addParents )
        throws MojoExecutionException
    {
        Set<Artifact> pomArtifacts = new HashSet<Artifact>();
        Artifact pomArtifact = artifactFactory.createArtifact( groupId, artifactId, version, "pom", "pom" );
        File pomFile = new File( localRepository, layout.pathOf( pomArtifact ) );

        if ( pomFile.exists() )
        {
            File tmpFile = null;
            try
            {
                tmpFile = File.createTempFile( "pom" + artifactId, "pom" );
                FileUtils.copyFile( pomFile, tmpFile );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            pomArtifact.setFile( tmpFile );
            pomArtifacts.add( pomArtifact );
            FileReader fileReader;
            try
            {
                fileReader = new FileReader( tmpFile );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoExecutionException( "NPANDAY-1700-004: Unable to read pom" );
            }
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model;
            try
            {
                model = reader.read( fileReader );
            }
            catch ( XmlPullParserException e )
            {
                throw new MojoExecutionException( "NPANDAY-1700-005: Unable to read model" );

            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NPANDAY-1700-006: Unable to read model" );
            }

            Parent parent = model.getParent();

            if ( parent != null && addParents )
            {
                pomArtifacts.addAll( getPomArtifactsFor( parent.getGroupId(), parent.getArtifactId(),
                                                         parent.getVersion(), layout, true ) );
            }
        }

        return pomArtifacts;
    }
}
