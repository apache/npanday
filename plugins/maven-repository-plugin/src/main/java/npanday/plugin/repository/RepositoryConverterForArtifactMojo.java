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
package npanday.plugin.repository;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import npanday.repository.RepositoryConverter;
import npanday.artifact.ApplicationConfig;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;

/**
 * Converts the project's RDF artifact repository format into the default Maven repository format.
 *
 * @goal convert-artifact
 * @description Converts the project's RDF artifact repository format into the default Maven repository format. 
 */
public class RepositoryConverterForArtifactMojo
    extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @component
     */
    private RepositoryConverter repositoryConverter;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }
        
        org.openrdf.repository.Repository rdfRepository = new SailRepository( new MemoryStore( localRepository ) );
        try
        {
            rdfRepository.initialize();
        }
        catch ( RepositoryException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        ApplicationConfig config = ApplicationConfig.Factory.createDefaultApplicationConfig( project.getArtifact(),
                                                                                             project.getBasedir(),
                                                                                             new File(
                                                                                                 project.getBuild().getDirectory() ) );
        try
        {
            repositoryConverter.convertRepositoryFormatFor( project.getArtifact(), config, localRepository );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }
    }
}
