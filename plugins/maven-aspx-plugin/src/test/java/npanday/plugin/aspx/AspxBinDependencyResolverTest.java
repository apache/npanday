package npanday.plugin.aspx;

/*
* Copyright 2009
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class AspxBinDependencyResolverTest extends PlexusTestCase {
    private ArtifactFactory factory;

    public void setUp() throws Exception {
        super.setUp();

        factory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
    }

    public void testFinalNameChanged() throws MojoExecutionException, MojoFailureException, IOException {
        AspxBinDependencyResolver mojo = new AspxBinDependencyResolver();

        File binDir = getTestFile("target/bin-dir");
        FileUtils.deleteDirectory(binDir);
        binDir.mkdirs();
        mojo.setBinDir(binDir);

        VersionRange version = VersionRange.createFromVersion("1.0");
        Artifact artifact = factory.createDependencyArtifact("groupId", "artifactId", version, "dll", null,
                Artifact.SCOPE_RUNTIME);
        artifact.setFile(getTestFile("src/test/resources/artifactId-1.0.dll"));

        mojo.setDependencies(Collections.singleton(artifact));

        assertFalse(new File(binDir, "artifactId.dll").exists());
        assertFalse(new File(binDir, "artifactId-1.0.dll").exists());
        mojo.execute();

        assertTrue(new File(binDir, "artifactId.dll").exists());
        assertEquals("artifactId-1.0.dll", FileUtils.fileRead(new File(binDir, "artifactId.dll")).trim());
        assertFalse(new File(binDir, "artifactId-1.0.dll").exists());
    }

    public void testFinalNameMatches() throws MojoExecutionException, MojoFailureException, IOException {
        AspxBinDependencyResolver mojo = new AspxBinDependencyResolver();

        File binDir = getTestFile("target/bin-dir");
        FileUtils.deleteDirectory(binDir);
        binDir.mkdirs();
        mojo.setBinDir(binDir);

        VersionRange version = VersionRange.createFromVersion("1.0");
        Artifact artifact = factory.createDependencyArtifact("groupId", "artifactId", version, "dll", null,
                Artifact.SCOPE_RUNTIME);
        artifact.setFile(getTestFile("src/test/resources/artifactId.dll"));

        mojo.setDependencies(Collections.singleton(artifact));

        assertFalse(new File(binDir, "artifactId.dll").exists());
        assertFalse(new File(binDir, "artifactId-1.0.dll").exists());
        mojo.execute();

        assertTrue(new File(binDir, "artifactId.dll").exists());
        assertEquals("artifactId.dll", FileUtils.fileRead(new File(binDir, "artifactId.dll")).trim());
        assertFalse(new File(binDir, "artifactId-1.0.dll").exists());
    }
}
