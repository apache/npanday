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
import npanday.ArtifactTypeHelper;
import npanday.ArtifactType;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
        
        File defaultGac = new File(
            "C:\\WINDOWS\\assembly\\" + artifact.getType() + File.separator + artifact.getArtifactId() + File.separator + version + "__" +
                artifact.getClassifier() + File.separator + artifact.getArtifactId() + ".dll" );
        if(defaultGac.exists())
        {
            return defaultGac;
        }
        else
        {
            return getDotNetArtifact( artifact , gacRepository );
        }
        
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
            logger.warning( "NPANDAY-040-007: Artifact is null - Cannot get repository file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NPANDAY-040-008: Local Repository is null - Cannot get repository file." );
            return null;
        }
        return new File( localRepository, new DefaultRepositoryLayout().pathOf( artifact ) );
    }

    /**
     * Returns the path of the artifact within the private application base.
     *
     * @param artifact        the artifact to find the path of.  This value should not be null.
     * @param localRepository the local repository.  This value should not be null.
     * @return the path of the artifact within the private application base or null if either of the specified
     *         parameters is null
     */
    public static File getPrivateApplicationBaseFileFor( Artifact artifact, File localRepository )
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
        
        return  getDotNetArtifact( artifact, localRepository );
    }

    /**
     * Returns the path of the artifact within the user assembly cache.
     *
     * @param artifact        the artifact to find the path of. This value should not be null.
     * @param localRepository the local repository.  This value should not be null.
     * @return the path of the artifact within the user assembly cache or null if either of the specified
     *         parameters is null
     */
    public static File getUserAssemblyCacheFileFor( Artifact artifact, File localRepository )
    {
        if ( artifact == null )
        {
            logger.warning( "NPANDAY-040-005: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NPANDAY-040-006: Local Repository is null - Cannot get application file." );
            return null;
        }
      
        return  getDotNetArtifact( artifact , localRepository );
        
    }
    
    private static String getTokenizedPath(String path)
    {
        return path.replace(".",File.separator);
    }
    
    /**
     * Returns the path of the artifact within the user assembly cache.
     *
     * @param artifact        the artifact to find the path of. This value should not be null.
     * @return the path of the artifact within the user assembly cache or null if either of the specified
     *         parameters is null
     */
    public static File getDotNetArtifact( Artifact artifact, String source )
    {
        if ( artifact == null )
        {
            logger.warning( "NPANDAY-040-053.1: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( source == null )
        {
            logger.warning( "NPANDAY-040-054.1: Local Repository is null - Cannot get application file." );
            return null;
        }
        
        String outputDir = System.getProperty("user.dir");
        outputDir = outputDir+File.separator+"target";

        new File(outputDir).mkdir();
           
        String filename = artifact.getArtifactId() + "." + artifact.getArtifactHandler().getExtension();
        File targetFile = new File(outputDir+File.separator+ filename);
        
        
        try
        {    
              FileUtils.copyFile(new File( source ), targetFile);
        }   
        catch (IOException ioe) 
        {
            logger.warning("\nNPANDAY-1005-0001: Error copying dependency " + artifact +" "+ioe.getMessage());
        }
         
        return targetFile;
    }
    
    /**
     * Returns the path of the artifact within the user assembly cache.
     *
     * @param artifact        the artifact to find the path of. This value should not be null.
     * @return the path of the artifact within the user assembly cache or null if either of the specified
     *         parameters is null
     */
    public static File getDotNetArtifact( Artifact artifact, File localRepository )
    {
        if ( artifact == null )
        {
            logger.warning( "NPANDAY-040-0532: Artifact is null - Cannot get application file." );
            return null;
        }
       
        String ext = ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension();
        
        //assumes that since it was not found as a .dll or a .exe it will be considered as a default library
        if(ext == null)
        {
            ext = "jar";
        }
        
        File source = null;
        
        String classifier = "";
        
        if(artifact.getClassifier()!= null)
        {
            classifier = "-"+artifact.getClassifier();
        }        
           
        
        if( localRepository!= null )
        {
          source = new File( localRepository + File.separator + getTokenizedPath(artifact.getGroupId() ) + File.separator + artifact.getArtifactId() + File.separator + artifact.getVersion() + File.separator + artifact.getArtifactId() + "-" + artifact.getVersion() + classifier +"." + ext );
        }
        else
        {
           source = new File( System.getProperty( "user.home" ),".m2" + File.separator + "repository" + File.separator + getTokenizedPath(artifact.getGroupId() ) + File.separator + artifact.getArtifactId() + File.separator + artifact.getVersion() + File.separator + artifact.getArtifactId() + "-" + artifact.getVersion() +"." + ext );
        
        }
                      
        File dotnetFile =  getDotNetArtifact( artifact, source.toString() );
        
        return dotnetFile;
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
            executablePath = executableFile.getParentFile();
            executable = executableFile.getName();
        }

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
}
