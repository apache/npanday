package npanday.dao;

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

import npanday.ArtifactTypeHelper;
import npanday.PathUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            dependency.setScope(projectDependency.getScope());
            dependency.setSystemPath(projectDependency.getSystemPath());
            dependencies.add( dependency );
        }
        model.setDependencies( dependencies );
        return model;

    }

    /**
     * Creates a project using information from the specified model
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
        
        if (parent != null) 
        {
            if (project.getGroupId() == null) 
            {
            	project.setGroupId(parent.getGroupId());
            }
            if(project.getVersion()== null)
			{
			    project.setVersion(parent.getVersion());
			}
		}        
        if ( parent != null && pomFileDirectory != null && (project.getGroupId() == null || project.getVersion() == null))
        {
            File parentPomFile = new File( pomFileDirectory , parent.getRelativePath().replace( "\\", File.separator)
                                           .replace( "/", File.separator) );

            if (  parentPomFile.exists() ) // if pom resides in remote repo, Maven should already handle this.
            {
                FileReader fileReader = new FileReader( parentPomFile );

                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model parentModel;
                try
                {
                    parentModel = reader.read( fileReader );
                    
                    if (project.getGroupId()==null)
                    {
                        project.setGroupId( parentModel.getGroupId() );                      
                    }
                    if ( project.getVersion()==null )
                    {
                        project.setVersion( parentModel.getVersion() );
                    }
                }
                catch ( XmlPullParserException e )
                {
                    logger.log( Level.SEVERE, "Unable to read model.", e );
                    throw new IOException( "NPANDAY-180-000: Unable to read model: Message = " + e.getMessage() );
                }
                //Project parentProject = createProjectFrom( parentModel, parentPomFile.getParentFile() );
                //project.setParentProject( parentProject );
            }
        }

        //TODO: publickey/classifier
        List<Dependency> sourceArtifactDependencies = model.getDependencies();
        for ( Dependency dependency : sourceArtifactDependencies )
        {
            project.addProjectDependency( createProjectDependencyFrom( dependency ) );
        }
        return project;
    }

    /**
     * Creates a project dependency using information from the specified dependency.
     *
     * @param dependency a dependency to use as the source of the returned project dependency
     * @return a project dependency
     */
    public static ProjectDependency createProjectDependencyFrom( Dependency dependency )
    {
        ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId( dependency.getGroupId() );
        projectDependency.setArtifactId( dependency.getArtifactId() );
        projectDependency.setVersion( dependency.getVersion() );
        projectDependency.setPublicKeyTokenId( dependency.getClassifier() );
        projectDependency.setArtifactType( dependency.getType() );
        projectDependency.setScope(dependency.getScope());
        projectDependency.setSystemPath(dependency.getSystemPath());
        
        projectDependency.setScope(dependency.getScope());
        projectDependency.setSystemPath(dependency.getSystemPath());

        logAndVerifyProjectParameters( projectDependency );

        return projectDependency;
    }

    /**
     * Creates a dependency using information from the specified project dependency.
     *
     * @param projectDependency a project dependency to use as the source of the returned dependency
     * @return a dependency created using information from the specified project dependency
     */
    public static Dependency createDependencyFrom( ProjectDependency projectDependency )
    {
        logAndVerifyProjectParameters( projectDependency );

        Dependency dependency = new Dependency();
        dependency.setGroupId( projectDependency.getGroupId() );
        dependency.setArtifactId( projectDependency.getArtifactId() );
        dependency.setVersion( projectDependency.getVersion() );
        dependency.setType( projectDependency.getArtifactType() );
        dependency.setClassifier( projectDependency.getPublicKeyTokenId() );
        dependency.setScope(projectDependency.getScope());
        dependency.setSystemPath(projectDependency.getSystemPath());
        return dependency;
    }

    /**
     * Logs missing value if specified project in invalid and returns true if the specified project is valid, otherwise
     * returns false.
     *
     * @param project the project to check
     * @return true if the project is valid, otherwise returns false
     */
    private static boolean logAndVerifyProjectParameters( Project project )
    {
        if ( project.getGroupId() == null )
        {
            logger.warning( "NPANDAY-180-001: Project Group ID is missing" );
            return false;
        }
        if ( project.getArtifactId() == null )
        {
            logger.warning( "NPANDAY-180-002: Project Artifact ID is missing: Group Id = " + project.getGroupId() );
            return false;
        }
        if ( project.getVersion() == null )
        {
            logger.warning( "NPANDAY-180-003: Project Version is missing: Group Id = " + project.getGroupId() +
                ", Artifact Id = " + project.getArtifactId() );
            return false;
        }
        if ( project.getArtifactType() == null )
        {
            logger.warning( "NPANDAY-180-004: Project Artifact Type is missing: Group Id" + project.getGroupId() +
                ", Artifact Id = " + project.getArtifactId() + ", Version = " + project.getVersion() );
            return false;
        }
        return true;
    }
}
