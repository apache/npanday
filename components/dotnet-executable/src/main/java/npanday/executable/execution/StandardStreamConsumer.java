package npanday.executable.execution;

import com.google.common.base.Preconditions;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * StreamConsumer instance that buffers the entire output
 *
 * @author Shane Isbell
 */
class StandardStreamConsumer
    implements StreamConsumer
{

    private DefaultConsumer consumer;

    private StringBuffer sb = new StringBuffer();

    private Logger logger;


    public StandardStreamConsumer( Logger logger )
    {
        Preconditions.checkArgument( logger != null, "logger must not be null" );

        this.logger = logger;
        consumer = new DefaultConsumer();
    }

    public void consumeLine( String line )
    {
        sb.append( line );
        if ( logger != null )
        {
            consumer.consumeLine( line );
        }
    }

    /**
     * Returns the stream
     *
     * @return the stream
     */
    public String toString()
    {
        return sb.toString();
    }
}
