package org.apache.npanday.plugins.silverlight;

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

import npanday.ArtifactType;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Package a Silverlight class library or application
 *
 * @goal package
 * @phase package
 */
public class SilverlightPackageMojo
    extends AbstractMojo
{

    /**
     * Where the package has been output to by MSBuild
     *
     * @parameter default-value="Bin/Debug"
     * @required
     */
    private File outputDirectory;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // TODO: currently relies on MSBuild having been run, but we can just call it here using the tools in dotnet-msbuild

        ArtifactType type = ArtifactType.getArtifactTypeForPackagingName( project.getPackaging() );

        File artifact = new File( outputDirectory, project.getArtifactId() + "." + type.getExtension() );
        if ( !artifact.exists() )
        {
            throw new MojoExecutionException( "The artifact to attach was not found: " + artifact );
        }

        getLog().debug( "Set the artifact file to '" + artifact.getAbsolutePath() + "'." );
        project.getArtifact().setFile( artifact );
    }
}
