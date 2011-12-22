package npanday.artifact.impl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
