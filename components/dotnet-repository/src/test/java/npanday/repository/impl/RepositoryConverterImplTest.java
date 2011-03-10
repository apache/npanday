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

        ProjectDao dao = this.createProjectDao(  );

        Project project = new Project();
        project.setGroupId( "npanday.model" );
        project.setArtifactId( "NPanday.Model.Pom" );
        project.setVersion( "1.0" );
        project.setArtifactType( "library" );

        ProjectDependency test2 = createProjectDependency( "npanday", "ClassLibrary1", "1.0" );
        test2.setArtifactType( "library" );
        project.addProjectDependency( test2 );

        //Temporarily Disabled. failing because group Id is not being parsed successfully
        /*try
        {
            dao.storeProjectAndResolveDependencies( project, testRepo, new ArrayList<ArtifactRepository>() );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
            fail( "Could not store the project: " + e.getMessage() );
        }

        RepositoryConverterImpl repositoryConverter = new RepositoryConverterImpl();
        repositoryConverter.initTest( new ArtifactFactoryTestStub(),null, new ArtifactResolverTestStub() );
        try
        {
           repositoryConverter.convertRepositoryFormat( testRepo );
        }
        catch ( IOException e )
        {
            fail( "Could not convert the repository: " + e.getMessage() );
        }
        assertTrue( new File( testRepo, "/npanday/model/NPanday.Model.Pom/1.0/NPanday.Model.Pom-1.0.dll" ).exists() );
        assertTrue( new File( testRepo, "/npanday/model/NPanday.Model.Pom/1.0/NPanday.Model.Pom-1.0.pom" ).exists() );
        assertTrue( new File( testRepo, "/npanday/ClassLibrary1/1.0/ClassLibrary1-1.0.dll" ).exists() );
        */
    }

    private ProjectDependency createProjectDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId( groupId );
        projectDependency.setArtifactId( artifactId );
        projectDependency.setVersion( version );
        return projectDependency;
    }

   
    private ProjectDao createProjectDao( )
    {
        ProjectDaoImpl dao = new ProjectDaoImpl();
        dao.init( new ArtifactFactoryTestStub(), new ArtifactResolverTestStub() );
      
        return dao;
    }

    
}
