package org.apache.maven.dotnet.plugin.aspx;

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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Maven Mojo for copying ASPx project dependencies to sourceDirectory\Bin folder
 * 
 * @goal copy-dependency
 * @phase process-sources
 * @description Maven Mojo for copying ASPx project dependencies to sourceDirectory\Bin folder
 */
public class AspxBinDependencyResolver
    extends AbstractMojo
{
    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Set<Artifact> dependencies = project.getDependencyArtifacts();

        // project.getDependencyArtifacts();
        String binDir = project.getBuild().getSourceDirectory() + File.separator + "Bin";

        for ( Artifact dependency : dependencies )
        {
            try
            {
                getLog().info( "NMAVEN-000-0000: copying " + dependency.getFile().getAbsolutePath() + " , to " + binDir );

                FileUtils.copyFileToDirectory( dependency.getFile().getAbsolutePath(), binDir );
            }
            catch ( IOException ioe )
            {
                throw new MojoExecutionException( "NMAVEN-000-0000: Error copying dependency " + dependency, ioe );
            }
        }

    }

}
