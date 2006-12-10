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
package org.apache.maven.dotnet.assembler.impl;

import org.apache.maven.dotnet.assembler.AssemblerContext;
import org.apache.maven.dotnet.assembler.AssemblyInfo;
import org.apache.maven.dotnet.assembler.AssemblyInfoMarshaller;
import org.apache.maven.dotnet.assembler.AssemblyInfoException;
import org.apache.maven.dotnet.InitializationException;
import org.apache.maven.dotnet.model.assembly.plugins.AssemblyPlugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Organization;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.apache.maven.dotnet.registry.RepositoryRegistry;

import java.io.*;


/**
 * Provides an implementation of the <code>AssemblerContext</code>.
 *
 * @author Shane Isbell
 */
public final class AssemblerContextImpl
    implements AssemblerContext, LogEnabled
{

    /**
     * A registry component of repository (config) files
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The maven project
     */
    private MavenProject mavenProject;

    /**
     * A logger for writing log messages
     */
    private Logger logger;

    /**
     * The assembly plugins repository used for accessing assembly plugin information, which in turn is used for
     * generating an <code>AssemblyInfo</code> object.
     */
    private AssemblyPluginsRepository repository;

    /**
     * Constructor. This method is intended to by invoked by the plexus-container, not by the application developer.
     */
    public AssemblerContextImpl()
    {
    }

    /**
     * @see LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    /**
     * @see org.apache.maven.dotnet.assembler.AssemblerContext#getAssemblyInfo()
     */
    public AssemblyInfo getAssemblyInfo()
    {
        String basedir = mavenProject.getBasedir().toString();
        AssemblyInfo assemblyInfo = new AssemblyInfo();
        String description = mavenProject.getDescription();
        String version = mavenProject.getVersion();
        String name = mavenProject.getName();
        Organization org = mavenProject.getOrganization();
        String company = ( org != null ) ? org.getName() : "";
        String copyright = "";
        File file = new File( basedir + "/COPYRIGHT.txt" );
        if ( file.exists() )
        {
            logger.debug( "NMAVEN-020-000: Found Copyright: " + file.getAbsolutePath() );
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( file );
                copyright = IOUtil.toString( fis ).replace( "\r", "" ).replace( "\n", "" ).replace( "\"", "\\" );
            }
            catch ( IOException e )
            {
                logger.info( "NMAVEN-020-001: Could not get copyright: File = " + file.getAbsolutePath(), e );
            }
            finally
            {
                if ( fis != null )
                {
                    IOUtil.close( fis );
                }
            }
        }

        assemblyInfo.setCompany( company );
        assemblyInfo.setCopyright( copyright );
        assemblyInfo.setCulture( "" );
        assemblyInfo.setDescription( description );
        assemblyInfo.setProduct( company + "-" + name );
        assemblyInfo.setTitle( name );
        assemblyInfo.setTrademark( "" );
        assemblyInfo.setVersion( version );
        assemblyInfo.setConfiguration( "" );

        return assemblyInfo;
    }

    /**
     * @see AssemblerContext#getAssemblyInfoMarshallerFor(String)
     */
    public AssemblyInfoMarshaller getAssemblyInfoMarshallerFor( String language )
        throws AssemblyInfoException
    {
        AssemblyPlugin plugin = repository.getAssemblyPluginFor( language );
        String className = plugin.getPluginClass();
        AssemblyInfoMarshaller marshaller = null;
        try
        {
            Class cc = Class.forName( className );
            marshaller = (AssemblyInfoMarshaller) cc.newInstance();
            marshaller.init( plugin );
        }
        catch ( ClassNotFoundException e )
        {
            throw new AssemblyInfoException(
                "NMAVEN-020-002: Unable to create AssemblyInfoMarshaller: Class Name = " + className, e );
        }
        catch ( InstantiationException e )
        {
            throw new AssemblyInfoException(
                "NMAVEN-020-003: Unable to create AssemblyInfoMarshaller: Class Name = " + className, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new AssemblyInfoException(
                "NMAVEN-020-004: Unable to create AssemblyInfoMarshaller: Class Name = " + className, e );
        }

        return marshaller;
    }

    /**
     * @see AssemblerContext#init(org.apache.maven.project.MavenProject)
     */
    public void init( MavenProject mavenProject )
        throws InitializationException
    {
        this.mavenProject = mavenProject;
        repository = (AssemblyPluginsRepository) repositoryRegistry.find( "assembly-plugins" );
        if ( repository == null )
        {
            throw new InitializationException( "NMAVEN-020-005: Unable to find the assembly-plugins.xml file" );
        }
    }
}
