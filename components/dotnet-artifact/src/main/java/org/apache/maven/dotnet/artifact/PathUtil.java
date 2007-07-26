package org.apache.maven.dotnet.artifact;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.dotnet.ArtifactType;

import java.io.File;

public class PathUtil
{
    public static File getPrivateApplicationBaseFileFor( Artifact artifact, File localRepository )
    {
        return new File( localRepository.getParentFile(), "pab\\gac_msil\\" + artifact.getArtifactId() + "\\" +
            artifact.getVersion() + "__" + artifact.getGroupId() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
    }

    public static File getUserAssemblyCacheFileFor( Artifact artifact, File localRepository )
    {
        return new File( localRepository.getParentFile(), "uac\\gac_msil\\" + artifact.getArtifactId() + "\\" +
            artifact.getVersion() + "__" + artifact.getGroupId() + "\\" + artifact.getArtifactId() + "." +
            ArtifactType.getArtifactTypeForPackagingName( artifact.getType() ).getExtension() );
    }
}
