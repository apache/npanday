package npanday.resolver.resolvers;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import npanday.ArtifactType;
import npanday.ArtifactTypeHelper;
import npanday.resolver.ArtifactResolvingContributor;

/**
 * PDB assemblies contributor class.
 */
public class PdbAssemblyResolver extends AbstractLogEnabled implements ArtifactResolvingContributor 
{
    
    ArtifactResolver mavenResolver;
    ArtifactFactory artifactFactory;
    

    /**
     * {@inheritDoc}
     */
    public void tryResolve(Artifact artifact, Set<Artifact> additionalDependenciesCollector)
            throws ArtifactNotFoundException
    {
        // NO-OP
    }

    
    /**
     * {@inheritDoc}
     */
    public void contribute(Artifact artifact, ArtifactRepository localRepository, 
            @SuppressWarnings("rawtypes") List remoteRepositories,
            Set<Artifact> additionalDependenciesCollector) throws ArtifactNotFoundException
    {
        final ArtifactType artifactType = ArtifactType.getArtifactTypeForPackagingName(artifact.getType());
        if ( !ArtifactTypeHelper.isDotnetAnyGac(artifactType) && 
             !ArtifactTypeHelper.isComReference(artifactType) &&
             artifactType != ArtifactType.NULL &&
             artifactType != ArtifactType.DOTNET_SYMBOLS) 
        {
            tryResolveArtifactPdbAssembly(artifact, localRepository, remoteRepositories, 
                    additionalDependenciesCollector);
        }
    }


    /**
     * Try to resolve PDB artifact for given artifact.
     * @param artifact
     * @param localRepository
     * @param remoteRepositories
     * @param additionalDependenciesCollector
     * @throws ArtifactNotFoundException
     */
    private void tryResolveArtifactPdbAssembly(Artifact artifact, ArtifactRepository localRepository, 
            List remoteRepositories, Set<Artifact> additionalDependenciesCollector) throws ArtifactNotFoundException 
    {
        Artifact pdbArtifact = artifactFactory.createArtifactWithClassifier(artifact.getGroupId(), artifact.getArtifactId(), 
                artifact.getVersion(), ArtifactType.DOTNET_SYMBOLS.getPackagingType(), artifact.getClassifier());
        boolean pdbArtifactFound = true;
        try {
            mavenResolver.resolve(pdbArtifact, remoteRepositories, localRepository);
        } catch (ArtifactNotFoundException e) {
            // Ignore PDF assembly not found exception
            pdbArtifactFound = false;
        } catch (ArtifactResolutionException e) {
            throw new ArtifactNotFoundException(e.getMessage(), artifact);
        }
        
        if (pdbArtifactFound) 
        {
            additionalDependenciesCollector.add(pdbArtifact);
        }
        
    }

}
