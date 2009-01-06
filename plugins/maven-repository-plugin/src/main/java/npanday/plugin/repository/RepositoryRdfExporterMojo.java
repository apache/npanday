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
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Exports the RDF repository into the RDF/XML format
 *
 * @goal export-rdf
 * @description Exports the RDF repository into the RDF/XML format.
 */
public class RepositoryRdfExporterMojo
    extends AbstractMojo
{
    /**
     * The local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File dataDir = new File( localRepository.getParentFile(), "/uac/rdfRepository" );
        org.openrdf.repository.Repository rdfRepository = new SailRepository( new MemoryStore( dataDir ) );
        try
        {
            rdfRepository.initialize();
        }
        catch ( RepositoryException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        RDFHandler rdfxmlWriter;
        try
        {
            File exportFile = new File( localRepository.getParentFile(), "/uac/rdfRepository/rdf-repository-export.xml" );
            rdfxmlWriter = new RDFXMLWriter( new FileOutputStream( exportFile ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }

        try
        {
            RepositoryConnection repositoryConnection = rdfRepository.getConnection();
            repositoryConnection.export( rdfxmlWriter );
        }
        catch ( RepositoryException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }
        catch ( RDFHandlerException e )
        {
            throw new MojoExecutionException( e.getMessage() );
        }
    }
}
