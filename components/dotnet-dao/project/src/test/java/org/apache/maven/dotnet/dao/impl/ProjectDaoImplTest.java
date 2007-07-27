package org.apache.maven.dotnet.dao.impl;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.net.URI;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.MemoryStoreRDFSInferencer;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDependency;
import org.apache.maven.dotnet.dao.Requirement;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.ProjectUri;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.Artifact;

public class ProjectDaoImplTest
    extends TestCase
{

    private static File basedir = new File( System.getProperty( "basedir" ) );

    private static File localRepository = new File( System.getProperty( "basedir" ), "/target/local-test-repo" );

    public void testGetAllProjects()
    {
        ProjectDao dao = this.createProjectDao();

        Project project = new Project();
        project.setGroupId( "NMaven" );
        project.setArtifactId( "NMaven.Test5" );
        project.setVersion( "1.0.0" );

        //ProjectDependency test2 = createProjectDependency( "NMaven", "NMaven.Test5", "1.0.0" );
        //project.addProjectDependency( test2 );

        try
        {
            dao.storeProjectAndResolveDependencies( project, localRepository, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        Set<Project> projects = null;
        try
        {
            projects = dao.getAllProjects();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "Could not retrieve the project: " + e.getMessage() );
        }
        /*
        assertEquals("Incorrect Number of Projects", 3, projects.size());

        for(Project proj : projects)
        {
            if(proj.getArtifactId().equals( "NMaven.Test5"))
            {
                assertEquals("Incorrect number of dependencies", 2, proj.getProjectDependencies().size());
            }
        }
        */
        dao.closeConnection();
    }

    public void testStore_WithRequirements()
    {
        ProjectDao dao = this.createProjectDao();

        Project project = new Project();
        project.setGroupId( "NMaven.Model" );
        project.setArtifactId( "NMaven.Model.Pom" );
        project.setVersion( "0.14.0.0" );
        project.setPublicKeyTokenId( "b03f5f7f11d50a3a");
        Set<Requirement> requirements = new HashSet<Requirement>();
        try
        {
            requirements.add( Requirement.Factory.createDefaultRequirement( new URI( ProjectUri.VENDOR.getPredicate() ),
                                                                            "MICROSOFT" ) );
            requirements.add( Requirement.Factory.createDefaultRequirement(
                new URI( ProjectUri.FRAMEWORK_VERSION.getPredicate() ), "2.0" ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        project.setRequirements( requirements );

        Set<Artifact> artifacts = null;
        try
        {
            artifacts =
                dao.storeProjectAndResolveDependencies( project, localRepository, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        Project proj = null;
        try
        {
            proj = dao.getProjectFor( "NMaven.Model", "NMaven.Model.Pom", "0.14.0.0", null, null );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not query the project: " + e.getMessage() );
        }

        requirements = proj.getRequirements();

        assertEquals( "Incorrect number of requirements.", 2, requirements.size() );
        assertTrue( "Could not find framework requirement",
                    hasRequirement( ProjectUri.FRAMEWORK_VERSION.getPredicate(), "2.0", requirements ) );
        assertTrue( "Could not find vendor requirement",
                    hasRequirement( ProjectUri.VENDOR.getPredicate(), "MICROSOFT", requirements ) );
        dao.closeConnection();
    }

    private boolean hasRequirement( String uri, String value, Set<Requirement> requirements )
    {
        for ( Requirement requirement : requirements )
        {
            if ( uri.equals( requirement.getUri().toString() ) && value.equals( requirement.getValue() ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasDependency( String groupId, String artifactId, String version,
                                   Set<ProjectDependency> projectDependencies )
    {
        for ( ProjectDependency projectDependency : projectDependencies )
        {
            if ( projectDependency.getGroupId().equals( groupId ) &&
                projectDependency.getArtifactId().equals( artifactId ) &&
                projectDependency.getVersion().equals( version ) )
            {
                return true;
            }
        }
        return false;
    }

    public void testStoreDependency()
    {
        ProjectDao dao = this.createProjectDao();

        Project project1 = new Project();
        project1.setGroupId( "NMaven" );
        project1.setArtifactId( "NMaven.Plugin" );
        project1.setVersion( "0.14.0.0" );

        project1.addProjectDependency( this.createProjectDependency( "NMaven", "NMaven.Test4", "1.0.0" ) );

        Set<Artifact> artifacts = null;
        try
        {
            artifacts = dao.storeProjectAndResolveDependencies( project1, localRepository,
                                                                new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        assertEquals( "Incorrect number of returned artifacts", 1, artifacts.size() );

        Artifact artifact = (Artifact) artifacts.toArray()[0];
        assertEquals( "Group Id is incorrect", "NMaven", artifact.getGroupId() );
        assertEquals( "Artifact Id is incorrect", "NMaven.Test4", artifact.getArtifactId() );
        assertEquals( "Version is incorrect", "1.0.0", artifact.getVersion() );

        Project testProject = null;
        try
        {
            testProject = dao.getProjectFor( "NMaven", "NMaven.Plugin", "0.14.0.0", null, null );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "Could not retrieve the project: " + e.getMessage() );
        }

        Set<ProjectDependency> projectDependencies = testProject.getProjectDependencies();
        assertEquals( "Incorrect number of dependencies", 1, projectDependencies.size() );
        ProjectDependency projectDependency = (ProjectDependency) projectDependencies.toArray()[0];
        assertTrue( "Could not find required dependency. Found Dependency: GroupId = " +
            projectDependency.getGroupId() + ", Artifact Id = " + projectDependency.getArtifactId(),
                    this.hasDependency( "NMaven", "NMaven.Test4", "1.0.0", projectDependencies ) );
        dao.closeConnection();
    }

    public void testStoreTransitiveDependency()
    {
        ProjectDao dao = this.createProjectDao();

        Project project = new Project();
        project.setGroupId( "NMaven" );
        project.setArtifactId( "NMaven.Test" );
        project.setVersion( "1.0.0" );

        ProjectDependency test2 = createProjectDependency( "NMaven", "NMaven.Test2", "1.0.0" );
        project.addProjectDependency( test2 );
        Set<Artifact> artifacts = null;

        try
        {
            artifacts =
                dao.storeProjectAndResolveDependencies( project, localRepository, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }
        assertEquals( "Incorrect number of returned artifacts", 3, artifacts.size() );

        Project testProject = null;
        try
        {
            testProject = dao.getProjectFor( "NMaven", "NMaven.Test", "1.0.0", null, null );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "Could not retrieve the project: " + e.getMessage() );
        }

        Set<ProjectDependency> projectDependencies =
            ( (ProjectDependency) testProject.getProjectDependencies().toArray()[0] ).getProjectDependencies();
        assertEquals( "Incorrect number of dependencies", 1, projectDependencies.size() );

        ProjectDependency projectDependency = (ProjectDependency) projectDependencies.toArray()[0];
        assertTrue( "Could not find required dependency. Found Dependency: GroupId = " +
            projectDependency.getGroupId() + ", Artifact Id = " + projectDependency.getArtifactId(),
                    this.hasDependency( "NMaven", "NMaven.Test3", "1.0.0", projectDependencies ) );
        dao.closeConnection();
    }

    public void testSingleStore()
    {
        ProjectDao dao = this.createProjectDao();

        Project project = new Project();
        project.setGroupId( "NMaven.Model" );
        project.setArtifactId( "NMaven.Model.Pom" );
        project.setVersion( "0.14.0.0" );
        Set<Artifact> artifacts = null;
        try
        {
            artifacts =
                dao.storeProjectAndResolveDependencies( project, localRepository, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        Project proj = null;
        try
        {
            proj = dao.getProjectFor( "NMaven.Model", "NMaven.Model.Pom", "0.14.0.0", null, null );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not query the project: " + e.getMessage() );
        }

        assertEquals( "Group IDs do not match", project.getGroupId(), proj.getGroupId() );
        assertEquals( "Artifact IDs do not match", project.getArtifactId(), proj.getArtifactId() );
        assertEquals( "Versions do not match", project.getVersion(), proj.getVersion() );

        assertEquals( "Found dependency when there should be none", 0, artifacts.size() );
        dao.closeConnection();
    }

    public void testSingleStore_WithIncorrectVersion()
    {
        ProjectDao dao = this.createProjectDao();

        Project project = new Project();
        project.setGroupId( "NMaven.Model" );
        project.setArtifactId( "NMaven.Model.Pom" );
        project.setVersion( "0.14.0.0" );
        try
        {
            dao.storeProjectAndResolveDependencies( project, localRepository, new ArrayList<ArtifactRepository>() );

        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        try
        {
            Project proj = dao.getProjectFor( "NMaven.Model", "NMaven.Model.Pom", "0.15.0.0", null, null );
        }
        catch ( java.io.IOException e )
        {
            return;

        }
        dao.closeConnection();
        fail( "Found project when none should exist." );
    }

    private ProjectDependency createProjectDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId( groupId );
        projectDependency.setArtifactId( artifactId );
        projectDependency.setVersion( version );
        return projectDependency;
    }

    private ProjectDao createProjectDao()
    {
        File dataDir = new File( basedir, ( "/target/rdf-repos/rdf-repo-" + System.currentTimeMillis() ) );
        org.openrdf.repository.Repository rdfRepository =
            new SailRepository( new MemoryStoreRDFSInferencer( new MemoryStore( dataDir ) ) );
        try
        {
            rdfRepository.initialize();
        }
        catch ( RepositoryException e )
        {
            return null;
        }
        ProjectDaoImpl dao = new ProjectDaoImpl();
        WagonManagerTestStub stub = new WagonManagerTestStub();
        stub.setBaseDir( basedir );
        dao.initForUnitTest( rdfRepository, "", "", stub, new ArtifactFactoryTestStub() );
        dao.openConnection();
        return dao;
    }
}
