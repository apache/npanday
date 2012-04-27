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

package npanday.plugin.vsinstaller;

import npanday.registry.RepositoryRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Installs Visual Studio 2005 addin.
 *
 * @author Shane Isbell
 * @goal install
 * @requiresProject false
 * @requiresDirectInvocation true
 */
public class VsInstallerMojo
    extends AbstractMojo
{
    /**
     * @parameter expression ="${installationLocation}"
     */ 
    public File installationLocation;
    /**
     * @parameter expression = "${project}"
     */
    public org.apache.maven.project.MavenProject mavenProject;

    /**
     * @parameter
     */
    public List<File> vsAddinDirectories = new ArrayList<File>();

    /**
     * The the path to the local maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    private String localRepository;

    /**
     * Provides access to configuration information used by NPanday.
     *
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /** @component role="org.apache.maven.artifact.handler.ArtifactHandler" */
    private List<ArtifactHandler> artifactHandlers;

    /** @component */
    private ArtifactHandlerManager artifactHandlerManager;

    private FileSystemView filesystemView = FileSystemView.getFileSystemView();

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Map<String, ArtifactHandler> map = new HashMap<String, ArtifactHandler>();

        for ( ArtifactHandler artifactHandler : artifactHandlers )
        {
            //If I add a handler that already exists, the runtime breaks.
            if ( isDotNetHandler( artifactHandler ) )
            {
                map.put( artifactHandler.getPackaging(), artifactHandler );
            }
        }
        artifactHandlerManager.addHandlers( map );

        // in case Maven doesn't populate the base directory
        if ( mavenProject.getBasedir() == null || "${project.basedir}".equals( mavenProject.getBasedir().getName() ) )
        {
            mavenProject.setBasedir( new File( System.getProperty( "user.dir" ) ) );
            mavenProject.getBuild().setDirectory( new File( mavenProject.getBasedir(), "target" ).getAbsolutePath() );
        }

        getLog().warn( "NPANDAY-251: removed net dependency resolution for VS2005-profile here!" );

        collectDefaultVSAddinDirectories();

        getInstallationLocation();

        for ( File vsAddinsDir : vsAddinDirectories )
        {
            writePlugin( vsAddinsDir );
        }

        copyDependenciesToBin();
    }

    /**
     * Returns true if the artifact handler can handle the dotnet types, otherwise returns false
     *
     * @param artifactHandler the artifact handler to check
     * @return true if the artifact handler can handle the dotnet types, otherwise returns false
     */
    private boolean isDotNetHandler( ArtifactHandler artifactHandler )
    {
        String extension = artifactHandler.getExtension();
        return extension.equals( "dll" ) || extension.equals( "nar" ) || extension.equals( "exe" ) ||
            extension.equals( "exe.config" );
    }

    private void collectDefaultVSAddinDirectories()
    {
        File homeDir = filesystemView.getDefaultDirectory();

        String vs2010 = "Visual Studio 2010";
        
        String vs2008 = "Visual Studio 2008";
        String vs2005 = "Visual Studio 2005";

        List<File> defaultVSDirs = new ArrayList<File>();

        defaultVSDirs.add( new File( homeDir, vs2010 ) );
        
        defaultVSDirs.add( new File( homeDir, vs2008 ) );
        defaultVSDirs.add( new File( homeDir, vs2005 ) );

        File enHomeDir = new File( System.getProperty( "user.home" ), "My Documents" );
        if ( !homeDir.getPath().toLowerCase().equals( enHomeDir.getPath().toLowerCase() ) )
        {
            defaultVSDirs.add( new File( enHomeDir, vs2010 ) );

            defaultVSDirs.add( new File( enHomeDir, vs2008 ) );
            defaultVSDirs.add( new File( enHomeDir, vs2005 ) );
        }

        for ( File dir : defaultVSDirs )
        {
            if ( dir.exists() )
            {
                File addInPath = new File( dir, "AddIns" );

                if ( !addInPath.exists() )
                {
                    addInPath.mkdir();
                }

                vsAddinDirectories.add( addInPath );
            }
        }
    }

    private void writePlugin( File addinPath )
        throws MojoExecutionException
    {
        OutputStreamWriter writer = null;

        if ( !addinPath.exists() )
        {
            addinPath.mkdirs();
        }

        try
        {
            String addin = IOUtil.toString( VsInstallerMojo.class.getResourceAsStream(
                "/template/NPanday.VisualStudio.AddIn" ) );
            File outputFile = new File( addinPath, "NPanday.VisualStudio.AddIn" );

            writer = new OutputStreamWriter( new FileOutputStream( outputFile ), "Unicode" );

            writer.write( addin.replaceAll( "\\$\\{installationLocation\\}", installationLocation.getAbsolutePath().replaceAll( "\\\\", "\\\\\\\\" ) ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-121-001: Unable to write to Visual Studio AddIns directory: " + e.getMessage() );
        }
        finally
        {
            IOUtil.close( writer );
        }

    }

    private void copyDependenciesToBin()
         throws MojoExecutionException
    {
        try
        {
            IOFileFilter dllSuffixFilter = FileFilterUtils.suffixFileFilter( ".dll" );
            IOFileFilter dllFiles = FileFilterUtils.andFileFilter( FileFileFilter.FILE, dllSuffixFilter );

            FileUtils.copyDirectory( new File( mavenProject.getBuild().getDirectory() ), installationLocation, dllFiles,
                                     true );
        }

        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-121-004: Error on copying dll-dependencies to bin", e );
        }
    }

    private void getInstallationLocation()
    {
        if ( installationLocation == null )
        {
/* For now, reserve the "program files" default for the MSI - typically admin permissions will cause these to conflict
            String programFilesPath = System.getenv( "PROGRAMFILES" );

            if ( programFilesPath != null && programFilesPath.length() != 0 )
            {
                installationLocation = new File ( programFilesPath, "NPanday/bin" );

                if ( !installationLocation.exists() )
                {
                    if ( !installationLocation.mkdirs() )
                    {
                        installationLocation = new File( System.getProperty( "user.home" ), "NPanday/bin" );
                    }
                }
            }
            else
            {
                installationLocation = new File( System.getProperty( "user.home" ), "NPanday/bin" );
            }
*/
            installationLocation = new File( System.getProperty( "user.home" ), "NPanday/bin" );
        }
        else
        {
            installationLocation = new File ( installationLocation, "bin" );
        }

        if ( !installationLocation.exists() )
        {
            installationLocation.mkdirs();
        }
    }

}
