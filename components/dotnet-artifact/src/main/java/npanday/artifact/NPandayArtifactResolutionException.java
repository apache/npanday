package npanday.artifact;

/**
 * Wrapper for exceptions caught during artifact resolution.
 *
 * @author Maria Odea Ching
 */
public class NPandayArtifactResolutionException
    extends Exception
{
    public NPandayArtifactResolutionException()
    {
        super();
    }

    public NPandayArtifactResolutionException( String message )
    {
        super( message );
    }

    public NPandayArtifactResolutionException( String message, Throwable t )
    {
        super( message, t );
    }

    public NPandayArtifactResolutionException( Throwable t )
    {
        super( t );
    }
}
