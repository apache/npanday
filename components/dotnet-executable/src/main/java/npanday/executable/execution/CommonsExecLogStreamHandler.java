package npanday.executable.execution;

import org.apache.commons.exec.ExecuteStreamHandler;
import org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class CommonsExecLogStreamHandler
    implements ExecuteStreamHandler
{
    public CommonsExecLogStreamHandler( Logger logger )
    {
    }

    public void setProcessInputStream( OutputStream os ) throws IOException
    {

    }

    public void setProcessErrorStream( InputStream is ) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setProcessOutputStream( InputStream is ) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start() throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
