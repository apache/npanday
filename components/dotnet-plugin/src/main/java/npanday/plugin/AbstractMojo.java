package npanday.plugin;

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
import npanday.LocalRepositoryUtil;
import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutableFactory;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * The base class for plugins that execute a .NET plugin. Classes that extend this class are only expected to provide
 * information (through the abstract methods) to this base class implementation.
 *
 * @author Shane Isbell
 */
public abstract class AbstractMojo
    extends org.apache.maven.plugin.AbstractMojo
    implements DotNetMojo, Contextualizable
{
    protected PlexusContainer container;

    public void contextualize(Context context) throws ContextException {
        container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }

    // TODO: get npandayVersion injected somehow
    private String npandayVersion = "1.5.0-incubating-SNAPSHOT";

    // TODO: get the version of the actual plugin to run; this can be external to NPanday!!!
    private String pluginVersion = "1.5.0-incubating-SNAPSHOT";

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
            throw new MojoExecutionException( "NPANDAY-115-000: Unable to create document builder", e );
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
            throw new MojoExecutionException(
                "NPANDAY-115-001: IO error on creating or accessing Plugin.xml temp file", e
            );
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
            throw new MojoExecutionException( "NPANDAY-115-002", e );

        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( "NPANDAY-115-003", e );
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NPANDAY-115-004: Unable to write file "
                                                      + paramFile.getAbsolutePath(), e );
            }
        }

        MavenProject project = getMavenProject();

        // TODO: should be configurable, but relies on it being passed into everywhere
        File targetDir = PathUtil.getPrivateApplicationBaseDirectory( project );

        VendorRequirement vendorRequirement = new VendorRequirement( getVendor(), getVendorVersion(), getFrameworkVersion());

        try
        {
            ArtifactRepository localRepository = LocalRepositoryUtil.create( getLocalRepository() );

            Artifact artifact = getArtifactFactory().createDependencyArtifact(
                getMojoGroupId(),
                getMojoArtifactId(),
                VersionRange.createFromVersion( pluginVersion ),
                ArtifactType.DOTNET_MAVEN_PLUGIN.getPackagingType(),
                null,
                "runtime"
            );

            getNetExecutableFactory().getPluginExecutable(
                project, artifact, vendorRequirement, localRepository, paramFile, getClassName(), targetDir,
                npandayVersion
            ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-115-005: Vendor configuration seems to be unsupported: "
                                                  + vendorRequirement, e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-115-006: Error occurred while running the .NET plugin", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoFailureException( "NPANDAY-115-010: Error on resolving plugin artifact(s)", e );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoFailureException( "NPANDAY-115-011: Error on resolving plugin artifact(s)", e );
        }

        postExecute();
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
     * @see npanday.plugin.DotNetMojo#getLocalRepository()
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

    public abstract ArtifactFactory getArtifactFactory();

    /**
     * Override this method for pre-execution commands.
     */
    public boolean preExecute()
        throws MojoExecutionException, MojoFailureException
    {
        return true;
    }
    
    public void postExecute()
        throws MojoExecutionException, MojoFailureException
    {

    }

}
