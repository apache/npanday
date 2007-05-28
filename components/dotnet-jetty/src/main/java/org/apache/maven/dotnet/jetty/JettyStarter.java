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
            logger.addHandler( new FileHandler( "C:\\tmp\\nmaven-jetty.log" ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        String port = System.getProperty( "port" );
        String warFile = System.getProperty( "warFile" );
        logger.info( "NMAVEN: Port = " + port + ", warFile = " + warFile );
        if ( !new File( warFile ).exists() )
        {
            logger.severe( "NMAVEN: War File does not exist" );
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
            logger.severe( "NMAVEN: Problem starting the server: " + e.getMessage() );
        }

        logger.info( "NMAVEN: Successfully started server" );

        try
        {
            server.join();
        }
        catch ( InterruptedException e )
        {
            logger.severe( "NMAVEN: Problem joining the server: " + e.getMessage() );
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


