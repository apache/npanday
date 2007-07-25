package org.apache.maven.dotnet.artifact.impl;

import org.codehaus.plexus.logging.Logger;

public class DummyLogger implements Logger
{
    public void setThreshold(int i)
    {

    }

    public void debug( String string )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void debug( String string, Throwable throwable )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDebugEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void info( String string )
    {
        System.out.println(string);
    }

    public void info( String string, Throwable throwable )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isInfoEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void warn( String string )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void warn( String string, Throwable throwable )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWarnEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void error( String string )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void error( String string, Throwable throwable )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isErrorEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void fatalError( String string )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void fatalError( String string, Throwable throwable )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isFatalErrorEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Logger getChildLogger( String string )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getThreshold()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
