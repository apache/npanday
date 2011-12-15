package npanday.plugin.aspnet;

import npanday.PathUtil;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFileFilter;

import java.io.File;
import java.util.Collections;
import java.util.List;


/**
 * Used as a quite static configuration source for preparation of NPanday
 * packages.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
  */
public class PackagePreparationConfigurationSource
    implements AssemblerConfigurationSource
{
    private File outputDirectory;

    private String finalName;

    private String assemblyDescriptorRef;

    private String assemblyDescriptorFile;

    private MavenSession session;

    private MavenFileFilter fileFilter;

    private MavenProject project;

    public PackagePreparationConfigurationSource( MavenSession session, MavenFileFilter fileFilter )
    {
        this.session = session;
        this.project = session.getCurrentProject();
        this.fileFilter = fileFilter;

        File prepackageDir = PathUtil.getPreparedPackageFolder( project );
        outputDirectory = prepackageDir.getParentFile();
        finalName = prepackageDir.getName();
    }

    public String getDescriptor()
    {
        return null; 
    }

    public String getDescriptorId()
    {
        return null; 
    }

    public String[] getDescriptors()
    {
        if (assemblyDescriptorFile != null){
         return new String[] {assemblyDescriptorFile};
        }

        return new String[0]; 
    }

    public String[] getDescriptorReferences()
    {
        if ( assemblyDescriptorRef != null){
           return new String[] { assemblyDescriptorRef };
        }

        return new String[0];
    }

    public File getDescriptorSourceDirectory()
    {
        return null;
    }

    public File getBasedir()
    {
        return project.getBasedir();
    }

    public MavenProject getProject()
    {
        return project;
    }

    public boolean isSiteIncluded()
    {
        return false; 
    }

    public File getSiteDirectory()
    {
        throw new UnsupportedOperationException();
    }

    public String getFinalName()
    {
        return finalName;
    }

    public boolean isAssemblyIdAppended()
    {
        return false;
    }

    public String getClassifier()
    {
        throw new UnsupportedOperationException("The classifier will be part of the final name already.");
    }

    public String getTarLongFileMode()
    {
        throw new UnsupportedOperationException("Tar is not supported for packaging preparations!");
    }

    public File getOutputDirectory()
    {
        return this.outputDirectory;
    }

    public File getWorkingDirectory()
    {
        return new File(project.getBuild().getDirectory(), "packages\\temp\\workdir");
    }

    public MavenArchiveConfiguration getJarArchiveConfiguration()
    {
        throw new UnsupportedOperationException();
    }

    public ArtifactRepository getLocalRepository()
    {
       return session.getLocalRepository();
    }

    public File getTemporaryRootDirectory()
    {
        return new File(project.getBuild().getDirectory(), "packages\\temp\\tmpdir");
    }

    public File getArchiveBaseDirectory()
    {
        return project.getBasedir();
    }

    public List<String> getFilters()
    {
        return null; 
    }

    public List<MavenProject> getReactorProjects()
    {
        throw new UnsupportedOperationException();
    }

    public List<ArtifactRepository> getRemoteRepositories()
    {
        return Collections.<ArtifactRepository>emptyList();
    }

    public boolean isDryRun()
    {
        return false; 
    }

    public boolean isIgnoreDirFormatExtensions()
    {
        return true;
    }

    public boolean isIgnoreMissingDescriptor()
    {
        return false;
    }

    public MavenSession getMavenSession()
    {
        return session;
    }

    public String getArchiverConfig()
    {
        // we won't do any archiving
        return null;
    }

    public MavenFileFilter getMavenFileFilter()
    {
        return fileFilter;
    }

    public boolean isUpdateOnly()
    {
        return false; 
    }

    public boolean isUseJvmChmod()
    {
        return false; 
    }

    public boolean isIgnorePermissions()
    {
        return false; 
    }

    public void setDescriptorFile( String assemblyDescriptorFile )
    {
        this.assemblyDescriptorFile = assemblyDescriptorFile;
    }

    public void setDescriptorRef( String assemblyDescriptorRef )
    {
        this.assemblyDescriptorRef = assemblyDescriptorRef;
    }
}


