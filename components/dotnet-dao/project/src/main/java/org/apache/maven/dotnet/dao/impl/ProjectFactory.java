package org.apache.maven.dotnet.dao.impl;

import org.apache.maven.dotnet.repository.Project;
import org.apache.maven.dotnet.repository.ProjectDependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.FileUtils;

import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class ProjectFactory
{
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
                throw new IOException( "NMAVEN-000-000: Unable to read model: Message = " + e.getMessage() );

            }
            System.out.println( "Pom File: " + pomFileDirectory.getAbsolutePath() );
            System.out.println( "Parent: " + parentPomFile.getParentFile().getAbsolutePath() );
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
        return projectDependency;
    }

    public static Artifact createArtifactFrom( ProjectDependency projectDependency, ArtifactFactory artifactFactory )
    {
        String scope = ( projectDependency.getScope() == null ) ? Artifact.SCOPE_COMPILE : projectDependency.getScope();
        Artifact assembly = artifactFactory.createDependencyArtifact( projectDependency.getGroupId(),
                                                                      projectDependency.getArtifactId(),
                                                                      VersionRange.createFromVersion(
                                                                          projectDependency.getVersion() ),
                                                                      projectDependency.getArtifactType(),
                                                                      projectDependency.getPublicKeyTokenId(), scope,
                                                                      null );

        File artifactFile = ( ( projectDependency.getArtifactType().startsWith( "gac" ) ) ) ? new File(
            "C:\\WINDOWS\\assembly\\" + projectDependency.getArtifactType() + "\\" + projectDependency.getArtifactId() +
                "\\" + projectDependency.getVersion() + "__" + projectDependency.getPublicKeyTokenId() + "\\" +
                projectDependency.getArtifactId() + ".dll" ) : new File( System.getProperty( "user.home" ),
                                                                         "\\.m2\\uac\\gac_msil\\" +
                                                                             projectDependency.getArtifactId() + "\\" +
                                                                             projectDependency.getVersion() + "__" +
                                                                             projectDependency.getGroupId() + "\\" +

                                                                             projectDependency.getArtifactId() +
                                                                             ".dll" );

        assembly.setFile( artifactFile );
        return assembly;
    }
}
