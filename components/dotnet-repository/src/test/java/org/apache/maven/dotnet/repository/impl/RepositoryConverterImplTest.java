package org.apache.maven.dotnet.repository.impl;

import junit.framework.TestCase;
import org.apache.maven.dotnet.dao.impl.ProjectDaoImpl;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDependency;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStoreRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class RepositoryConverterImplTest
    extends TestCase
{

    private static File basedir = new File( System.getProperty( "basedir" ) );

    public void testConvert()
    {
        File testRepo = new File( System.getProperty( "basedir" ), "target/test-repo/repository" );
        testRepo.mkdir();

        Repository repository = this.createRepository();
        ProjectDao dao = this.createProjectDao(repository);

        Project project = new Project();
        project.setGroupId( "NMaven.Model" );
        project.setArtifactId( "NMaven.Model.Pom" );
        project.setVersion( "1.0" );

        ProjectDependency test2 = createProjectDependency( "NMaven", "NMaven.Test", "1.0" );
        test2.setArtifactType( "library");
        project.addProjectDependency( test2 );

        try
        {
            dao.storeProjectAndResolveDependencies( project, null, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        RepositoryConverterImpl repositoryConverter = new RepositoryConverterImpl();
        repositoryConverter.initTest( new DataAccessObjectRegistryStub(), new ArtifactFactoryTestStub(),
                                      new WagonManagerTestStub() );
        try
        {
            repositoryConverter.convert( repository, testRepo );
        }
        catch ( IOException e )
        {
            fail( "Could not convert the repository: " + e.getMessage() );
        }

        assertTrue(new File(testRepo, "/NMaven/Model/NMaven.Model.Pom/1.0/NMaven.Model.Pom-1.0.dll").exists());
        assertTrue(new File(testRepo, "/NMaven/Model/NMaven.Model.Pom/1.0/NMaven.Model.Pom-1.0.pom").exists());
        assertTrue(new File(testRepo, "/NMaven/NMaven.Test/1.0/NMaven.Test-1.0.dll").exists());
        assertTrue(new File(testRepo, "/NMaven/NMaven.Test/1.0/NMaven.Test-1.0.pom").exists());
    }

    private ProjectDependency createProjectDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId( groupId );
        projectDependency.setArtifactId( artifactId );
        projectDependency.setVersion( version );
        return projectDependency;
    }

    private Repository createRepository()
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
        return rdfRepository;
    }

    private ProjectDao createProjectDao(Repository rdfRepository)
    {
        ProjectDaoImpl dao = new ProjectDaoImpl();
        WagonManagerTestStub stub = new WagonManagerTestStub();
        stub.setBaseDir( basedir );
        dao.init( new ArtifactFactoryTestStub(), stub );
        dao.setRdfRepository( rdfRepository );
        dao.openConnection();
        return dao;
    }
}
