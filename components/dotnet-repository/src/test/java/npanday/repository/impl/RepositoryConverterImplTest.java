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
package npanday.repository.impl;

import junit.framework.TestCase;
import npanday.dao.impl.ProjectDaoImpl;
import npanday.dao.ProjectDao;
import npanday.dao.Project;
import npanday.dao.ProjectDependency;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.Artifact;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStoreRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
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
        ProjectDao dao = this.createProjectDao( repository );

        Project project = new Project();
        project.setGroupId( "npanday.model" );
        project.setArtifactId( "NPanday.Model.Pom" );
        project.setVersion( "1.0" );
        project.setArtifactType( "library" );

        ProjectDependency test2 = createProjectDependency( "npanday", "ClassLibrary1", "1.0" );
        test2.setArtifactType( "library" );
        project.addProjectDependency( test2 );

        try
        {
            dao.storeProjectAndResolveDependencies( project, testRepo, new ArrayList<ArtifactRepository>() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        RepositoryConverterImpl repositoryConverter = new RepositoryConverterImpl();
        repositoryConverter.initTest( new DataAccessObjectRegistryStub(), new ArtifactFactoryTestStub(),
                                      null, new ArtifactResolverTestStub() );
        try
        {
            repositoryConverter.convertRepositoryFormat( repository, testRepo );
        }
        catch ( Exception e )
        {
            fail( "Could not convert the repository: " + e.getMessage() );
        }
        this.exportRepositoryToRdf( "testConvert-rdf.xml", testRepo, repository );

        assertTrue( new File( testRepo, "/npanday/model/NPanday.Model.Pom/1.0/NPanday.Model.Pom-1.0.dll" ).exists() );
        assertTrue( new File( testRepo, "/npanday/model/NPanday.Model.Pom/1.0/NPanday.Model.Pom-1.0.pom" ).exists() );
        assertTrue( new File( testRepo, "/npanday/ClassLibrary1/1.0/ClassLibrary1-1.0.dll" ).exists() );

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

    private ProjectDao createProjectDao( Repository rdfRepository )
    {
        ProjectDaoImpl dao = new ProjectDaoImpl();
        dao.init( new ArtifactFactoryTestStub(), new ArtifactResolverTestStub() );
        dao.setRdfRepository( rdfRepository );
        dao.openConnection();
        return dao;
    }

    private void exportRepositoryToRdf( String fileName, File localRepository, Repository rdfRepository )
    {
        RDFHandler rdfxmlWriter;
        try
        {
            File exportFile = new File( localRepository.getParentFile(), fileName );
            rdfxmlWriter = new RDFXMLWriter( new FileOutputStream( exportFile ) );
        }
        catch ( IOException e )
        {
            return;
        }

        try
        {
            RepositoryConnection repositoryConnection = rdfRepository.getConnection();
            repositoryConnection.export( rdfxmlWriter );
        }
        catch ( RepositoryException e )
        {
            e.printStackTrace();
        }
        catch ( RDFHandlerException e )
        {
            e.printStackTrace();
        }
    }
}
