package org.apache.maven.plugin.csharp.source;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.csharp.helper.PackagingHelper;
import org.apache.maven.project.MavenProject;

/**
 * This Mojo adds the result of the compile to the classpath elements
 * This is required by the NUnitMojo.
 *
 * @goal  process-classes
 * @phase process-classes
 * 
 * @author <a href="mailto:chris.stevenson@gmail.com">Chris Stevenson</a>
 */
public class ProcessClassesMojo extends AbstractMojo {

    /**
     * Name of the generated assembly
     *
     * @parameter alias="jarName" expression="${project.build.finalName}"
     * @required
     */
    private String finalName;
	
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
   
    /**
	 * Directory containing the classes.
	 *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    /**
     * The classpath elements of the project being tested.
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List classpathElements;
    
    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;
    
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		File mainAssembly = getFile( outputDirectory, finalName, project.getPackaging() );
		
		project.getBuild().setOutputDirectory( mainAssembly.getAbsolutePath() );
		
		this.getLog().info("Setting [" + mainAssembly.getAbsolutePath() + "] to as output folder.");
	}
	
	public File getFile(File directory, String fileName, String packaging) throws MojoExecutionException{
		
		String fileAbsolutePath = directory.getAbsolutePath() + File.separator + fileName + "." + PackagingHelper.getExtension( packaging );		
		
		this.getLog().debug("Looking for file[" + fileAbsolutePath + "]");
		
		File f = new File(fileAbsolutePath);
		
		if( ! f.exists() ) throw new MojoExecutionException("Cannot find file[" + fileAbsolutePath + "]");
		
		return f;
	}
	
}
