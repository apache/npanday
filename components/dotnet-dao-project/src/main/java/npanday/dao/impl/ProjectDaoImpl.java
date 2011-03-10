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
import npanday.PathUtil;
import npanday.dao.Project;
import npanday.dao.ProjectDao;
import npanday.dao.ProjectDependency;
import npanday.dao.ProjectFactory;
import npanday.dao.ProjectUri;
import npanday.dao.Requirement;
import npanday.registry.RepositoryRegistry;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
/*import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
*/import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

    private String dependencyQuery;

    private String projectQuery;

    private ArtifactResolver artifactResolver;
    
    public void init( String id, String className )
        throws IllegalArgumentException
    {   
        this.id = id;
        this.className = className;    
    }

    public void init( ArtifactFactory artifactFactory, ArtifactResolver artifactResolver)
    {
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;
        this.className = className;
    }

    public Set<Project> getAllProjects()
        throws IOException
    {
        Set<Project> projects = new HashSet<Project>();
       /* TupleQueryResult result = null;
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
		*/
		
		//Implement a new getAllProjects

        return projects;
    }

    public void removeProjectFor( String groupId, String artifactId, String version, String artifactType )
        throws IOException
    {
       /* ValueFactory valueFactory = rdfRepository.getValueFactory();
        URI id = valueFactory.createURI( groupId + ":" + artifactId + ":" + version + ":" + artifactType );
        try
        {
            repositoryConnection.remove( repositoryConnection.getStatements( id, null, null, true ) );
        }
        catch ( RepositoryException e )
        {
            throw new IOException( e.getMessage() );
        }
		*/
		
		//Investigate behavior of removeProjectFor and Implment 
    }

    public Project getProjectFor( String groupId, String artifactId, String version, String artifactType,
                                  String publicKeyTokenId )
        throws IOException
    {
        long startTime = System.currentTimeMillis();
      
        ProjectDependency project = new ProjectDependency();
        project.setArtifactId( artifactId );
        project.setGroupId( groupId );
        project.setVersion( version );
        project.setArtifactType( artifactType );
        project.setPublicKeyTokenId( publicKeyTokenId );
        
        //Read default settings.xml of maven to get LocalRepository Location
        String m2_home = System.getenv("M2_HOME");
        
        String localRepo = getLocalRepository(m2_home+"/conf/settings.xml");
        
        String pomFile = localRepo + groupId.replace(".","/")+"/"+artifactId+"/"+version+"/"+artifactId+"-"+version+".pom.sha1";
        
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try 
        {
            reader = new FileReader(pomFile);
            model = mavenreader.read(reader);
            model.setPomFile(new File( pomFile ) );
        }
        catch(Exception ex)
        {
            throw new IOException( "NPANDAY-180-224: Message = " + ex.getMessage() );
        }
        MavenProject mavenProject = new MavenProject(model);

        List<Dependency> deps = mavenProject.getDependencies();
        
        for(Dependency dep : deps)
        {
            ProjectDependency projectDep = new ProjectDependency();
            project.setArtifactId( dep.getArtifactId() );
            project.setGroupId( dep.getGroupId() );
            project.setVersion( dep.getVersion() );
            Artifact artifact = createArtifactFrom( projectDep, artifactFactory );
                    
                    if ( !artifact.getFile().exists() )
                    {
                        throw new IOException( "NPANDAY-180-123: Could not find GAC assembly: Group ID = " + groupId
                            + ", Artifact ID = " + artifactId + ", Version = " + version + ", Artifact Type = "
                            + artifactType + ", File Path = " + artifact.getFile().getAbsolutePath() );
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
        return storeProjectAndResolveDependencies( project, localRepository, artifactRepositories,
                                                   new HashMap<String, Set<Artifact>>() );
    }

    public Set<Artifact> storeProjectAndResolveDependencies( Project project, File localRepository,
                                                             List<ArtifactRepository> artifactRepositories,
                                                             Map<String, Set<Artifact>> cache )
        throws IOException, IllegalArgumentException
    {
        
       
        String key = getKey( project );
        
        if ( cache.containsKey( key ) )
        {
            return cache.get( key );
        }

        long startTime = System.currentTimeMillis();
        String snapshotVersion;
      
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

       
        Set<Model> modelDependencies = new HashSet<Model>();
        try
        {
            if ( project.getParentProject() != null )
            {
                Project parentProject = project.getParentProject();
             
                artifactDependencies.addAll( storeProjectAndResolveDependencies( parentProject, null,
                                                                                 artifactRepositories, cache ) );
            }

            for ( ProjectDependency projectDependency : project.getProjectDependencies() )
            {
                Artifact assembly = createArtifactFrom( projectDependency, artifactFactory );

                snapshotVersion = null;
                
                if(!assembly.getFile().exists())
                {
                    
                    try
                    {                    
                     ArtifactRepository localArtifactRepository =
                        new DefaultArtifactRepository( "local", "file://" + localRepository,
                                                       new DefaultRepositoryLayout() );
                    
                     artifactResolver.resolve( assembly, artifactRepositories,
                                                      localArtifactRepository );
                                 
                     projectDependency.setResolved( true );                                 
                    }
                    catch ( ArtifactNotFoundException e )
                    {
                        logger.warning( "NPANDAY-181-121:  Problem in resolving assembly: " + assembly.toString()
                        + ", Message = " + e.getMessage() );
                    }
                    catch ( ArtifactResolutionException e )
                    {
                        logger.warning( "NPANDAY-181-122: Problem in resolving assembly: " + assembly.toString()
                        + ", Message = " + e.getMessage() );
                    }
                }
                
                logger.finer( "NPANDAY-180-011: Project Dependency: Artifact ID = "
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
                    
                    File dependencyFile = PathUtil.getDotNetArtifact( assembly , localRepository );
                    
                    if ( !dependencyFile.exists() )
                    {
                        projectDependency.setResolved( false );
                    }
                    else
                    {
                         projectDependency.setResolved( true );        
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

                    assembly.setFile( f );
                    assembly.setResolved( true );
                    artifactDependencies.add( assembly );

                    projectDependency.setResolved( true );

                    logger.finer( "NPANDAY-180-011.1: Project Dependency Resolved: Artifact ID = "
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

                    assembly.setFile( f );
                    assembly.setResolved( true );
                    artifactDependencies.add( assembly );

                    projectDependency.setResolved( true );

                    logger.fine( "NPANDAY-180-011.1: Project Dependency Resolved: Artifact ID = "
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
                //we have already set file for the assembly above (in createArtifactFrom) so we do not need re-resovle it
                if ( !projectDependency.isResolved() )
                {
                    if ( ArtifactTypeHelper.isDotnetAnyGac( projectDependency.getArtifactType() ) )
                    {
                        try
                        {
                            if (assembly.getFile().exists())
                            {
                                projectDependency.setSystemPath( assembly.getFile().getAbsolutePath());
                                projectDependency.setResolved( true );
                                assembly.setResolved( true );
                            }
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
                            // re-resolve snapshots
                            if ( !assembly.isSnapshot() )
                            {
                                Project dep =
                                    this.getProjectFor( projectDependency.getGroupId(), projectDependency.getArtifactId(),
                                                        projectDependency.getVersion(),
                                                        projectDependency.getArtifactType(),
                                                        projectDependency.getPublicKeyTokenId() );
                                if ( dep.isResolved() )
                                {
                                    projectDependency = (ProjectDependency) dep;
                                    artifactDependencies.add( assembly );
                                    Set<Artifact> deps = this.storeProjectAndResolveDependencies( projectDependency,
                                                                                                  localRepository,
                                                                                                  artifactRepositories,
                                                                                                  cache );
                                    artifactDependencies.addAll( deps );
                                }
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
                    logger.finest("NPANDAY-180-055: dependency:" + projectDependency.getClass());
                    logger.finest("NPANDAY-180-056: dependency:" + assembly.getClass());
                    
                    if ( assembly.getType().equals( "jar" ) )
                    {
                        logger.info( "Detected jar dependency - skipping: Artifact Dependency ID = "
                            + assembly.getArtifactId() );
                        continue;
                    }

                    ArtifactType type = ArtifactType.getArtifactTypeForPackagingName( assembly.getType() );

                    logger.finer( "NPANDAY-180-012: Resolving artifact for unresolved dependency: "
                                + assembly.getId());

                    ArtifactRepository localArtifactRepository =
                        new DefaultArtifactRepository( "local", "file://" + localRepository,
                                                       new DefaultRepositoryLayout() );
                    if ( !ArtifactTypeHelper.isDotnetExecutableConfig( type ))// TODO: Generalize to any attached artifact
                    {
                        logger.finest( "NPANDAY-180-016: set file....");
                    
                        Artifact pomArtifact =
                            artifactFactory.createProjectArtifact( projectDependency.getGroupId(),
                                                                   projectDependency.getArtifactId(),
                                                                   projectDependency.getVersion() );

                        try
                        {
                            artifactResolver.resolve( pomArtifact, artifactRepositories,
                                                      localArtifactRepository );

                            projectDependency.setResolved( true );                          
                            
                            logger.finer( "NPANDAY-180-024: resolving pom artifact: " + pomArtifact.toString() );
                            snapshotVersion = pomArtifact.getVersion();

                        }
                        catch ( ArtifactNotFoundException e )
                        {
                            logger.warning( "NPANDAY-180-025:  Problem in resolving pom artifact: " + pomArtifact.toString()
                                + ", Message = " + e.getMessage() );

                        }
                        catch ( ArtifactResolutionException e )
                        {
                            logger.warning( "NPANDAY-180-026: Problem in resolving pom artifact: " + pomArtifact.toString()
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
                            if( model.getArtifactId().equals( projectDependency.getArtifactId() ) && projectDependency.isResolved() )
                            {
                               modelDependencies.add( model );
                            }
                        }

                    }
                    logger.finest( "NPANDAY-180-019: set file...");
                    if ( snapshotVersion != null )
                    {
                        assembly.setVersion( snapshotVersion );
                    }

                    File dotnetFile = PathUtil.getDotNetArtifact( assembly , localRepository );
                    
                    logger.warning( "NPANDAY-180-018: Not found in local repository, now retrieving artifact from wagon:"
                            + assembly.getId()
                            + ", Failed Path Check = " + dotnetFile.getAbsolutePath());

                    if ( !ArtifactTypeHelper.isDotnetExecutableConfig( type ) || !dotnetFile.exists() )// TODO: Generalize to any attached artifact
                    {
                        try
                        {
                            artifactResolver.resolve( assembly, artifactRepositories,
                                                      localArtifactRepository );

                            projectDependency.setResolved( true );
                            
                            if ( assembly != null && assembly.getFile().exists() )
                            {
                                dotnetFile.getParentFile().mkdirs();
                                FileUtils.copyFile( assembly.getFile(), dotnetFile );
                                assembly.setFile( dotnetFile );
                            }
                        }
                        catch ( ArtifactNotFoundException e )
                        {
                            logger.log(Level.SEVERE, "NPANDAY-180-0201: Error resolving artifact. Reason:", e);
                            throw new IOException(
                                                   "NPANDAY-180-020: Problem in resolving artifact: Artifact = "
                                                       + assembly.getId()
                                                       + ", Message = " + e.getMessage() );
                        }
                        catch ( ArtifactResolutionException e )
                        {
                            logger.log( Level.SEVERE, "NPANDAY-180-019: Problem in resolving artifact: Artifact = "
                                              + assembly.getId()
                                              + ", Message = " + e.getMessage(), e );
                            throw new IOException(
                                                   "NPANDAY-180-019: Problem in resolving artifact: Artifact = "
                                                       + assembly.getId()
                                                       + ", Message = " + e.getMessage() );
                        }
                    }
                    artifactDependencies.add( assembly );
                }// end if dependency not resolved
            }// end for
        }
        catch ( Exception e )
        {
            throw new IOException( "NPANDAY-180-021: Could not resolve project: Message =" + e);
        }

        for ( Model model : modelDependencies )
        {
            Project projectModel = ProjectFactory.createProjectFrom( model, null );
            artifactDependencies.addAll( storeProjectAndResolveDependencies( projectModel, localRepository,
                                                                             artifactRepositories, cache ) );
        }
        logger.finest( "NPANDAY-180-022: ProjectDao.storeProjectAndResolveDependencies - Artifact Id = "
            + project.getArtifactId() + ", Time = " + ( System.currentTimeMillis() - startTime ) + ", Count = "
            + storeCounter++ );

        cache.put( key, artifactDependencies );

        return artifactDependencies;
    }

    private String getKey( Project project )
    {
        return project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
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

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {

    }

    protected void initForUnitTest( String id, String className,
                                    ArtifactResolver artifactResolver, ArtifactFactory artifactFactory )
    {
        this.className = className;
        init( artifactFactory, artifactResolver );
    }

    private void addDependenciesToProject( Project project )
    {
         //Implement new way on how to get and add the project dependencies
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
    
    /*
     * Read Default settings from M2_HOME value of ENV Var to check if there local repository is not in default Location
     * Related to NPanday-361
    */
    private String getLocalRepository( String defaultMavenSettings)
    {
        List<String> attributes = new ArrayList<String>();
        String localRepo = "";
        try
        {
            File file = new File( defaultMavenSettings );

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse( file );
            doc.getDocumentElement().normalize();

           NodeList nodeLst = doc.getElementsByTagName( "localRepository");
            

             for ( int s = 0; s < nodeLst.getLength(); s++ )
            {
                Node currentNode = nodeLst.item( s );

                NodeList childrenList = currentNode.getChildNodes();

                Node child = childrenList.item( 0 );
                localRepo = child.getNodeValue();     
            }
            
        }
        catch ( Exception e )
        {
             logger.warning( "NPANDAY-181-218:  Default Maven Settings not found, localRepo is set to default " + ", Message = " + e.getMessage() );
        }
        
        if (localRepo == "")
        {
            localRepo = System.getProperty("user.home")+"/.m2/repository/";
        }

        return localRepo;
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
     * Creates an artifact using information from the specified project dependency.
     *
     * @param projectDependency a project dependency to use as the source of the returned artifact
     * @param artifactFactory   artifact factory used to create the artifact
     * @return an artifact using information from the specified project dependency
     */
    private static Artifact createArtifactFrom( ProjectDependency projectDependency, ArtifactFactory artifactFactory )
    {
        String groupId = projectDependency.getGroupId();
        String artifactId = projectDependency.getArtifactId();
        String version = projectDependency.getVersion();
        String artifactType = projectDependency.getArtifactType();
        String scope = ( projectDependency.getScope() == null ) ? Artifact.SCOPE_COMPILE : projectDependency.getScope();
        String publicKeyTokenId = projectDependency.getPublicKeyTokenId();

        if ( groupId == null )
        {
            logger.warning( "NPANDAY-180-001: Project Group ID is missing" );
        }
        if ( artifactId == null )
        {
            logger.warning( "NPANDAY-180-002: Project Artifact ID is missing: Group Id = " + groupId );
        }
        if ( version == null )
        {
            logger.warning( "NPANDAY-180-003: Project Version is missing: Group Id = " + groupId +
                ", Artifact Id = " + artifactId );
        }
        if ( artifactType == null )
        {
            logger.warning( "NPANDAY-180-004: Project Artifact Type is missing: Group Id" + groupId +
                ", Artifact Id = " + artifactId + ", Version = " + version );
        }
        
        Artifact assembly = artifactFactory.createDependencyArtifact( groupId, artifactId,
                                                                      VersionRange.createFromVersion( version ),
                                                                      artifactType, publicKeyTokenId, scope,
                                                                      null );
 
        //using PathUtil
        File artifactFile = null;
        if (ArtifactTypeHelper.isDotnetAnyGac( artifactType ))
        {
            if (!ArtifactTypeHelper.isDotnet4Gac(artifactType))
            {
                artifactFile = PathUtil.getGlobalAssemblyCacheFileFor( assembly, new File("C:\\WINDOWS\\assembly\\") );
            }
            else
            {
                artifactFile = PathUtil.getGACFile4Artifact(assembly);
            }
        }
        else
        {
            artifactFile = PathUtil.getUserAssemblyCacheFileFor( assembly, new File( System.getProperty( "user.home" ),
                                                                                      File.separator + ".m2" + File.separator + "repository") );
        }

        assembly.setFile( artifactFile );
        return assembly;
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
