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
package org.apache.maven.dotnet.dao;

import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.dotnet.ArtifactType;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.FileUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

/**
 * Class for creating Project/ProjectDependency objects.
 */
public final class ProjectFactory
{
    private static final Logger logger = Logger.getAnonymousLogger();

    /**
     * Creates model from the specified project.
     *
     * @param project the project to create the model from.
     * @return model created from the specified project
     * @throws IOException if there was a problem creating the model
     */
    public static Model createModelFrom( Project project )
        throws IOException
    {
        Model model = new Model();
        model.setGroupId( project.getGroupId() );
        model.setArtifactId( project.getArtifactId() );
        model.setVersion( project.getVersion() );
        model.setPackaging( project.getArtifactType() );

        List<Dependency> dependencies = new ArrayList<Dependency>();
        for ( ProjectDependency projectDependency : project.getProjectDependencies() )
        {
            Dependency dependency = new Dependency();
            dependency.setGroupId( projectDependency.getGroupId() );
            dependency.setArtifactId( projectDependency.getArtifactId() );
            dependency.setVersion( projectDependency.getVersion() );
            dependency.setType( projectDependency.getArtifactType() );
            dependency.setClassifier( projectDependency.getPublicKeyTokenId() );
            dependencies.add( dependency );
        }
        model.setDependencies( dependencies );
        return model;

    }

    /**
     * Creates a project from the specified model
     *
     * @param model            the project object model used to create the project.
     * @param pomFileDirectory the directory containing the pom.xml (model).
     * @return a project created from the specified model
     * @throws IOException if there was a problem creating the project
     */
    public static Project createProjectFrom( Model model, File pomFileDirectory )
        throws IOException
    {
        Project project = new Project();
        project.setGroupId( model.getGroupId() );
        project.setArtifactId( model.getArtifactId() );
        project.setVersion( model.getVersion() );
        project.setArtifactType( model.getPackaging() );
        Parent parent = model.getParent();
        if ( parent != null && pomFileDirectory != null )
        {
            String parentPomName = FileUtils.filename( parent.getRelativePath() );
            File parentPomFile = new File( pomFileDirectory, parentPomName );
            FileReader fileReader = new FileReader( parentPomFile );

            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model parentModel;
            try
            {
                parentModel = reader.read( fileReader );
            }
            catch ( XmlPullParserException e )
            {
                throw new IOException( "NMAVEN-180-000: Unable to read model: Message = " + e.getMessage() );

            }
            Project parentProject = createProjectFrom( parentModel, parentPomFile.getParentFile() );
            project.setParentProject( parentProject );
        }

        //TODO: publickey/classifier
        List<Dependency> sourceArtifactDependencies = model.getDependencies();
        for ( Dependency dependency : sourceArtifactDependencies )
        {
            project.addProjectDependency( createProjectDependencyFrom( dependency ) );
        }
        return project;
    }

    public static ProjectDependency createProjectDependencyFrom( Dependency dependency )
    {
        ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId( dependency.getGroupId() );
        projectDependency.setArtifactId( dependency.getArtifactId() );
        projectDependency.setVersion( dependency.getVersion() );
        projectDependency.setPublicKeyTokenId( dependency.getClassifier() );
        projectDependency.setArtifactType( dependency.getType() );

        logAndVerifyProjectParameters(projectDependency);

        return projectDependency;
    }

    public static Dependency createDependencyFrom( ProjectDependency projectDependency )
    {
        logAndVerifyProjectParameters(projectDependency);

        Dependency dependency = new Dependency();
        dependency.setGroupId( projectDependency.getGroupId() );
        dependency.setArtifactId( projectDependency.getArtifactId() );
        dependency.setVersion( projectDependency.getVersion() );
        dependency.setType( projectDependency.getArtifactType() );
        dependency.setClassifier( projectDependency.getPublicKeyTokenId() );
        return dependency;
    }

