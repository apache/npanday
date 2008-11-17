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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.PathUtil;
import org.apache.maven.dotnet.artifact.ArtifactContext;
import org.apache.maven.dotnet.artifact.AssemblyResolver;
import org.apache.maven.dotnet.dao.Project;
import org.apache.maven.dotnet.dao.ProjectDao;
import org.apache.maven.dotnet.dao.ProjectDependency;
import org.apache.maven.dotnet.dao.ProjectFactory;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.executable.NetExecutableFactory;
import org.apache.maven.dotnet.registry.DataAccessObjectRegistry;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.vendor.VendorInfo;
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
import java.util.*;

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
    private PlexusContainer container;

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

            Artifact artifact = getNetExecutableFactory().getArtifactFor(getMojoGroupId(), getMojoArtifactId());
            resolveArtifact(artifact);
            getNetExecutableFactory().getPluginLoaderFor( artifact, vendorInfo,
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
        } catch (ComponentLookupException e) {
            throw new MojoExecutionException( "NMAVEN-xxx-000", e );
        }
    }

    private void resolveArtifact(Artifact artifact) throws ComponentLookupException, MojoExecutionException {
        File localRepository = new File(getLocalRepository());
        if (PathUtil.getPrivateApplicationBaseFileFor(artifact, localRepository).exists())
        {
            return;
        }

        ArtifactContext artifactContext = null;
        AssemblyResolver assemblyResolver = null;
        ArtifactFactory artifactFactory = null;
        DataAccessObjectRegistry daoRegistry = null;
        try {
            artifactContext = (ArtifactContext) container.lookup(ArtifactContext.ROLE);
            assemblyResolver = (AssemblyResolver) container.lookup(AssemblyResolver.ROLE);
            artifactFactory = (ArtifactFactory) container.lookup(ArtifactFactory.ROLE);
            daoRegistry = (DataAccessObjectRegistry) container.lookup(DataAccessObjectRegistry.ROLE);

            Dependency dependency = new Dependency();
            dependency.setGroupId(artifact.getGroupId());
            dependency.setArtifactId(artifact.getArtifactId());
            dependency.setVersion(artifact.getVersion());
            dependency.setScope(Artifact.SCOPE_RUNTIME);
            dependency.setType(artifact.getType());

            assemblyResolver.resolveTransitivelyFor(new MavenProject(), Collections.singletonList(dependency), getMavenProject().getRemoteArtifactRepositories(),
                    localRepository, false);

            ProjectDao dao = (ProjectDao) daoRegistry.find( "dao:project" );
            dao.openConnection();
            Project project = dao.getProjectFor(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getVersion(), dependency.getType(),
                    dependency.getClassifier());

            List<Dependency> sourceArtifactDependencies = new ArrayList<Dependency>();
            for (ProjectDependency projectDependency : project.getProjectDependencies()) {
                sourceArtifactDependencies.add(ProjectFactory.createDependencyFrom(projectDependency));
            }
            artifactContext.getArtifactInstaller().installArtifactAndDependenciesIntoPrivateApplicationBase(localRepository, artifact,
                    sourceArtifactDependencies);
            dao.closeConnection();
        }
        catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        finally {
            release(artifactContext);
            release(assemblyResolver);
            release(artifactFactory);
            release(daoRegistry);
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
