package org.apache.maven.dotnet.artifact.impl;

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
        File testRepo = new File( System.getProperty( "basedir" ), "target/test-repo/repository" );
        ArtifactInstallerImpl artifactInstaller = new ArtifactInstallerImpl();
        artifactInstaller.init( null, new ArrayList<ArtifactRepository>(), testRepo );
        ArtifactFactory stub = new ArtifactFactoryTestStub();
        artifactInstaller.initTest( stub, new DummyLogger() );
        Artifact artifact = stub.createArtifact( "NMaven.Model", "NMaven.Model.Pom", "1.0", "compile", "library" );
        artifact.setFile( new File( System.getProperty( "basedir" ),
                                    "target/test-repo/uac/gac_msil/NMaven.Model.Pom/1.0__NMaven.Model/NMaven.Model.Pom.dll" ) );
        Dependency dependency = new Dependency();
        dependency.setGroupId( "NMaven" );
        dependency.setArtifactId( "NMaven.Test" );
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
            "target/test-repo/pab/gac_msil/NMaven.Model.Pom/1.0__NMaven.Model/NMaven.Model.Pom.dll" ).exists() );
        assertTrue( "Could not find dependent artifact", new File(
            "target/test-repo/pab/gac_msil/NMaven.Model.Pom/1.0__NMaven.Model/NMaven.Test.dll" ).exists() );
    }
}
