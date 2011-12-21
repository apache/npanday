package npanday.executable.execution;

import org.apache.commons.exec.LogOutputStream;

/**
 * Stream that captures and logs lines to a plexus logger.
 * Designed for anonymous implementation.
 *
 * Access the captured output through {@link #toString}.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public abstract class CapturingLogOutputStream
    extends LogOutputStream
{
    private StringBuffer contents = new StringBuffer();

    private String NEW_LINE = System.getProperty("line.separator");

    @Override
    protected final void processLine(String line, int level) {
        processLine( line );

        contents.append( line );
        contents.append( NEW_LINE );
    }

    /**
     * Override this to log to a logger.
     *
     * @param line
     */
    protected abstract void processLine( String line );

    /**
     * Returns the captured output.
     */
    @Override
    public String toString()
    {
        return contents.toString();
    }
}
