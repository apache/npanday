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
package npanday.plugin.customLifecycle;

import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * If an artifact with final name is found, it is added.
 *
 * @author Lars Corneliussen
 * @goal set-artifact
 * @phase package
 */
public class SetArtifactMojo
        extends AbstractMojo
{

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /** @parameter default-value="false" */
    protected boolean skip;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip)  {
            return;
        }

        if (project.getArtifact().getFile() != null) {
            getLog().debug("NPANDAY-105-004: Execution skipped, because the artifact file is already set: "
                    + project.getArtifact().getFile().getAbsolutePath() );
            return;
        }

        String outputDirectory = project.getBuild().getDirectory();
        String finalName = project.getBuild().getFinalName();
        ArtifactType type = ArtifactType.getArtifactTypeForPackagingName(project.getPackaging());

        if (type == ArtifactType.NULL) {
            throw new MojoFailureException("NPANDAY-105-000: Packaging " + project.getPackaging() + " is not supported by this Mojo!");
        }

        File artifact = new File(outputDirectory, finalName + "." + type.getExtension());
        if (!artifact.exists()) {
            getLog().warn("NPANDAY-105-001: Could not set expected artifact to '" + artifact.getAbsolutePath() + "', because it does not exist");
            return;
        }

        getLog().debug("NPANDAY-105-003: Set the artifact file to '" + artifact.getAbsolutePath() + "'.");
        project.getArtifact().setFile(artifact);
    }
}
