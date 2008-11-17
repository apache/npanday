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
package org.apache.maven.dotnet.plugin.embedder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.NetDependenciesRepository;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.vendor.VendorUnsupportedException;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.registry.RepositoryRegistry;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;

/**
 * Start the Maven Embedder SOAP service.
 *
 * @goal start
 * @requiresProject false
 * @requiresDirectInvocation true
 * @description Starts the Maven Embedder SOAP service
 */
public class EmbedderStarterMojo
    extends AbstractMojo
{
    private static final String COULD_NOT_CONNECT = "Could not open a connection to: http://localhost:9191/dotnet-service-embedder:";

	/**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * @component
     */
    private AssemblyResolver assemblyResolver;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private File localRepository;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * @component
     */
    private ArtifactMetadataSource metadata;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${port}" default-value="9191"
     */
    private int port;

    /**
     * @parameter expression = "${warFile}"
     * @required
     */
    private File warFile;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * Provides access to configuration information used by NMaven.
     *
     * @component
     */
    private org.apache.maven.dotnet.NMavenRepositoryRegistry nmavenRegistry;

    /**
     * File logger: needed for creating logs when the IDE starts because the console output and thrown exceptions are
     * not available
     */
    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File logs = new File(System.getProperty( "user.home" ) + "\\.m2\\embedder-logs");
        if(!logs.exists())
        {
            logs.mkdir();
        }

        try
        {
            logger.addHandler( new FileHandler(
                System.getProperty( "user.home" ) + "\\.m2\\embedder-logs\\nmaven-embedder-log.xml" ) );
        }
        catch ( IOException e )
        {
            FileOutputStream errorStream;
            try
            {
            errorStream =
                new FileOutputStream( System.getProperty( "user.home" ) + "\\.m2\\embedder-logs\\error.txt" );
                String command = e.getMessage();
                errorStream.write( command.getBytes() );
            }
            catch ( IOException ex )
            {

            }
            e.printStackTrace();
        }

        if ( localRepository == null )
        {
            localRepository = new File( System.getProperty( "user.home" ), ".m2/repository" );
        }
        logger.info( "NMAVEN: Found local repository: Path =  " + localRepository );

        ArtifactRepository localArtifactRepository =
            new DefaultArtifactRepository( "local", "file://" + localRepository, new DefaultRepositoryLayout() );


        Set<Artifact> artifactDependencies = new HashSet<Artifact>();
        Artifact artifact = artifactFactory.createDependencyArtifact( "org.mortbay.jetty", "jetty-embedded",
                                                                      VersionRange.createFromVersion( "6.1.5" ), "jar",
                                                                      null, "runtime", null );
        logger.info( "NMAVEN-000-000: Dependency: Type  = " + artifact.getType() + ", Artifact ID = " +
            artifact.getArtifactId() );
        artifactDependencies.add( artifact );

        ArtifactResolutionResult result;
        try
        {
            result = resolver.resolveTransitively( artifactDependencies, project.getArtifact(), localArtifactRepository,
                                                   project.getRemoteArtifactRepositories(), metadata, null );
        }
        catch ( ArtifactResolutionException e )
        {
            logger.severe( "NMAVEN:" + e.getMessage() );
            throw new MojoExecutionException( "", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            logger.severe( "NMAVEN:" + e.getMessage() );
            throw new MojoExecutionException( "", e );
        }

        String classPath = artifactsToClassPath( result.getArtifacts() );

        List<String> commands = new ArrayList<String>();
        commands.add( "-Dport=" + String.valueOf( port ) );
        commands.add( "-DwarFile=" + warFile.getAbsolutePath() );
        commands.add( "-classpath" );
        commands.add( classPath );
        commands.add( "org.apache.maven.dotnet.jetty.JettyStarter" );
        logger.info( commands.toString() );
        FileOutputStream commandFile = null;
        try
        {
            //For logging purposes
            commandFile =
                new FileOutputStream( System.getProperty( "user.home" ) + "\\.m2\\embedder-logs\\command.txt" );
            String command = "java  -classpath " + classPath + " -Dport=" +
                port + " -DwarFile=\"" + warFile.getAbsolutePath() + "\" org.apache.maven.dotnet.jetty.JettyStarter";
            commandFile.write( command.getBytes() );
        }
        catch ( IOException e )
        {

        }
        finally
        {
            if ( commandFile != null )
            {
                try
                {
                    commandFile.close();
                }
                catch ( IOException e )
                {

                }
            }
        }

        VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
        if ( vendor != null )
        {
            try
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( vendor ) );
            }
            catch ( VendorUnsupportedException e )
            {
                throw new MojoExecutionException( "", e );
            }
        }
        vendorInfo.setFrameworkVersion( frameworkVersion );
        vendorInfo.setVendorVersion( vendorVersion );
        try
        {

            Runnable executable =
                (Runnable) netExecutableFactory.getJavaExecutableFromRepository( vendorInfo, commands );
            Thread thread = new Thread( executable );
            thread.start();
        }
        catch ( PlatformUnsupportedException e )
        {
            logger.severe( "NMAVEN-1400-001: Platform Unsupported: Vendor " + ", frameworkVersion = " +
                frameworkVersion + ", Message =" + e );
            throw new MojoExecutionException(
                "NMAVEN-1400-001: Platform Unsupported: Vendor " + ", frameworkVersion = " + frameworkVersion, e );
        }

        URL embedderUrl = null;
        try
        {
            embedderUrl = new URL( "http://localhost:9191/dotnet-service-embedder" );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }
        
        boolean isDotnetServiceEmbedderUp = false;

        for ( int i = 0; i < 3; i++ )
        {
            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            URLConnection connection;
            try
            {
                connection = embedderUrl.openConnection();
                
                connection.getInputStream();
                
                isDotnetServiceEmbedderUp = true;
            }
            catch ( IOException e )
            {
                logger.severe( COULD_NOT_CONNECT );
            }
        }
        
        if ( isDotnetServiceEmbedderUp == false )
        {
        	throw new MojoFailureException( COULD_NOT_CONNECT );
        }
    }

    private String artifactsToClassPath( Set<Artifact> artifacts ) throws MojoExecutionException
    {
        RepositoryRegistry repositoryRegistry;
        try
        {
            repositoryRegistry = nmavenRegistry.createRepositoryRegistry();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "NMAVEN-1400-002: Failed to create the repository registry for this plugin", e );
        }

        NetDependenciesRepository repository =
            (NetDependenciesRepository) repositoryRegistry.find( "net-dependencies" );
        String pomVersion = repository.getProperty( "nmaven.version");

        StringBuffer sb = new StringBuffer();
        for ( Artifact artifact : artifacts )
        {
            sb.append( "\"" ).append( artifact.getFile().getAbsolutePath() ).append( "\"" ).append( ";" );
        }

        File starterFile = new File( localRepository, "org\\apache\\maven\\dotnet\\dotnet-jetty\\" + pomVersion +
            "\\dotnet-jetty-" + pomVersion + ".jar" );
        sb.append( "\"" ).append( starterFile.getAbsolutePath() ).append( "\"" );
        return sb.toString();
    }
}
/*
  ~ Copyright 2005 Exist Global
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
*/
