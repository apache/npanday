package npanday.plugin.aspx;

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

import java.lang.StackTraceElement; 
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import npanday.PathUtil;


/**
 * Maven Mojo for copying ASPx project dependencies to sourceDirectory\Bin folder
 *
 * @goal copy-dependency
 * @phase process-sources
 * @description Maven Mojo for copying ASPx project dependencies to sourceDirectory\Bin folder
 */
public class AspxBinDependencyResolver
        extends AbstractMojo {
    /**
     * The dependencies.
     *
     * @parameter expression="${project.dependencyArtifacts}"
     * @required
     */
    private Set<Artifact> dependencies;

    /**
     * The bin directory.
     *
     * @parameter default-value="${project.build.sourceDirectory}/Bin"
     * @required
     */
    private File binDir;

    public void execute()
            throws MojoExecutionException, MojoFailureException {
        for (Artifact dependency : dependencies) {
            try {
                String filename = dependency.getArtifactId() + "." + dependency.getArtifactHandler().getExtension();
                File targetFile = new File(binDir, filename);

                if (!targetFile.exists()) {
                    getLog().debug("NPANDAY-000-0001: copy dependency: typeof:" +  dependency.getClass());
                    getLog().debug("NPANDAY-000-0001: copy dependency: " + dependency);
                    getLog().debug("NPANDAY-000-0002: copying " + dependency.getFile().getAbsolutePath() + " to " + targetFile);
                    File sourceFile = PathUtil.getGACFile4Artifact(dependency);

                    FileUtils.copyFile(sourceFile, targetFile);
                    
                }
            }
            catch (IOException ioe) {
                throw new MojoExecutionException("NPANDAY-000-00002: Error copying dependency " + dependency, ioe);
                
            }
        }
    }

    public void setBinDir(File binDir) {
        this.binDir = binDir;
    }

    public void setDependencies(Set<Artifact> dependencies) {
        this.dependencies = dependencies;
    }
}
