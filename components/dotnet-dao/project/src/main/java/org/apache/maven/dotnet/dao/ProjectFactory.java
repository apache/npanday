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
import java.io.FileReader;
import java.io.IOException;
import java.io.File;


public class ProjectFactory
{
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
            //System.out.println( "Pom File: " + pomFileDirectory.getAbsolutePath() );
            //System.out.println( "Parent: " + parentPomFile.getParentFile().getAbsolutePath() );
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

    public static Dependency createDependencyFrom( ProjectDependency projectDependency )
    {
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
}
