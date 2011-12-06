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
package npanday;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class provides methods for obtaining the location of artifacts.
 */

public final class PathUtil
{
    private static final Logger logger = Logger.getAnonymousLogger();

    public static File getGACFile4Artifact(Artifact artifact) {

        File gacFile;
        String type = artifact.getType();
        if (type.equals(ArtifactType.GAC_MSIL4.getPackagingType())) {
            String gacRoot = System.getenv( "SystemRoot" ) + "\\Microsoft.NET\\assembly\\GAC_MSIL\\";
            gacFile = new File( gacRoot, artifact.getArtifactId() + File.separator + "v4.0" + "_" + artifact.getVersion() + "__" +
                artifact.getClassifier() + File.separator + artifact.getArtifactId() + ".dll" );
        }
        else if (type.equals(ArtifactType.GAC_32_4.getPackagingType())) {
            String gacRoot = System.getenv( "SystemRoot" ) + "\\Microsoft.NET\\assembly\\GAC_32\\";
            gacFile = new File( gacRoot, artifact.getArtifactId() + File.separator + "v4.0" + "_" + artifact.getVersion() + "__" +
                artifact.getClassifier() + File.separator + artifact.getArtifactId() + ".dll" );
        }
        else if (type.equals(ArtifactType.GAC_64_4.getPackagingType())) {
            String gacRoot = System.getenv( "SystemRoot" ) + "\\Microsoft.NET\\assembly\\GAC_64\\";
            gacFile = new File( gacRoot, artifact.getArtifactId() + File.separator + "v4.0" + "_" + artifact.getVersion() + "__" +
                artifact.getClassifier() + File.separator + artifact.getArtifactId() + ".dll" );
        }
        else {
            gacFile = artifact.getFile();
        }

        return gacFile;
    } 
 
    /**
     * Returns the path of the artifact within the global assembly cache.
     *
     * @param artifact      the artifact to find the path of.  This value should not be null.
     * @param gacRepository the root directory of the GAC.  This value should not be null.
     * @return the path of the artifact within the global assembly cache or null if either of the specified
     *         parameters is null
     */
    public static File getGlobalAssemblyCacheFileFor( Artifact artifact, File gacRepository )
    {
        if ( artifact == null )
        {
            logger.warning( "NPANDAY-040-000: Artifact is null - Cannot get application file." );
            return null;
        }

        if ( gacRepository == null )
        {
            logger.warning( "NPANDAY-040-001: GAC Repository is null - Cannot get application file." );
            return null;
        }

        if ( artifact.getClassifier() == null )
        {
            logger.warning( "NPANDAY-040-002: Assembly must be signed - Cannot get application file." );
        }
        String version = artifact.getVersion();
        int count = version.split( "[.]" ).length;
        for ( int i = 0; i < 4 - count; i++ )
        {
            version = version + ".0";
        }

        logger.finest( "NPANDAY-040-003: Read global assembly cache folder for: " + artifact);        
        //TODO: gac_generic
        //String processArchitecture = ( artifact.getType().equals( "gac_generic" ) );

        return new File( "C:\\WINDOWS\\assembly\\" + artifact.getType() + File.separator + artifact.getArtifactId() +
                             File.separator + version + "__" + artifact.getClassifier() + File.separator +
                             artifact.getArtifactId() + ".dll" );
    }

    /**
     * Returns the path of the artifact within the local repository using the default repository layout.
     *
     * @param artifact        the artifact to find the path of.  This value should not be null.
     * @param localRepository the local repository.  This value should not be null.
     * @return the path of the artifact within the local maven repository or null if either of the specified
     *         parameters is null
     */
    public static File getMavenLocalRepositoryFileFor( Artifact artifact, File localRepository )
    {
        if ( artifact == null )
        {
            throw new NullPointerException( "NPANDAY-040-007: Artifact is null - Cannot get repository file." );
        }
        if ( localRepository == null )
        {
            throw new NullPointerException( "NPANDAY-040-008: Local Repository is null - Cannot get repository file." );
        }
        return new File( localRepository, new DefaultRepositoryLayout().pathOf( artifact ) );
    }

    /**
     * Returns the path of the artifact within the private application base.
     *
     *
     * @param artifact        the artifact to find the path of.  This value should not be null.
     * @param localRepository the local repository.  This value should not be null.
     * @param outputDir
     * @return the path of the artifact within the private application base or null if either of the specified
     *         parameters is null
     */
    public static File getPrivateApplicationBaseFileFor( Artifact artifact, File localRepository, File outputDir )
    {
        if ( artifact == null )
        {
            logger.warning( "NPANDAY-040-003: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NPANDAY-040-004: Local Repository is null - Cannot get application file." );
            return null;
        }
        
        return  getDotNetArtifact( artifact, localRepository, outputDir );
    }

    /**
     * Returns the path of the artifact within the user assembly cache.
     *
     *
     * @param artifact        the artifact to find the path of. This value should not be null.
     * @param outputDir
     * @return the path of the artifact within the user assembly cache or null if either of the specified
     *         parameters is null
     */
    public static File getDotNetArtifact( Artifact artifact, File localRepository, File outputDir )
    {
        if ( artifact == null )
        {
            logger.warning( "NPANDAY-040-0532: Artifact is null - Cannot get application file." );
            return null;
        }

        outputDir.mkdir();

        String filename = artifact.getArtifactId() + "." + artifact.getArtifactHandler().getExtension();
        File targetFile = new File( outputDir, filename );

        try
        {
            File artifactFile = artifact.getFile();
            File sourceFile;
            if ( artifactFile != null && artifactFile.exists() )
            {
                sourceFile = artifactFile;
            }
            else
            {
                sourceFile = getMavenLocalRepositoryFileFor( artifact, localRepository );
            }

            FileUtils.copyFile( sourceFile, targetFile );
        }
        catch (IOException ioe)
        {
            logger.warning("\nNPANDAY-1005-0001: Error copying dependency " + artifact +" "+ioe.getMessage());
        }

        return targetFile;
    }


    public static boolean containsExecutable(String executablePath, String executable) {
        return (getExecutable(new File(executablePath), executable) != null);
    }

    public static File getExecutable(File executablePath, String executable) {

        if (executablePath == null || !executablePath.exists())
            return null;

        File executableFile = new File(executable);
        if (executableFile.isAbsolute())
        {
            // handle case where executable is absolute
            executablePath = executableFile.getParentFile();
            executable = executableFile.getName();
        }
        else 
        {
            // handle case where executable is relative to executable path
            executableFile = new File(executablePath, executable);
        }

        // determine if the executable exists (without an extension)
        if (executableFile.exists())
            return executableFile;

        // TODO: handle linux/mac ?
        String[] extensions = new String[] {"exe", "com", "bat", "cmd"};

        for (String extension : extensions)
        {
            executableFile = new File(executablePath, executable + "." + extension);
            if (executableFile.exists())
                return executableFile;
        }

        return null;
    }

    public static File buildSettingsFilePath( String settingsPathOrFile )
    {
        if (settingsPathOrFile.endsWith( "xml" ))
            return new File(settingsPathOrFile);

        return new File( settingsPathOrFile, "npanday-settings.xml" );
    }

    public static String getHomeM2Folder()
    {
        return new File(System.getProperty( "user.home" ), ".m2").getAbsolutePath();
    }

    public static File getPrivateApplicationBaseDirectory( MavenProject project )
    {
        // Maven already basedir aligns this
        return new File( project.getBuild().getDirectory() );
    }
}
