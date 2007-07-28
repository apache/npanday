package org.apache.maven.dotnet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.dotnet.ArtifactType;

import java.io.File;
import java.util.logging.Logger;

public class PathUtil
{
    private static Logger logger = Logger.getAnonymousLogger();

    public static synchronized File getGlobalAssemblyCacheFileFor( Artifact artifact, File gacRepository )
    {
        if ( artifact == null )
        {
            logger.warning( "NMAVEN-000-000: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( gacRepository == null )
        {
            logger.warning( "NMAVEN-000-000: GAC Repository is null - Cannot get application file." );
            return null;
        }
        if ( artifact.getClassifier() == null )
        {
            logger.warning( "NMAVEN-000-000: Assembly must be signed - Cannot get application file." );
        }
        String version = artifact.getVersion();
        int count = version.split( "[.]" ).length;
        for ( int i = 0; i < 4 - count; i++ )
        {
            version = version + ".0";
        }
        return new File( gacRepository, "\\gac_msil\\" + artifact.getArtifactId() + "\\" + version + "__" +
            artifact.getClassifier() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
    }

    public static synchronized File getPrivateApplicationBaseFileFor( Artifact artifact, File localRepository )
    {
        if ( artifact == null )
        {
            logger.warning( "NMAVEN-000-000: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NMAVEN-000-000: Local Repository is null - Cannot get application file." );
            return null;
        }
        return new File( localRepository.getParentFile(), "pab\\gac_msil\\" + artifact.getArtifactId() + "\\" +
            artifact.getVersion() + "__" + artifact.getGroupId() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
    }

    public static synchronized File getUserAssemblyCacheFileFor( Artifact artifact, File localRepository )
    {
        if ( artifact == null )
        {
            logger.warning( "NMAVEN-000-000: Artifact is null - Cannot get application file." );
            return null;
        }
        if ( localRepository == null )
        {
            logger.warning( "NMAVEN-000-000: Local Repository is null - Cannot get application file." );
            return null;
        }
        return new File( localRepository.getParentFile(), "uac\\gac_msil\\" + artifact.getArtifactId() + "\\" +
            artifact.getVersion() + "__" + artifact.getGroupId() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
    }


}
