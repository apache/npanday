package npanday.registry;

/**
 * Wrapper for exceptions caught in NetDependenciesRepository
 *
 * @author Maria Odea Ching
 */
public class NPandayRepositoryException
    extends Exception
{
    public NPandayRepositoryException()
    {
        super();
    }

    public NPandayRepositoryException(String message)
    {
        super( message );
    }

    public NPandayRepositoryException(String message, Throwable t)
    {
        super( message, t );
    }

    public NPandayRepositoryException(Throwable t)
    {
        super( t );
    }
}
