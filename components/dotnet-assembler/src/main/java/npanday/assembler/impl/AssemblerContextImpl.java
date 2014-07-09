package npanday.assembler.impl;

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

import npanday.assembler.AssemblerContext;
import npanday.assembler.AssemblyInfo;
import npanday.assembler.AssemblyInfoMarshaller;
import npanday.assembler.AssemblyInfoException;
import npanday.PlatformUnsupportedException;
import npanday.model.assembly.plugins.AssemblyPlugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Organization;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import npanday.registry.RepositoryRegistry;

import java.io.*;

/**
 * Provides an implementation of the <code>AssemblerContext</code>.
 *
 * @author Shane Isbell
 *
 * @plexus.component
 *   role="npanday.assembler.AssemblerContext"
 */
public final class AssemblerContextImpl
    implements AssemblerContext, LogEnabled, Initializable
{

    private static final String SNAPSHOT_SUFFIX = "SNAPSHOT";

    /**
     * A registry component of repository (config) files
     *
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

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

    public AssemblyInfo getAssemblyInfo( MavenProject mavenProject )
    {
        String basedir = mavenProject.getBasedir().toString();
        AssemblyInfo assemblyInfo = new AssemblyInfo();
        String description = ( mavenProject.getDescription() != null ) ? mavenProject.getDescription() : "";
        String version = ( mavenProject.getVersion() != null ) ? mavenProject.getVersion() : "";
        String name = mavenProject.getName();
        Organization org = mavenProject.getOrganization();
        String company = ( org != null ) ? org.getName() : "";
        String copyright = "";
        String informationalVersion = "";
        String configuration = "";

        File file = new File( basedir + "/COPYRIGHT.txt" );
        if ( file.exists() )
        {
            logger.debug( "NPANDAY-020-000: Found Copyright: " + file.getAbsolutePath() );
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( file );
                copyright = IOUtil.toString( fis ).replace( "\r", " " ).replace( "\n", " " ).replace( "\"", "'" );
            }
            catch ( IOException e )
            {
                logger.info( "NPANDAY-020-001: Could not get copyright: File = " + file.getAbsolutePath(), e );
            }
            finally
            {
                if ( fis != null )
                {
                    IOUtil.close( fis );
                }
            }
        }
        informationalVersion = version;
        if ( version.contains( "-" ) )
        {
            version = version.split( "-" )[0];
        }
        assemblyInfo.setCompany( company );
        assemblyInfo.setCopyright( copyright );
        assemblyInfo.setCulture( "" );
        assemblyInfo.setDescription( description );
        assemblyInfo.setProduct( company + "-" + name );
        assemblyInfo.setTitle( name );
        assemblyInfo.setTrademark( "" );
        assemblyInfo.setInformationalVersion( informationalVersion );
        assemblyInfo.setVersion( version );
        assemblyInfo.setConfiguration( configuration );

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
        AssemblyInfoMarshaller marshaller;
        try
        {
            Class cc = Class.forName( className );
            marshaller = (AssemblyInfoMarshaller) cc.newInstance();
            marshaller.init( plugin );
        }
        catch ( ClassNotFoundException e )
        {
            throw new AssemblyInfoException(
                "NPANDAY-020-002: Unable to create AssemblyInfoMarshaller: Class Name = " + className, e );
        }
        catch ( InstantiationException e )
        {
            throw new AssemblyInfoException(
                "NPANDAY-020-003: Unable to create AssemblyInfoMarshaller: Class Name = " + className, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new AssemblyInfoException(
                "NPANDAY-020-004: Unable to create AssemblyInfoMarshaller: Class Name = " + className, e );
        }

        return marshaller;
    }

    /**
     * @see AssemblerContext#getClassExtensionFor(String)
     */
    public String getClassExtensionFor( String language )
        throws PlatformUnsupportedException
    {
        try
        {
            return repository.getAssemblyPluginFor( language ).getExtension();
        }
        catch ( AssemblyInfoException e )
        {
            throw new PlatformUnsupportedException( "NPANDAY-020-006: Language not supported: Language = " + language,
                                                    e );
        }
    }

    public void initialize() throws InitializationException {
        repository = (AssemblyPluginsRepository) repositoryRegistry.find( "assembly-plugins" );
        if ( repository == null )
        {
            throw new InitializationException( "NPANDAY-020-005: Unable to find the assembly-plugins.xml file" );
        }
    }
}
