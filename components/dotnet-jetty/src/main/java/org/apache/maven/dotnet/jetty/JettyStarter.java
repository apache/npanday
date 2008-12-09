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
package org.apache.maven.dotnet.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.io.IOException;
import java.io.File;

public class JettyStarter
{
    private static Logger logger = Logger.getAnonymousLogger();

    public static void main( String[] args )
    {
        try
        {
            logger.addHandler( new FileHandler(System.getProperty( "user.home" ) + "\\.m2\\embedder-logs\\jetty-log.xml" ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        String port = System.getProperty( "port" );
        String warFile = System.getProperty( "warFile" );
        logger.info( "NPANDAY: Port = " + port + ", warFile = " + warFile );
        if ( !new File( warFile ).exists() )
        {
            logger.severe( "NPANDAY: War File does not exist" );
            return;
        }

        Server server = new Server( Integer.parseInt( port ) );
        ShutdownHandler shutdownHandler = new ShutdownHandler();
        shutdownHandler.setServer( server );

        WebAppContext context = new WebAppContext();
        context.setWar( warFile );

        context.setServer( server );
        context.setContextPath( "/dotnet-service-embedder" );
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers( new Handler[]{context, new DefaultHandler(), shutdownHandler} );
        server.setHandler( handlers );

        BoundedThreadPool pool = new BoundedThreadPool();
        server.setThreadPool( pool );

        try
        {
            server.start();
        }
        catch ( Exception e )
        {
            logger.severe( "NPANDAY: Problem starting the server: " + e.getMessage() );
        }

        logger.info( "NPANDAY: Successfully started server" );

        try
        {
            server.join();
        }
        catch ( InterruptedException e )
        {
            logger.severe( "NPANDAY: Problem joining the server: " + e.getMessage() );
        }
    }

    private static class ShutdownHandler
        implements Handler
    {

        private Server server;

        public void handle( String string, javax.servlet.http.HttpServletRequest httpServletRequest,
                            javax.servlet.http.HttpServletResponse httpServletResponse, int i )
            throws IOException, javax.servlet.ServletException
        {
            logger.info( "HTTP Request URL = " + httpServletRequest.getRequestURL().toString() );
            if ( httpServletRequest.getParameter( "shutdown" ) != null )
            {
                logger.info( "Shutting down server" );
                try
                {
                    server.stop();
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }

        public void setServer( Server server )
        {
            this.server = server;
        }

        public Server getServer()
        {
            return server;
        }

        public void destroy()
        {

        }

        public void start()
            throws Exception
        {

        }

        public void stop()
            throws Exception
        {

        }

        public boolean isRunning()
        {
            return false;
        }

        public boolean isStarted()
        {
            return false;
        }

        public boolean isStarting()
        {
            return false;
        }

        public boolean isStopping()
        {
            return false;
        }

        public boolean isStopped()
        {
            return false;
        }

        public boolean isFailed()
        {
            return false;
        }
    }
}


