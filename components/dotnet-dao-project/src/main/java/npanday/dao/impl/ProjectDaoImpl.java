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
package npanday.dao.impl;

import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import npanday.dao.ProjectDao;
import npanday.dao.ProjectUri;
import npanday.dao.ProjectFactory;
import npanday.dao.Project;
import npanday.dao.ProjectDependency;
import npanday.dao.Requirement;
import npanday.registry.RepositoryRegistry;
import npanday.PathUtil;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.Model;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.Repository;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Binding;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.FileUtils;

import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.EOFException;
import java.lang.ExceptionInInitializerError;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ProjectDaoImpl
    implements ProjectDao
{

    private String className;

    private String id;

    private static Logger logger = Logger.getAnonymousLogger();

    private org.openrdf.repository.Repository rdfRepository;

    /**
     * The artifact factory component, which is used for creating artifacts.
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    private int getProjectForCounter = 0;

    private int storeCounter = 0;

    private RepositoryConnection repositoryConnection;

    private String dependencyQuery;

    private String projectQuery;

    private ArtifactResolver artifactResolver;

    public void init( ArtifactFactory artifactFactory, ArtifactResolver artifactResolver )
    {
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;

        List<ProjectUri> projectUris = new ArrayList<ProjectUri>();
        projectUris.add( ProjectUri.GROUP_ID );
        projectUris.add( ProjectUri.ARTIFACT_ID );
        projectUris.add( ProjectUri.VERSION );
        projectUris.add( ProjectUri.ARTIFACT_TYPE );
        projectUris.add( ProjectUri.IS_RESOLVED );
        projectUris.add( ProjectUri.DEPENDENCY );
        projectUris.add( ProjectUri.CLASSIFIER );

        dependencyQuery = "SELECT * FROM " + this.constructQueryFragmentFor( "{x}", projectUris );

        projectUris = new ArrayList<ProjectUri>();
        projectUris.add( ProjectUri.GROUP_ID );
        projectUris.add( ProjectUri.ARTIFACT_ID );
        projectUris.add( ProjectUri.VERSION );
        projectUris.add( ProjectUri.ARTIFACT_TYPE );
        projectUris.add( ProjectUri.IS_RESOLVED );
        projectUris.add( ProjectUri.DEPENDENCY );
        projectUris.add( ProjectUri.CLASSIFIER );
        projectUris.add( ProjectUri.PARENT );

        projectQuery = "SELECT * FROM " + this.constructQueryFragmentFor( "{x}", projectUris );
    }

    public Set<Project> getAllProjects()
        throws IOException
    {
        Set<Project> projects = new HashSet<Project>();
        TupleQueryResult result = null;
        try
        {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery( QueryLanguage.SERQL, projectQuery );
            result = tupleQuery.evaluate();
            while ( result.hasNext() )
            {
                BindingSet set = result.next();

                String groupId = set.getBinding( ProjectUri.GROUP_ID.getObjectBinding() ).getValue().toString();
                String version = set.getBinding( ProjectUri.VERSION.getObjectBinding() ).getValue().toString();
                String artifactId = set.getBinding( ProjectUri.ARTIFACT_ID.getObjectBinding() ).getValue().toString();
                String artifactType =
                    set.getBinding( ProjectUri.ARTIFACT_TYPE.getObjectBinding() ).getValue().toString();
                String classifier = null;
                if ( set.hasBinding( ProjectUri.CLASSIFIER.getObjectBinding() ) )
                {
                    classifier = set.getBinding( ProjectUri.CLASSIFIER.getObjectBinding() ).getValue().toString();
                }

                // Project project = getProjectFor( groupId, artifactId, version, artifactType, null );
                /*
                 * for ( Iterator<Binding> i = set.iterator(); i.hasNext(); ) { Binding b = i.next();
                 * System.out.println( b.getName() + ":" + b.getValue() ); }
                 */
                projects.add( getProjectFor( groupId, artifactId, version, artifactType, classifier ) );
            }
        }
        catch ( RepositoryException e )
        {
            throw new IOException( "NPANDAY-180-000: Message = " + e.getMessage() );
        }
        catch ( MalformedQueryException e )
        {
            throw new IOException( "NPANDAY-180-001: Message = " + e.getMessage() );
        }
        catch ( QueryEvaluationException e )
        {
            throw new IOException( "NPANDAY-180-002: Message = " + e.getMessage() );
        }
        finally
        {
            if ( result != null )
            {
                try
                {
                    result.close();
                }
                catch ( QueryEvaluationException e )
                {

                }
            }
        }

        return projects;
    }

    public void setRdfRepository( Repository repository )
    {
        this.rdfRepository = repository;
    }

    public boolean openConnection()
    {
        try
        {
            repositoryConnection = rdfRepository.getConnection();
            repositoryConnection.setAutoCommit( false );
        }
        catch ( RepositoryException e )
        {
            return false;
        }

        return true;
    }

    public boolean closeConnection()
    {

        if ( repositoryConnection != null )
        {
            try
            {
                repositoryConnection.commit();
                repositoryConnection.close();
            }
            catch ( RepositoryException e )
            {
                return false;
            }
        }

        return true;
    }

    public void removeProjectFor( String groupId, String artifactId, String version, String artifactType )
        throws IOException
    {
        ValueFactory valueFactory = rdfRepository.getValueFactory();
        URI id = valueFactory.createURI( groupId + ":" + artifactId + ":" + version + ":" + artifactType );
        try
        {
            repositoryConnection.remove( repositoryConnection.getStatements( id, null, null, true ) );
        }
        catch ( RepositoryException e )
        {
            throw new IOException( e.getMessage() );
        }
    }

    public Project getProjectFor( String groupId, String artifactId, String version, String artifactType,
                                  String publicKeyTokenId )
        throws IOException
    {
        long startTime = System.currentTimeMillis();

        ValueFactory valueFactory = rdfRepository.getValueFactory();

        Project project = new ProjectDependency();
        project.setArtifactId( artifactId );
        project.setGroupId( groupId );
        project.setVersion( version );
        project.setArtifactType( artifactType );
        project.setPublicKeyTokenId( publicKeyTokenId );

        TupleQueryResult result = null;

        try
        {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery( QueryLanguage.SERQL, projectQuery );
            tupleQuery.setBinding( ProjectUri.GROUP_ID.getObjectBinding(), valueFactory.createLiteral( groupId ) );
            tupleQuery.setBinding( ProjectUri.ARTIFACT_ID.getObjectBinding(), valueFactory.createLiteral( artifactId ) );
            tupleQuery.setBinding( ProjectUri.VERSION.getObjectBinding(), valueFactory.createLiteral( version ) );
            tupleQuery.setBinding( ProjectUri.ARTIFACT_TYPE.getObjectBinding(),
                                   valueFactory.createLiteral( artifactType ) );

            if ( publicKeyTokenId != null )
            {
                tupleQuery.setBinding( ProjectUri.CLASSIFIER.getObjectBinding(),
                                       valueFactory.createLiteral( publicKeyTokenId ) );
                project.setPublicKeyTokenId( publicKeyTokenId.replace( ":", "" ) );
            }

            result = tupleQuery.evaluate();

            if ( !result.hasNext() )
            {
                if ( artifactType != null && ArtifactTypeHelper.isDotnetAnyGac( artifactType ) )
                {
                    Artifact artifact =
                        ProjectFactory.createArtifactFrom( (ProjectDependency) project, artifactFactory );
                    if ( !artifact.getFile().exists() )
                    {
                        throw new IOException( "NPANDAY-180-003: Could not find GAC assembly: Group ID = " + groupId
                            + ", Artifact ID = " + artifactId + ", Version = " + version + ", Artifact Type = "
                            + artifactType + ", File Path = " + artifact.getFile().getAbsolutePath() );
                    }
                    project.setResolved( true );
                    return project;
                }

                throw new IOException( "NPANDAY-180-004: Could not find the project: Group ID = " + groupId
                    + ", Artifact ID = " + artifactId + ", Version = " + version + ", Artifact Type = " + artifactType );
            }

            while ( result.hasNext() )
            {
                BindingSet set = result.next();
                /*
                 * for ( Iterator<Binding> i = set.iterator(); i.hasNext(); ) { Binding b = i.next();
                 * System.out.println( b.getName() + ":" + b.getValue() ); }
                 */
                if ( set.hasBinding( ProjectUri.IS_RESOLVED.getObjectBinding() )
                    && set.getBinding( ProjectUri.IS_RESOLVED.getObjectBinding() ).getValue().toString().equalsIgnoreCase(
                                                                                                                           "true" ) )
                {
                    project.setResolved( true );
                }

                project.setArtifactType( set.getBinding( ProjectUri.ARTIFACT_TYPE.getObjectBinding() ).getValue().toString() );
                /*
                 * if ( set.hasBinding( ProjectUri.PARENT.getObjectBinding() ) ) { String pid = set.getBinding(
                 * ProjectUri.PARENT.getObjectBinding() ).getValue().toString(); String[] tokens = pid.split( "[:]" );
                 * Project parentProject = getProjectFor( tokens[0], tokens[1], tokens[2], null, null );
                 * project.setParentProject( parentProject ); }
                 */
                if ( set.hasBinding( ProjectUri.DEPENDENCY.getObjectBinding() ) )
                {
                    Binding binding = set.getBinding( ProjectUri.DEPENDENCY.getObjectBinding() );
                    addDependenciesToProject( project, repositoryConnection, binding.getValue() );
                }

                if ( set.hasBinding( ProjectUri.CLASSIFIER.getObjectBinding() ) )
                {
                    Binding binding = set.getBinding( ProjectUri.CLASSIFIER.getObjectBinding() );
                    addClassifiersToProject( project, repositoryConnection, binding.getValue() );
                }
            }
        }
        catch ( QueryEvaluationException e )
        {
            throw new IOException( "NPANDAY-180-005: Message = " + e.getMessage() );
        }
        catch ( RepositoryException e )
        {
            throw new IOException( "NPANDAY-180-006: Message = " + e.getMessage() );
        }
        catch ( MalformedQueryException e )
        {
            throw new IOException( "NPANDAY-180-007: Message = " + e.getMessage() );
        }
        finally
        {
            if ( result != null )
            {
                try
                {
                    result.close();
                }
                catch ( QueryEvaluationException e )
                {

                }
            }
        }

        // TODO: If has parent, then need to modify dependencies, etc of returned project
        logger.finest( "NPANDAY-180-008: ProjectDao.GetProjectFor - Artifact Id = " + project.getArtifactId()
            + ", Time = " + ( System.currentTimeMillis() - startTime ) + ",  Count = " + getProjectForCounter++ );
        return project;
    }

    public Project getProjectFor( MavenProject mavenProject )
        throws IOException
    {
        return getProjectFor( mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(),
                              mavenProject.getArtifact().getType(), mavenProject.getArtifact().getClassifier() );
    }

    public void storeProject( Project project, File localRepository, List<ArtifactRepository> artifactRepositories )
        throws IOException
    {

    }

    /**
     * Generates the system path for gac dependencies.
     */
    private String generateDependencySystemPath( ProjectDependency projectDependency )
    {
        return new File( System.getenv( "SystemRoot" ), "/assembly/"
            + projectDependency.getArtifactType().toUpperCase() + "/" + projectDependency.getArtifactId() + "/"
            + projectDependency.getVersion() + "__" + projectDependency.getPublicKeyTokenId() + "/"
            + projectDependency.getArtifactId() + ".dll" ).getAbsolutePath();

    }

    public Set<Artifact> storeProjectAndResolveDependencies( Project project, File localRepository,
                                                             List<ArtifactRepository> artifactRepositories )
        throws IOException, IllegalArgumentException
    {
        long startTime = System.currentTimeMillis();
        String snapshotVersion = null;

        if ( project == null )
        {
            throw new IllegalArgumentException( "NPANDAY-180-009: Project is null" );
        }

        if ( project.getGroupId() == null || project.getArtifactId() == null || project.getVersion() == null )
        {
            throw new IllegalArgumentException( "NPANDAY-180-010: Project value is null: Group Id ="
                + project.getGroupId() + ", Artifact Id = " + project.getArtifactId() + ", Version = "
                + project.getVersion() );
        }

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();

        ValueFactory valueFactory = rdfRepository.getValueFactory();
        URI id =
            valueFactory.createURI( project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion()
                + ":" + project.getArtifactType() );
        URI groupId = valueFactory.createURI( ProjectUri.GROUP_ID.getPredicate() );
        URI artifactId = valueFactory.createURI( ProjectUri.ARTIFACT_ID.getPredicate() );
        URI version = valueFactory.createURI( ProjectUri.VERSION.getPredicate() );
        URI artifactType = valueFactory.createURI( ProjectUri.ARTIFACT_TYPE.getPredicate() );
        URI classifier = valueFactory.createURI( ProjectUri.CLASSIFIER.getPredicate() );
        URI isResolved = valueFactory.createURI( ProjectUri.IS_RESOLVED.getPredicate() );

        URI artifact = valueFactory.createURI( ProjectUri.ARTIFACT.getPredicate() );
        URI dependency = valueFactory.createURI( ProjectUri.DEPENDENCY.getPredicate() );
        URI parent = valueFactory.createURI( ProjectUri.PARENT.getPredicate() );

        Set<Model> modelDependencies = new HashSet<Model>();
        try
        {

            repositoryConnection.add( id, RDF.TYPE, artifact );
            repositoryConnection.add( id, groupId, valueFactory.createLiteral( project.getGroupId() ) );
            repositoryConnection.add( id, artifactId, valueFactory.createLiteral( project.getArtifactId() ) );
            repositoryConnection.add( id, version, valueFactory.createLiteral( project.getVersion() ) );
            repositoryConnection.add( id, artifactType, valueFactory.createLiteral( project.getArtifactType() ) );
            if ( project.getPublicKeyTokenId() != null )
            {
                URI classifierNode = valueFactory.createURI( project.getPublicKeyTokenId() + ":" );
                for ( Requirement requirement : project.getRequirements() )
                {
                    URI uri = valueFactory.createURI( requirement.getUri().toString() );
                    repositoryConnection.add( classifierNode, uri, valueFactory.createLiteral( requirement.getValue() ) );
                }

                repositoryConnection.add( id, classifier, classifierNode );
            }

            if ( project.getParentProject() != null )
            {
                Project parentProject = project.getParentProject();
                URI pid =
                    valueFactory.createURI( parentProject.getGroupId() + ":" + parentProject.getArtifactId() + ":"
                        + parentProject.getVersion() + ":" + project.getArtifactType() );
                repositoryConnection.add( id, parent, pid );
                artifactDependencies.addAll( storeProjectAndResolveDependencies( parentProject, null,
                                                                                 artifactRepositories ) );
            }

            for ( ProjectDependency projectDependency : project.getProjectDependencies() )
            {
                snapshotVersion = null;
                
                logger.finest( "NPANDAY-180-011: Project Dependency: Artifact ID = "
                    + projectDependency.getArtifactId() + ", Group ID = " + projectDependency.getGroupId()
                    + ", Version = " + projectDependency.getVersion() + ", Artifact Type = "
                    + projectDependency.getArtifactType() );

                // If artifact has been deleted, then re-resolve
                if ( projectDependency.isResolved() && !ArtifactTypeHelper.isDotnetAnyGac( projectDependency.getArtifactType() ) )
                {
                    if ( projectDependency.getSystemPath() == null )
                    {
                        projectDependency.setSystemPath( generateDependencySystemPath( projectDependency ) );
                    }
                    Artifact assembly = ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );

                    File dependencyFile = PathUtil.getUserAssemblyCacheFileFor( assembly, localRepository );
                    if ( !dependencyFile.exists() )
                    {
                        projectDependency.setResolved( false );
                    }
                }

                // resolve system scope dependencies
                if ( projectDependency.getScope() != null && projectDependency.getScope().equals( "system" ) )
                {
                    if ( projectDependency.getSystemPath() == null )
                    {
                        throw new IOException( "systemPath required for System Scoped dependencies " + "in Group ID = "
                            + projectDependency.getGroupId() + ", Artiract ID = " + projectDependency.getArtifactId() );
                    }

                    File f = new File( projectDependency.getSystemPath() );

                    if ( !f.exists() )
                    {
                        throw new IOException( "Dependency systemPath File not found:"
                            + projectDependency.getSystemPath() + "in Group ID = " + projectDependency.getGroupId()
                            + ", Artiract ID = " + projectDependency.getArtifactId() );
                    }

                    Artifact assembly = ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                    assembly.setFile( f );
                    assembly.setResolved( true );
                    artifactDependencies.add( assembly );

                    projectDependency.setResolved( true );

                    logger.info( "NPANDAY-180-011.1: Project Dependency Resolved: Artifact ID = "
                        + projectDependency.getArtifactId() + ", Group ID = " + projectDependency.getGroupId()
                        + ", Version = " + projectDependency.getVersion() + ", Scope = " + projectDependency.getScope()
                        + "SystemPath = " + projectDependency.getSystemPath()

                    );

                    continue;
                }

                // resolve com reference
                // flow:
                // 1. generate the interop dll in temp folder and resolve to that path during dependency resolution
                // 2. cut and paste the dll to buildDirectory and update the paths once we grab the reference of
                // MavenProject (CompilerContext.java)
                if ( projectDependency.getArtifactType().equals( "com_reference" ) )
                {
                    String tokenId = projectDependency.getPublicKeyTokenId();
                    String interopPath = generateInteropDll( projectDependency.getArtifactId(), tokenId );

                    File f = new File( interopPath );

                    if ( !f.exists() )
                    {
                        throw new IOException( "Dependency com_reference File not found:" + interopPath
                            + "in Group ID = " + projectDependency.getGroupId() + ", Artiract ID = "
                            + projectDependency.getArtifactId() );
                    }

                    Artifact assembly = ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                    assembly.setFile( f );
                    assembly.setResolved( true );
                    artifactDependencies.add( assembly );

                    projectDependency.setResolved( true );

                    logger.info( "NPANDAY-180-011.1: Project Dependency Resolved: Artifact ID = "
                        + projectDependency.getArtifactId() + ", Group ID = " + projectDependency.getGroupId()
                        + ", Version = " + projectDependency.getVersion() + ", Scope = " + projectDependency.getScope()
                        + "SystemPath = " + projectDependency.getSystemPath()

                    );

                    continue;
                }

                // resolve gac references
                // note: the old behavior of gac references, used to have system path properties in the pom of the
                // project
                // now we need to generate the system path of the gac references so we can use
                // System.getenv("SystemRoot")
                if ( !projectDependency.isResolved() )
                {
                    if ( ArtifactTypeHelper.isDotnetAnyGac( projectDependency.getArtifactType() ) )
                    {
                        try
                        {
                            projectDependency.setResolved( true );
                            if ( projectDependency.getSystemPath() == null )
                            {
                                projectDependency.setSystemPath( generateDependencySystemPath( projectDependency ) );
                            }
                            File f = new File( projectDependency.getSystemPath() );
                            Artifact assembly = ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                            assembly.setFile( f );
                            assembly.setResolved( true );
                            artifactDependencies.add( assembly );

                        }
                        catch ( ExceptionInInitializerError e )
                        {
                            logger.warning( "NPANDAY-180-516.82: Project Failed to Resolve Dependency: Artifact ID = "
                                + projectDependency.getArtifactId() + ", Group ID = " + projectDependency.getGroupId()
                                + ", Version = " + projectDependency.getVersion() + ", Scope = "
                                + projectDependency.getScope() + "SystemPath = " + projectDependency.getSystemPath() );
                        }
                    }
                    else
                    {
                        try
                        {
                            Project dep =
                                this.getProjectFor( projectDependency.getGroupId(), projectDependency.getArtifactId(),
                                                    projectDependency.getVersion(),
                                                    projectDependency.getArtifactType(),
                                                    projectDependency.getPublicKeyTokenId() );
                            if ( dep.isResolved() )
                            {
                                projectDependency = (ProjectDependency) dep;
                                Artifact assembly =
                                    ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                                artifactDependencies.add( assembly );
                                artifactDependencies.addAll( this.storeProjectAndResolveDependencies(
                                                                                                      projectDependency,
                                                                                                      localRepository,
                                                                                                      artifactRepositories ) );
                            }
                        }
                        catch ( IOException e )
                        {
                            // safe to ignore: dependency not found
                        }
                    }
                }

                if ( !projectDependency.isResolved() )
                {
                    Artifact assembly = ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                    if ( assembly.getType().equals( "jar" ) )
                    {
                        logger.info( "Detected jar dependency - skipping: Artifact Dependency ID = "
                            + assembly.getArtifactId() );
                        continue;
                    }

                    ArtifactType type = ArtifactType.getArtifactTypeForPackagingName( assembly.getType() );

                    logger.info( "NPANDAY-180-012: Resolving artifact for unresolved dependency: "
                                + assembly.getId());

                    ArtifactRepository localArtifactRepository =
                        new DefaultArtifactRepository( "local", "file://" + localRepository,
                                                       new DefaultRepositoryLayout() );
                    if ( !ArtifactTypeHelper.isDotnetExecutableConfig( type ))// TODO: Generalize to any attached artifact
                    {
                        Artifact pomArtifact =
                            artifactFactory.createProjectArtifact( projectDependency.getGroupId(),
                                                                   projectDependency.getArtifactId(),
                                                                   projectDependency.getVersion() );

                        try
                        {
                            artifactResolver.resolve( pomArtifact, artifactRepositories,
                                                      localArtifactRepository );

                            logger.info( "NPANDAY-180-024: resolving pom artifact: " + pomArtifact.toString() );
                            snapshotVersion = pomArtifact.getVersion();

                        }
                        catch ( ArtifactNotFoundException e )
                        {
                            logger.info( "NPANDAY-180-025:  Problem in resolving pom artifact: " + pomArtifact.toString()
                                + ", Message = " + e.getMessage() );

                        }
                        catch ( ArtifactResolutionException e )
                        {
                            logger.info( "NPANDAY-180-026: Problem in resolving pom artifact: " + pomArtifact.toString()
                                + ", Message = " + e.getMessage() );
                        }

                        if ( pomArtifact.getFile() != null && pomArtifact.getFile().exists() )
                        {
                            FileReader fileReader = new FileReader( pomArtifact.getFile() );

                            MavenXpp3Reader reader = new MavenXpp3Reader();
                            Model model;
                            try
                            {
                                model = reader.read( fileReader ); // TODO: interpolate values
                            }
                            catch ( XmlPullParserException e )
                            {
                                throw new IOException( "NPANDAY-180-015: Unable to read model: Message = "
                                    + e.getMessage() + ", Path = " + pomArtifact.getFile().getAbsolutePath() );

                            }
                            catch ( EOFException e )
                            {
                                throw new IOException( "NPANDAY-180-016: Unable to read model: Message = "
                                    + e.getMessage() + ", Path = " + pomArtifact.getFile().getAbsolutePath() );
                            }

                            // small hack for not doing inheritence
                            String g = model.getGroupId();
                            if ( g == null )
                            {
                                g = model.getParent().getGroupId();
                            }
                            String v = model.getVersion();
                            if ( v == null )
                            {
                                v = model.getParent().getVersion();
                            }
                            if ( !( g.equals( projectDependency.getGroupId() )
                                && model.getArtifactId().equals( projectDependency.getArtifactId() ) && v.equals( projectDependency.getVersion() ) ) )
                            {
                                throw new IOException(
                                                       "NPANDAY-180-017: Model parameters do not match project dependencies parameters: Model: "
                                                           + g + ":" + model.getArtifactId() + ":" + v + ", Project: "
                                                           + projectDependency.getGroupId() + ":"
                                                           + projectDependency.getArtifactId() + ":"
                                                           + projectDependency.getVersion() );
                            }
                            modelDependencies.add( model );
                        }

                    }

                    if ( snapshotVersion != null )
                    {
                        assembly.setVersion( snapshotVersion );
                    }

                    File uacFile = PathUtil.getUserAssemblyCacheFileFor( assembly, localRepository );
                    if (uacFile.exists())
                    {
                        assembly.setFile( uacFile );
                    }
                    else
                    {
                        logger.info( "NPANDAY-180-018: Not found in UAC, now retrieving artifact from wagon:"
                                + assembly.getId()
                                + ", Failed UAC Path Check = " + uacFile.getAbsolutePath());

                        try
                        {
                            artifactResolver.resolve( assembly, artifactRepositories,
                                                      localArtifactRepository );

                            uacFile.getParentFile().mkdirs();
                            FileUtils.copyFile( assembly.getFile(), uacFile );
                        }
                        catch ( ArtifactNotFoundException e )
                        {
                            throw new IOException(
                                                   "NPANDAY-180-020: Problem in resolving artifact: Artifact = "
                                                       + assembly.getId()
                                                       + ", Message = " + e.getMessage() );
                        }
                        catch ( ArtifactResolutionException e )
                        {
                            throw new IOException(
                                                   "NPANDAY-180-019: Problem in resolving artifact: Artifact = "
                                                       + assembly.getId()
                                                       + ", Message = " + e.getMessage() );
                        }
                    }
                    artifactDependencies.add( assembly );
                }// end if dependency not resolved
                URI did =
                    valueFactory.createURI( projectDependency.getGroupId() + ":" + projectDependency.getArtifactId()
                        + ":" + projectDependency.getVersion() + ":" + projectDependency.getArtifactType() );
                repositoryConnection.add( did, RDF.TYPE, artifact );
                repositoryConnection.add( did, groupId, valueFactory.createLiteral( projectDependency.getGroupId() ) );
                repositoryConnection.add( did, artifactId,
                                          valueFactory.createLiteral( projectDependency.getArtifactId() ) );
                repositoryConnection.add( did, version, valueFactory.createLiteral( projectDependency.getVersion() ) );
                repositoryConnection.add( did, artifactType,
                                          valueFactory.createLiteral( projectDependency.getArtifactType() ) );
                if ( projectDependency.getPublicKeyTokenId() != null )
                {
                    repositoryConnection.add(
                                              did,
                                              classifier,
                                              valueFactory.createLiteral( projectDependency.getPublicKeyTokenId() + ":" ) );
                }
                repositoryConnection.add( id, dependency, did );

            }// end for
            repositoryConnection.add( id, isResolved, valueFactory.createLiteral( true ) );
            repositoryConnection.commit();
        }
        catch ( OpenRDFException e )
        {
            if ( repositoryConnection != null )
            {
                try
                {
                    repositoryConnection.rollback();
                }
                catch ( RepositoryException e1 )
                {

                }
            }
            throw new IOException( "NPANDAY-180-021: Could not open RDF Repository: Message =" + e.getMessage() );
        }

        for ( Model model : modelDependencies )
        {
            // System.out.println( "Storing dependency: Artifact Id = " + model.getArtifactId() );
            artifactDependencies.addAll( storeProjectAndResolveDependencies( ProjectFactory.createProjectFrom( model,
                                                                                                               null ),
                                                                             localRepository, artifactRepositories ) );
        }
        logger.finest( "NPANDAY-180-022: ProjectDao.storeProjectAndResolveDependencies - Artifact Id = "
            + project.getArtifactId() + ", Time = " + ( System.currentTimeMillis() - startTime ) + ", Count = "
            + storeCounter++ );
        return artifactDependencies;
    }

    public Set<Artifact> storeModelAndResolveDependencies( Model model, File pomFileDirectory,
                                                           File localArtifactRepository,
                                                           List<ArtifactRepository> artifactRepositories )
        throws IOException
    {
        return storeProjectAndResolveDependencies( ProjectFactory.createProjectFrom( model, pomFileDirectory ),
                                                   localArtifactRepository, artifactRepositories );
    }

    public String getClassName()
    {
        return className;
    }

    public String getID()
    {
        return id;
    }

    public void init( Object dataStoreObject, String id, String className )
        throws IllegalArgumentException
    {
        if ( dataStoreObject == null || !( dataStoreObject instanceof org.openrdf.repository.Repository ) )
        {
            throw new IllegalArgumentException(
                                                "NPANDAY-180-023: Must initialize with an instance of org.openrdf.repository.Repository" );
        }

        this.id = id;
        this.className = className;
        this.rdfRepository = (org.openrdf.repository.Repository) dataStoreObject;
    }

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {

    }

    protected void initForUnitTest( Object dataStoreObject, String id, String className,
                                    ArtifactResolver artifactResolver, ArtifactFactory artifactFactory )
    {
        this.init( dataStoreObject, id, className );
        init( artifactFactory, artifactResolver );
    }

    private void addClassifiersToProject( Project project, RepositoryConnection repositoryConnection,
                                          Value classifierUri )
        throws RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        String query = "SELECT * FROM {x} p {y}";
        TupleQuery tq = repositoryConnection.prepareTupleQuery( QueryLanguage.SERQL, query );
        tq.setBinding( "x", classifierUri );
        TupleQueryResult result = tq.evaluate();

        while ( result.hasNext() )
        {
            BindingSet set = result.next();
            for ( Iterator<Binding> i = set.iterator(); i.hasNext(); )
            {
                Binding binding = i.next();
                if ( binding.getValue().toString().startsWith( "http://maven.apache.org/artifact/requirement" ) )
                {
                    try
                    {
                        project.addRequirement( Requirement.Factory.createDefaultRequirement(
                                                                                              new java.net.URI(
                                                                                                                binding.getValue().toString() ),
                                                                                              set.getValue( "y" ).toString() ) );
                    }
                    catch ( URISyntaxException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void addDependenciesToProject( Project project, RepositoryConnection repositoryConnection,
                                           Value dependencyUri )
        throws RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        TupleQuery tq = repositoryConnection.prepareTupleQuery( QueryLanguage.SERQL, dependencyQuery );
        tq.setBinding( "x", dependencyUri );
        TupleQueryResult dependencyResult = tq.evaluate();
        try
        {
            while ( dependencyResult.hasNext() )
            {
                ProjectDependency projectDependency = new ProjectDependency();
                BindingSet bs = dependencyResult.next();
                projectDependency.setGroupId( bs.getBinding( ProjectUri.GROUP_ID.getObjectBinding() ).getValue().toString() );
                projectDependency.setArtifactId( bs.getBinding( ProjectUri.ARTIFACT_ID.getObjectBinding() ).getValue().toString() );
                projectDependency.setVersion( bs.getBinding( ProjectUri.VERSION.getObjectBinding() ).getValue().toString() );
                projectDependency.setArtifactType( bs.getBinding( ProjectUri.ARTIFACT_TYPE.getObjectBinding() ).getValue().toString() );

                Binding classifierBinding = bs.getBinding( ProjectUri.CLASSIFIER.getObjectBinding() );
                if ( classifierBinding != null )
                {
                    projectDependency.setPublicKeyTokenId( classifierBinding.getValue().toString().replace( ":", "" ) );
                }

                project.addProjectDependency( projectDependency );
                if ( bs.hasBinding( ProjectUri.DEPENDENCY.getObjectBinding() ) )
                {
                    addDependenciesToProject( projectDependency, repositoryConnection,
                                              bs.getValue( ProjectUri.DEPENDENCY.getObjectBinding() ) );
                }
            }
        }
        finally
        {
            dependencyResult.close();
        }
    }

    private String constructQueryFragmentFor( String subject, List<ProjectUri> projectUris )
    {
        // ProjectUri nonOptionalUri = this.getNonOptionalUriFrom( projectUris );
        // projectUris.remove( nonOptionalUri );

        StringBuffer buffer = new StringBuffer();
        buffer.append( subject );
        for ( Iterator<ProjectUri> i = projectUris.iterator(); i.hasNext(); )
        {
            ProjectUri projectUri = i.next();
            buffer.append( " " );
            if ( projectUri.isOptional() )
            {
                buffer.append( "[" );
            }
            buffer.append( "<" ).append( projectUri.getPredicate() ).append( "> {" ).append(
                                                                                             projectUri.getObjectBinding() ).append(
                                                                                                                                     "}" );
            if ( projectUri.isOptional() )
            {
                buffer.append( "]" );
            }
            if ( i.hasNext() )
            {
                buffer.append( ";" );
            }
        }
        return buffer.toString();
    }

    private ProjectUri getNonOptionalUriFrom( List<ProjectUri> projectUris )
    {
        for ( ProjectUri projectUri : projectUris )
        {
            if ( !projectUri.isOptional() )
            {
                return projectUri;
            }
        }
        return null;
    }

    // TODO: move generateInteropDll, getInteropParameters, getTempDirectory, and execute methods to another class
    private String generateInteropDll( String name, String classifier )
        throws IOException
    {

        File tmpDir;
        String comReferenceAbsolutePath = "";
        try
        {
            tmpDir = getTempDirectory();
        }
        catch ( IOException e )
        {
            throw new IOException( "Unable to create temporary directory" );
        }

        try
        {
            comReferenceAbsolutePath = resolveComReferencePath( name, classifier );
        }
        catch ( Exception e )
        {
            throw new IOException( e.getMessage() );
        }

        String interopAbsolutePath = tmpDir.getAbsolutePath() + File.separator + "Interop." + name + ".dll";
        List<String> params = getInteropParameters( interopAbsolutePath, comReferenceAbsolutePath, name );

        try
        {
            execute( "tlbimp", params );
        }
        catch ( Exception e )
        {
            throw new IOException( e.getMessage() );
        }

        return interopAbsolutePath;
    }

    private String resolveComReferencePath( String name, String classifier )
        throws Exception
    {
        String[] classTokens = classifier.split( "}" );

        classTokens[1] = classTokens[1].replace( "-", "\\" );

        String newClassifier = classTokens[0] + "}" + classTokens[1];

        String registryPath = "HKEY_CLASSES_ROOT\\TypeLib\\" + newClassifier + "\\win32\\";
        int lineNoOfPath = 1;

        List<String> parameters = new ArrayList<String>();
        parameters.add( "query" );
        parameters.add( registryPath );
        parameters.add( "/ve" );

        StreamConsumer outConsumer = new StreamConsumerImpl();
        StreamConsumer errorConsumer = new StreamConsumerImpl();

        try
        {
            // TODO: investigate why outConsumer ignores newline
            execute( "reg", parameters, outConsumer, errorConsumer );
        }
        catch ( Exception e )
        {
            throw new Exception( "Cannot find information of [" + name
                + "] ActiveX component in your system, you need to install this component first to continue." );
        }

        // parse outConsumer
        String out = outConsumer.toString();

        String tokens[] = out.split( "\n" );

        String lineResult = "";
        String[] result;
        if ( tokens.length >= lineNoOfPath - 1 )
        {
            lineResult = tokens[lineNoOfPath - 1];
        }

        result = lineResult.split( "REG_SZ" );

        if ( result.length > 1 )
        {
            return result[1].trim();
        }

        return null;
    }

    private List<String> readPomAttribute( String pomFileLoc, String tag )
    {
        List<String> attributes = new ArrayList<String>();

        try
        {
            File file = new File( pomFileLoc );

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse( file );
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName( tag );

            for ( int s = 0; s < nodeLst.getLength(); s++ )
            {
                Node currentNode = nodeLst.item( s );

                NodeList childrenList = currentNode.getChildNodes();

                for ( int i = 0; i < childrenList.getLength(); i++ )
                {
                    Node child = childrenList.item( i );
                    attributes.add( child.getNodeValue() );
                }
            }
        }
        catch ( Exception e )
        {

        }

        return attributes;
    }

    private List<String> getInteropParameters( String interopAbsolutePath, String comRerefenceAbsolutePath,
                                               String namespace )
    {
        List<String> parameters = new ArrayList<String>();
        parameters.add( comRerefenceAbsolutePath );
        parameters.add( "/out:" + interopAbsolutePath );
        parameters.add( "/namespace:" + namespace );
        try
        {
            // beginning code for checking of strong name key or signing of projects
            String key = "";
            String keyfile = "";
            String currentWorkingDir = System.getProperty( "user.dir" );

            // Check if Parent pom
            List<String> modules = readPomAttribute( currentWorkingDir + File.separator + "pom.xml", "module" );
            if ( !modules.isEmpty() )
            {
                // check if there is a matching dependency with the namespace
                for ( String child : modules )
                {
                    // check each module pom file if there is existing keyfile
                    String tempDir = currentWorkingDir + File.separator + child;
                    try
                    {
                        List<String> keyfiles = readPomAttribute( tempDir + "\\pom.xml", "keyfile" );
                        if ( keyfiles.get( 0 ) != null )
                        {
                            // PROBLEM WITH MULTIMODULES
                            boolean hasComRef = false;
                            List<String> dependencies = readPomAttribute( tempDir + "\\pom.xml", "groupId" );
                            for ( String item : dependencies )
                            {
                                if ( item.equals( namespace ) )
                                {
                                    hasComRef = true;
                                    break;
                                }
                            }
                            if ( hasComRef )
                            {
                                key = keyfiles.get( 0 );
                                currentWorkingDir = tempDir;
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                    }

                }
            }
            else
            {
                // not a parent pom, so read project pom file for keyfile value

                List<String> keyfiles = readPomAttribute( currentWorkingDir + File.separator + "pom.xml", "keyfile" );
                key = keyfiles.get( 0 );
            }
            if ( key != "" )
            {
                keyfile = currentWorkingDir + File.separator + key;
                parameters.add( "/keyfile:" + keyfile );
            }
            // end code for checking of strong name key or signing of projects
        }
        catch ( Exception ex )
        {
        }

        return parameters;
    }

    private File getTempDirectory()
        throws IOException
    {
        File tempFile = File.createTempFile( "interop-dll-", "" );
        File tmpDir = new File( tempFile.getParentFile(), tempFile.getName() );
        tempFile.delete();
        tmpDir.mkdir();
        return tmpDir;
    }

    // can't use dotnet-executable due to cyclic dependency.
    private void execute( String executable, List<String> commands )
        throws Exception
    {
        execute( executable, commands, null, null );
    }

    private void execute( String executable, List<String> commands, StreamConsumer systemOut, StreamConsumer systemError )
        throws Exception
    {
        int result = 0;
        Commandline commandline = new Commandline();
        commandline.setExecutable( executable );
        commandline.addArguments( commands.toArray( new String[commands.size()] ) );
        try
        {
            result = CommandLineUtils.executeCommandLine( commandline, systemOut, systemError );

            System.out.println( "NPANDAY-040-000: Executed command: Commandline = " + commandline + ", Result = "
                + result );

            if ( result != 0 )
            {
                throw new Exception( "NPANDAY-040-001: Could not execute: Command = " + commandline.toString()
                    + ", Result = " + result );
            }
        }
        catch ( CommandLineException e )
        {
            throw new Exception( "NPANDAY-040-002: Could not execute: Command = " + commandline.toString() );
        }
    }

    /**
     * TODO: refactor this to another class and all methods concerning com_reference StreamConsumer instance that
     * buffers the entire output
     */
    class StreamConsumerImpl
        implements StreamConsumer
    {

        private DefaultConsumer consumer;

        private StringBuffer sb = new StringBuffer();

        public StreamConsumerImpl()
        {
            consumer = new DefaultConsumer();
        }

        public void consumeLine( String line )
        {
            sb.append( line );
            if ( logger != null )
            {
                consumer.consumeLine( line );
            }
        }

        /**
         * Returns the stream
         * 
         * @return the stream
         */
        public String toString()
        {
            return sb.toString();
        }
    }

}
