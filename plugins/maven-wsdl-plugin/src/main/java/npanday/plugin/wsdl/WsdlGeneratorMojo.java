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
package npanday.plugin.wsdl;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates WSDL class
 *
 * @author Shane Isbell
 * @goal wsdl
 * @phase process-sources
 * @description Generates WSDL class
 */
public class WsdlGeneratorMojo
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
     * The Vendor for the executable. Supports MONO and MICROSOFT.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"  default-value = "2.0.50727"
     */
    private String frameworkVersion;

    /**
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "WSDL"
     */
    private String profile;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * Webreferences
     * 
     * @parameter
     */
    private WebReference[] webreferences;

    /**
     * The directory to place the generated binding classes.
     * 
     * @parameter  expression="${project.build.sourceDirectory}"
     */
    private String outputDirectory;

    /**
     * Paths (or URLs) of the WSDL files.
     * 
     * @parameter expression="${paths}"
     */
    private String[] paths;

    /**
     * Language of the WSDL binding. Default value is CS (CSHARP).
     * 
     * @parameter expression="${language}" default-value="CS"
     */
    private String language;

    /**
     * Turns on type sharing feature. Not supported for MONO.
     * 
     * @parameter expression="${sharetypes}" default-value="false"
     */
    private boolean sharetypes;

    /**
     * Displays extra information when the sharetypes switch is specified. Not supported for MONO.
     * 
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Generates fields instead of properties.
     * 
     * @parameter expression="${fields}" default-value="false"
     */
    private boolean fields;

    /**
     * Generates explicit order identifiers on all particle members. Not supported for MONO.
     * 
     * @parameter expression = "${order}" default-value = "false"
     */
    private boolean order;

    /**
     * Generates the INotifyPropertyChanged interface to enable data binding. Not supported for MONO.
     * 
     * @parameter expression="${enableDataBinding}" default-value="false"
     */
    private boolean enableDataBinding;

    /**
     * Namespace of the WSDL binding. Default value is ${project.groupId}
     * 
     * @parameter expression="${namespace}" default-value="${project.groupId}"
     */
    private String namespace;

    /**
     * Override the default protocol.
     * 
     * @parameter expression="${protocol}"
     */
    private String protocol;

    /**
     * The server to retrieve the WSDL from.
     * 
     * @parameter
     */
    private npanday.plugin.wsdl.Server server;

    /**
     * The proxy server
     * 
     * @parameter
     */
    private npanday.plugin.wsdl.Proxy proxy;

    /**
     * Server values from the settings.xml file.
     * 
     * @parameter expression="${settings.servers}"
     * @required
     */
    private List<Server> servers;

    /**
     * Server values from the settings.xml file.
     * 
     * @parameter expression="${settings.proxies}"
     * @required
     */
    private List<Proxy> proxies;

    /**
     * @parameter expression="${netHome}"
     */
    private File netHome;

    /**
     * Generates server implementation
     * 
     * @parameter expression="${serverInterface}" default-value ="false"
     */
    private boolean serverInterface;

    /**
     * Tells the plugin to ignore options not appropriate to the vendor.
     * 
     * @parameter expresion ="${ignoreUnusedOptions}" default-value="false"
     */
    private boolean ignoreUnusedOptions;

    public void execute()
        throws MojoExecutionException
    {
        SettingsUtil.applyCustomSettings(getLog(), repositoryRegistry, settingsPath );

        FileUtils.mkdir( outputDirectory );

        for ( WebReference webreference : webreferences )
        {
            List<String> commands = getCommandsFor( webreference );
            try
            {
                netExecutableFactory.getNetExecutableFor(
                    new ExecutableRequirement( vendor, null, frameworkVersion, profile ), commands, netHome
                ).execute();
            }
            catch ( ExecutionException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-1300-007: Unable to execute wsdl: Vendor " + vendor + ", frameworkVersion = "
                        + frameworkVersion + ", Profile = " + profile, e
                );
            }
            catch ( PlatformUnsupportedException e )
            {
                throw new MojoExecutionException(
                    "NPANDAY-1300-009: Platform Unsupported: Vendor " + vendor + ", frameworkVersion = "
                        + frameworkVersion + ", Profile = " + profile, e
                );
            }

            getLog().info(
                "NPANDAY-1300-008: Generated WSDL: File = " + buildOutputFilePath( webreference )
            );
        }
    }

    public List<String> getCommandsFor( WebReference webreference )
        throws MojoExecutionException
    {

        List<String> commands = new ArrayList<String>();
        populateServerCommands( commands );
        populateProxyCommands( commands, "/" );
        commands.add( "/language:" + language );
        commands.add( "/namespace:" + webreference.getNamespace() );
        commands.add( "/fields:" + fields );
        if ( !isEmpty( protocol ) )
        {
            commands.add( "/protocol:" + protocol );
        }

        commands.add(
            "/out:" + buildOutputFilePath( webreference )
        );

        commands.add( buildInputFilePath( webreference ) );

        if ( serverInterface )
        {
            commands.add( "/server" );
        }
        if ( enableDataBinding )
        {
            commands.add( "/enableDataBinding" );
        }
        if ( sharetypes )
        {
            commands.add( "/sharetypes" );
        }
        if ( verbose )
        {
            commands.add( "/verbose" );
        }
        if ( order )
        {
            commands.add( "/order" );
        }

        return commands;
    }

    private String buildInputFilePath( WebReference webreference )
    {
        return new File( outputDirectory, webreference.getPath() ).getAbsolutePath();
    }

    private String buildOutputFilePath( WebReference webreference )
    {
        return new File(
            new File( outputDirectory, webreference.getOutput() ),
            getFileNameFor( buildInputFilePath( webreference ) )
        ).getAbsolutePath();
    }

    private boolean isURL( String path )
    {
        try
        {
            new URL( path );
        }
        catch ( MalformedURLException e )
        {
            return false;
        }
        return true;
    }

    private String getFileNameFor( String path )
    {
        String wsdlName;
        if ( isURL( path ) )
        {
            wsdlName = path.substring( path.lastIndexOf( "/" ), path.length() );
        }
        else
        {
            if ( path.contains( "/" ) )
            {
                wsdlName = path.substring( path.lastIndexOf( "/" ), path.length() );
            }
            else if ( path.contains( "\\" ) )
            {
                wsdlName = path.substring( path.lastIndexOf( "\\" ), path.length() );
            }
            else
            {
                wsdlName = path;
            }
        }
        return wsdlName.substring( 0, wsdlName.lastIndexOf( "." ) ) + "." + language.toLowerCase();
    }

    private void populateProxyCommands( List<String> commands, String commandFlag )
        throws MojoExecutionException
    {
        if ( proxy != null )
        {
            Proxy mProxy = getProxyFor( proxy.getId() );
            if ( mProxy != null )
            {
                getLog().debug( "NPANDAY-1300-003: Found proxy: ID = " + mProxy.getId() );
                String username = mProxy.getUsername();
                String password = mProxy.getPassword();
                boolean isHashed = proxy.isHashPassword();
                if ( !isEmpty( password ) && isHashed )
                {
                    String alg = proxy.getHashAlg();
                    if ( isEmpty( alg ) )
                    {
                        alg = "SHA1";
                    }
                    try
                    {
                        password = new String( MessageDigest.getInstance( alg ).digest( password.getBytes() ) );
                    }
                    catch ( NoSuchAlgorithmException e )
                    {
                        throw new MojoExecutionException(
                                                          "NPANDAY-1300-004: No Such Algorithm for hashing the password: "
                                                              + "Algorithm = " + alg, e );
                    }
                }
                String proxyHost = mProxy.getHost();
                int proxyPort = mProxy.getPort();
                String proxyProtocol = mProxy.getProtocol();

                StringBuffer proxyUrl = new StringBuffer();
                if ( !isEmpty( proxyProtocol ) )
                {
                    proxyUrl.append( proxyProtocol ).append( "://" );
                }
                if ( !isEmpty( proxyHost ) )
                {
                    proxyUrl.append( proxyHost );
                }
                if ( proxyPort > 0 )
                {
                    proxyUrl.append( ":" ).append( proxyPort );
                }
                if ( proxyUrl.length() != 0 )
                {
                    commands.add( commandFlag + "proxy:" + proxyUrl.toString() );
                }
                if ( !isEmpty( username ) )
                {
                    commands.add( commandFlag + "proxyusername:" + username );
                }
                if ( !isEmpty( password ) )
                {
                    commands.add( commandFlag + "proxypassword:" + password );
                }
            }
        }
    }

    private void populateServerCommands( List<String> commands )
        throws MojoExecutionException
    {
        if ( server != null )
        {
            Server mServer = getServerFor( server.getId() );
            if ( mServer != null )
            {
                String username = mServer.getUsername();
                String password = mServer.getPassword();
                boolean isHashed = server.isHashPassword();
                if ( !isEmpty( password ) && isHashed )
                {
                    String alg = server.getHashAlg();
                    if ( isEmpty( alg ) )
                    {
                        alg = "SHA1";
                    }
                    try
                    {
                        password = new String( MessageDigest.getInstance( alg ).digest( password.getBytes() ) );
                    }
                    catch ( NoSuchAlgorithmException e )
                    {
                        throw new MojoExecutionException(
                                                          "NPANDAY-1300-005: No Such Algorithm for hashing the password: "
                                                              + "Algorithm = " + alg, e );
                    }
                }
                if ( !isEmpty( username ) )
                {
                    commands.add( "/username:" + username );
                }
                if ( !isEmpty( password ) )
                {
                    commands.add( "/password:" + password );
                }
            }
        }
    }

    private Proxy getProxyFor( String id )
    {
        for ( Proxy proxy : proxies )
        {
            if ( proxy.getId().trim().equals( id.trim() ) )
            {
                return proxy;
            }
        }
        return null;
    }

    private Server getServerFor( String id )
    {
        for ( Server server : servers )
        {
            if ( server.getId().trim().equals( id.trim() ) )
            {
                return server;
            }
        }
        return null;
    }

    private boolean isEmpty( String value )
    {
        return ( value == null || value.trim().equals( "" ) );
    }
}
