package org.apache.maven.dotnet.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;

public class JettyStarter
{

    public static void main( String[] args )
    {
        String port = System.getProperty( "port" );
        Server server = new Server( Integer.parseInt( port ) );
        WebAppContext context = new WebAppContext();
        context.setWar(System.getProperty( "warFile" ));

        context.setServer( server );
        context.setContextPath( "/dotnet-service-embedder" );
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers( new Handler[]{context, new DefaultHandler()} );
        server.setHandler( handlers );
        BoundedThreadPool pool = new BoundedThreadPool();
        server.setThreadPool( pool );
        try
        {
            server.start();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        try
        {
            server.join();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}
