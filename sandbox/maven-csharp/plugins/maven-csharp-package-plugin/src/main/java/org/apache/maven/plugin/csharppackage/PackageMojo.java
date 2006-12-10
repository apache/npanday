package org.apache.maven.plugin.csharppackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * This Mojo just sets the artifact to the result of the 
 * compile step, which is the actual dll/exe 
 * (doesn't need to jar up like the java compilers).
 *
 * @goal  package
 * @phase package
 */
public class PackageMojo extends AbstractMojo {

    /**
     * Name of the generated JAR.
     *
     * @parameter alias="jarName" expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
	 * Directory containing the classes.
	 *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    public void execute() throws MojoExecutionException {
		
		String builddirname = project.getBuild().getDirectory();
		
		File builddir = new File(builddirname);
		
		if( builddir.exists() ){
			
			this.getLog().info("Copying artifact to build directory [" + builddir.getAbsolutePath() + "]");
			this.getLog().debug("nb. this is to allow the create bundle functionality in csharp projects");
			
			try{
				
				FileUtils.copyFileToDirectory( outputDirectory, builddir );
				project.getArtifact().setFile( outputDirectory );
				
			}catch(IOException ioex){
				this.getLog().error("Exception occurred copying file [" + outputDirectory.getAbsolutePath() + "] to [" + builddir.getAbsoluteFile() + "]", ioex);
				throw new MojoExecutionException(ioex.getMessage(), ioex);
			}
		}else {
			this.getLog().warn("BuildDirectory [" + builddir.getAbsolutePath() + "] doesn't exist, something strange has happened");
		}
	}
}
