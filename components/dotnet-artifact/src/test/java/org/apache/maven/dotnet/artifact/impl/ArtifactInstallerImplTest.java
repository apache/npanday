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

package npanday.artifact.impl;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

public class ArtifactInstallerImplTest
    extends TestCase
{

    public void testInstallArtifactAndDependenciesIntoPrivateApplicationBase()
    {
	    /*
        File testRepo = new File( System.getProperty( "basedir" ), "target/test-repo/repository" );
        ArtifactInstallerImpl artifactInstaller = new ArtifactInstallerImpl();
        artifactInstaller.init( null, new ArrayList<ArtifactRepository>(), testRepo );
        ArtifactFactory stub = new ArtifactFactoryTestStub();
        artifactInstaller.initTest( stub, new DummyLogger() );
        Artifact artifact = stub.createArtifact( "NPanday.Model", "NPanday.Model.Pom", "1.0", "compile", "library" );
        artifact.setFile( new File( System.getProperty( "basedir" ),
                                    "target/test-repo/uac/gac_msil/NPanday.Model.Pom/1.0__NPanday.Model/NPanday.Model.Pom.dll" ) );
        Dependency dependency = new Dependency();
        dependency.setGroupId( "NPanday" );
        dependency.setArtifactId( "NPanday.Test" );
        dependency.setVersion( "1.0" );
        dependency.setType( "library" );

        List<Dependency> dependencies = new ArrayList<Dependency>();
        dependencies.add( dependency );
        try
        {
            artifactInstaller.installArtifactAndDependenciesIntoPrivateApplicationBase( testRepo, artifact,
                                                                                        dependencies );
        }
        catch ( java.io.IOException e )
        {
            fail(e.getMessage());
        }

        assertTrue( "Could not find main artifact", new File(
            "target/test-repo/pab/gac_msil/NPanday.Model.Pom/1.0__NPanday.Model/NPanday.Model.Pom.dll" ).exists() );
        assertTrue( "Could not find dependent artifact", new File(
            "target/test-repo/pab/gac_msil/NPanday.Model.Pom/1.0__NPanday.Model/NPanday.Test.dll" ).exists() );
			*/
    }
}
