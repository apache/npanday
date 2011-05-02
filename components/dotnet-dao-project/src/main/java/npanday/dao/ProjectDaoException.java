package npanday.dao;

/**
 * Exception thrown by project dao component.
 *
 * @author Maria Odea Ching
 */
public class ProjectDaoException
    extends Exception
{
    public ProjectDaoException()
    {
        super();
    }

    public ProjectDaoException( String message )
    {
        super( message );
    }

    public ProjectDaoException( String message, Throwable t )
    {
        super( message, t );
    }

    public ProjectDaoException( Throwable t )
    {
        super( t );
    }
}