    public static Artifact createArtifactFrom( Project project, ArtifactFactory artifactFactory, File localRepository )
    {
        logAndVerifyProjectParameters(project);

        Artifact assembly = artifactFactory.createArtifactWithClassifier( project.getGroupId(), project.getArtifactId(),
                                                                          project.getVersion(),
                                                                          project.getArtifactType(),
                                                                          project.getPublicKeyTokenId() );

        File artifactFile = ( ( project.getArtifactType().startsWith( "gac" ) ) ) ? new File(
            "C:\\WINDOWS\\assembly\\" + project.getArtifactType() + "\\" + project.getArtifactId() + "\\" +
                project.getVersion() + "__" + project.getPublicKeyTokenId() + "\\" + project.getArtifactId() + ".dll" )
            : new File( localRepository.getParentFile(), "\\uac\\gac_msil\\" + project.getArtifactId() + "\\" +
                project.getVersion() + "__" + project.getGroupId() + "\\" + project.getArtifactId() + "." +
                ArtifactType.getArtifactTypeForPackagingName( project.getArtifactType() ).getExtension() );

        assembly.setFile( artifactFile );
        return assembly;
    }

    public static Artifact createArtifactFrom( ProjectDependency projectDependency, ArtifactFactory artifactFactory )
    {
        logAndVerifyProjectParameters(projectDependency);
        
        String scope = ( projectDependency.getScope() == null ) ? Artifact.SCOPE_COMPILE : projectDependency.getScope();
        Artifact assembly = artifactFactory.createDependencyArtifact( projectDependency.getGroupId(),
                                                                      projectDependency.getArtifactId(),
                                                                      VersionRange.createFromVersion(
                                                                          projectDependency.getVersion() ),
                                                                      projectDependency.getArtifactType(),
                                                                      projectDependency.getPublicKeyTokenId(), scope,
                                                                      null );
        //System.out.println("Scope = " + assembly.getScope() + ", Type = " + assembly.getType() + ", Classifier = " + assembly.getClassifier());
        File artifactFile = ( ( projectDependency.getArtifactType().startsWith( "gac" ) ) ) ? new File(
            "C:\\WINDOWS\\assembly\\" + projectDependency.getArtifactType() + "\\" + projectDependency.getArtifactId() +
                "\\" + projectDependency.getVersion() + "__" + projectDependency.getPublicKeyTokenId() + "\\" +
                projectDependency.getArtifactId() + ".dll" ) : new File( System.getProperty( "user.home" ),
                                                                         "\\.m2\\uac\\gac_msil\\" +
                                                                             projectDependency.getArtifactId() + "\\" +
                                                                             projectDependency.getVersion() + "__" +
                                                                             projectDependency.getGroupId() + "\\" +
                                                                             projectDependency.getArtifactId() + "." +
                                                                             ArtifactType.getArtifactTypeForPackagingName(
                                                                                 projectDependency.getArtifactType() ).getExtension() );

        assembly.setFile( artifactFile );
        return assembly;
    }

    private static boolean logAndVerifyProjectParameters( Project project )
    {
        if ( project.getGroupId() == null )
        {
            logger.warning( "NMAVEN-180-001: Project Group ID is missing" );
            return false;
        }
        if ( project.getArtifactId() == null )
        {
            logger.warning(
                "NMAVEN-180-002: Project Artifact ID is missing: Group Id = " + project.getGroupId() );
            return false;
        }
        if ( project.getVersion() == null )
        {
            logger.warning( "NMAVEN-180-003: Project Version is missing: Group Id = " +
                project.getGroupId() + ", Artifact Id = " + project.getArtifactId() );
            return false;
        }
        if ( project.getArtifactType() == null )
        {
            logger.warning( "NMAVEN-180-004: Project Artifact Type is missing: Group Id" +
                project.getGroupId() + ", Artifact Id = " + project.getArtifactId() +
                ", Version = " + project.getVersion() );
            return false;
        }
        return true;
    }
}
