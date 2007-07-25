package org.apache.maven.dotnet.artifact;

import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;

public class DummyResolver
    implements ArtifactResolver
{
    private ArtifactResolutionResult result;

    public DummyResolver()
    {
        result = new ArtifactResolutionResult();
        result.setArtifactResolutionNodes( new HashSet());
    }
    public void resolve( Artifact artifact, List list, ArtifactRepository artifactRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {

    }

    public ArtifactResolutionResult resolveTransitively( Set set, Artifact artifact, List list,
                                                         ArtifactRepository artifactRepository,
                                                         ArtifactMetadataSource artifactMetadataSource )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        
        return result;
    }

    public ArtifactResolutionResult resolveTransitively( Set set, Artifact artifact, List list,
                                                         ArtifactRepository artifactRepository,
                                                         ArtifactMetadataSource artifactMetadataSource, List list1 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return result;
    }

    public ArtifactResolutionResult resolveTransitively( Set set, Artifact artifact,
                                                         ArtifactRepository artifactRepository, List list,
                                                         ArtifactMetadataSource artifactMetadataSource,
                                                         ArtifactFilter artifactFilter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return result;
    }

    public ArtifactResolutionResult resolveTransitively( Set set, Artifact artifact, Map map,
                                                         ArtifactRepository artifactRepository, List list,
                                                         ArtifactMetadataSource artifactMetadataSource )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return result;
    }

    public ArtifactResolutionResult resolveTransitively( Set set, Artifact artifact, Map map,
                                                         ArtifactRepository artifactRepository, List list,
                                                         ArtifactMetadataSource artifactMetadataSource,
                                                         ArtifactFilter artifactFilter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return result;
    }

    public ArtifactResolutionResult resolveTransitively( Set set, Artifact artifact, Map map,
                                                         ArtifactRepository artifactRepository, List list,
                                                         ArtifactMetadataSource artifactMetadataSource,
                                                         ArtifactFilter artifactFilter, List list1 )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return result;
    }

    public void resolveAlways( Artifact artifact, List list, ArtifactRepository artifactRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {

    }
}
