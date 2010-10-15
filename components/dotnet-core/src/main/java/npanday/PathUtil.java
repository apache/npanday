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
        //TODO: gac_generic
        //String processArchitecture = ( artifact.getType().equals( "gac_generic" ) );
        return getDotNetArtifact( artifact );
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
        
        return  getDotNetArtifact( artifact );
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
      
        return  getDotNetArtifact( artifact );
        
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
    public static File getDotNetArtifact( Artifact artifact, File source )
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
              FileUtils.copyFile(source, targetFile);
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
    public static File getDotNetArtifact( Artifact artifact )
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
       
        File source = new File( System.getProperty( "user.home" ),".m2" + File.separator + "repository" + File.separator + getTokenizedPath(artifact.getGroupId() ) + File.separator + artifact.getArtifactId() + File.separator + artifact.getVersion() + File.separator + artifact.getArtifactId() + "-" + artifact.getVersion() +"." + ext );
               
        File dotnetFile =  getDotNetArtifact( artifact, source );
        
        return dotnetFile;
    }
    
}
