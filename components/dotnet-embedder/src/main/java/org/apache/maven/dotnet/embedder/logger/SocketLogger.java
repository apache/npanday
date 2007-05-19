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
package org.apache.maven.dotnet.embedder.logger;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.util.logging.SocketHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Provides logging services for writing log messages over a socket port.
 *
 * @author Shane Isbell
 * TODO: This class needs a lot more work. Currently only support INFO messages.
 */
public class SocketLogger
    extends AbstractLogger
{
    /**
     * The underlying JDK logger
     */
    private java.util.logging.Logger logger;

    /**
     * Constructor. This method is intended to by invoked by an instance of the org.codehaus.plexus.logging.LoggerManager
     * class, not by the application developer.
     *
     * @param threshold the threshold for the logger. If the log message level is below this defined level, the logger
     *                  will not write out the log.
     */
    public SocketLogger( int threshold )
    {
        super( threshold, "SocketLogger" );
        logger = java.util.logging.Logger.getAnonymousLogger();
        logger.setLevel( Level.INFO );//TODO: This should use the specified threshold value.
        this.setThreshold( org.codehaus.plexus.logging.Logger.LEVEL_INFO );
    }

    /**
     * Sets a socket handler on the logger for the specified port. The host will be local. The application may
     * re-invoke this method to set a new port for the logger.
     *
     * @param port the socket port of the loggers socket handler.
     * @throws IOException if there is a problem binding to the specified port
     */
    public synchronized void setHandlerFor( int port )
        throws IOException
    {
        SocketHandler socketHandler = new SocketHandler( "localhost", port );
        socketHandler.setFormatter( new MavenFormatter() );
        for ( Handler handler : logger.getHandlers() )
        {
            logger.removeHandler( handler );
        }
        logger.addHandler( socketHandler );
    }

    public Logger getChildLogger( String name )
    {
        return this;
    }

    public void debug( String message, Throwable throwable )
    {

        if ( isDebugEnabled() )
        {
            System.out.print( "[ maven embedder DEBUG---] " );
            System.out.println( message );

            if ( null != throwable )
            {
                throwable.printStackTrace( System.out );
            }
        }

    }

    public synchronized void info( String message )
    {
        logger.info( message );
/*
        if ( isInfoEnabled() )
        {
            System.out.print( "[ maven embedder INFO] " );
            System.out.println( message );


            if ( null != throwable )
            {
                throwable.printStackTrace( System.out );
            }
        }
        */
    }

    public synchronized void info( String message, Throwable throwable )
    {
        logger.info( message );
/*
        if ( isInfoEnabled() )
        {
            System.out.print( "[ maven embedder INFO] " );
            System.out.println( message );


            if ( null != throwable )
            {
                throwable.printStackTrace( System.out );
            }
        }
        */
    }

    public void warn( String message, Throwable throwable )
    {
        if ( isWarnEnabled() )
        {
            System.out.print( "[ maven embedder WARNING] " );
            System.out.println( message );

            if ( null != throwable )
            {
                throwable.printStackTrace( System.out );
            }
        }
    }

    public void error( String message, Throwable throwable )
    {
        if ( isErrorEnabled() )
        {
            System.out.print( "[ maven embedder ERROR] " );
            System.out.println( message );

            if ( null != throwable )
            {
                throwable.printStackTrace( System.out );
            }
        }
    }

    public void fatalError( String message, Throwable throwable )
    {
        if ( isFatalErrorEnabled() )
        {
            System.out.print( "[ maven embedder FATAL ERROR] " );
            System.out.println( message );

            if ( null != throwable )
            {
                throwable.printStackTrace( System.out );
            }
        }
    }
}
