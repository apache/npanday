package npanday.registry;

/**
 * Thrown to indicate an error while accessing the windows registry.
 *
 * @author Shane Isbell
 */
public class WindowsRegistryAccessException
    extends Exception
{

    /**
     * Constructs an <code>WindowsRegistryAccessException</code>  with no exception message.
     */
    public WindowsRegistryAccessException()
    {
        super();
    }

    /**
     * Constructs an <code>WindowsRegistryAccessException</code> with the specified exception message.
     *
     * @param message the exception message
     */
    public WindowsRegistryAccessException( String message )
    {
        super( message );
    }

    /**
     * Constructs an <code>WindowsRegistryAccessException</code> with the specified exception message and cause of the exception.
     *
     * @param message the exception message
     * @param cause   the cause of the exception
     */
    public WindowsRegistryAccessException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructs an <code>WindowsRegistryAccessException</code> with the cause of the exception.
     *
     * @param cause the cause of the exception
     */
    public WindowsRegistryAccessException( Throwable cause )
    {
        super( cause );
    }
}
