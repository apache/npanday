package org.apache.maven.dotnet.registry;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.MemoryStoreRDFSInferencer;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.logging.Logger;

public class ConnectionsRepository
    implements Repository
{

    private static Logger logger = Logger.getAnonymousLogger();

    private RepositoryRegistry registry;

    private Set<DataAccessObject> daos = new HashSet<DataAccessObject>();

    private Hashtable properties;

    public void lazyLoad() throws IOException
    {
        long start = System.currentTimeMillis();

        File dataDir = new File( System.getProperty( "user.home" ), ".m2/uac/rdfRepository" );
        MemoryStore store = new MemoryStore( dataDir );
        store.setPersist( true );
        store.setSyncDelay( 0 );
        org.openrdf.repository.Repository rdfRepository = new SailRepository( store );
        try
        {
            rdfRepository.initialize();
        }
        catch ( RepositoryException e )
        {
            e.printStackTrace();
        }

        Set<String> keys = properties.keySet();
        for ( String keyName : keys )
        {
            if ( keyName.trim().startsWith( "dao:" ) )
            {
                String daoClassName = (String) properties.get( keyName );
                try
                {
                    Class c = Class.forName( daoClassName );
                    Object o = c.getConstructor().newInstance();
                    if ( !( o instanceof DataAccessObject ) )
                    {
                        throw new IOException(
                            "JV-100-103: dao tag references a class that does not implement the DataAccessObject interface." );
                    }
                    DataAccessObject dao = (DataAccessObject) o;
                    dao.init( rdfRepository, keyName, daoClassName );
                    dao.setRepositoryRegistry( registry );
                    daos.add( dao );
                    logger.info( "JV-100-104: Adding data access object: Class Name = " + daoClassName );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    throw new IOException(
                        "JV-100-105: Problem instantiating the DAO Class: Class Name = " + daoClassName );
                }
                catch ( Error e )
                {
                    e.printStackTrace();
                    throw new IOException(
                        "JV-100-106: Problem instantiating the DAO Class: Class Name = " + daoClassName );
                }
            }
        }
        logger.info( "Connection Start Up: Time = " + ( System.currentTimeMillis() - start ) );
    }

    public void load( InputStream inputStream, Hashtable properties )
        throws IOException
    {
        this.properties = properties;
    }

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
        this.registry = repositoryRegistry;
    }

    public Set<DataAccessObject> getDataAccessObjects()
    {
        return Collections.unmodifiableSet( daos );
    }
}
