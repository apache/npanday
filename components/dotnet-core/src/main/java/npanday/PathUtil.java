package npanday;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * This class provides methods for obtaining the location of artifacts.
 */

public final class PathUtil
{
    private static final Logger logger = Logger.getAnonymousLogger();

    public static File getGACFile4Artifact(Artifact artifact) {
        // TODO: Should be harmonized with VendorInfo#getGlobalAssemblyCacheDirectoryFor

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
        // TODO: Should be harmonized with VendorInfo#getGlobalAssemblyCacheDirectoryFor

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

    public static String getExecutable( String executable, Collection<String> executablePaths,
                                        org.codehaus.plexus.logging.Logger logger )
    {
        if ( executablePaths != null && executablePaths.size() > 0 )
        {
            for ( String executablePath : executablePaths )
            {
                File executableFile = PathUtil.getExecutable( new File(executablePath), executable );
                if ( executableFile != null )
                {
                    logger.info(
                        "NPANDAY-126-001: Found executable file for " + executable + ": \"" + executableFile + "\""
                    );

                    return executableFile.getAbsolutePath();
                }
            }
        }
        logger.warn( "NPANDAY-126-002: Did not find path for " + executable + " in " + executablePaths );

        return executable;
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

    public static File getPrivateApplicationBaseDirectory( MavenProject project )
    {
        // Maven already basedir aligns this
        return new File( project.getBuild().getDirectory() );
    }
    
    public static File getPreparedPackageFolder( MavenProject project )
    {
        String folderName = project.getArtifactId();
        return new File(new File(project.getBuild().getDirectory(), "packages"), folderName);
    }

    /**
     * Builds an filename with artifact id and extension only.
     */
    public static String getPlainArtifactFileName( Artifact artifact )
    {
        return artifact.getArtifactId() + "." + ArtifactType.getArtifactTypeForPackagingName(artifact.getType()).getExtension();
    }

    /**
     * Will strip the version and classifier!
     */
    public static void copyPlainArtifactFileToDirectory( Artifact artifact, File targetFolder)
        throws IOException
    {
        File destination = new File(
            targetFolder, PathUtil.getPlainArtifactFileName( artifact )
        );

        try
        {
            FileUtils.copyFile(
                artifact.getFile(), destination
            );
        }
        catch ( IOException e )
        {
            throw new IOException( "Error on copying " + artifact + " to " + destination);
        }
    }
}
