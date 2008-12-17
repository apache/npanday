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
package npanday.plugin.webapp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Deploys a .NET Web Archive
 *
 * @author Shane Isbell
 * @goal deploy
 * @phase deploy
 * @description Deploys a .NET Web Archive
 */
public class NetArchiveDeployerMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Path to deploy .NET Web Archive to.
     *
     * @parameter expression="${deployPath}"
     * @required
     */
    private String deployPath;

    public void execute()
        throws MojoExecutionException
    {
        String outputDirectory = project.getBuild().getDirectory() + File.separator + project.getArtifactId();
        String deployArtifact = deployPath + File.separator + project.getArtifactId();
        try
        {
            FileUtils.copyDirectoryStructure( new File( outputDirectory ), new File( deployArtifact ) );
            getLog().info( "NPANDAY-1201-001: Copied .NET Web Application to deployment directory: " + "From = " +
                outputDirectory + ", To = " + deployArtifact );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-1201-000: Failed to copy .NET Web Application to deployment directory: " + "From = " +
                    outputDirectory + ", To = " + deployArtifact, e );
        }
    }
}
