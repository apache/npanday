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
package org.apache.maven.dotnet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;

import java.io.File;
import java.util.logging.Logger;

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
            logger.warning( "NMAVEN-040-000: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( gacRepository == null )
        {
            logger.warning( "NMAVEN-040-001: GAC Repository is null - Cannot get application file." );
            return null;
        }
        if ( artifact.getClassifier() == null )
        {
            logger.warning( "NMAVEN-040-002: Assembly must be signed - Cannot get application file." );
        }
        String version = artifact.getVersion();
        int count = version.split( "[.]" ).length;
        for ( int i = 0; i < 4 - count; i++ )
        {
            version = version + ".0";
        }
        //TODO: gac_generic
        //String processArchitecture = ( artifact.getType().equals( "gac_generic" ) );
        return new File( gacRepository, "\\" + artifact.getType() + "\\" + artifact.getArtifactId() + "\\" + version +
            "__" + artifact.getClassifier() + "\\" + artifact.getArtifactId() + ".dll" );
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
            logger.warning( "NMAVEN-040-007: Artifact is null - Cannot get repository file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NMAVEN-040-008: Local Repository is null - Cannot get repository file." );
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
            logger.warning( "NMAVEN-040-003: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NMAVEN-040-004: Local Repository is null - Cannot get application file." );
            return null;
        }
        return new File( localRepository.getParentFile(), "pab\\gac_msil\\" + artifact.getArtifactId() + "\\" +
            artifact.getVersion() + "__" + artifact.getGroupId() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
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
            logger.warning( "NMAVEN-040-005: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NMAVEN-040-006: Local Repository is null - Cannot get application file." );
            return null;
        }
        return new File( localRepository.getParentFile(), "uac\\gac_msil\\" + artifact.getArtifactId() + "\\" +
            artifact.getVersion() + "__" + artifact.getGroupId() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
    }
}
