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

 package NPanday.Plugin.Settings;

import npanday.artifact.ArtifactContext;
import npanday.registry.RepositoryRegistry;
import npanday.registry.impl.StandardRepositoryLoader;
import npanday.vendor.impl.SettingsRepository;
import npanday.plugin.FieldAnnotation;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileReader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.io.*;

/**
 * @phase validate
 * @goal generate-settings
 */
public class SettingsGeneratorMojo
    extends npanday.plugin.AbstractMojo
{

    /**
     * @parameter expression = "${project}"
     */
    private org.apache.maven.project.MavenProject project;

    /**
     * @parameter expression = "${settings.localRepository}"
     */
    private String localRepository;

    /**
     * @parameter expression = "${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @parameter default-value="false"
     */
    private boolean skip;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @component
     */
    private npanday.plugin.PluginContext pluginContext;

    /**
     * @parameter expression ="${npanday.settings}"
     */
    private String settingsPath;

    /**
     * @parameter expression = "${npanday.settings}"
     */
    @FieldAnnotation()
    public java.lang.String npandaySettingsPath;

    public String getMojoArtifactId()
    {
        return "NPanday.Plugin.Settings";
    }

    public String getMojoGroupId()
    {
        return "npanday.plugin";
    }

    public String getClassName()
    {
        return "NPanday.Plugin.Settings.SettingsGeneratorMojo";
    }

    public npanday.plugin.PluginContext getNetPluginContext()
    {
        return pluginContext;
    }

    public npanday.executable.NetExecutableFactory getNetExecutableFactory()
    {
        return netExecutableFactory;
    }

    public org.apache.maven.project.MavenProject getMavenProject()
    {
        return project;
    }

    public String getLocalRepository()
    {
        return localRepository;
    }

    public String getVendorVersion()
    {
        return vendorVersion;
    }

    public String getVendor()
    {
        return vendor;
    }

    public String getFrameworkVersion()
    {
        return frameworkVersion;
    }

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    public boolean preExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !System.getProperty( "os.name" ).contains( "Windows" ) )
        {
            return false;
        }

        if ( skip )
        {
            return false;
        }

        Plugin compilePlugin = lookupCompilePlugin();

        if ( compilePlugin != null )
        {
            frameworkVersion = getProjectFrameworkVersion( (Xpp3Dom) compilePlugin.getConfiguration() );
            try
            {
                if ( isFrameworkVersionExisting( frameworkVersion ) )
                {
                    return false;
                }
            }
            catch ( IOException e )
            {
                return false;
            }
        }

        return true;
    }

    public boolean isFrameworkVersionExisting(String frameworkVersion)
        throws MojoExecutionException, IOException
    {

        if ( settingsPath == null )
        {
            settingsPath = System.getProperty( "user.home" ) + "/.m2";
        }

        File file = new File( settingsPath, "npanday-settings.xml" );

        if ( !file.exists() )
        {
            return false;
        }

        SettingsRepository settingsRepository = ( SettingsRepository) repositoryRegistry.find( "npanday-settings" );

        if ( settingsRepository != null )
        {
            repositoryRegistry.removeRepository( "npanday-settings" );
        }

        try
        {
            // load npanday-settings and store in registry
            StandardRepositoryLoader repoLoader = new StandardRepositoryLoader();
            repoLoader.setRepositoryRegistry( repositoryRegistry );
            settingsRepository = (SettingsRepository) repoLoader.loadRepository( file.getAbsolutePath(), SettingsRepository.class.getName(), new Hashtable() );
            repositoryRegistry.addRepository( "npanday-settings", settingsRepository );

            // check if npanday-settings contains the framework
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse( file );
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName( "frameworkVersion" );
            for ( int i = 0; i < nodeList.getLength(); i++ )
            {
                Node currentNode = nodeList.item( i );

                if ( frameworkVersion.equals( currentNode.getFirstChild().getNodeValue() ) )
                {
                    return true;
                }
            }
        }
        catch ( Exception e )
        {
            throw new IOException( "Error opening/parsing settings.xml", e );
        }

        return false;
    }

    private Plugin lookupCompilePlugin()
    {

        List plugins = project.getBuildPlugins();

        if ( plugins != null )
        {
            for ( Iterator iterator = plugins.iterator(); iterator.hasNext(); )
            {
                Plugin plugin = (Plugin) iterator.next();
                if ( "npanday.plugin:maven-compile-plugin".equalsIgnoreCase( plugin.getKey() ) );
                {
                   return plugin;
                }
            }
        }
        return null;
    }

    private String getProjectFrameworkVersion( Xpp3Dom config )
    {
       if ( config != null && config.getChildCount() > 0 )
       {
           final Xpp3Dom subelement = config.getChild( "frameworkVersion" );

           if ( subelement != null )
           {
               return subelement.getValue();
           }
       }
       return null;
    }

    public void postExecute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            RepositoryRegistry repositoryRegistry = (RepositoryRegistry) container.lookup( RepositoryRegistry.ROLE );
            SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
            if ( settingsRepository != null )
            {
                settingsRepository.reload();
            }
        }
        catch ( ComponentLookupException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

    }
}
