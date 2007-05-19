package org.apache.maven.dotnet.artifact;

import java.io.File;

import junit.framework.TestCase;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;

public class AssemblyRepositoryLayoutTest
    extends TestCase
{
    public void testPathOfWithClassifier()
    {
        /*
        String groupId,
        String artifactId,
        VersionRange versionRange,
        String scope, 
        String type,
        String classifier,
        ArtifactHandler artifactHandler
        */
        System.out.println(new File("org\\apache\\maven\\artifact.test\\1.0.0\\3.0\\artifact.test.dll").getAbsolutePath());
        Artifact artifact = new DefaultArtifact( "org.apache.maven", "artifact.test",
                                                 VersionRange.createFromVersion( "1.0.0" ), "compile", "dll", "3.0",
                                                 new DefaultArtifactHandler( "dll" ) );
        assertEquals( new AssemblyRepositoryLayout().pathOf( artifact ),
                      normalizePathForTargetPlatform("org\\apache\\maven\\artifact.test\\1.0.0\\3.0\\artifact.test.dll"));
    }

    public void testPathOf()
    {
        Artifact artifact = new DefaultArtifact( "org.apache.maven", "artifact.test",
                                                 VersionRange.createFromVersion( "1.0.0" ), "compile", "dll", null,
                                                 new DefaultArtifactHandler( "dll" ) );
        assertEquals( new AssemblyRepositoryLayout().pathOf( artifact ),
                      normalizePathForTargetPlatform("org\\apache\\maven\\artifact.test\\1.0.0\\artifact.test.dll") );
    }

    public void testPathOfRemoteRepositoryMetadata()
    {
        Artifact artifact = new DefaultArtifact( "org.apache.maven", "artifact.test",
                                                 VersionRange.createFromVersion( "1.0.0" ), "compile", "dll", null,
                                                 new DefaultArtifactHandler( "dll" ) );
        ArtifactMetadata artifactMetadata = new ProjectArtifactMetadata( artifact );
        assertEquals( new AssemblyRepositoryLayout().pathOfRemoteRepositoryMetadata( artifactMetadata ),
                      normalizePathForTargetPlatform("org\\apache\\maven\\artifact.test\\1.0.0\\artifact.test-1.0.0.pom") );

    }

    public void testPathOfRemoteRepositoryMetadataWithClassifier()
    {
        Artifact artifact = new DefaultArtifact( "org.apache.maven", "artifact.test",
                                                 VersionRange.createFromVersion( "1.0.0" ), "compile", "dll", "3.0",
                                                 new DefaultArtifactHandler( "dll" ) );
        ArtifactMetadata artifactMetadata = new ProjectArtifactMetadata( artifact );
        assertEquals( new AssemblyRepositoryLayout().pathOfRemoteRepositoryMetadata( artifactMetadata ),
                      normalizePathForTargetPlatform("org\\apache\\maven\\artifact.test\\1.0.0\\artifact.test-1.0.0.pom") );

    }

    public void testPathOfLocalRepositoryMetadata()
    {
        Artifact artifact = new DefaultArtifact( "org.apache.maven", "artifact.test",
                                                 VersionRange.createFromVersion( "1.0.0" ), "compile", "dll", null,
                                                 new DefaultArtifactHandler( "dll" ) );
        ArtifactMetadata artifactMetadata = new ProjectArtifactMetadata( artifact );
        ArtifactRepository artifactRepository =
            new DefaultArtifactRepository( "testRepo", "http://localhost/maven2", new AssemblyRepositoryLayout() );
        assertEquals(
            new AssemblyRepositoryLayout().pathOfLocalRepositoryMetadata( artifactMetadata, artifactRepository ),
            normalizePathForTargetPlatform("org\\apache\\maven\\artifact.test\\1.0.0\\artifact.test-1.0.0.pom") );

    }

    public void testPathOfLocalRepositoryMetadataWithClassifier()
    {
        Artifact artifact = new DefaultArtifact( "org.apache.maven", "artifact.test",
                                                 VersionRange.createFromVersion( "1.0.0" ), "compile", "dll", "3.0",
                                                 new DefaultArtifactHandler( "dll" ) );
        ArtifactMetadata artifactMetadata = new ProjectArtifactMetadata( artifact );
        ArtifactRepository artifactRepository =
            new DefaultArtifactRepository( "testRepo", "http://localhost/maven2", new AssemblyRepositoryLayout() );
        assertEquals(
            new AssemblyRepositoryLayout().pathOfLocalRepositoryMetadata( artifactMetadata, artifactRepository ),
            normalizePathForTargetPlatform("org\\apache\\maven\\artifact.test\\1.0.0\\artifact.test-1.0.0.pom") );
    }

    private String normalizePathForTargetPlatform(String path)
    {
        return path.replace( "\\", File.separator);
    }
}
