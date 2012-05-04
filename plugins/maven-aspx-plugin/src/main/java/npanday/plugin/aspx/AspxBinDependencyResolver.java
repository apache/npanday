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

package npanday.plugin.aspx;

import npanday.LocalRepositoryUtil;
import npanday.PathUtil;
import npanday.resolver.NPandayDependencyResolution;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;


/**
 * Maven Mojo for copying ASPx project dependencies to sourceDirectory\Bin folder
 *
 * @goal copy-dependency
 * @phase process-sources
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

    /**
     * @component
     */
    private NPandayDependencyResolution dependencyResolution;

    /**
     * The scope up to which dependencies should be included.
     *
     * @parameter default-value="runtime"
     */
    private String scope;

    /**
     * The location of the local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    private void resolveDependencies() throws MojoExecutionException
    {
        try
        {
            dependencyResolution.require( project, LocalRepositoryUtil.create( localRepository ), scope );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-157-003: Could not satisfy required dependencies of scope " + scope, e
            );
        }
    }

    public void execute()
            throws MojoExecutionException, MojoFailureException {

        resolveDependencies();

        ScopeArtifactFilter filter = new ScopeArtifactFilter( scope );

        for (Artifact dependency : dependencies) {

            if (!filter.include( dependency )){
                continue;
            }

            try {
                File targetFile = new File(binDir, PathUtil.getPlainArtifactFileName(dependency));
                if (!targetFile.exists()) {
                    getLog().debug("NPANDAY-157-001: copy dependency " +  dependency + " to " + targetFile);
                    FileUtils.copyFile(dependency.getFile(), targetFile);
                }
            }
            catch (IOException ioe) {
                throw new MojoExecutionException("NPANDAY-157-002: Error copying dependency " + dependency, ioe);
                
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
