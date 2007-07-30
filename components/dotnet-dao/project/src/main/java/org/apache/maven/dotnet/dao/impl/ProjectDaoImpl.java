package org.apache.maven.dotnet.dao.impl;

import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.ProjectUri;
import org.apache.maven.dotnet.dao.ProjectFactory;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDependency;
import org.apache.maven.dotnet.dao.Requirement;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.PathUtil;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.EOFException;
import java.net.URISyntaxException;

public final class ProjectDaoImpl
    implements ProjectDao
{

    private String className;

    private String id;

    private static Logger logger = Logger.getAnonymousLogger();

    private org.openrdf.repository.Repository rdfRepository;

    private org.apache.maven.artifact.manager.WagonManager wagonManager;

    /**
     * The artifact factory component, which is used for creating artifacts.
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    private int getProjectForCounter = 0;

    private int storeCounter = 0;

    private RepositoryConnection repositoryConnection;

    private String dependencyQuery;

    private String projectQuery;

    public void init( ArtifactFactory artifactFactory, WagonManager wagonManager )
    {
        this.artifactFactory = artifactFactory;
        this.wagonManager = wagonManager;

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
                // Project project = getProjectFor( groupId, artifactId, version, artifactType, null );
                /*
                for ( Iterator<Binding> i = set.iterator(); i.hasNext(); )
                {
                    Binding b = i.next();
                    System.out.println( b.getName() + ":" + b.getValue() );
                }
               */
                projects.add( getProjectFor( groupId, artifactId, version, artifactType, null ) );
            }
        }
        catch ( RepositoryException e )
        {
            throw new IOException( e.getMessage() );
        }
        catch ( MalformedQueryException e )
        {
            throw new IOException( e.getMessage() );
        }
        catch ( QueryEvaluationException e )
        {
            throw new IOException( e.getMessage() );
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
            tupleQuery.setBinding( ProjectUri.ARTIFACT_ID.getObjectBinding(),
                                   valueFactory.createLiteral( artifactId ) );
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
                if ( artifactType != null && artifactType.startsWith( "gac" ) )
                {
                    Artifact artifact =
                        ProjectFactory.createArtifactFrom( (ProjectDependency) project, artifactFactory );
                    if ( !artifact.getFile().exists() )
                    {
                        throw new IOException( "Could not find GAC assembly: Group ID = " + groupId +
                            ", Artifact ID = " + artifactId + ", Version = " + version + ", Artifact Type = " +
                            artifactType + ", File Path = " + artifact.getFile().getAbsolutePath() );
                    }
                    project.setResolved( true );
                    return project;
                }

                throw new IOException( "NMAVEN-000-000: Could not find the project: Group ID = " + groupId +
                    ", Artifact ID = " + artifactId + ", Version = " + version + ", Artifact Type = " + artifactType );
            }

            while ( result.hasNext() )
            {
                BindingSet set = result.next();
                /*
                for ( Iterator<Binding> i = set.iterator(); i.hasNext(); )
                {
                    Binding b = i.next();
                    System.out.println( b.getName() + ":" + b.getValue() );
                }
                */
                if ( set.hasBinding( ProjectUri.IS_RESOLVED.getObjectBinding() ) &&
                    set.getBinding( ProjectUri.IS_RESOLVED.getObjectBinding() ).getValue().toString().equalsIgnoreCase(
                        "true" ) )
                {
                    project.setResolved( true );
                }

                project.setArtifactType(
                    set.getBinding( ProjectUri.ARTIFACT_TYPE.getObjectBinding() ).getValue().toString() );
                /*
                if ( set.hasBinding( ProjectUri.PARENT.getObjectBinding() ) )
                {
                    String pid = set.getBinding( ProjectUri.PARENT.getObjectBinding() ).getValue().toString();
                    String[] tokens = pid.split( "[:]" );
                    Project parentProject = getProjectFor( tokens[0], tokens[1], tokens[2], null, null );
                    project.setParentProject( parentProject );
                }
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
            throw new IOException( e.getMessage() );
        }
        catch ( RepositoryException e )
        {
            throw new IOException( e.getMessage() );
        }
        catch ( MalformedQueryException e )
        {
            throw new IOException( e.getMessage() );
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

        //TODO: If has parent, then need to modify dependencies, etc of returned project
        logger.finest( "ProjectDao.GetProjectFor - Artifact Id = " + project.getArtifactId() + ", Time = " +
            ( System.currentTimeMillis() - startTime ) + ",  Count = " + getProjectForCounter++ );
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


    public Set<Artifact> storeProjectAndResolveDependencies( Project project, File localRepository,
                                                             List<ArtifactRepository> artifactRepositories )
        throws IOException, IllegalArgumentException
    {
        long startTime = System.currentTimeMillis();

        if ( project == null )
        {
            throw new IllegalArgumentException( "NMAVEN-000-000: Project is null" );
        }

        if ( project.getGroupId() == null || project.getArtifactId() == null || project.getVersion() == null )
        {
            throw new IllegalArgumentException( "NMAVEN-000-000: Project value is null: Group Id =" +
                project.getGroupId() + ", Artifact Id = " + project.getArtifactId() + ", Version = " +
                project.getVersion() );
        }

        Set<Artifact> artifactDependencies = new HashSet<Artifact>();

        ValueFactory valueFactory = rdfRepository.getValueFactory();
        URI id = valueFactory.createURI( project.getGroupId() + ":" + project.getArtifactId() + ":" +
            project.getVersion() + ":" + project.getArtifactType() );
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
                    //System.out.println( "Add requirement: " + requirement.getUri() );
                    URI uri = valueFactory.createURI( requirement.getUri().toString() );
                    repositoryConnection.add( classifierNode, uri,
                                              valueFactory.createLiteral( requirement.getValue() ) );
                }

                repositoryConnection.add( id, classifier, classifierNode );
            }

            if ( project.getParentProject() != null )
            {
                Project parentProject = project.getParentProject();
                URI pid = valueFactory.createURI( parentProject.getGroupId() + ":" + parentProject.getArtifactId() +
                    ":" + parentProject.getVersion() + ":" + project.getArtifactType() );
                repositoryConnection.add( id, parent, pid );
                artifactDependencies.addAll(
                    storeProjectAndResolveDependencies( parentProject, null, artifactRepositories ) );
            }

            for ( ProjectDependency projectDependency : project.getProjectDependencies() )
            {
                logger.info( "Project Dependency: Artifact ID = " + projectDependency.getArtifactId() +
                    ", Group ID = " + projectDependency.getGroupId() + ", Version = " + projectDependency.getVersion() +
                    ", Artifact Type = " + projectDependency.getArtifactType() );
                if ( !projectDependency.isResolved() )
                {
                    if ( projectDependency.getArtifactType().startsWith( "gac" ) )
                    {
                        projectDependency.setResolved( true );
                        artifactDependencies.add(
                            ProjectFactory.createArtifactFrom( projectDependency, artifactFactory ) );
                    }
                    else
                    {
                        try
                        {
                            Project dep = this.getProjectFor( projectDependency.getGroupId(),
                                                              projectDependency.getArtifactId(),
                                                              projectDependency.getVersion(),
                                                              projectDependency.getArtifactType(),
                                                              projectDependency.getPublicKeyTokenId() );
                            if ( dep.isResolved() )
                            {
                                projectDependency = (ProjectDependency) dep;
                                Artifact assembly =
                                    ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                                artifactDependencies.add( assembly );
                                artifactDependencies.addAll( this.storeProjectAndResolveDependencies( projectDependency,
                                                                                                      localRepository,
                                                                                                      artifactRepositories ) );
                            }
                        }
                        catch ( IOException e )
                        {
                            //e.printStackTrace();
                            //safe to ignore: dependency not found
                        }
                    }
                }

                if ( !projectDependency.isResolved() )
                {
                    Artifact assembly = ProjectFactory.createArtifactFrom( projectDependency, artifactFactory );
                    if ( !assembly.getType().equals( "exe.config" ) )//TODO: Generalize to any attached artifact
                    {
                        Artifact pomArtifact = artifactFactory.createProjectArtifact( projectDependency.getGroupId(),
                                                                                      projectDependency.getArtifactId(),
                                                                                      projectDependency.getVersion() );

                        File tmpFile = new File( System.getProperty( "java.io.tmpdir" ),
                                                 "pom-." + System.currentTimeMillis() + ".xml" );
                        tmpFile.deleteOnExit();
                        pomArtifact.setFile( tmpFile );

                        try
                        {
                            logger.info( "NMAVEN-000-000: Retrieving artifact: Artifact ID  = " +
                                projectDependency.getArtifactId() );
                            wagonManager.getArtifact( pomArtifact, artifactRepositories );
                        }
                        catch ( TransferFailedException e )
                        {
                            logger.info( "NMAVEN-000-000a: Problem in resolving artifact: Assembly Artifact Id = " +
                                assembly.getArtifactId() + ", Type = " + assembly.getType() + ", Message = " +
                                e.getMessage() );
                        }
                        catch ( ResourceDoesNotExistException e )
                        {
                            logger.info( "NMAVEN-000-000b: Problem in resolving artifact: Assembly Artifact Id = " +
                                assembly.getArtifactId() + ", Type = " + assembly.getType() + ", Message = " +
                                e.getMessage() );
                        }

                        if ( pomArtifact.getFile() != null && pomArtifact.getFile().exists() )
                        {
                            FileReader fileReader = new FileReader( pomArtifact.getFile() );

                            MavenXpp3Reader reader = new MavenXpp3Reader();
                            Model model;
                            try
                            {
                                model = reader.read( fileReader );
                            }
                            catch ( XmlPullParserException e )
                            {
                                throw new IOException( "NMAVEN-000-000: Unable to read model: Message = " +
                                    e.getMessage() + ", Path = " + pomArtifact.getFile().getAbsolutePath() );

                            }
                            catch ( EOFException e )
                            {
                                throw new IOException( "NMAVEN-000-000: Unable to read model: Message = " +
                                    e.getMessage() + ", Path = " + pomArtifact.getFile().getAbsolutePath() );
                            }

                            if ( !( model.getGroupId().equals( projectDependency.getGroupId() ) &&
                                model.getArtifactId().equals( projectDependency.getArtifactId() ) &&
                                model.getVersion().equals( projectDependency.getVersion() ) ) )
                            {
                                throw new IOException(
                                    "Model parameters do not match project dependencies parameters: Model: " +
                                        model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion() +
                                        ", Project: " + projectDependency.getGroupId() + ":" +
                                        projectDependency.getArtifactId() + ":" + projectDependency.getVersion() );
                            }
                            modelDependencies.add( model );
                        }

                    }

                    assembly.setFile( PathUtil.getUserAssemblyCacheFileFor( assembly, localRepository ) );
                    if ( !assembly.getFile().exists() )
                    {
                        try
                        {
                            logger.info( "NMAVEN-000-000: Retrieving artifact: Artifact ID  = " +
                                projectDependency.getArtifactId() );
                            wagonManager.getArtifact( assembly, artifactRepositories );
                        }
                        catch ( TransferFailedException e )
                        {
                            throw new IOException(
                                "NMAVEN-000-000c: Problem in resolving artifact: Assembly Artifact Id = " +
                                    assembly.getArtifactId() + ", Type = " + assembly.getType() + ", Message = " +
                                    e.getMessage() );
                        }
                        catch ( ResourceDoesNotExistException e )
                        {
                            throw new IOException(
                                "NMAVEN-000-000d: Problem in resolving artifact: Assembly Artifact Id = " +
                                    assembly.getArtifactId() + ", Type = " + assembly.getType() + ", Message = " +
                                    e.getMessage() );
                        }
                    }
                    artifactDependencies.add( assembly );
                }//end if dependency not resolved
                URI did = valueFactory.createURI( projectDependency.getGroupId() + ":" +
                    projectDependency.getArtifactId() + ":" + projectDependency.getVersion() + ":" +
                    projectDependency.getArtifactType() );
                repositoryConnection.add( did, RDF.TYPE, artifact );
                repositoryConnection.add( did, groupId, valueFactory.createLiteral( projectDependency.getGroupId() ) );
                repositoryConnection.add( did, artifactId,
                                          valueFactory.createLiteral( projectDependency.getArtifactId() ) );
                repositoryConnection.add( did, version, valueFactory.createLiteral( projectDependency.getVersion() ) );
                repositoryConnection.add( did, artifactType,
                                          valueFactory.createLiteral( projectDependency.getArtifactType() ) );
                if ( projectDependency.getPublicKeyTokenId() != null )
                {
                    repositoryConnection.add( did, classifier, valueFactory.createLiteral(
                        projectDependency.getPublicKeyTokenId() + ":" ) );
                }
                repositoryConnection.add( id, dependency, did );

            }//end for
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
            throw new IOException( "NMAVEN-000-000: Could not open RDF Repository: Message =" + e.getMessage() );
        }

        for ( Model model : modelDependencies )
        {
            //System.out.println( "Storing dependency: Artifact Id = " + model.getArtifactId() );
            artifactDependencies.addAll( storeProjectAndResolveDependencies(
                ProjectFactory.createProjectFrom( model, null ), localRepository, artifactRepositories ) );
        }
        logger.finest( "ProjectDao.storeProjectAndResolveDependencies - Artifact Id = " + project.getArtifactId() +
            ", Time = " + ( System.currentTimeMillis() - startTime ) + ", Count = " + storeCounter++ );
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
                "NMAVEN: Must initialize with an instance of org.openrdf.repository.Repository" );
        }

        this.id = id;
        this.className = className;
        this.rdfRepository = (org.openrdf.repository.Repository) dataStoreObject;
    }

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {

    }

    protected void initForUnitTest( Object dataStoreObject, String id, String className, WagonManager wagonManager,
                                    ArtifactFactory artifactFactory )
    {
        this.init( dataStoreObject, id, className );
        init( artifactFactory, wagonManager );
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
                            new java.net.URI( binding.getValue().toString() ), set.getValue( "y" ).toString() ) );
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
                projectDependency.setGroupId(
                    bs.getBinding( ProjectUri.GROUP_ID.getObjectBinding() ).getValue().toString() );
                projectDependency.setArtifactId(
                    bs.getBinding( ProjectUri.ARTIFACT_ID.getObjectBinding() ).getValue().toString() );
                projectDependency.setVersion(
                    bs.getBinding( ProjectUri.VERSION.getObjectBinding() ).getValue().toString() );
                projectDependency.setArtifactType(
                    bs.getBinding( ProjectUri.ARTIFACT_TYPE.getObjectBinding() ).getValue().toString() );

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
        //   projectUris.remove( nonOptionalUri );

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
                projectUri.getObjectBinding() ).append( "}" );
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
}
