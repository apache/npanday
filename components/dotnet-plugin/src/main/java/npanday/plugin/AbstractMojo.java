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
package npanday.plugin;

import npanday.PathUtil;
import npanday.PlatformUnsupportedException;
import npanday.artifact.ArtifactContext;
import npanday.artifact.AssemblyResolver;
import npanday.artifact.NPandayArtifactResolutionException;
import npanday.executable.ExecutionException;
import npanday.executable.NetExecutableFactory;
import npanday.vendor.VendorFactory;
import npanday.vendor.VendorInfo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );
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
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );
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
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );

        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "NPANDAY-xxx-000: Unable to write file", e );
            }
        }

        MavenProject project = getMavenProject();
        // TODO: should be configurable, but relies on it being passed into everywhere
        File targetDir = PathUtil.getPrivateApplicationBaseDirectory( project );

        ArtifactContext artifactContext = null;
        try
        {
            VendorInfo vendorInfo = VendorInfo.Factory.createDefaultVendorInfo();
            if ( getVendor() != null )
            {
                vendorInfo.setVendor( VendorFactory.createVendorFromName( getVendor() ) );
            }
            vendorInfo.setFrameworkVersion( getFrameworkVersion() );
            vendorInfo.setVendorVersion( getVendorVersion() );

            String localRepository = getLocalRepository();

            artifactContext = (ArtifactContext) container.lookup(ArtifactContext.ROLE);
            artifactContext.init( project, project.getRemoteArtifactRepositories(), new File( localRepository ) );

            Artifact artifact = getNetExecutableFactory().getArtifactFor(getMojoGroupId(), getMojoArtifactId());
            resolveArtifact(project, artifact, targetDir);
            getNetExecutableFactory().getPluginLoaderFor( artifact, vendorInfo, localRepository, paramFile,
                                                          getClassName(), targetDir ).execute();
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );
        } catch (ComponentLookupException e) {
            throw new MojoExecutionException( "NPANDAY-xxx-000", e );
        }
        finally
        {
            release(artifactContext);
        }

        postExecute();
    }

    private void resolveArtifact( MavenProject project, Artifact artifact, File targetDir )
        throws ComponentLookupException, MojoExecutionException
    {
        File localRepository = new File(getLocalRepository());
        
        if (PathUtil.getPrivateApplicationBaseFileFor(artifact, localRepository, targetDir ).exists())
        {
            return;
        }

        AssemblyResolver assemblyResolver = null;
        try {
            assemblyResolver = (AssemblyResolver) container.lookup(AssemblyResolver.ROLE);

            Dependency dependency = new Dependency();
            dependency.setGroupId(artifact.getGroupId());
            dependency.setArtifactId(artifact.getArtifactId());
            dependency.setVersion(artifact.getVersion());
            dependency.setScope(Artifact.SCOPE_RUNTIME);
            dependency.setType(artifact.getType());

            try
            {
                assemblyResolver.resolveTransitivelyFor( project, Collections.singletonList( dependency ),
                                                         project.getRemoteArtifactRepositories(), localRepository,
                                                         false );
            }
            catch( NPandayArtifactResolutionException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        finally {
            release(assemblyResolver);
        }
    }

    private void release(Object component) {
        try {
            if (component != null) {
                container.release(component);
            }
        } catch (ComponentLifecycleException e) {
            // ignore
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
