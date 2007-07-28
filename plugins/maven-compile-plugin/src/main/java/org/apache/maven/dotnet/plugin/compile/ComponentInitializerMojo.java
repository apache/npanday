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
package org.apache.maven.dotnet.plugin.compile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.apache.maven.dotnet.InitializationException;
import org.apache.maven.dotnet.assembler.AssemblerContext;
import org.apache.maven.dotnet.artifact.AssemblyResolver;


import java.io.File;

/**
 * This class initializes and validates the setup.
 *
 * @author Shane Isbell
 * @goal initialize
 * @phase compile
 */
public class ComponentInitializerMojo
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
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    private File pomFile;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.CapabilityMatcher capabilityMatcher;

    /**
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    /**
     * @component
     */
    private org.apache.maven.dotnet.registry.DataAccessObjectRegistry daoRegistry;

    /**
     * @component
     */
    private AssemblerContext assemblerContext;

    public void execute()
        throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }

        try
        {
            assemblyResolver.resolveTransitivelyFor( project, project.getDependencies(),
                                                     project.getRemoteArtifactRepositories(), localRepository,
                                                     true );
        }
        catch ( java.io.IOException e )
        {
            throw new MojoExecutionException(e.getMessage());
        }

        try
        {
            assemblerContext.init( project );
        }
        catch ( InitializationException e )
        {
            throw new MojoExecutionException( "NMAVEN-901-002: Failed to initialize the assembler context" );
        }

        long endTime = System.currentTimeMillis();
        getLog().info( "Mojo Execution Time = " + ( endTime - startTime ) );
    }
}
