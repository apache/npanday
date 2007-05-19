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
package org.apache.maven.dotnet.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.dotnet.vendor.VendorInfo;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.NetExecutableFactory;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.HashSet;

/**
 * The base class for plugins that execute a .NET plugin. Classes that extend this class are only expected to provide
 * information (through the abstract methods) to this base class implementation.
 *
 * @author Shane Isbell
 */
public abstract class AbstractMojo
    extends org.apache.maven.plugin.AbstractMojo
    implements DotNetMojo
{
    /**
     * Executes the mojo.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !preExecute() )
        {
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
        }
        catch ( ParserConfigurationException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );
        }
        Document document = builder.newDocument();

        FileOutputStream fos;
        File paramFile;
        try
        {
            paramFile = File.createTempFile( "Plugin", ".xml" );
            fos = new FileOutputStream( paramFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );
        }

        StreamResult result = new StreamResult();
        result.setOutputStream( fos );
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform( this.getDOMSourceFor( document ), result );
        }
        catch ( TransformerConfigurationException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );

        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NMAVEN-xxx-000: Unable to write file", e );
            }
        }

        try
        {
            VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
            if ( getVendor() != null )
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( getVendor() ) );
            }
            vendorInfo.setFrameworkVersion( getFrameworkVersion() );
            vendorInfo.setVendorVersion( getVendorVersion() );
            getNetExecutableFactory().getPluginLoaderFor( getMojoGroupId(), getMojoArtifactId(), vendorInfo,
                                                          getLocalRepository(), paramFile,
                                                          getClassName() ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );
        }
    }

    private Set<Field> getAnnotatedFieldsFrom( Field[] fields )
    {
        Set<Field> fieldSet = new HashSet<Field>();
        for ( Field field : fields )
        {
            if ( field.getAnnotation( FieldAnnotation.class ) != null )
            {
                fieldSet.add( field );
            }
        }
        return fieldSet;
    }

    public DOMSource getDOMSourceFor( Document document )
    {
        Element root = document.createElement( "configuration" );
        document.appendChild( root );

        Set<Field> fields = getAnnotatedFieldsFrom( this.getClass().getDeclaredFields() );
        for ( Field field : fields )
        {
            ConfigurationAppender configurationAppender = getNetPluginContext().getConfigurationAppenderFor( field );
            try
            {
                try
                {
                    configurationAppender.append( document, root, FieldInfo.Factory.createFieldInfo( field.getName(),
                                                                                                     field.get(
                                                                                                         this ) ) );
                }
                catch ( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
            }
            catch ( MojoExecutionException e )
            {
                e.printStackTrace();
            }
        }
        return new DOMSource( document );
    }

    /**
     * @see org.apache.maven.dotnet.plugin.DotNetMojo#getLocalRepository()
     */
    public abstract String getLocalRepository();

    /**
     * @see DotNetMojo#getMavenProject()
     */
    public abstract MavenProject getMavenProject();

    /**
     * @see DotNetMojo#getNetExecutableFactory()
     */
    public abstract NetExecutableFactory getNetExecutableFactory();

    public abstract PluginContext getNetPluginContext();

    public abstract String getMojoGroupId();

    public abstract String getMojoArtifactId();

    public abstract String getClassName();

    public abstract String getVendor();

    public abstract String getVendorVersion();

    public abstract String getFrameworkVersion();

    /**
     * Override this method for pre-execution commands.
     */
    public boolean preExecute()
        throws MojoExecutionException, MojoFailureException
    {
        return true;
    }

}
