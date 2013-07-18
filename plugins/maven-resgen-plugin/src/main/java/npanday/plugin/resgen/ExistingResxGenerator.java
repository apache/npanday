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
package npanday.plugin.resgen;

import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionException;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.SettingsUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Generates existing resx to .resource (binary)
 *
 * @author Jan Ancajas
 * @goal generate-existing-resx-to-resource
 * @phase process-resources
 */
public class ExistingResxGenerator extends AbstractMojo
{
    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    /**
     * Emebedded Resources
     * 
     * @parameter expression="${embeddedResources}"
     */
    private EmbeddedResource[] embeddedResources;
    
    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;
    
    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;
    
    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;
    
    /**
     * The home directory of your .NET SDK.
     *
     * @parameter expression="${netHome}"
     */
    private File netHome;
    
    public static final String ASSEMBLY_RESOURCES_DIR = "assembly-resources";

    /**  
     * Transforms each of the input files (relative to sourceDirectory) and compiles them to a .resources file, placed
     * under target/assembly-resources/resource, where it will be included in the assembly.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        SettingsUtil.applyCustomSettings( getLog(), repositoryRegistry, settingsPath );

        if ( vendor != null && vendor.equals( "DotGNU" ) )
        {
            getLog().info( "NPANDAY-1501-005: Unsupported Plugin" );
            return;
        }

        String outputDirectory = project.getBuild().getDirectory();
        //resgen.exe
        String resourceDirectory = outputDirectory + File.separator + ASSEMBLY_RESOURCES_DIR + File.separator + "resource" ;
		getLog().debug("NPANDAY-1501-006: Resource directory:" + resourceDirectory);        
        if ( !FileUtils.fileExists( resourceDirectory ) )
        {
            FileUtils.mkdir( resourceDirectory );        
        }       
        
        try
        {
            List commands = null;
            for (EmbeddedResource embeddedResource : embeddedResources)
            {   
            	File file = new File(project.getBuild().getSourceDirectory() + File.separator + embeddedResource.getSourceFile());
            	if(!file.exists()) continue;
                commands = getCommands(file.getAbsoluteFile(), resourceDirectory, embeddedResource.getName());
                netExecutableFactory.getExecutable(
                    new ExecutableRequirement( vendor, null, frameworkVersion, "RESGEN" ), commands, netHome
                )
                    .execute();
            }
          
            if(embeddedResources == null)
            {
               String sourceDirectory = project.getBasedir().getPath();
        	   String[] resourceFilenames  = FileUtils.getFilesFromExtension(sourceDirectory, new String[]{"resx"});
        	
               for(String resourceFilename : resourceFilenames)
               {
            	  File file = new File(resourceFilename);
            	  if(!file.exists()) continue;
            	  String name = resourceFilename.substring(sourceDirectory.length() + 1).replace('\\', '.');
            	  name = project.getArtifactId() + "." + name.substring(0, name.lastIndexOf('.'));

            	  commands = getCommands(file.getAbsoluteFile(), resourceDirectory, name);
                   netExecutableFactory.getExecutable(
                       new ExecutableRequirement( vendor, null, frameworkVersion, "RESGEN" ), commands, netHome
                   )
                       .execute();
              }
            }
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-1501-002: Unable to execute resgen: Vendor = " + vendor +
                ", frameworkVersion = " + frameworkVersion, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-1501-003: Platform Unsupported", e );
        }        
    }
    
    private List<String> getCommands( File sourceFile, String resourceDirectory)    throws MojoExecutionException
    {
    	return getCommands(sourceFile, resourceDirectory, null);
    }
    
    private List<String> getCommands( File sourceFile, String resourceDirectory, String name )    throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
                                       
        // NPANDAY-358
        // Adding the "/useSourcePath" argument to the RESGEN executable will
        // allow RESGEN to use the relative paths defined inside *.resx files
        commands.add("/useSourcePath");
		
        commands.add( sourceFile.getAbsolutePath() );
        if( name != null || "".equals(name))
        {
        	commands.add( resourceDirectory + File.separator + name + ".resources"   );
        }
        else
        {
        	commands.add( resourceDirectory + File.separator + getFileNameMinusExtension(sourceFile) + ".resources"   );                               
        }       
        return commands;
    }
    
    private String getFileNameMinusExtension(File file)
    {
        if (file==null) return null;
        
        String name = file.getName();
        int lastIndex = name.lastIndexOf( '.' );
                
        return name.substring( 0, lastIndex );
    }
    
    private String[] getResxFiles( String sourceDirectory, List<String> includes )
    {
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory );
        if ( !includes.isEmpty() )
        {
            directoryScanner.setIncludes( includes.toArray( new String[includes.size()] ) );
        }
       
        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();

        return files;
    }

}
